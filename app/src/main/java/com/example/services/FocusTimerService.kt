package com.example.services
import com.example.ui.*


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.RevisionRepository
import com.example.receivers.TimerActionReceiver
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.cancel
import kotlinx.coroutines.tasks.await

class FocusTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var shutdownJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    @Volatile private var isRunning = false
    @Volatile private var targetRealtimeMs: Long = 0L
    @Volatile private var pausedRemainingMs: Long = 0L
    @Volatile private var isPaused = false

    private var totalDurationMs: Long = 0L
    private var sessionTitle: String = "Focus Session"
    private var subjectName: String = ""
    private var topicId: String = ""

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    @Volatile private var isCompletionTriggered = false

    private val repository = RevisionRepository()

    companion object {
        const val CHANNEL_FOCUS = "focus_timer_channel"
        const val CHANNEL_BREAK = "break_timer_channel"
        const val CHANNEL_SILENT_COMPLETION = "silent_completion_channel_v2"
        
        const val NOTIFICATION_ID = 101

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        
        const val ACTION_START_BREAK = "ACTION_START_BREAK"
        const val ACTION_CONTINUE_STUDY = "ACTION_CONTINUE_STUDY"
        const val ACTION_SKIP_BREAK = "ACTION_SKIP_BREAK"
        
        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_SUBJECT = "EXTRA_SUBJECT"
        const val EXTRA_TOPIC_ID = "EXTRA_TOPIC_ID"

        private val _remainingTimeMs = MutableStateFlow(0L)
        val remainingTimeMs: StateFlow<Long> = _remainingTimeMs.asStateFlow()

        private val _timerState = MutableStateFlow(TimerState.IDLE)
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

        val isRunning = MutableStateFlow(false)
        val isPaused = MutableStateFlow(false)
        val isCompleted = MutableStateFlow(false)
        val hasShownCompletionDialog = MutableStateFlow(false)
        val hasSentNotification = MutableStateFlow(false)
        val currentSessionType = MutableStateFlow("Focus")
        val activeTimerTopicId = MutableStateFlow("")
        
        val completedPomodorosToday = MutableStateFlow(0)
    }

    enum class TimerState { IDLE, STUDY, BREAK, PAUSED, COMPLETED }

    override fun onCreate() {
        super.onCreate()
        try { FirebaseApp.getInstance() } catch(e: Exception) { FirebaseApp.initializeApp(this) }
        createNotificationChannels()
        initializeTts()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Always satisfy Android's ForegroundService contract immediately on entry
        safeStartForeground(NOTIFICATION_ID, buildNotification(pausedRemainingMs.takeIf { it > 0L } ?: totalDurationMs))

        val prefs = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        if (intent == null || intent.action == null) {
            val savedDuration = prefs.getLong("total_duration", 0L)
            val savedAbsoluteTarget = prefs.getLong("target_absolute", 0L)
            val savedPaused = prefs.getBoolean("is_paused", false)
            val savedPausedRemaining = prefs.getLong("paused_remaining", 0L)
            
            val nowAbsolute = System.currentTimeMillis()
            
            if (savedDuration > 0 && (savedAbsoluteTarget > nowAbsolute || savedPaused)) {
                totalDurationMs = savedDuration
                sessionTitle = prefs.getString("title", "Focus Session") ?: "Focus Session"
                subjectName = prefs.getString("subject", "Exam Prep") ?: "Exam Prep"
                topicId = prefs.getString("topic_id", "") ?: ""
                activeTimerTopicId.value = topicId
                
                if (savedPaused) {
                    isRunning = true
                    isPaused = true
                    pausedRemainingMs = savedPausedRemaining
                    targetRealtimeMs = SystemClock.elapsedRealtime() + pausedRemainingMs
                    _timerState.value = TimerState.PAUSED
                    Companion.isRunning.value = false
                    Companion.isPaused.value = true
                    Companion.isCompleted.value = false
                    safeStartForeground(NOTIFICATION_ID, buildNotification(pausedRemainingMs))
                } else {
                    isRunning = true
                    isPaused = false
                    val remainingMs = savedAbsoluteTarget - nowAbsolute
                    targetRealtimeMs = SystemClock.elapsedRealtime() + remainingMs
                    _timerState.value = if (currentSessionType.value == "Break") TimerState.BREAK else TimerState.STUDY
                    Companion.isRunning.value = true
                    Companion.isPaused.value = false
                    Companion.isCompleted.value = false
                    safeStartForeground(NOTIFICATION_ID, buildNotification(targetRealtimeMs - SystemClock.elapsedRealtime()))
                    resumeTimerLoop()
                }
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            return START_STICKY
        }
        
        when (intent.action) {
            ACTION_START -> {
                cancelAndResetActiveTimer()
                totalDurationMs = intent.getLongExtra(EXTRA_DURATION, 25 * 60 * 1000L)
                sessionTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Focus Session"
                subjectName = intent.getStringExtra(EXTRA_SUBJECT) ?: "Exam Prep"
                topicId = intent.getStringExtra(EXTRA_TOPIC_ID) ?: ""
                activeTimerTopicId.value = topicId
                
                if (sessionTitle.contains("Break", ignoreCase = true)) {
                    currentSessionType.value = "Break"
                } else {
                    currentSessionType.value = "Focus"
                }
                
                prefs.edit()
                    .putLong("total_duration", totalDurationMs)
                    .putString("title", sessionTitle)
                    .putString("subject", subjectName)
                    .putString("topic_id", topicId)
                    .apply()
                startTimer(totalDurationMs)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
            ACTION_START_BREAK -> {
                val breakMins = prefs.getInt("break_mins_pref", 5)
                val breakIntent = Intent(this, FocusTimerService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_DURATION, breakMins * 60 * 1000L)
                    putExtra(EXTRA_TITLE, "Short Break")
                    putExtra(EXTRA_SUBJECT, "Relaxation")
                }
                startService(breakIntent)
            }
            ACTION_CONTINUE_STUDY -> {
                val focusMins = prefs.getInt("focus_mins_pref", 45)
                val focusIntent = Intent(this, FocusTimerService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_DURATION, focusMins * 60 * 1000L)
                    putExtra(EXTRA_TITLE, "Focus Session")
                    val savedSubject = prefs.getString("subject", "Exam Prep")
                    val savedTopic = prefs.getString("topic_id", "")
                    putExtra(EXTRA_SUBJECT, savedSubject)
                    putExtra(EXTRA_TOPIC_ID, savedTopic)
                }
                startService(focusIntent)
            }
            ACTION_SKIP_BREAK -> stopTimer()
        }
        return START_STICKY
    }
    
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RevisionSpace::FocusTimerWakeLock")
            try {
                wakeLock?.acquire(totalDurationMs + 10 * 60 * 1000L)
            } catch (e: Exception) {
                e.printStackTrace()
                wakeLock = null
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
        } catch (e: Exception) {} finally { wakeLock = null }
    }

    private fun saveState() {
        val prefs = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("target_absolute", System.currentTimeMillis() + (targetRealtimeMs - SystemClock.elapsedRealtime()))
            .putLong("paused_remaining", pausedRemainingMs)
            .putBoolean("is_paused", isPaused)
            .apply()
    }

    private fun clearState() {
        val prefs = getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun initializeTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val currentLocale = java.util.Locale.getDefault()
                val result = tts?.setLanguage(currentLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to English
                    val fallbackResult = tts?.setLanguage(java.util.Locale.ENGLISH)
                    if (fallbackResult != TextToSpeech.LANG_MISSING_DATA && fallbackResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isTtsInitialized = true
                    }
                } else {
                    isTtsInitialized = true
                }
            }
        }
    }

    private fun speakCompletionAlert() {
        val isHi = java.util.Locale.getDefault().language == "hi"
        val speechText = if (currentSessionType.value == "Break") {
            if (isHi) "ब्रेक खत्म हो गया है। अपनी पढ़ाई जारी रखने का समय आ गया है।"
            else "Break is over. It's time to continue your study session."
        } else {
            if (isHi) "बहुत बढ़िया! आपका फोकस सेशन पूरा हो गया है। अब एक छोटा ब्रेक लेने का समय है।"
            else "Great job! Your focus session is complete. Time to take a short break."
        }
        
        serviceScope.launch(Dispatchers.Main) {
            try {
                if (tts == null) {
                    initializeTts()
                }
                val params = android.os.Bundle()
                params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_MUSIC)
                tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, "FocusTimerCompletionUtterance")
            } catch (e: Exception) {
                android.util.Log.e("FocusTimerService", "TTS speak failed", e)
            }
        }
    }

    private fun safeStartForeground(id: Int, notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    id,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(id, notification)
            }
        } catch (e: Exception) {
            try {
                startForeground(id, notification)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun cancelAndResetActiveTimer() {
        timerJob?.cancel()
        timerJob = null
        shutdownJob?.cancel()
        shutdownJob = null
        isRunning = false
        isPaused = false
        isCompletionTriggered = false
        _timerState.value = TimerState.IDLE
        Companion.isRunning.value = false
        Companion.isPaused.value = false
        Companion.isCompleted.value = false
        releaseWakeLock()
        clearState()
    }

    private fun startTimer(durationMs: Long) {
        acquireWakeLock()
        isRunning = true
        isPaused = false
        isCompletionTriggered = false
        targetRealtimeMs = SystemClock.elapsedRealtime() + durationMs
        _timerState.value = if (currentSessionType.value == "Break") TimerState.BREAK else TimerState.STUDY
        
        Companion.isRunning.value = true
        Companion.isPaused.value = false
        Companion.isCompleted.value = false
        Companion.hasShownCompletionDialog.value = false
        Companion.hasSentNotification.value = false
        
        saveState()
        safeStartForeground(NOTIFICATION_ID, buildNotification(durationMs))
        resumeTimerLoop()
    }

    private fun resumeTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch(Dispatchers.Default) {
            while (isRunning && !isPaused && isActive) {
                val remaining = targetRealtimeMs - SystemClock.elapsedRealtime()
                if (remaining <= 0) {
                    onTimerComplete()
                    break
                }
                _remainingTimeMs.value = remaining
                updateNotification(remaining)
                delay(1000)
            }
        }
    }

    private fun pauseTimer() {
        if (!isRunning || isPaused) return
        isPaused = true
        timerJob?.cancel()
        pausedRemainingMs = targetRealtimeMs - SystemClock.elapsedRealtime()
        _timerState.value = TimerState.PAUSED
        Companion.isRunning.value = false
        Companion.isPaused.value = true
        saveState()
        updateNotification(pausedRemainingMs)
        releaseWakeLock()
    }

    private fun resumeTimer() {
        if (!isRunning || !isPaused) return
        isPaused = false
        targetRealtimeMs = SystemClock.elapsedRealtime() + pausedRemainingMs
        _timerState.value = if (currentSessionType.value == "Break") TimerState.BREAK else TimerState.STUDY
        Companion.isRunning.value = true
        Companion.isPaused.value = false
        acquireWakeLock()
        saveState()
        updateNotification(pausedRemainingMs)
        resumeTimerLoop()
    }

    private fun stopTimer() {
        cancelAndResetActiveTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onTimerComplete() {
        if (isCompletionTriggered) return
        isCompletionTriggered = true

        timerJob?.cancel()
        timerJob = null
        isRunning = false
        isPaused = false
        _timerState.value = TimerState.COMPLETED
        _remainingTimeMs.value = 0L
        clearState()
        releaseWakeLock()
        
        Companion.isRunning.value = false
        Companion.isPaused.value = false
        Companion.isCompleted.value = true
        Companion.hasShownCompletionDialog.value = false
        
        saveStudyDataBackground()

        if (!Companion.hasSentNotification.value) {
            showCompletionNotification()
            Companion.hasSentNotification.value = true
        }
        
        speakCompletionAlert()
        
        shutdownJob?.cancel()
        shutdownJob = null
    }
    
    private fun saveStudyDataBackground() {
        val durationMs = totalDurationMs
        val isBreak = currentSessionType.value == "Break"
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                if (repository.userId.isEmpty()) return@launch
                
                val now = System.currentTimeMillis()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val dateStr = dateFormat.format(java.util.Date(now))
                val endTimeStr = timeFormat.format(java.util.Date(now))
                val startTimeStr = timeFormat.format(java.util.Date(now - durationMs))
                
                val mins = kotlin.math.ceil(durationMs / 60000.0).toInt()
                
                if (!isBreak && topicId.isNotEmpty() && mins > 0) {
                    val topic = repository.getTopicById(topicId)
                    val isRev = topic != null && topic.totalRevisions > 0
                    repository.recordStudySessionTransaction(topicId, isRev, durationMs, dateStr)
                    
                    val coins = (mins * 10) / 45 // 10 coins per 45 mins
                    if (coins > 0) {
                        repository.db?.collection("users")?.document(repository.userId)
                            ?.update("coins", com.google.firebase.firestore.FieldValue.increment(coins.toLong()))
                    }
                }
                
                val sessionId = java.util.UUID.randomUUID().toString()
                val sessionData = hashMapOf(
                    "sessionId" to sessionId,
                    "userId" to repository.userId,
                    "subject" to subjectName,
                    "topic" to (if (isBreak) "Relaxation" else topicId),
                    "startTime" to startTimeStr,
                    "endTime" to endTimeStr,
                    "duration" to durationMs,
                    "sessionType" to currentSessionType.value,
                    "completionStatus" to "Completed",
                    "date" to dateStr
                )
                repository.db?.collection("users")?.document(repository.userId)
                    ?.collection("pomodoro_sessions")?.document(sessionId)?.set(sessionData)?.await()
                    
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showCompletionNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val isBreak = currentSessionType.value == "Break"
        val titleText = if (isBreak) "☕ Break Finished" else "✅ Focus Session Completed"
        val contentText = if (isBreak) {
            "Ready for the next focus session?"
        } else {
            "Subject: $subjectName\nTake a short break."
        }
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val b = NotificationCompat.Builder(this, CHANNEL_SILENT_COMPLETION)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setSmallIcon(R.drawable.ic_stat_revision)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // Force show on lockscreen immediately
            
        if (isBreak) {
            val startFocusIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_START_FOCUS }
            val skipBreakIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_SKIP_BREAK }
            val pStart = PendingIntent.getBroadcast(this, 4, startFocusIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val pSkip = PendingIntent.getBroadcast(this, 5, skipBreakIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            b.addAction(0, "Start Focus", pStart)
            b.addAction(0, "Skip", pSkip)
            b.addAction(0, "Home", pendingIntent)
        } else {
            val startBreakIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_START_BREAK }
            val continueIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_CONTINUE_STUDY }
            val pBreak = PendingIntent.getBroadcast(this, 6, startBreakIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val pContinue = PendingIntent.getBroadcast(this, 7, continueIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            b.addAction(0, "Start Break", pBreak)
            b.addAction(0, "Continue Study", pContinue)
            b.addAction(0, "Back to Home", pendingIntent)
        }
            
        manager.notify(102, b.build())
    }

    private fun formatTime(ms: Long): String {
        val totalSecs = ms / 1000
        val m = totalSecs / 60
        val s = totalSecs % 60
        return String.format("%02d:%02d", m, s)
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val collapsedView = RemoteViews(packageName, R.layout.notification_timer_collapsed)
        val expandedView = RemoteViews(packageName, R.layout.notification_timer_expanded)
        
        val timeStr = formatTime(remainingMs)
        val progressPercent = if (totalDurationMs > 0) ((totalDurationMs - remainingMs).toFloat() / totalDurationMs * 100).toInt() else 0
        
        collapsedView.setTextViewText(R.id.text_time, timeStr)
        collapsedView.setTextViewText(R.id.text_title, sessionTitle)
        collapsedView.setProgressBar(R.id.progress_bar, 100, progressPercent, false)
        
        expandedView.setTextViewText(R.id.text_time, timeStr)
        expandedView.setTextViewText(R.id.text_title, sessionTitle)
        expandedView.setTextViewText(R.id.text_subject, subjectName)
        expandedView.setProgressBar(R.id.progress_bar, 100, progressPercent, false)
        expandedView.setTextViewText(R.id.text_progress_percent, "$progressPercent%")
        
        val pauseIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_PAUSE }
        val resumeIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_RESUME }
        val stopIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_GIVE_UP }
        val skipIntent = Intent(this, TimerActionReceiver::class.java).apply { action = TimerActionReceiver.ACTION_SKIP_BREAK }
        
        val pPause = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pResume = PendingIntent.getBroadcast(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pStop = PendingIntent.getBroadcast(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pSkip = PendingIntent.getBroadcast(this, 5, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val isBreak = currentSessionType.value == "Break"
        
        if (isPaused) {
            collapsedView.setImageViewResource(R.id.btn_pause_resume, android.R.drawable.ic_media_play)
            collapsedView.setOnClickPendingIntent(R.id.btn_pause_resume, pResume)
            expandedView.setImageViewResource(R.id.btn_pause_resume, android.R.drawable.ic_media_play)
            expandedView.setOnClickPendingIntent(R.id.btn_pause_resume, pResume)
        } else {
            collapsedView.setImageViewResource(R.id.btn_pause_resume, android.R.drawable.ic_media_pause)
            collapsedView.setOnClickPendingIntent(R.id.btn_pause_resume, pPause)
            expandedView.setImageViewResource(R.id.btn_pause_resume, android.R.drawable.ic_media_pause)
            expandedView.setOnClickPendingIntent(R.id.btn_pause_resume, pPause)
        }
        
        if (isBreak) {
            collapsedView.setOnClickPendingIntent(R.id.btn_stop, pSkip)
            expandedView.setOnClickPendingIntent(R.id.btn_stop, pSkip)
            expandedView.setOnClickPendingIntent(R.id.btn_give_up, pSkip)
        } else {
            collapsedView.setOnClickPendingIntent(R.id.btn_stop, pStop)
            expandedView.setOnClickPendingIntent(R.id.btn_stop, pStop)
            expandedView.setOnClickPendingIntent(R.id.btn_give_up, pStop)
        }
        
        val channelId = if (isBreak) CHANNEL_BREAK else CHANNEL_FOCUS

        return try {
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_revision)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
        } catch (e: Exception) {
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(sessionTitle)
                .setContentText("$subjectName • $timeStr")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
        }
    }

    private fun updateNotification(remainingMs: Long) {
        serviceScope.launch(Dispatchers.Default) {
            try {
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = buildNotification(remainingMs)
                manager.notify(NOTIFICATION_ID, notification)
            } catch (e: Exception) {}
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS,
                "Focus Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows active focus session timer"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            
            val breakChannel = NotificationChannel(
                CHANNEL_BREAK,
                "Break Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows active break timer"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }

            val voiceChannel = NotificationChannel(
                CHANNEL_SILENT_COMPLETION,
                "Session Completion",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Silent notification when sessions complete"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            manager.createNotificationChannel(focusChannel)
            manager.createNotificationChannel(breakChannel)
            manager.createNotificationChannel(voiceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
        timerJob?.cancel()
        shutdownJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
    }
}
