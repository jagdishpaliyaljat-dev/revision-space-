package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            // Initialize local SQLite Room Database for 100% mobile offline storage
            database = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "offline_database"
            ).addMigrations(AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
             // Upgrades must NEVER wipe user data: every schema change ships a real
             // migration above. Only installing an OLDER build may reset the DB.
             .fallbackToDestructiveMigrationOnDowngrade(true)
             .build()
        } catch (e: Exception) {
            android.util.Log.e("MyApplication", "Room Database init failed", e)
        }

        try {
            // Firebase is initialized manually (no google-services.json needed).
            // Real configuration is injected into BuildConfig from .env / .env.example
            // by the Secrets Gradle plugin. This replaces the old dummy values which
            // made Firebase Auth / Firestore / Storage unusable.
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .build()
            com.google.firebase.FirebaseApp.initializeApp(this, options)
            android.util.Log.i(
                "MyApplication",
                "Firebase initialized for project ${BuildConfig.FIREBASE_PROJECT_ID}"
            )
        } catch (e: Exception) {
            android.util.Log.e("MyApplication", "Firebase initialization failed", e)
        }

        // NOTE: Firestore offline persistence is enabled by default on Android.
        // We intentionally do not override firestoreSettings here: the legacy
        // PersistentLocalCacheSettings API is not available in this Firebase BOM,
        // and overriding it caused a compile error. Offline reads are instead made
        // best-effort in RevisionRepository (sign-in does not fail when a cached
        // document is missing) and Navigation treats offline errors as success.

        try {
            applyNotificationSchedules()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Restores every notification schedule from the saved user preferences.
     *
     * The old code unconditionally scheduled the daily reminder and the daily
     * mission on each launch. That silently re-enabled a reminder the user had
     * turned off, ignored the chosen reminder hour/minute, and scheduled work
     * even when the notification master switch was off.
     */
    private fun applyNotificationSchedules() {
        val prefs = getSharedPreferences(
            com.example.notification.NotificationHelper.APP_PREFS_NAME,
            android.content.Context.MODE_PRIVATE
        )
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        val reminderEnabled = prefs.getBoolean("reminder_enabled", false)
        val hour = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)

        if (notificationsEnabled && reminderEnabled) {
            com.example.notification.ReminderScheduler.scheduleDailyReminder(this, hour, minute)
        } else {
            com.example.notification.ReminderScheduler.cancelReminder(this)
        }

        if (notificationsEnabled) {
            com.example.services.SrsNotificationManager.scheduleDailyMission(this)
        } else {
            com.example.services.SrsNotificationManager.cancelDailyMission(this)
        }

        // Exact alarms do not survive reboots/updates, so rebuild them from Room.
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                com.example.services.SrsNotificationManager.rescheduleAllRevisionAlarms(this@MyApplication)
            }
        }
    }
}
