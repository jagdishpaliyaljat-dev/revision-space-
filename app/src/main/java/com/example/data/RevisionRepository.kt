package com.example.data
import com.example.ui.*


import com.example.data.local.toDomain
import com.example.data.local.toEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
open class RevisionRepository {
    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth? get() = try { FirebaseAuth.getInstance() } catch (e: Throwable) { null }
    
    open val roomDb: com.example.data.local.AppDatabase?
        get() = try { com.example.MyApplication.database } catch (e: Throwable) { null }

    open val userId: String
        get() = "offline_user"

    val userIdFlow: Flow<String> = flowOf(userId)

    val globalConfig: Flow<GlobalAppConfig> = flowOf(GlobalAppConfig())

    suspend fun updateGlobalConfig(updates: Map<String, Any>) {
        // try { db.collection("global_config").document("settings").set(updates, com.google.firebase.firestore.SetOptions.merge()) } catch (e: Exception) {}
    }

    suspend fun resetAllUserStreaks() {}
    suspend fun resetAllUserCoins() {}

    suspend fun sendEmergencyBroadcast(title: String, message: String, adminEmail: String) {}

    open val currentUserProfile: Flow<UserProfile?> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.userProfileDao().getProfile("offline_user").map { entity ->
                entity?.toDomain() ?: UserProfile(id = "offline_user", name = "Student", email = "user@mobile.local")
            })
        } else {
            emit(UserProfile(id = "offline_user", name = "Student", email = "user@mobile.local"))
        }
    }

    open fun getRevisionSettings(): Flow<RevisionSettings> = flowOf(RevisionSettings())

    open suspend fun updateRevisionSettings(settings: RevisionSettings) {}

    open val recentRevisionHistory: Flow<List<RevisionHistoryRecord>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.revisionHistoryDao().getHistory("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    open val allSubjects: Flow<List<Subject>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.subjectDao().getSubjects("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    open val allChapters: Flow<List<Chapter>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.chapterDao().getChapters("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    open val allTopics: Flow<List<Topic>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.topicDao().getTopics("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    open val allStudyLogs: Flow<List<StudyLog>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.studyLogDao().getStudyLogs("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }
    open val allExams: Flow<List<ExamCountdown>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.examDao().getExams("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    suspend fun insertExam(name: String, examDateMs: Long?, targetStudyHours: Int?, category: String = "", targetRank: String = "", totalSubjects: Int = 0) {
        val examId = java.util.UUID.randomUUID().toString()
        val exam = ExamCountdown(
            id = examId,
            userId = "offline_user",
            name = name,
            examDateMs = examDateMs,
            targetStudyHours = targetStudyHours,
            createdAtMs = System.currentTimeMillis(),
            category = category,
            targetRank = targetRank,
            totalSubjects = totalSubjects
        )
        roomDb?.examDao()?.insert(exam.toEntity())
        // try { db.collection("exams").document(examId).set(exam) } catch (e: Exception) {}
    }

    suspend fun insertExtendedSyllabus(
        examId: String, 
        subjectName: String, 
        topicName: String, 
        totalChapters: Int, 
        estimatedRevisionTimeMins: Int
    ) {
        val subjectId = java.util.UUID.randomUUID().toString()
        val subject = Subject(
            id = subjectId,
            userId = "offline_user",
            examId = examId,
            name = subjectName,
            colorHex = "#6C4DFF"
        )
        roomDb?.subjectDao()?.insert(subject.toEntity())
        // try { db.collection("subjects").document(subjectId).set(subject) } catch (e: Exception) {}

        val validChapters = if (totalChapters > 0) totalChapters else 1
        var firstChapterId = ""
        for (i in 1..validChapters) {
            val chapterId = java.util.UUID.randomUUID().toString()
            if (i == 1) firstChapterId = chapterId
            val chapter = Chapter(
                id = chapterId,
                userId = "offline_user",
                subjectId = subjectId,
                name = "Chapter $i"
            )
            roomDb?.chapterDao()?.insert(chapter.toEntity())
            // try { db.collection("chapters").document(chapterId).set(chapter) } catch (e: Exception) {}
        }

        val topicId = java.util.UUID.randomUUID().toString()
        val topic = Topic(
            id = topicId,
            userId = "offline_user",
            chapterId = firstChapterId,
            subjectId = subjectId,
            name = topicName,
            estimatedRevisionTimeMins = estimatedRevisionTimeMins,
            examIds = if (examId.isNotEmpty() && examId != "all") listOf(examId) else emptyList()
        )
        roomDb?.topicDao()?.insert(topic.toEntity())
        // try { db.collection("topics").document(topicId).set(topic) } catch (e: Exception) {}
    }

    suspend fun updateExam(exam: ExamCountdown) {
        val updated = exam.copy(userId = "offline_user")
        roomDb?.examDao()?.insert(updated.toEntity())
        // try { db.collection("exams").document(exam.id).set(exam) } catch (e: Exception) {}
    }

    suspend fun deleteExam(examId: String) {
        roomDb?.examDao()?.delete(examId)
        // try { db.collection("exams").document(examId).delete() } catch (e: Exception) {}
    }

    open val coinTransactions: Flow<List<CoinTransaction>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.coinTransactionDao().getTransactions("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    open fun getAllCoinTransactions(): Flow<List<CoinTransaction>> = coinTransactions

    suspend fun adminAwardCoins(targetUserId: String, amount: Int, description: String, adminEmail: String): Boolean {
        if (targetUserId.isEmpty() || amount == 0) return false
        
        val ref = db.collection("coin_history").document()
        val params = mutableMapOf<String, Any>(
            "id" to ref.id,
            "userId" to targetUserId,
            "amount" to amount,
            "description" to description,
            "timestampMs" to System.currentTimeMillis(),
            "type" to if (amount > 0) "EARNED" else "SPENT"
        )
        
        db.runTransaction { tx ->
            val userRef = db.collection("users").document(targetUserId)
            val userSnap = tx.get(userRef)
            if (userSnap.exists()) {
                val currentCoins = userSnap.getLong("totalCoins") ?: 0L
                val earnedCoins = userSnap.getLong("totalCoinsEarned") ?: 0L
                
                if (amount > 0) {
                    tx.update(userRef, "totalCoins", currentCoins + amount)
                    tx.update(userRef, "totalCoinsEarned", earnedCoins + amount)
                } else {
                    tx.update(userRef, "totalCoins", (currentCoins + amount).coerceAtLeast(0))
                }
                tx.set(ref, params)
            }
        }.await()
        
        logSystemAction(userId, adminEmail, "Admin Coin Modification", "Modified $amount coins for user $targetUserId")
        return true
    }

    suspend fun awardCoins(amount: Int, description: String, sessionId: String? = null): Boolean {
        if (amount <= 0) return false
        val rDb = roomDb
        val txId = java.util.UUID.randomUUID().toString()
        val tx = CoinTransaction(
            id = txId,
            userId = "offline_user",
            amount = amount,
            description = description,
            timestampMs = System.currentTimeMillis(),
            type = "EARNED"
        )
        if (rDb != null) {
            rDb.coinTransactionDao().insert(tx.toEntity())
            val currentProfileEntity = rDb.userProfileDao().getProfileSync("offline_user")
            val currentProfile = currentProfileEntity?.toDomain() ?: UserProfile(id = "offline_user")
            val updatedProfile = currentProfile.copy(totalCoins = currentProfile.totalCoins + amount)
            rDb.userProfileDao().insert(updatedProfile.toEntity())
        }
        try {
            db.collection("coin_history").document(txId).set(tx)
        } catch (e: Exception) {}
        return true
    }

    suspend fun recordRevisionEarnings(topicId: String, topicName: String, coins: Int): Boolean {
        return awardCoins(
            amount = coins,
            description = "Revision completed: $topicName",
            sessionId = "revision_${topicId}_${System.currentTimeMillis() / 150000}"
        )
    }

    suspend fun recordStreakReward(streakDays: Int, coins: Int): Boolean {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        return awardCoins(
            amount = coins,
            description = "Streak reward: $streakDays Days active",
            sessionId = "streak_${streakDays}_$todayStr"
        )
    }

    suspend fun recordAchievementBonus(achievementId: String, achievementName: String, coins: Int): Boolean {
        return awardCoins(
            amount = coins,
            description = "Achievement unlocked: $achievementName",
            sessionId = "achievement_$achievementId"
        )
    }

    
    suspend fun clearAllData() {
        val rDb = roomDb ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            rDb.runInTransaction {
                rDb.subjectDao().deleteAllForUserSync("offline_user")
                rDb.chapterDao().deleteAllForUserSync("offline_user")
                rDb.topicDao().deleteAllForUserSync("offline_user")
                rDb.revisionDao().deleteAllForUserSync("offline_user")
                rDb.studyLogDao().deleteAllForUserSync("offline_user")
                rDb.examDao().deleteAllForUserSync("offline_user")
                rDb.revisionHistoryDao().deleteAllForUserSync("offline_user")
                rDb.subjectTestDao().deleteAllForUserSync("offline_user")
                rDb.quickTaskDao().deleteAllForUserSync("offline_user")
                rDb.coinTransactionDao().deleteAllForUserSync("offline_user")
                rDb.appNotificationDao().deleteAllForUserSync("offline_user")
            }
        }
    }

    open suspend fun generateTestDataIfEmpty() {
        try {
            com.example.data.DatabaseSeeder.purgeDemoData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open suspend fun insertSubject(name: String, colorHex: String = "#0061A4", examId: String = "", icon: String = "book", totalTopicsExpected: Int = 0) {
        val subject = Subject(
            id = java.util.UUID.randomUUID().toString(),
            userId = "offline_user",
            examId = examId,
            name = name,
            colorHex = colorHex,
            icon = icon,
            totalTopicsExpected = totalTopicsExpected,
            isArchived = false
        )
        roomDb?.subjectDao()?.insert(subject.toEntity())
        try { db.collection("subjects").document(subject.id).set(subject) } catch (e: Exception) {}
    }

    open suspend fun updateSubject(subjectId: String, name: String, colorHex: String, icon: String, totalTopicsExpected: Int) {
        val rDb = roomDb
        if (rDb != null) {
            val existingList = rDb.subjectDao().getSubjects("offline_user").firstOrNull() ?: emptyList()
            val existing = existingList.find { it.id == subjectId }
            val updated = Subject(
                id = subjectId,
                userId = "offline_user",
                examId = existing?.examId ?: "",
                name = name,
                colorHex = colorHex,
                icon = icon,
                totalTopicsExpected = totalTopicsExpected,
                isArchived = existing?.isArchived ?: false
            )
            rDb.subjectDao().insert(updated.toEntity())
        }
        try {
            val updates = mapOf(
                "name" to name,
                "colorHex" to colorHex,
                "icon" to icon,
                "totalTopicsExpected" to totalTopicsExpected
            )
            db.collection("subjects").document(subjectId).update(updates)
        } catch (e: Exception) {}
    }

    open suspend fun deleteSubject(id: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val rDb = roomDb
        if (rDb != null) {
            rDb.subjectDao().delete(id)
            rDb.chapterDao().deleteBySubject(id)
            rDb.topicDao().deleteBySubject(id)
        }
        try { db.collection("subjects").document(id).delete() } catch (e: Exception) {}
    }
        


    open suspend fun insertChapter(subjectId: String, name: String, importance: String = "Medium") {
        val chapter = Chapter(
            id = java.util.UUID.randomUUID().toString(),
            userId = "offline_user",
            subjectId = subjectId,
            name = name,
            importance = importance
        )
        roomDb?.chapterDao()?.insert(chapter.toEntity())
        try { db.collection("chapters").document(chapter.id).set(chapter) } catch (e: Exception) {}
    }

    open suspend fun updateChapter(chapter: Chapter) {
        roomDb?.chapterDao()?.insert(chapter.toEntity())
        try { db.collection("chapters").document(chapter.id).set(chapter) } catch (e: Exception) {}
    }

    open suspend fun updateChapterProgress(chapterId: String) {
        if (userId.isEmpty() || chapterId.isEmpty()) return
        try {
            val revisionsRef = db.collection("users").document(userId).collection("revisions")
            val snap = revisionsRef.whereEqualTo("chapterId", chapterId).get().await()
            var completedCount = 0
            for (doc in snap.documents) {
                val r = doc.toObject(Revision::class.java) ?: continue
                if (r.status == "Completed") {
                    completedCount++
                }
            }
            val progress = (completedCount.toFloat() / 10f * 100f).coerceAtMost(100f)
            
            db.collection("chapters").document(chapterId).update(
                "revisionCount", completedCount,
                "progress", progress,
                "isCompleted", progress >= 100f,
                "lastRevisionDateMs", System.currentTimeMillis()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open suspend fun recalculateAllChaptersProgress() {
        if (userId.isEmpty()) return
        try {
            val chaptersSnap = db.collection("chapters").whereEqualTo("userId", userId).get().await()
            val revisionsSnap = db.collection("users").document(userId).collection("revisions").get().await()
            
            val revisionsGroupedByChapter = revisionsSnap.documents.mapNotNull { doc -> try { doc.toObject(Revision::class.java) } catch (e: Exception) { e.printStackTrace(); null } }
                .filter { it.status == "Completed" }
                .groupBy { it.chapterId }
                
            for (doc in chaptersSnap.documents) {
                val chapter = doc.toObject(Chapter::class.java) ?: continue
                val completedCount = revisionsGroupedByChapter[chapter.id]?.size ?: 0
                val progress = (completedCount.toFloat() / 10f * 100f).coerceAtMost(100f)
                
                if (chapter.revisionCount != completedCount || kotlin.math.abs(chapter.progress - progress) > 0.01f) {
                    doc.reference.update(
                        "revisionCount", completedCount,
                        "progress", progress,
                        "isCompleted", progress >= 100f
                    ).await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertSubjectTest(subjectId: String, testName: String, dateMs: Long, marksObtained: Float, totalMarks: Float, remarks: String) {
        val testId = java.util.UUID.randomUUID().toString()
        val test = SubjectTest(
            id = testId,
            userId = "offline_user",
            subjectId = subjectId,
            testName = testName,
            dateMs = dateMs,
            marksObtained = marksObtained,
            totalMarks = totalMarks,
            remarks = remarks
        )
        roomDb?.subjectTestDao()?.insert(test.toEntity())
        try { db.collection("subject_tests").document(testId).set(test) } catch (e: Exception) {}
    }

    suspend fun updateSubjectTest(test: SubjectTest) {
        val updated = test.copy(userId = "offline_user")
        roomDb?.subjectTestDao()?.insert(updated.toEntity())
        try { db.collection("subject_tests").document(test.id).set(test) } catch (e: Exception) {}
    }

    suspend fun deleteSubjectTest(testId: String) {
        roomDb?.subjectTestDao()?.delete(testId)
        try { db.collection("subject_tests").document(testId).delete() } catch (e: Exception) {}
    }

    open val allSubjectTests: Flow<List<SubjectTest>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.subjectTestDao().getTests("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    private suspend fun <T> runWithServerOrCache(
        serverBlock: suspend () -> T,
        cacheBlock: suspend () -> T
    ): T {
        return try {
            // Try server with a 1.5 second timeout
            kotlinx.coroutines.withTimeout(1500) {
                serverBlock()
            }
        } catch (e: Exception) {
            try {
                cacheBlock()
            } catch (ex: Exception) {
                throw e // if cache fails too, propagate original
            }
        }
    }

    open suspend fun deleteChapter(id: String) {
        if (userId.isEmpty()) return
        
        val refsToDelete = mutableListOf<com.google.firebase.firestore.DocumentReference>()
        
        // 1. Get Subject ID from Chapter
        var subjectId = ""
        try {
            val chapterDoc = runWithServerOrCache(
                serverBlock = { db.collection("chapters").document(id).get(com.google.firebase.firestore.Source.SERVER).await() },
                cacheBlock = { db.collection("chapters").document(id).get().await() }
            )
            subjectId = chapterDoc.getString("subjectId") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // 2. Add Chapter document itself
        refsToDelete.add(db.collection("chapters").document(id))
        
        if (subjectId.isNotEmpty()) {
            // Delete nested chapter progress/doc
            refsToDelete.add(
                db.collection("users").document(userId)
                    .collection("subjects").document(subjectId)
                    .collection("chapters").document(id)
            )
        }
        
        // 3. Add Topics and their revisions/plans/history/schedule
        val topics = try {
            runWithServerOrCache(
                serverBlock = { db.collection("topics").whereEqualTo("chapterId", id).get(com.google.firebase.firestore.Source.SERVER).await() },
                cacheBlock = { db.collection("topics").whereEqualTo("chapterId", id).get().await() }
            )
        } catch (e: Exception) {
            null
        }
        
        if (topics != null) {
            for (topicDoc in topics.documents) {
                refsToDelete.add(topicDoc.reference)
                val topicId = topicDoc.id
                
                // Delete nested topic progress
                if (subjectId.isNotEmpty()) {
                    val nestedTopicRef = db.collection("users").document(userId)
                        .collection("subjects").document(subjectId)
                        .collection("chapters").document(id)
                        .collection("topics").document(topicId)
                    nestedTopicRef?.let { refsToDelete.add(it) }
                }
                
                refsToDelete.add(db.collection("users").document(userId).collection("revision_plans").document(topicId))
                refsToDelete.add(db.collection("users").document(userId).collection("topic_revisions").document(topicId))
                
                val history = try {
                    runWithServerOrCache(
                        serverBlock = { db.collection("users").document(userId).collection("revision_history").whereEqualTo("topicId", topicId).get(com.google.firebase.firestore.Source.SERVER).await() },
                        cacheBlock = { db.collection("users").document(userId).collection("revision_history").whereEqualTo("topicId", topicId).get().await() }
                    )
                } catch (e: Exception) {
                    null
                }
                history?.documents?.forEach { refsToDelete.add(it.reference) }
                
                val schedule = try {
                    runWithServerOrCache(
                        serverBlock = { db.collection("users").document(userId).collection("revision_schedule").whereEqualTo("topicId", topicId).get(com.google.firebase.firestore.Source.SERVER).await() },
                        cacheBlock = { db.collection("users").document(userId).collection("revision_schedule").whereEqualTo("topicId", topicId).get().await() }
                    )
                } catch (e: Exception) {
                    null
                }
                schedule?.documents?.forEach { refsToDelete.add(it.reference) }
                
                val revisions = try {
                    runWithServerOrCache(
                        serverBlock = { db.collection("users").document(userId).collection("revisions").whereEqualTo("topicId", topicId).get(com.google.firebase.firestore.Source.SERVER).await() },
                        cacheBlock = { db.collection("users").document(userId).collection("revisions").whereEqualTo("topicId", topicId).get().await() }
                    )
                } catch (e: Exception) {
                    null
                }
                revisions?.documents?.forEach { refsToDelete.add(it.reference) }
            }
        }
        
        try {
            refsToDelete.chunked(500).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { batch.delete(it) }
                batch.commit().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

        suspend fun insertTopicFull(subjectId: String, chapterId: String, name: String, nextRevisionDateMs: Long, estimatedRevisionTimeMins: Int, notes: String) {
        if (userId.isEmpty()) return
        val ref = db.collection("topics").document()
        val topic = Topic(
            id = ref.id,
            userId = userId,
            subjectId = subjectId,
            chapterId = chapterId,
            name = name,
            difficulty = "Medium",
            estimatedRevisionTimeMins = estimatedRevisionTimeMins,
            notes = notes,
            nextRevisionDateMs = nextRevisionDateMs,
            status = "Pending"
        )
        ref.set(topic).await()
    }


    
    
    
    open val allRevisions: Flow<List<Revision>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.revisionDao().getRevisions("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    open val allQuickTasks: Flow<List<QuickTask>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(rDb.quickTaskDao().getTasks("offline_user").map { list -> list.map { it.toDomain() } })
        } else {
            emit(emptyList())
        }
    }

    suspend fun addQuickTask(task: QuickTask) {
        val taskId = task.id.ifEmpty { java.util.UUID.randomUUID().toString() }
        val taskWithId = task.copy(id = taskId, userId = "offline_user")
        roomDb?.quickTaskDao()?.insert(taskWithId.toEntity())
        try { db.collection("users").document(userId).collection("quick_tasks").document(taskId).set(taskWithId) } catch (e: Exception) {}
    }

    suspend fun updateQuickTask(task: QuickTask) {
        val updated = task.copy(userId = "offline_user")
        roomDb?.quickTaskDao()?.insert(updated.toEntity())
        try { db.collection("users").document(userId).collection("quick_tasks").document(task.id).set(task) } catch (e: Exception) {}
    }

    suspend fun deleteQuickTask(taskId: String) {
        roomDb?.quickTaskDao()?.delete(taskId)
        try { db.collection("users").document(userId).collection("quick_tasks").document(taskId).delete() } catch (e: Exception) {}
    }

    suspend fun addManualRevision(
        topicId: String,
        dateMs: Long,
        durationMins: Int,
        revisionNumber: Int,
        notes: String,
        status: String
    ) {
        if (userId.isEmpty()) return
        
        val topicRef = db.collection("topics").document(topicId)
        val topicSnap = topicRef.get().await()
        val topic = topicSnap.toObject(Topic::class.java) ?: return

        val now = System.currentTimeMillis()
        val durationMs = durationMins * 60 * 1000L

        val chapterSnap = db.collection("chapters").document(topic.chapterId).get().await()
        val chapterName = chapterSnap.getString("name") ?: ""
        val subjectId = if (topic.subjectId.isNotBlank()) topic.subjectId else chapterSnap.getString("subjectId") ?: ""
        val subjectSnap = db.collection("subjects").document(subjectId).get().await()
        val subjectName = subjectSnap.getString("name") ?: ""

        val revRef = db.collection("users").document(userId).collection("revisions").document()
        val pattern = listOf(1, 3, 7, 14, 30)
        
        val isCompleted = status == "Completed" || status == "Complete"
        
        val nextDate = if (isCompleted) {
            val nextIntervalIndex = (revisionNumber - 1).coerceAtMost(pattern.lastIndex).coerceAtLeast(0)
            val daysMulti = pattern.getOrElse(nextIntervalIndex) { 1 }
            dateMs + (daysMulti * 86400000L)
        } else {
            dateMs
        }

        val revision = Revision(
            id = revRef.id,
            userId = userId,
            subjectId = subjectId,
            chapterId = topic.chapterId,
            topicId = topicId,
            subjectName = subjectName,
            chapterName = chapterName,
            topicName = topic.name,
            revisionCount = if (isCompleted) revisionNumber else (revisionNumber - 1).coerceAtLeast(0),
            pattern = pattern,
            lastRevisionDateMs = if (isCompleted) dateMs else 0L,
            nextRevisionDateMs = if (isCompleted) 0L else dateMs,
            status = status,
            notes = notes,
            createdAt = now
        )
        revRef.set(revision).await()

        if (isCompleted) {
            // also create the next scheduled revision
            val nextRef = db.collection("users").document(userId).collection("revisions").document()
            val nextRev = revision.copy(
                id = nextRef.id,
                revisionCount = revisionNumber,
                lastRevisionDateMs = 0L,
                nextRevisionDateMs = nextDate,
                status = "Pending",
                notes = ""
            )
            nextRef.set(nextRev).await()

            // Update topic
            val updates = mutableMapOf<String, Any>(
                "revisionCount" to revisionNumber,
                "lastRevisionDateMs" to dateMs,
                "nextRevisionDateMs" to nextDate,
                "revisionTimeMs" to (topic.revisionTimeMs + durationMs),
                
                // Explicit fields
                "totalRevisionCount" to revisionNumber,
                "lastRevisionDate" to dateMs,
                "lastRevisionDuration" to durationMs,
                "nextRevisionDate" to nextDate,
                "currentRevisionStage" to revisionNumber
            )
            if (topic.status != "Complete") {
                updates["status"] = "Complete"
            }
            topicRef.update(updates).await()

            val df = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val dateStr = df.format(java.util.Date(dateMs))
            recordStudyTime(dateStr, true, durationMs)
        }
    }
    suspend fun insertRevision(subjectId: String, chapterId: String, topicId: String, subjectName: String, chapterName: String, topicName: String, pattern: List<Int>, nextRevisionDateMs: Long, reminderTimeMins: Int, reminderTime: String, notes: String): Revision? {
        if (userId.isEmpty()) return null
        var sName = subjectName
        var cName = chapterName
        try {
            if (sName.isBlank() && subjectId.isNotEmpty()) {
                val subDoc = db.collection("subjects").document(subjectId).get().await()
                sName = subDoc.getString("name") ?: ""
            }
            if (cName.isBlank() && chapterId.isNotEmpty()) {
                val chapDoc = db.collection("chapters").document(chapterId).get().await()
                cName = chapDoc.getString("name") ?: ""
            }
        } catch (e: Exception) {}
        val ref = db.collection("users").document(userId).collection("revisions").document()
        val rev = Revision(
            id = ref.id,
            userId = userId,
            subjectId = subjectId,
            chapterId = chapterId,
            topicId = topicId,
            subjectName = sName,
            chapterName = cName,
            topicName = topicName,
            pattern = pattern,
            nextRevisionDateMs = nextRevisionDateMs,
            notes = notes,
            status = "Pending"
        )
        ref.set(rev).await()
        return rev
    }


    private fun getStartOfTodayMs(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    suspend fun checkAndUpdateOverdueRevisionsStatus() {
        val startOfToday = getStartOfTodayMs()
        val rDb = roomDb
        if (rDb != null) {
            try {
                val localRevs = rDb.revisionDao().getRevisionsList(userId.ifEmpty { "offline_user" }).map { it.toDomain() }
                for (rev in localRevs) {
                    if (rev.status == "Completed") continue
                    val shouldBeOverdue = rev.nextRevisionDateMs in 1 until startOfToday
                    if (shouldBeOverdue && rev.status != "Overdue") {
                        rDb.revisionDao().insert(rev.copy(status = "Overdue").toEntity())
                    } else if (!shouldBeOverdue && rev.status == "Overdue") {
                        rDb.revisionDao().insert(rev.copy(status = "Pending").toEntity())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }
        if (userId.isEmpty()) return
        try {
            val revisionsRef = db.collection("users").document(userId).collection("revisions")
            val snapshot = revisionsRef.get().await()
            for (doc in snapshot.documents) {
                val rev = doc.toObject(Revision::class.java) ?: continue
                if (rev.status == "Completed") continue
                
                val shouldBeOverdue = rev.nextRevisionDateMs < startOfToday && rev.nextRevisionDateMs > 0L
                val currentStatus = rev.status
                
                if (shouldBeOverdue && currentStatus != "Overdue") {
                    doc.reference.update("status", "Overdue").await()
                } else if (!shouldBeOverdue && currentStatus == "Overdue") {
                    doc.reference.update("status", "Pending").await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteRevisionsForTopic(topicId: String) {
        val rDb = roomDb
        if (rDb != null) {
            rDb.revisionDao().deleteByTopic(topicId)
            return
        }
        if (userId.isEmpty()) return
        try {
            val snap = db.collection("users").document(userId).collection("revisions")
                .whereEqualTo("topicId", topicId).get().await()
            for (doc in snap.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {}
    }

    suspend fun recalculateFutureRevisions(newPattern: List<Int>) {
        if (userId.isEmpty() || newPattern.isEmpty()) return
        val rDb = roomDb
        if (rDb != null) {
            val localRevs = rDb.revisionDao().getRevisionsList(userId.ifEmpty { "offline_user" }).map { it.toDomain() }
            for (rev in localRevs) {
                if (rev.status == "Completed") continue
                val baseDate = if (rev.lastRevisionDateMs > 0) {
                    rev.lastRevisionDateMs
                } else {
                    rev.createdAt.coerceAtLeast(System.currentTimeMillis() - 86400000L)
                }
                val index = rev.revisionCount.coerceIn(0, newPattern.size - 1)
                val daysMulti = newPattern[index]
                val nextDate = baseDate + (daysMulti * 86400000L)
                val status = if (nextDate < getStartOfTodayMs()) "Overdue" else "Pending"
                rDb.revisionDao().insert(
                    rev.copy(
                        pattern = newPattern,
                        nextRevisionDateMs = nextDate,
                        status = status
                    ).toEntity()
                )
                if (rev.topicId.isNotEmpty()) {
                    val existingTopic = rDb.topicDao().getTopicById(rev.topicId)?.toDomain()
                    if (existingTopic != null) {
                        rDb.topicDao().insert(
                            existingTopic.copy(
                                revisionPattern = newPattern,
                                customSpacingPattern = newPattern,
                                nextRevisionDateMs = nextDate,
                                nextRevisionDate = nextDate
                            ).toEntity()
                        )
                    }
                }
            }
            return
        }
        val revisionsRef = db.collection("users").document(userId).collection("revisions")
        val snapshot = try {
            revisionsRef.get().await()
        } catch (e: Exception) {
            try {
                revisionsRef.get().await()
            } catch (ex: Exception) {
                null
            }
        }
        if (snapshot != null) {
            for (doc in snapshot.documents) {
                val rev = doc.toObject(Revision::class.java) ?: continue
                if (rev.status == "Completed") continue
                
                val baseDate = if (rev.lastRevisionDateMs > 0) {
                    rev.lastRevisionDateMs
                } else {
                    rev.createdAt.coerceAtLeast(System.currentTimeMillis() - 86400000L)
                }
                
                val index = rev.revisionCount.coerceIn(0, newPattern.size - 1)
                val daysMulti = newPattern[index]
                val nextDate = baseDate + (daysMulti * 86400000L)
                val status = if (nextDate < getStartOfTodayMs()) "Overdue" else "Pending"
                
                doc.reference.update(
                    mapOf(
                        "pattern" to newPattern,
                        "nextRevisionDateMs" to nextDate,
                        "status" to status
                    )
                ).await()
                
                if (rev.topicId.isNotEmpty()) {
                    try {
                        db.collection("topics").document(rev.topicId).update(
                            mapOf(
                                "revisionPattern" to newPattern,
                                "nextRevisionDateMs" to nextDate
                            )
                        ).await()
                    } catch (e: Exception) {}
                }
            }
        }
    }

    open suspend fun markRevisionComplete(revisionId: String) {
        if (userId.isEmpty()) return
        val now = System.currentTimeMillis()
        val dateRaw = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(now))

        val rDb = roomDb
        val localRev = rDb?.revisionDao()?.getRevisionById(revisionId)?.toDomain()
        val rev = localRev ?: if (rDb == null) {
            try {
                val ref = db.collection("users").document(userId).collection("revisions").document(revisionId)
                val snapshot = ref.get().await()
                snapshot.toObject(Revision::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        } ?: return

        val durationMs = (rev.duration.takeIf { it > 0 } ?: 30) * 60 * 1000L

        // 1. Mark current revision record as Completed
        rDb?.revisionDao()?.insert(
            rev.copy(
                status = "Completed",
                nextRevisionDateMs = 0L,
                lastRevisionDateMs = now
            ).toEntity()
        )
        if (rDb == null) {
            try {
                val ref = db.collection("users").document(userId).collection("revisions").document(revisionId)
                ref.update("status", "Completed", "nextRevisionDateMs", 0L, "lastRevisionDateMs", now).await()
                if (rev.chapterId.isNotEmpty()) {
                    updateChapterProgress(rev.chapterId)
                }
            } catch (e: Exception) {}
        }

        // 2. Record study session and schedule NEXT revision (repeats every 30 days)
        if (rev.topicId.isNotEmpty()) {
            recordStudySessionTransaction(rev.topicId, isRevision = true, durationMs = durationMs, dateRaw = dateRaw)
        }
        
        // 3. Award coins for completion based on priority
        try {
            val coinsToAward = when (rev.priority.lowercase()) {
                "high" -> 50
                "medium" -> 30
                else -> 20
            }
            awardCoins(coinsToAward, "Completed revision for ${rev.topicName}", "revision_${rev.id}_$now")
            
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            recordStudyTime(todayStr, true, 15 * 60000L)
            
            if (rDb != null) {
                val uid = userId.ifEmpty { "offline_user" }
                val currentProfile = rDb.userProfileDao().getProfileSync(uid)?.toDomain() ?: UserProfile(id = uid)
                rDb.userProfileDao().insert(
                    currentProfile.copy(
                        totalRevisions = currentProfile.totalRevisions + 1,
                        lastActiveTimeMs = now
                    ).toEntity()
                )
            } else {
                val docRef = db.collection("users").document(userId)
                db.runTransaction { tx ->
                    val userSnap = tx.get(docRef)
                    if (userSnap.exists()) {
                        val currentRevs = userSnap.getLong("totalRevisions") ?: 0L
                        tx.update(docRef, "totalRevisions", currentRevs + 1, "lastActiveTimeMs", System.currentTimeMillis())
                    }
                    null
                }.await()
                evaluateDailyRewardsAndStreak(userId)
            }
        } catch (e: Exception) {}
    }
    
    suspend fun updateRevision(revision: Revision) {
        val rDb = roomDb
        if (rDb != null) {
            rDb.revisionDao().insert(revision.toEntity())
            return
        }
        if (userId.isEmpty()) return
        try {
            db.collection("users").document(userId).collection("revisions").document(revision.id).set(revision).await()
        } catch (e: Exception) {}
    }

    suspend fun deleteRevision(revisionId: String) {
        val rDb = roomDb
        if (rDb != null) {
            rDb.revisionDao().delete(revisionId)
            return
        }
        if (userId.isEmpty()) return
        try {
            db.collection("users").document(userId).collection("revisions").document(revisionId).delete().await()
        } catch (e: Exception) {}
    }

    open suspend fun insertTopic(chapterId: String, name: String, difficulty: String = "Medium", estimatedRevisionTimeMins: Int = 30, notes: String = "", tags: List<String> = emptyList()) {
        if (userId.isEmpty()) return
        val rDb = roomDb
        var resolvedSubjectId = rDb?.chapterDao()?.getChapterById(chapterId)?.subjectId ?: ""
        if (resolvedSubjectId.isEmpty() && rDb == null) {
            try {
                val chapterDoc = db.collection("chapters").document(chapterId).get().await()
                if (chapterDoc.exists()) {
                    resolvedSubjectId = chapterDoc.getString("subjectId") ?: ""
                }
            } catch (e: Exception) {
                android.util.Log.e("RevisionRepository", "Failed to resolve subjectId for chapterId $chapterId", e)
            }
        }
        val topicId = java.util.UUID.randomUUID().toString()
        val topic = Topic(id = topicId, userId = userId, chapterId = chapterId, subjectId = resolvedSubjectId, name = name, difficulty = difficulty, estimatedRevisionTimeMins = estimatedRevisionTimeMins, notes = notes, tags = tags)
        if (rDb != null) {
            rDb.topicDao().insert(topic.toEntity())
            return
        }
        try {
            db.collection("topics").document(topicId).set(topic).await()
        } catch (e: Exception) {}
    }

    suspend fun insertTopicWithSubjectId(subjectId: String, name: String, difficulty: String, estimatedRevisionTimeMins: Int, notes: String = "", tags: List<String> = emptyList()) {
        if (userId.isEmpty()) return
        val topicId = java.util.UUID.randomUUID().toString()
        val topic = Topic(id = topicId, userId = userId, subjectId = subjectId, name = name, difficulty = difficulty, estimatedRevisionTimeMins = estimatedRevisionTimeMins, notes = notes, tags = tags)
        val rDb = roomDb
        if (rDb != null) {
            rDb.topicDao().insert(topic.toEntity())
            return
        }
        try {
            db.collection("topics").document(topicId).set(topic).await()
        } catch (e: Exception) {}
    }

    open suspend fun updateTopicDetails(topicId: String, name: String, difficulty: String, estimatedRevisionTimeMins: Int, notes: String, tags: List<String> = emptyList()) {
        if (userId.isEmpty()) return
        val rDb = roomDb
        if (rDb != null) {
            val existing = rDb.topicDao().getTopicById(topicId)
            if (existing != null) {
                val domain = existing.toDomain().copy(
                    name = name,
                    difficulty = difficulty,
                    estimatedRevisionTimeMins = estimatedRevisionTimeMins,
                    notes = notes,
                    tags = tags
                )
                rDb.topicDao().insert(domain.toEntity())
            }
            return
        }
        val updates = mapOf(
            "name" to name,
            "difficulty" to difficulty,
            "estimatedRevisionTimeMins" to estimatedRevisionTimeMins,
            "notes" to notes,
            "tags" to tags
        )
        try {
            db.collection("topics").document(topicId).update(updates).await()
            val revisionsRef = db.collection("users").document(userId).collection("revisions")
            val snap = revisionsRef.whereEqualTo("topicId", topicId).get().await()
            for (doc in snap.documents) {
                doc.reference.update("topicName", name).await()
            }
        } catch (e: Exception) {}
    }

    open suspend fun updateTopicSpacedRepetition(
        topicId: String,
        difficulty: String,
        revisionPattern: List<Int>,
        nextRevisionDateMs: Long,
        startDateMs: Long
    ) {
        if (userId.isEmpty() || topicId.isBlank()) {
            throw IllegalArgumentException("Invalid topic ID")
        }
        if (revisionPattern.isEmpty() || revisionPattern.any { it <= 0 }) {
            throw IllegalArgumentException("Spacing intervals must be positive integers")
        }
        val rDb = roomDb
        if (rDb != null) {
            rDb.withTransaction {
                val existingEntity = rDb.topicDao().getTopicById(topicId)
                    ?: throw IllegalStateException("Topic does not exist: $topicId")
                val existing = existingEntity.toDomain()
                val isUnstudied = (existing.firstStudyDate == null && existing.revisionCount == 0)
                val resolvedStartDateMs = if (existing.firstStudyDate != null) existing.firstStudyDate else (if (startDateMs > 0L && !isUnstudied) startDateMs else existing.startDateMs)
                val resolvedNextDateMs = if (nextRevisionDateMs > 0L) {
                    nextRevisionDateMs
                } else if (!isUnstudied) {
                    com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                        revisionCount = existing.revisionCount,
                        pattern = revisionPattern,
                        firstStudyMs = existing.firstStudyDate ?: System.currentTimeMillis(),
                        lastRevisionMs = existing.lastRevisionDateMs.takeIf { it > 0L } ?: (existing.firstStudyDate ?: System.currentTimeMillis())
                    )
                } else {
                    0L
                }
                val startOfToday = getStartOfTodayMs()
                val endOfToday = startOfToday + 86400000L - 1
                val revStatus = when {
                    resolvedNextDateMs <= 0L -> "Pending"
                    resolvedNextDateMs < startOfToday -> "Overdue"
                    resolvedNextDateMs in startOfToday..endOfToday -> "Today"
                    else -> "Upcoming"
                }
                val studyDone = existing.studyCompleted || existing.firstStudyDate != null || existing.revisionCount > 0
                val progScore = calculateProgressScore(studyDone, existing.revisionCount, revisionPattern.size)
                val updatedTopic = existing.copy(
                    difficulty = difficulty,
                    revisionPattern = revisionPattern,
                    customSpacingPattern = revisionPattern,
                    nextRevisionDateMs = resolvedNextDateMs,
                    nextRevisionDate = resolvedNextDateMs,
                    startDateMs = resolvedStartDateMs,
                    totalRequiredRevisions = revisionPattern.size,
                    progressScore = progScore,
                    progressPercentage = progScore,
                    revisionStatus = revStatus,
                    updatedAt = System.currentTimeMillis()
                )
                rDb.topicDao().insert(updatedTopic.toEntity())
                syncRevisionDocument(updatedTopic, revisionPattern, isFirstCompletion = false)
            }
            return
        } else {
            val updates = mapOf(
                "difficulty" to difficulty,
                "revisionPattern" to revisionPattern,
                "customSpacingPattern" to revisionPattern,
                "nextRevisionDateMs" to nextRevisionDateMs,
                "nextRevisionDate" to nextRevisionDateMs,
                "startDateMs" to startDateMs
            )
            db.collection("topics").document(topicId).update(updates).await()
        }
    }

    suspend fun saveCustomRevisionPlan(topicId: String, intervals: List<Int>) {
        if (userId.isEmpty()) return
        val map = mapOf(
            "topicId" to topicId,
            "intervals" to intervals,
            "mode" to "CUSTOM"
        )
        db.collection("users").document(userId).collection("revision_plans").document(topicId).set(map).await()
    }

    suspend fun calculateNextRevisionDate(topicId: String, currentRevisionCount: Int): Long {
        val uid = userId
        if (uid.isEmpty()) return System.currentTimeMillis() + 86400000L
        
        val planDoc = db.collection("users").document(uid).collection("revision_plans").document(topicId).get().await()
        
        val settingsDoc = db.collection("users").document(uid).collection("revision_settings").document("data").get().await()
        val settings = if (settingsDoc.exists()) settingsDoc.toObject(RevisionSettings::class.java) ?: RevisionSettings() else RevisionSettings()
        val defaultIntervals = listOf(
            settings.firstRevisionDays,
            settings.secondRevisionDays,
            settings.thirdRevisionDays,
            settings.fourthRevisionDays,
            settings.fifthRevisionDays,
            settings.fifthRevisionDays * 2,
            settings.fifthRevisionDays * 3
        )
        
        var isSmart = true
        val intervals = if (planDoc.exists()) {
            val mode = planDoc.getString("mode")
            isSmart = mode != "CUSTOM"
            val list = planDoc.get("intervals") as? List<*>
            list?.mapNotNull { (it as? Number)?.toInt() } ?: defaultIntervals
        } else {
            defaultIntervals
        }
        
        val index = (currentRevisionCount - 1).coerceIn(0, intervals.size - 1)
        var gapDays = intervals[index]
        
        val now = System.currentTimeMillis()
        val examsSnapshot = db.collection("exams").whereEqualTo("userId", uid).get().await()
        val nearestUpcomingExamMs = examsSnapshot.documents.mapNotNull { it.getLong("examDateMs") }
            .filter { it > now }
            .minOrNull()
            
        if (isSmart && settings.smartRevisionEnabled && nearestUpcomingExamMs != null) {
            val daysToExam = (nearestUpcomingExamMs - now) / 86400000L
            if (daysToExam in 1..30) {
                // Shorten revision gaps
                gapDays = when {
                    gapDays >= 30 -> 15
                    gapDays >= 15 -> 7
                    gapDays >= 7 -> 3
                    gapDays >= 3 -> 1
                    else -> gapDays
                }
            }
        }
        
        return now + (gapDays * 86400000L)
    }

    private suspend fun saveSmartRevisionData(topic: Topic, revNum: Int, nextRevisionMs: Long, isFirst: Boolean) {
        val uid = userId
        if (uid.isEmpty()) return
        
        val now = System.currentTimeMillis()
        
        // 1. users/{uid}/topic_revisions
        val topicRevRef = db.collection("users").document(uid).collection("topic_revisions").document(topic.id)
        topicRevRef.set(mapOf(
            "topicId" to topic.id,
            "topicName" to topic.name,
            "revisionCount" to revNum,
            "lastRevisionDateMs" to now,
            "nextRevisionDateMs" to nextRevisionMs
        )).await()

        // 2. users/{uid}/revision_history
        val historyRef = db.collection("users").document(uid).collection("revision_history").document()
        historyRef.set(mapOf(
            "id" to historyRef.id,
            "topicId" to topic.id,
            "topicName" to topic.name,
            "completedDateMs" to now,
            "revisionNumber" to revNum,
            "durationMs" to (if (isFirst) topic.estimatedRevisionTimeMins * 60000L else 15L * 60000L)
        )).await()

        // 3. users/{uid}/revision_schedule
        try {
            val existingSchedules = db.collection("users").document(uid).collection("revision_schedule")
                .whereEqualTo("topicId", topic.id)
                .whereEqualTo("status", "PENDING")
                .get().await()
            for (doc in existingSchedules.documents) {
                doc.reference.update("status", "COMPLETED").await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val nextScheduleRef = db.collection("users").document(uid).collection("revision_schedule").document()
        nextScheduleRef.set(mapOf(
            "id" to nextScheduleRef.id,
            "topicId" to topic.id,
            "topicName" to topic.name,
            "scheduledDateMs" to nextRevisionMs,
            "status" to "PENDING"
        )).await()

        // 4. users/{uid}/notifications
        val notifRef = db.collection("users").document(uid).collection("notifications").document()
        notifRef.set(mapOf(
            "id" to notifRef.id,
            "topicId" to topic.id,
            "title" to "Revision Due Today",
            "message" to "${topic.name} revision is scheduled today. Tap to revise now.",
            "timestampMs" to nextRevisionMs,
            "isRead" to false,
            "type" to "DUE"
        )).await()
    }

    open suspend fun updateTopicTimes(topicId: String, isRevision: Boolean, durationMs: Long) {
        if (topicId.isEmpty()) return
        try {
            val tRef = db.collection("topics").document(topicId)
            db.runTransaction { tx ->
                val snap = tx.get(tRef)
                if (snap.exists()) {
                    if (isRevision) {
                        val currentMs = snap.getLong("revisionTimeMs") ?: 0L
                        tx.update(
                            tRef, 
                            "revisionTimeMs", currentMs + durationMs,
                            "lastRevisionDuration", durationMs
                        )
                    } else {
                        val currentMs = snap.getLong("studyTimeMs") ?: 0L
                        tx.update(
                            tRef, 
                            "studyTimeMs", currentMs + durationMs,
                            "firstStudyDuration", durationMs,
                            "status", "Complete"
                        )
                    }
                }
                null
            }.await()
        } catch (e: Exception) {}
    }

    private suspend fun syncRevisionDocument(topic: Topic, pattern: List<Int>, isFirstCompletion: Boolean) {
        val nextDate = topic.nextRevisionDateMs
        val status = if (nextDate < getStartOfTodayMs()) "Overdue" else "Pending"

        val rDb = roomDb
        var sName = ""
        var cName = ""
        if (rDb != null) {
            if (topic.subjectId.isNotEmpty()) {
                sName = rDb.subjectDao().getSubjectById(topic.subjectId)?.name ?: ""
            }
            if (topic.chapterId.isNotEmpty()) {
                cName = rDb.chapterDao().getChapterById(topic.chapterId)?.name ?: ""
            }
            val localRevisions = rDb.revisionDao().getRevisionsForTopic(topic.id).map { it.toDomain() }
            val activeLocal = localRevisions.filter { it.status != "Completed" }
            if (nextDate <= 0L) {
                for (rev in activeLocal) {
                    rDb.revisionDao().insert(rev.copy(status = "Completed", nextRevisionDateMs = 0L).toEntity())
                }
            } else if (activeLocal.isEmpty()) {
                val newRev = Revision(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = userId.ifEmpty { "offline_user" },
                    subjectId = topic.subjectId,
                    chapterId = topic.chapterId,
                    topicId = topic.id,
                    topicName = topic.name,
                    subjectName = sName,
                    chapterName = cName,
                    pattern = pattern,
                    revisionCount = topic.revisionCount,
                    lastRevisionDateMs = topic.lastRevisionDateMs,
                    nextRevisionDateMs = nextDate,
                    status = status,
                    duration = topic.estimatedRevisionTimeMins.coerceAtLeast(15),
                    createdAt = System.currentTimeMillis()
                )
                rDb.revisionDao().insert(newRev.toEntity())
            } else {
                val primary = activeLocal.first()
                val updatedRev = primary.copy(
                    pattern = pattern,
                    revisionCount = topic.revisionCount,
                    lastRevisionDateMs = topic.lastRevisionDateMs,
                    nextRevisionDateMs = nextDate,
                    subjectName = if (sName.isNotEmpty()) sName else primary.subjectName,
                    chapterName = if (cName.isNotEmpty()) cName else primary.chapterName,
                    topicName = topic.name,
                    status = status,
                    duration = topic.estimatedRevisionTimeMins.coerceAtLeast(15)
                )
                rDb.revisionDao().insert(updatedRev.toEntity())
                for (i in 1..activeLocal.lastIndex) {
                    rDb.revisionDao().delete(activeLocal[i].id)
                }
            }
            return
        }

        val revisionsRef = try {
            db.collection("users").document(userId).collection("revisions")
        } catch (e: Exception) {
            return
        }
        val snap = try {
            revisionsRef.whereEqualTo("topicId", topic.id).get().await()
        } catch (e: Exception) {
            return
        }
        val activeRevisions = snap.documents.mapNotNull { doc -> try { doc.toObject(Revision::class.java) } catch (e: Exception) { e.printStackTrace(); null } }.filter { it.status != "Completed" }
        
        if (nextDate <= 0L) {
            // Delete/complete all active revisions for this topic if stopped
            for (doc in snap.documents) {
                val rev = doc.toObject(Revision::class.java) ?: continue
                if (rev.status != "Completed") {
                    try { doc.reference.update("status", "Completed", "nextRevisionDateMs", 0L).await() } catch (e: Exception) {}
                }
            }
        } else {
            try {
                if (sName.isEmpty() && topic.subjectId.isNotEmpty()) {
                    val subDoc = db.collection("subjects").document(topic.subjectId).get().await()
                    sName = subDoc.getString("name") ?: ""
                }
                if (cName.isEmpty() && topic.chapterId.isNotEmpty()) {
                    val chapDoc = db.collection("chapters").document(topic.chapterId).get().await()
                    cName = chapDoc.getString("name") ?: ""
                }
            } catch (e: Exception) {}

            try {
                if (activeRevisions.isEmpty()) {
                    val ref = revisionsRef.document()
                    val newRev = Revision(
                        id = ref.id,
                        userId = userId,
                        subjectId = topic.subjectId,
                        chapterId = topic.chapterId,
                        topicId = topic.id,
                        topicName = topic.name,
                        subjectName = sName,
                        chapterName = cName,
                        pattern = pattern,
                        revisionCount = topic.revisionCount,
                        lastRevisionDateMs = topic.lastRevisionDateMs,
                        nextRevisionDateMs = nextDate,
                        status = status,
                        createdAt = System.currentTimeMillis()
                    )
                    ref.set(newRev).await()
                } else {
                    val primary = activeRevisions.first()
                    val updatedRev = primary.copy(
                        pattern = pattern,
                        revisionCount = topic.revisionCount,
                        lastRevisionDateMs = topic.lastRevisionDateMs,
                        nextRevisionDateMs = nextDate,
                        subjectName = if (sName.isNotEmpty()) sName else primary.subjectName,
                        chapterName = if (cName.isNotEmpty()) cName else primary.chapterName,
                        status = status
                    )
                    revisionsRef.document(primary.id).set(updatedRev).await()
                    
                    for (i in 1..activeRevisions.lastIndex) {
                        revisionsRef.document(activeRevisions[i].id).delete().await()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    open suspend fun recordStudySessionTransaction(
        topicId: String,
        isRevision: Boolean,
        durationMs: Long,
        dateRaw: String
    ) {
        if (userId.isEmpty() || topicId.isEmpty()) return
        val rDb = roomDb

        if (rDb != null) {
            var finalTopicResult: Topic? = null
            var isFirstStudyResult = false
            rDb.withTransaction {
                val existingEntity = rDb.topicDao().getTopicById(topicId)
                    ?: throw IllegalStateException("Topic does not exist: $topicId")
                val topic = existingEntity.toDomain()
                val profile = rDb.userProfileDao().getProfileSync(userId.ifEmpty { "offline_user" })?.toDomain()

                val now = System.currentTimeMillis()
                val activePattern = topic.revisionPattern?.takeIf { it.isNotEmpty() }
                    ?: topic.customSpacingPattern?.takeIf { it.isNotEmpty() }
                    ?: profile?.revisionPattern?.takeIf { it.isNotEmpty() }
                    ?: com.example.services.RevisionEngine.DEFAULT_PATTERN

                val isFirstStudy = (topic.firstStudyDate == null && topic.revisionCount == 0 && topic.completedRevisions == 0)
                isFirstStudyResult = isFirstStudy

                val expected = topic.estimatedRevisionTimeMins * 60000L
                val actualDuration = if (durationMs > 0L) durationMs else expected
                val actualMins = Math.round(actualDuration / 60000.0).toInt().coerceAtLeast(1)

                val revNumber = if (isFirstStudy) 0 else topic.revisionCount + 1
                val historyId = java.util.UUID.randomUUID().toString()
                val newHistory = RevisionHistoryRecord(
                    id = historyId,
                    topicId = topic.id,
                    topicName = topic.name,
                    completedDateMs = now,
                    revisionNumber = revNumber,
                    durationMs = actualDuration
                )
                rDb.revisionHistoryDao().insert(newHistory.toEntity())

                val pastRecords = rDb.revisionHistoryDao().getHistoryListForTopic(topic.id).map { it.toDomain() }
                val revisionsOnly = pastRecords.filter { it.revisionNumber > 0 }.sortedBy { it.completedDateMs }
                val firstStudyRecord = pastRecords.find { it.revisionNumber == 0 }
                val firstMins = firstStudyRecord?.durationMs?.let { Math.round(it / 60000.0).toInt() } ?: topic.estimatedRevisionTimeMins

                val computedExpectedMins = when (revNumber) {
                    0 -> firstMins
                    1 -> actualMins
                    2 -> {
                        val sum = revisionsOnly.sumOf { Math.round(it.durationMs / 60000.0).toInt() }
                        if (revisionsOnly.isNotEmpty()) Math.round(sum / revisionsOnly.size.toDouble()).toInt() else actualMins
                    }
                    else -> {
                        val last3 = revisionsOnly.takeLast(3)
                        val sum = last3.sumOf { Math.round(it.durationMs / 60000.0).toInt() }
                        if (last3.isNotEmpty()) Math.round(sum / last3.size.toDouble()).toInt() else actualMins
                    }
                }.coerceAtLeast(1)

                val finalTopic: Topic = if (isFirstStudy) {
                    val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                        revisionCount = 0,
                        pattern = activePattern,
                        firstStudyMs = now,
                        lastRevisionMs = now
                    )
                    topic.copy(
                        status = "Complete",
                        revisionStatus = "Upcoming",
                        studyTimeMs = topic.studyTimeMs + actualDuration,
                        startDateMs = now,
                        lastRevisionDateMs = now,
                        nextRevisionDateMs = nextRevisionMs,
                        masteryScore = 20,
                        revisionPattern = activePattern,
                        revisionCount = 0,
                        firstStudyDate = now,
                        firstStudyTime = formatDurationSpent(actualDuration),
                        firstStudyDuration = actualDuration,
                        totalRevisions = 0,
                        totalRevisionCount = 0,
                        lastRevisionDate = null,
                        lastRevisionTime = null,
                        lastRevisionDuration = 0L,
                        nextRevisionDate = nextRevisionMs,
                        expectedRevisionTime = computedExpectedMins,
                        estimatedRevisionTimeMins = computedExpectedMins,
                        currentRevisionStage = 0,
                        customSpacingPattern = activePattern,
                        progressPercentage = calculateProgressScore(true, 0, activePattern.size),
                        expectedStudyTime = expected,
                        createdAt = topic.createdAt ?: now,
                        updatedAt = now,
                        studyCompleted = true,
                        completedRevisions = 0,
                        totalRequiredRevisions = activePattern.size,
                        progressScore = calculateProgressScore(true, 0, activePattern.size)
                    )
                } else {
                    val newRevisionCount = topic.revisionCount + 1
                    val firstStudyMs = topic.firstStudyDate ?: topic.startDateMs ?: topic.createdAt ?: now
                    val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                        revisionCount = newRevisionCount,
                        pattern = activePattern,
                        firstStudyMs = firstStudyMs,
                        lastRevisionMs = now
                    )
                    val progScore = calculateProgressScore(true, newRevisionCount, activePattern.size)
                    topic.copy(
                        status = "Complete",
                        studyTimeMs = if (topic.studyTimeMs > 0L) topic.studyTimeMs else actualDuration,
                        revisionTimeMs = topic.revisionTimeMs + actualDuration,
                        startDateMs = topic.startDateMs ?: firstStudyMs,
                        nextRevisionDateMs = nextRevisionMs,
                        lastRevisionDateMs = now,
                        revisionCount = newRevisionCount,
                        masteryScore = minOf(20 + (newRevisionCount * 15), 100),
                        revisionPattern = activePattern,
                        firstStudyDate = topic.firstStudyDate ?: firstStudyMs,
                        firstStudyTime = topic.firstStudyTime ?: formatDurationSpent(if (topic.firstStudyDuration > 0L) topic.firstStudyDuration else actualDuration),
                        firstStudyDuration = if (topic.firstStudyDuration > 0L) topic.firstStudyDuration else actualDuration,
                        totalRevisions = newRevisionCount,
                        totalRevisionCount = newRevisionCount,
                        lastRevisionDate = now,
                        lastRevisionTime = formatDurationSpent(actualDuration),
                        lastRevisionDuration = actualDuration,
                        nextRevisionDate = nextRevisionMs,
                        expectedRevisionTime = computedExpectedMins,
                        estimatedRevisionTimeMins = computedExpectedMins,
                        currentRevisionStage = newRevisionCount,
                        customSpacingPattern = activePattern,
                        revisionStatus = if (newRevisionCount >= activePattern.size) "Mastered (Recurring 30-day)" else "Upcoming",
                        progressPercentage = progScore,
                        updatedAt = now,
                        studyCompleted = true,
                        completedRevisions = newRevisionCount,
                        totalRequiredRevisions = activePattern.size,
                        progressScore = progScore
                    )
                }

                rDb.topicDao().insert(finalTopic.toEntity())
                syncRevisionDocument(finalTopic, activePattern, isFirstStudy)

                if (profile != null) {
                    val addMins = kotlin.math.ceil(actualDuration / 60000.0).toInt()
                    val updatedProfile = profile.copy(
                        studyTimeMinutes = profile.studyTimeMinutes + addMins,
                        totalRevisions = profile.totalRevisions + (if (isFirstStudy) 0 else 1),
                        topicsCompleted = profile.topicsCompleted + (if (isFirstStudy) 1 else 0),
                        lastActiveTimeMs = now
                    )
                    rDb.userProfileDao().insert(updatedProfile.toEntity())
                }

                finalTopicResult = finalTopic
            }

            val finalTopic = finalTopicResult ?: return
            recordStudyTime(dateRaw, !isFirstStudyResult, durationMs)

            try {
                val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
                if (finalTopic.nextRevisionDateMs != 0L) {
                    com.example.services.SrsNotificationManager.scheduleChapterRevision(
                        context,
                        finalTopic.id,
                        finalTopic.subjectId,
                        finalTopic.chapterId,
                        finalTopic.name,
                        finalTopic.nextRevisionDateMs,
                        finalTopic.estimatedRevisionTimeMins
                    )
                } else {
                    com.example.services.SrsNotificationManager.cancelChapterRevision(context, finalTopic.id)
                }
            } catch (e: Exception) {}
            return
        }

        val topicRef = db.collection("topics").document(topicId)
        val profileRef = db.collection("users").document(userId)
        
        try {
            val topicSnap = topicRef.get().await()
            if (!topicSnap.exists()) {
                throw Exception("Topic does not exist")
            }
            val topic = topicSnap.toObject(Topic::class.java) ?: throw Exception("Failed to parse Topic")
            
            val profileSnap = profileRef.get().await()
            val profile = if (profileSnap.exists()) profileSnap.toObject(UserProfile::class.java) else null
            
            val now = System.currentTimeMillis()
            val activePattern = topic.revisionPattern?.takeIf { it.isNotEmpty() } ?: profile?.revisionPattern?.takeIf { it.isNotEmpty() } ?: com.example.services.RevisionEngine.DEFAULT_PATTERN
            
            val isFirstStudy = (topic.firstStudyDate == null && topic.revisionCount == 0 && topic.completedRevisions == 0)
            
            val expected = topic.estimatedRevisionTimeMins * 60000L
            val actualDuration = if (durationMs > 0L) durationMs else expected
            val actualMins = Math.round(actualDuration / 60000.0).toInt().coerceAtLeast(1)

            // Save new Revision History Record
            val historyRef = db.collection("users").document(userId).collection("revision_history").document()
            val revNumber = if (isFirstStudy) 0 else topic.revisionCount + 1
            val newHistory = RevisionHistoryRecord(
                id = historyRef.id,
                topicId = topic.id,
                topicName = topic.name,
                completedDateMs = now,
                revisionNumber = revNumber,
                durationMs = actualDuration
            )
            historyRef.set(newHistory).await()

            // Fetch and Calculate Estimated Time (Rules 1-4)
            val historySnap = db.collection("users").document(userId).collection("revision_history")
                .whereEqualTo("topicId", topic.id)
                .get().await()
            val pastRecords = historySnap.documents.mapNotNull { doc -> try { doc.toObject(RevisionHistoryRecord::class.java) } catch (e: Exception) { e.printStackTrace(); null } }.toMutableList()
            if (pastRecords.none { it.id == newHistory.id }) pastRecords.add(newHistory)
            
            val revisionsOnly = pastRecords.filter { it.revisionNumber > 0 }.sortedBy { it.completedDateMs }
            val firstStudyRecord = pastRecords.find { it.revisionNumber == 0 }
            val firstMins = firstStudyRecord?.durationMs?.let { Math.round(it / 60000.0).toInt() } ?: topic.estimatedRevisionTimeMins

            val computedExpectedMins = if (revNumber == 0) {
                firstMins
            } else if (revNumber == 1) {
                actualMins
            } else if (revNumber == 2) {
                val sum = revisionsOnly.sumOf { Math.round(it.durationMs / 60000.0).toInt() }
                if (revisionsOnly.isNotEmpty()) Math.round(sum / revisionsOnly.size.toDouble()).toInt() else actualMins
            } else {
                val last3 = revisionsOnly.takeLast(3)
                val sum = last3.sumOf { Math.round(it.durationMs / 60000.0).toInt() }
                if (last3.isNotEmpty()) Math.round(sum / last3.size.toDouble()).toInt() else actualMins
            }.coerceAtLeast(1)
            
            val finalTopic: Topic
            
            if (isFirstStudy) {
                val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                    revisionCount = 0,
                    pattern = activePattern,
                    firstStudyMs = now,
                    lastRevisionMs = now
                )
                
                finalTopic = topic.copy(
                    status = "Complete",
                    revisionStatus = "Pending Revision",
                    studyTimeMs = topic.studyTimeMs + durationMs,
                    startDateMs = now,
                    lastRevisionDateMs = now,
                    nextRevisionDateMs = nextRevisionMs,
                    masteryScore = 20,
                    revisionPattern = activePattern,
                    revisionCount = 0,
                    
                    firstStudyDate = now,
                    firstStudyTime = formatDurationSpent(actualDuration),
                    firstStudyDuration = actualDuration,
                    totalRevisions = 0,
                    totalRevisionCount = 0,
                    lastRevisionDate = null,
                    lastRevisionTime = null,
                    lastRevisionDuration = 0L,
                    nextRevisionDate = nextRevisionMs,
                    expectedRevisionTime = computedExpectedMins,
                    estimatedRevisionTimeMins = computedExpectedMins,
                    currentRevisionStage = 0,
                    customSpacingPattern = activePattern,
                    progressPercentage = calculateProgressScore(true, 0, activePattern.size),
                    expectedStudyTime = expected,
                    createdAt = topic.createdAt ?: now,
                    updatedAt = now,
                    studyCompleted = true,
                    completedRevisions = 0,
                    totalRequiredRevisions = activePattern.size,
                    progressScore = calculateProgressScore(true, 0, activePattern.size)
                )
            } else {
                val newRevisionCount = topic.revisionCount + 1
                val firstStudyMs = topic.firstStudyDate ?: topic.startDateMs ?: topic.createdAt ?: now
                val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                    revisionCount = newRevisionCount,
                    pattern = activePattern,
                    firstStudyMs = firstStudyMs,
                    lastRevisionMs = now
                )
                
                finalTopic = topic.copy(
                    status = "Complete",
                    studyTimeMs = if (topic.studyTimeMs > 0L) topic.studyTimeMs else actualDuration,
                    revisionTimeMs = topic.revisionTimeMs + durationMs,
                    startDateMs = topic.startDateMs ?: firstStudyMs,
                    nextRevisionDateMs = nextRevisionMs,
                    lastRevisionDateMs = now,
                    revisionCount = newRevisionCount,
                    masteryScore = minOf(20 + (newRevisionCount * 10), 100),
                    revisionPattern = activePattern,
                    
                    firstStudyDate = topic.firstStudyDate ?: firstStudyMs,
                    firstStudyTime = topic.firstStudyTime ?: formatDurationSpent(actualDuration),
                    firstStudyDuration = if (topic.firstStudyDuration > 0L) topic.firstStudyDuration else actualDuration,
                    totalRevisions = newRevisionCount,
                    totalRevisionCount = newRevisionCount,
                    lastRevisionDate = now,
                    lastRevisionTime = formatDurationSpent(actualDuration),
                    lastRevisionDuration = actualDuration,
                    nextRevisionDate = nextRevisionMs,
                    
                    // Rules applied below
                    expectedRevisionTime = computedExpectedMins,
                    estimatedRevisionTimeMins = computedExpectedMins,

                    currentRevisionStage = newRevisionCount,
                    customSpacingPattern = activePattern,
                    revisionStatus = if (newRevisionCount >= activePattern.size) "Mastered (Recurring 30-day)" else "Pending Revision",
                    progressPercentage = calculateProgressScore(true, newRevisionCount, activePattern.size),
                    updatedAt = now,
                    studyCompleted = true,
                    completedRevisions = newRevisionCount,
                    totalRequiredRevisions = activePattern.size,
                    progressScore = calculateProgressScore(true, newRevisionCount, activePattern.size)
                )
            }
            
            val batch = db.batch()
            batch.set(topicRef, finalTopic)
            
            if (profile != null) {
                val currentMins = profile.studyTimeMinutes
                val currentRevs = profile.totalRevisions
                val currentCompleted = profile.topicsCompleted
                val addMins = kotlin.math.ceil(durationMs / 60000.0).toLong()
                
                val updates = mutableMapOf<String, Any>()
                updates["studyTimeMinutes"] = currentMins + addMins
                updates["totalRevisions"] = currentRevs + (if (isFirstStudy) 0 else 1)
                if (isFirstStudy) {
                    updates["topicsCompleted"] = currentCompleted + 1
                }
                updates["lastActiveTimeMs"] = now
                batch.update(profileRef, updates)
            }
            
            batch.commit().await()
            
            syncRevisionDocument(finalTopic, activePattern, isFirstStudy)
            
            // Schedule Notification
            try {
                val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
                if (finalTopic.nextRevisionDateMs != 0L) {
                    com.example.services.SrsNotificationManager.scheduleChapterRevision(
                        context,
                        finalTopic.id,
                        finalTopic.subjectId,
                        finalTopic.chapterId,
                        finalTopic.name,
                        finalTopic.nextRevisionDateMs,
                        finalTopic.estimatedRevisionTimeMins
                    )
                } else {
                    com.example.services.SrsNotificationManager.cancelChapterRevision(context, finalTopic.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Log study time for stats
            recordStudyTime(dateRaw, !isFirstStudy, durationMs)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    open suspend fun recordChapterStudySessionTransaction(
        chapterId: String,
        isRevision: Boolean,
        durationMs: Long,
        dateRaw: String
    ) {
        if (chapterId.isEmpty()) return
        val rDb = roomDb
        val now = System.currentTimeMillis()
        val startOfToday = getStartOfTodayMs()
        val endOfToday = startOfToday + 86400000L - 1
        val activePattern = com.example.services.RevisionEngine.DEFAULT_PATTERN

        if (rDb != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                rDb.runInTransaction {
                    val chapList = kotlinx.coroutines.runBlocking { rDb.chapterDao().getChaptersList("offline_user") }
                    val chapterEntity = chapList.find { it.id == chapterId }
                    val chapter = chapterEntity?.toDomain() ?: Chapter(id = chapterId, userId = "offline_user", name = "Chapter")

                    val isFirstStudy = !isRevision && (chapter.firstStudyDateMs == null)
                    val newRevisionCount = if (isFirstStudy) 0 else chapter.revisionCount + 1

                    val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                        revisionCount = newRevisionCount,
                        pattern = activePattern,
                        firstStudyMs = chapter.firstStudyDateMs ?: now,
                        lastRevisionMs = now
                    )

                    val statusStr = if (nextRevisionMs < startOfToday) {
                        "Overdue"
                    } else if (nextRevisionMs in startOfToday..endOfToday) {
                        "Today"
                    } else {
                        "Upcoming"
                    }

                    val finalChapter = chapter.copy(
                        firstStudyDateMs = chapter.firstStudyDateMs ?: now,
                        lastStudyDateMs = now,
                        lastRevisionDateMs = if (isRevision) now else chapter.lastRevisionDateMs,
                        totalStudyTimeMs = if (isRevision) chapter.totalStudyTimeMs else (chapter.totalStudyTimeMs + durationMs),
                        totalRevisionTimeMs = if (isRevision) (chapter.totalRevisionTimeMs + durationMs) else chapter.totalRevisionTimeMs,
                        revisionCount = newRevisionCount,
                        nextRevisionDateMs = nextRevisionMs,
                        status = statusStr,
                        isCompleted = newRevisionCount >= activePattern.size
                    )

                    kotlinx.coroutines.runBlocking {
                        rDb.chapterDao().insert(finalChapter.toEntity())

                        // Create scheduled revision record in local Room DB
                        val revId = java.util.UUID.randomUUID().toString()
                        val revision = Revision(
                            id = revId,
                            userId = "offline_user",
                            subjectId = chapter.subjectId,
                            chapterId = chapterId,
                            topicId = "",
                            subjectName = "",
                            chapterName = chapter.name,
                            topicName = chapter.name,
                            revisionCount = newRevisionCount,
                            pattern = activePattern,
                            lastRevisionDateMs = now,
                            nextRevisionDateMs = nextRevisionMs,
                            status = if (nextRevisionMs <= endOfToday) "Pending" else "Upcoming",
                            duration = kotlin.math.ceil(durationMs / 60000.0).toInt().coerceAtLeast(15)
                        )
                        rDb.revisionDao().insert(revision.toEntity())
                    }
                }
            }
        }

        try {
            val chapterRef = db.collection("chapters").document(chapterId)
            val chapterSnap = chapterRef.get().await()
            if (chapterSnap.exists()) {
                val chapter = chapterSnap.toObject(Chapter::class.java)
                if (chapter != null) {
                    val updated = chapter.copy(
                        lastStudyDateMs = now,
                        nextRevisionDateMs = now + 86400000L
                    )
                    chapterRef.set(updated).await()
                }
            }
        } catch (e: Exception) {}

        recordStudyTime(dateRaw, isRevision, durationMs)
    }

    open suspend fun updateTopic(topic: Topic) {
        if (userId.isEmpty()) return
        val rDb = roomDb
        val oldTopic = rDb?.topicDao()?.getTopicById(topic.id)?.toDomain() ?: try {
            val oldTopicDoc = db.collection("topics").document(topic.id).get().await()
            if (oldTopicDoc.exists()) oldTopicDoc.toObject(Topic::class.java) else null
        } catch (e: Exception) { null }
        
        val isFirstCompletion = oldTopic != null && oldTopic.status == "Pending" && topic.status == "Complete"
        val isSubsequentRevision = oldTopic != null && topic.revisionCount > oldTopic.revisionCount
        
        var finalTopic = topic
        val now = System.currentTimeMillis()
        
        val profile = rDb?.userProfileDao()?.getProfileSync(userId.ifEmpty { "offline_user" })?.toDomain() ?: try {
            val profileRef = db.collection("users").document(userId)
            val profileSnap = profileRef.get().await()
            profileSnap.toObject(UserProfile::class.java)
        } catch (e: Exception) { null }
        val activePattern = topic.revisionPattern?.takeIf { it.isNotEmpty() } ?: profile?.revisionPattern?.takeIf { it.isNotEmpty() } ?: com.example.services.RevisionEngine.DEFAULT_PATTERN

        if (isFirstCompletion) {
            // First study completed! Schedule first revision (Revision 1)
            val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                revisionCount = 0,
                pattern = activePattern,
                firstStudyMs = now,
                lastRevisionMs = now
            )
            
            if (rDb == null) {
                try {
                    val profileRef = db.collection("users").document(userId)
                    db.runTransaction { tx ->
                        val pSnap = tx.get(profileRef)
                        if (pSnap.exists()) {
                            val current = pSnap.getLong("topicsCompleted") ?: 0L
                            tx.update(profileRef, "topicsCompleted", current + 1)
                        }
                        null
                    }.await()
                } catch (e: Exception) {}
            }
            
            val studyDuration = if (topic.studyTimeMs > 0L) topic.studyTimeMs else (topic.estimatedRevisionTimeMins * 60000L)
            
            finalTopic = topic.copy(
                revisionCount = 0,
                startDateMs = now,
                lastRevisionDateMs = 0L,
                nextRevisionDateMs = nextRevisionMs,
                status = "Complete",
                masteryScore = 20,
                revisionPattern = activePattern,
                
                // Explicit fields requested in prompt
                firstStudyDate = now,
                firstStudyTime = formatDurationSpent(studyDuration),
                firstStudyDuration = studyDuration,
                totalRevisions = 0,
                totalRevisionCount = 0,
                lastRevisionDate = null,
                lastRevisionTime = null,
                lastRevisionDuration = 0L,
                nextRevisionDate = nextRevisionMs,
                expectedRevisionTime = topic.estimatedRevisionTimeMins,
                currentRevisionStage = 0,
                customSpacingPattern = activePattern,
                revisionStatus = "Pending Revision",
                progressPercentage = calculateProgressScore(true, 0, activePattern.size),
                studyCompleted = true,
                completedRevisions = 0,
                totalRequiredRevisions = activePattern.size,
                progressScore = calculateProgressScore(true, 0, activePattern.size)
            )
            
            syncRevisionDocument(finalTopic, activePattern, isFirstCompletion = true)
        } else if (isSubsequentRevision) {
            val revNum = topic.revisionCount
            val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                revisionCount = revNum,
                pattern = activePattern,
                firstStudyMs = oldTopic?.firstStudyDate ?: oldTopic?.startDateMs ?: oldTopic?.createdAt ?: now,
                lastRevisionMs = now
            )
            
            val diffTime = topic.revisionTimeMs - (oldTopic?.revisionTimeMs ?: 0L)
            val actualRevDuration = if (diffTime > 0L) diffTime else (topic.estimatedRevisionTimeMins * 60000L)
            
            finalTopic = topic.copy(
                nextRevisionDateMs = nextRevisionMs,
                lastRevisionDateMs = now,
                masteryScore = minOf(20 + (revNum * 10), 100),
                revisionPattern = activePattern,
                
                // Explicit fields requested in prompt
                firstStudyDate = oldTopic?.firstStudyDate ?: now,
                firstStudyTime = oldTopic?.firstStudyTime ?: formatDurationSpent(actualRevDuration),
                firstStudyDuration = oldTopic?.firstStudyDuration ?: 0L,
                totalRevisions = revNum,
                totalRevisionCount = revNum,
                lastRevisionDate = now,
                lastRevisionTime = formatDurationSpent(actualRevDuration),
                lastRevisionDuration = actualRevDuration,
                nextRevisionDate = nextRevisionMs,
                expectedRevisionTime = topic.estimatedRevisionTimeMins,
                currentRevisionStage = revNum,
                customSpacingPattern = activePattern,
                revisionStatus = if (revNum >= activePattern.size) "Mastered (Recurring 30-day)" else "Pending Revision",
                progressPercentage = minOf(revNum * 20, 100),
                studyCompleted = true,
                completedRevisions = revNum,
                totalRequiredRevisions = activePattern.size,
                progressScore = calculateProgressScore(true, revNum, activePattern.size)
            )
            
            syncRevisionDocument(finalTopic, activePattern, isFirstCompletion = false)
        } else {
            // Retain old fields on simple update
            val totalRevsVal = if (topic.totalRevisions > 0) topic.totalRevisions else (oldTopic?.totalRevisions ?: topic.revisionCount)
            finalTopic = topic.copy(
                firstStudyDate = topic.firstStudyDate ?: oldTopic?.firstStudyDate,
                firstStudyTime = topic.firstStudyTime ?: oldTopic?.firstStudyTime,
                firstStudyDuration = if (topic.firstStudyDuration > 0) topic.firstStudyDuration else (oldTopic?.firstStudyDuration ?: 0L),
                totalRevisions = totalRevsVal,
                totalRevisionCount = topic.revisionCount,
                lastRevisionDate = topic.lastRevisionDate ?: oldTopic?.lastRevisionDate,
                lastRevisionTime = topic.lastRevisionTime ?: oldTopic?.lastRevisionTime,
                lastRevisionDuration = if (topic.lastRevisionDuration > 0) topic.lastRevisionDuration else (oldTopic?.lastRevisionDuration ?: 0L),
                nextRevisionDate = topic.nextRevisionDate ?: oldTopic?.nextRevisionDate ?: topic.nextRevisionDateMs,
                expectedRevisionTime = topic.estimatedRevisionTimeMins,
                currentRevisionStage = topic.revisionCount,
                customSpacingPattern = topic.revisionPattern ?: activePattern,
                revisionStatus = topic.revisionStatus ?: oldTopic?.revisionStatus ?: (if (topic.status == "Complete") "Pending Revision" else "Pending"),
                progressPercentage = if (topic.progressPercentage > 0) topic.progressPercentage else (oldTopic?.progressPercentage ?: (topic.revisionCount * 10)),
                studyCompleted = topic.studyCompleted,
                completedRevisions = topic.completedRevisions,
                totalRequiredRevisions = activePattern.size,
                progressScore = calculateProgressScore(topic.studyCompleted, topic.completedRevisions, activePattern.size)
            )
            syncRevisionDocument(finalTopic, activePattern, isFirstCompletion = false)
        }
        
        if (rDb != null) {
            rDb.topicDao().insert(finalTopic.toEntity())
            try {
                val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
                if (finalTopic.nextRevisionDateMs != 0L) {
                    com.example.services.SrsNotificationManager.scheduleChapterRevision(
                        context,
                        finalTopic.id,
                        finalTopic.subjectId,
                        finalTopic.chapterId,
                        finalTopic.name,
                        finalTopic.nextRevisionDateMs,
                        finalTopic.estimatedRevisionTimeMins
                    )
                } else {
                    com.example.services.SrsNotificationManager.cancelChapterRevision(context, finalTopic.id)
                }
            } catch (e: Exception) {}
            return
        }
        try {
            db.collection("topics").document(finalTopic.id).set(finalTopic).await()
        } catch (e: Exception) {
            if (rDb == null) throw e
        }
        
        try {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            if (finalTopic.nextRevisionDateMs != 0L) {
                com.example.services.SrsNotificationManager.scheduleChapterRevision(
                    context,
                    finalTopic.id,
                    finalTopic.subjectId,
                    finalTopic.chapterId,
                    finalTopic.name,
                    finalTopic.nextRevisionDateMs,
                    finalTopic.estimatedRevisionTimeMins
                )
            } else {
                com.example.services.SrsNotificationManager.cancelChapterRevision(context, finalTopic.id)
            }
        } catch(e: Exception) {}

        // Auto-update parent chapter's revisionCount and isCompleted based on its topics
        try {
            if (finalTopic.chapterId.isNotEmpty()) {
                val chapterDoc = db.collection("chapters").document(finalTopic.chapterId).get().await()
                if (chapterDoc.exists()) {
                    val chapter = chapterDoc.toObject(Chapter::class.java)
                    if (chapter != null) {
                        val topicsInChapter = db.collection("topics").whereEqualTo("chapterId", chapter.id).get().await()
                        var allCompleted = true
                        var totalRevisions = 0
                        var topicCount = 0
                        
                        for (doc in topicsInChapter.documents) {
                            topicCount++
                            val tStatus = doc.getString("status") ?: "Pending"
                            val tRev = doc.getLong("revisionCount")?.toInt() ?: 0
                            if (tStatus != "Complete") allCompleted = false
                            totalRevisions += tRev
                        }
                        
                        // If we are currently updating a topic, make sure its new state is factored in
                        // (firestore may not have indexed it for the query yet if we just set it)
                        val avgRev = if (topicCount > 0) (totalRevisions / topicCount) else 0
                        
                        db.collection("chapters").document(chapter.id).update(
                            "isCompleted", allCompleted && topicCount > 0,
                            "revisionCount", avgRev,
                            "lastRevisionDateMs", System.currentTimeMillis()
                        ).await()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Fulfill explicit user request: Save revision data under nested path
        try {
            val nestedRef = db.collection("users").document(userId)
                .collection("subjects").document(finalTopic.subjectId.ifEmpty { "default" })
                .collection("chapters").document(finalTopic.chapterId.ifEmpty { "default" })
                .collection("topics").document(finalTopic.id)
            nestedRef.set(mapOf(
                "revisionCount" to finalTopic.revisionCount,
                "lastRevisionAt" to finalTopic.lastRevisionDateMs,
                "nextRevisionAt" to finalTopic.nextRevisionDateMs,
                "studyTime" to finalTopic.studyTimeMs,
                "completionPercentage" to finalTopic.masteryScore,
                "status" to finalTopic.status,
                "updatedAt" to System.currentTimeMillis(),
                
                // Custom fields
                "firstStudyDate" to finalTopic.firstStudyDate,
                "firstStudyTime" to finalTopic.firstStudyTime,
                "totalRevisions" to finalTopic.totalRevisions,
                "lastRevisionDate" to finalTopic.lastRevisionDate,
                "lastRevisionTime" to finalTopic.lastRevisionTime,
                "nextRevisionDate" to finalTopic.nextRevisionDate,
                "revisionStatus" to finalTopic.revisionStatus,
                "progressPercentage" to finalTopic.progressPercentage,
                "expectedRevisionTime" to finalTopic.expectedRevisionTime
            ), com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertSubjectWithId(id: String, name: String, colorHex: String = "#0061A4", examId: String = "", icon: String = "book", totalTopicsExpected: Int = 0) {
        val currentUid = if (userId.isNotEmpty()) userId else (auth?.currentUser?.uid ?: "")
        if (currentUid.isEmpty()) return
        val ref = db.collection("subjects").document(id)
        val subject = Subject(
            id = id,
            userId = currentUid,
            examId = examId,
            name = name,
            colorHex = colorHex,
            icon = icon,
            totalTopicsExpected = totalTopicsExpected,
            isArchived = false
        )
        ref.set(subject).await()
    }

    suspend fun insertChapterWithId(id: String, subjectId: String, name: String, importance: String = "Medium") {
        if (userId.isEmpty()) return
        val ref = db.collection("chapters").document(id)
        ref.set(Chapter(id = id, userId = userId, subjectId = subjectId, name = name, importance = importance)).await()
    }

    suspend fun insertTopicWithId(id: String, subjectId: String, chapterId: String, name: String, difficulty: String = "Medium", estimatedRevisionTimeMins: Int = 30, notes: String = "", tags: List<String> = emptyList()) {
        if (userId.isEmpty()) return
        val topic = Topic(id = id, userId = userId, chapterId = chapterId, subjectId = subjectId, name = name, difficulty = difficulty, estimatedRevisionTimeMins = estimatedRevisionTimeMins, notes = notes, tags = tags)
        roomDb?.topicDao()?.insert(topic.toEntity())
        try {
            db.collection("topics").document(id).set(topic).await()
        } catch (e: Exception) {}
    }

    suspend fun insertRevisionWithId(
        id: String,
        subjectId: String,
        chapterId: String,
        topicId: String,
        subjectName: String,
        chapterName: String,
        topicName: String,
        pattern: List<Int>,
        nextRevisionDateMs: Long,
        reminderTimeMins: Int,
        reminderTime: String,
        notes: String,
        studyDate: String,
        studyTime: String,
        duration: Int,
        priority: String,
        createdAt: Long
    ): Revision? {
        if (userId.isEmpty()) return null
        var sName = subjectName
        var cName = chapterName
        try {
            if (sName.isBlank() && subjectId.isNotEmpty()) {
                val subDoc = db.collection("subjects").document(subjectId).get().await()
                sName = subDoc.getString("name") ?: ""
            }
            if (cName.isBlank() && chapterId.isNotEmpty()) {
                val chapDoc = db.collection("chapters").document(chapterId).get().await()
                cName = chapDoc.getString("name") ?: ""
            }
        } catch (e: Exception) {}
        val ref = db.collection("users").document(userId).collection("revisions").document(id)
        val rev = Revision(
            id = id,
            userId = userId,
            subjectId = subjectId,
            chapterId = chapterId,
            topicId = topicId,
            subjectName = sName,
            chapterName = cName,
            topicName = topicName,
            pattern = pattern,
            nextRevisionDateMs = nextRevisionDateMs,
            notes = notes,
            status = "Pending",
            studyDate = studyDate,
            studyTime = studyTime,
            duration = duration,
            priority = priority,
            createdAt = createdAt
        )
        ref.set(rev).await()
        return rev
    }

    suspend fun deleteTopic(id: String) {
        val rDb = roomDb
        rDb?.topicDao()?.delete(id)
        rDb?.revisionDao()?.deleteByTopic(id)
        rDb?.revisionHistoryDao()?.deleteByTopic(id)
        try {
            val context = com.google.firebase.FirebaseApp.getInstance().applicationContext
            com.example.services.SrsNotificationManager.cancelChapterRevision(context, id)
        } catch(e: Exception) {}
        if (rDb != null || userId.isEmpty()) return
        try {
            val batch = db.batch()
            batch.delete(db.collection("topics").document(id))
            batch.delete(db.collection("users").document(userId).collection("revision_plans").document(id))
            batch.delete(db.collection("users").document(userId).collection("topic_revisions").document(id))
            val history = db.collection("users").document(userId).collection("revision_history").whereEqualTo("topicId", id).get().await()
            history.documents.forEach { batch.delete(it.reference) }
            val schedule = db.collection("users").document(userId).collection("revision_schedule").whereEqualTo("topicId", id).get().await()
            schedule.documents.forEach { batch.delete(it.reference) }
            val revisions = db.collection("users").document(userId).collection("revisions").whereEqualTo("topicId", id).get().await()
            revisions.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        } catch (e: Exception) {}
    }

    open fun getAllUsersProfiles(): Flow<List<UserProfile>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("users").whereEqualTo("id", uid).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(UserProfile::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
        }
    }

    suspend fun saveUserProfile(email: String) {
        if (userId.isEmpty()) return
        val docRef = db.collection("users").document(userId)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val initialReferralCode = generateReferralCodeForEmail(email)
            val profile = UserProfile(
                id = userId,
                email = email,
                referralCode = initialReferralCode,
                createdAtMs = System.currentTimeMillis()
            )
            updateUserProfile(profile)
        } else {
            val profile = snapshot.toObject(UserProfile::class.java)
            if (profile != null) {
                if (profile.referralCode.isNullOrEmpty()) {
                    val newCode = generateReferralCodeForEmail(email)
                    updateUserProfile(profile.copy(referralCode = newCode))
                } else {
                    updateUserProfile(profile)
                }
            }
        }
    }

    suspend fun getUserSubjectsCount(targetUserId: String): Int {
        val snapshot = db.collection("subjects").whereEqualTo("userId", targetUserId).get().await()
        return snapshot.size()
    }

    suspend fun checkAndSeedDefaultSyllabus(targetUserId: String) {
        // Demo/default syllabus seeding removed so new users start with a clean empty state
    }

    suspend fun getUserTopicsCount(targetUserId: String): Int {
        val snapshot = db.collection("topics").whereEqualTo("userId", targetUserId).get().await()
        return snapshot.size()
    }

    fun getAdminSubjects(targetUserId: String): Flow<List<Subject>> = callbackFlow {
        val listener = db.collection("subjects").whereEqualTo("userId", targetUserId)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(Subject::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAdminTopics(targetUserId: String): Flow<List<Topic>> = callbackFlow {
        val listener = db.collection("topics").whereEqualTo("userId", targetUserId)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(Topic::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    open suspend fun recordStudyTime(dateRaw: String, isRevision: Boolean, durationMs: Long) {
        if (userId.isEmpty()) return
        val rDb = roomDb
        if (rDb != null) {
            val localUser = userId.ifEmpty { "offline_user" }
            val existing = rDb.studyLogDao().getStudyLogForDate(localUser, dateRaw)?.toDomain()
                ?: rDb.studyLogDao().getStudyLogForDate("offline_user", dateRaw)?.toDomain()
            val updated = if (existing == null) {
                StudyLog(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = localUser,
                    dateString = dateRaw,
                    studyTimeMs = if (!isRevision) durationMs else 0L,
                    revisionTimeMs = if (isRevision) durationMs else 0L,
                    focusSessions = 1
                )
            } else {
                if (isRevision) {
                    existing.copy(
                        revisionTimeMs = existing.revisionTimeMs + durationMs,
                        focusSessions = existing.focusSessions + 1
                    )
                } else {
                    existing.copy(
                        studyTimeMs = existing.studyTimeMs + durationMs,
                        focusSessions = existing.focusSessions + 1
                    )
                }
            }
            rDb.studyLogDao().insert(updated.toEntity())
            return
        }
        try {
            val snapshot = db.collection("study_logs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("dateString", dateRaw)
                .get()
                .await()

            if (snapshot.isEmpty) {
                val ref = db.collection("study_logs").document()
                val log = StudyLog(
                    id = ref.id,
                    userId = userId,
                    dateString = dateRaw,
                    studyTimeMs = if (!isRevision) durationMs else 0L,
                    revisionTimeMs = if (isRevision) durationMs else 0L,
                    focusSessions = 1
                )
                ref.set(log).await()
            } else {
                val doc = snapshot.documents.first()
                val log = doc.toObject(StudyLog::class.java) ?: return
                val updatedLog = if (isRevision) {
                    log.copy(
                        revisionTimeMs = log.revisionTimeMs + durationMs,
                        focusSessions = log.focusSessions + 1
                    )
                } else {
                    log.copy(
                        studyTimeMs = log.studyTimeMs + durationMs,
                        focusSessions = log.focusSessions + 1
                    )
                }
                db.collection("study_logs").document(log.id).set(updatedLog).await()
            }
        } catch (e: Exception) {
            if (rDb == null) throw e
        }
        
        // Update UserProfile total metrics for analytics sync
        try {
            val docRef = db.collection("users").document(userId)
            db.runTransaction { tx ->
                val userSnap = tx.get(docRef)
                if (userSnap.exists()) {
                    val currentMins = userSnap.getLong("studyTimeMinutes") ?: 0L
                    val currentRevs = userSnap.getLong("totalRevisions") ?: 0L
                    val addMins = kotlin.math.ceil(durationMs / 60000.0).toLong()
                    val updates = mutableMapOf<String, Any>()
                    updates["studyTimeMinutes"] = currentMins + addMins
                    if (isRevision) {
                    // totalRevisions is incremented in markRevisionComplete
                    }
                    updates["lastActiveTimeMs"] = System.currentTimeMillis()
                    tx.update(docRef, updates)
                }
                null
            }.await()
            // Evaluate referral payout if they have reached the milestone
            evaluateReferralPayouts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun banUser(targetUserId: String, isBanned: Boolean, reason: String) {
        try {
            db.collection("users").document(targetUserId).update(
                mapOf(
                    "isBanned" to isBanned,
                    "banReason" to reason
                )
            ).await()
            logSystemAction("System", "system@revisionspace.com", "BAN_USER", "Updated ban status for $targetUserId to $isBanned. Reason: $reason")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteUser(targetUserId: String) {
        try {
            // Delete subcollections manually since we are doing it from client
            val docRef = db.collection("users").document(targetUserId)
            
            docRef.collection("profile").document("data").delete().await()
            docRef.collection("coins").document("data").delete().await()
            docRef.collection("wallet").document("data").delete().await()
            docRef.collection("streak").document("data").delete().await()
            docRef.collection("settings").document("data").delete().await()
            
            // Delete notifications
            val notifs = docRef.collection("notifications").get().await()
            for (doc in notifs.documents) doc.reference.delete().await()
            
            // Delete logs
            val logs = docRef.collection("study_logs").get().await()
            for (doc in logs.documents) doc.reference.delete().await()

            // Delete User Document
            docRef.delete().await()
            
            logSystemAction("System", "system@revisionspace.com", "DELETE_USER", "Deleted all app data for $targetUserId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open suspend fun updateUserProfile(profile: UserProfile) {
        val rDb = roomDb
        val oldProfile = rDb?.userProfileDao()?.getProfileSync(profile.id)?.toDomain()
        if (rDb != null) {
            rDb.userProfileDao().insert(profile.toEntity())
            if (oldProfile != null && oldProfile.revisionPattern != profile.revisionPattern) {
                recalculateAllTopicsSrs(profile.id, profile.revisionPattern)
            }
        }
        try {
            if (rDb == null) {
                val oldProfileDoc = db.collection("users").document(profile.id).get().await()
                val fsOldProfile = oldProfileDoc.toObject(UserProfile::class.java)
                if (fsOldProfile != null && fsOldProfile.revisionPattern != profile.revisionPattern) {
                    recalculateAllTopicsSrs(profile.id, profile.revisionPattern)
                }
            }

            db.collection("users").document(profile.id).set(profile)
            // Sync to required subcollections
            db.collection("users").document(profile.id).collection("profile").document("data").set(profile)

            db.collection("users").document(profile.id).collection("coins").document("data").set(mapOf(
                "totalCoins" to profile.totalCoins,
                "totalCoinsWithdrawn" to profile.totalCoinsWithdrawn
            ))
            db.collection("users").document(profile.id).collection("wallet").document("data").set(mapOf(
                "totalCoins" to profile.totalCoins,
                "totalCoinsWithdrawn" to profile.totalCoinsWithdrawn,
                "referralCoinsEarned" to profile.referralCoinsEarned
            ))
            db.collection("users").document(profile.id).collection("streak").document("data").set(mapOf(
                "currentStreak" to profile.currentStreak,
                "longestStreak" to profile.longestStreak
            ))
            db.collection("users").document(profile.id).collection("settings").document("data").set(mapOf(
                "focusSettings" to profile.focusSettings
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open suspend fun updateUserProfileFieldsOnly(
        userId: String,
        badge: String,
        league: String,
        rankPosition: Int?,
        isRanked: Boolean
    ) {
        if (userId.isEmpty()) return
        val rDb = roomDb
        if (rDb != null) {
            val existing = rDb.userProfileDao().getProfileSync(userId)?.toDomain()
            if (existing != null) {
                rDb.userProfileDao().insert(
                    existing.copy(
                        badge = badge,
                        league = league,
                        rankPosition = rankPosition,
                        isRanked = isRanked
                    ).toEntity()
                )
            }
            return
        }
        try {
            val updates = mapOf(
                "badge" to badge,
                "league" to league,
                "rankPosition" to rankPosition,
                "isRanked" to isRanked
            )
            db.collection("users").document(userId).set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
            db.collection("users").document(userId).collection("profile").document("data").set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun evaluateDailyRewardsAndStreak(uid: String) {
        if (uid.isEmpty()) return
        val rDb = roomDb
        if (rDb != null) {
            try {
                val profile = rDb.userProfileDao().getProfileSync(uid)?.toDomain() ?: return
                var currentStreak = profile.currentStreak
                var longestStreak = profile.longestStreak
                val lastActiveMs = profile.lastActiveTimeMs
                val currentMs = java.lang.System.currentTimeMillis()
                val calLast = java.util.Calendar.getInstance()
                val calCurrent = java.util.Calendar.getInstance()
                calCurrent.timeInMillis = currentMs

                if (lastActiveMs > 0) {
                    calLast.timeInMillis = lastActiveMs
                    if (calCurrent.get(java.util.Calendar.DAY_OF_YEAR) != calLast.get(java.util.Calendar.DAY_OF_YEAR) || calCurrent.get(java.util.Calendar.YEAR) != calLast.get(java.util.Calendar.YEAR)) {
                        val calYesterday = java.util.Calendar.getInstance()
                        calYesterday.timeInMillis = currentMs
                        calYesterday.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        val isConsecutive = (calYesterday.get(java.util.Calendar.YEAR) == calLast.get(java.util.Calendar.YEAR) && calYesterday.get(java.util.Calendar.DAY_OF_YEAR) == calLast.get(java.util.Calendar.DAY_OF_YEAR))
                        if (isConsecutive) {
                            currentStreak++
                        } else {
                            currentStreak = 1
                        }
                    }
                } else {
                    currentStreak = 1
                }
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
                val updatedProfile = profile.copy(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    lastActiveTimeMs = currentMs
                )
                rDb.userProfileDao().insert(updatedProfile.toEntity())
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                awardCoins(5, "Daily Login Reward", "login_${uid}_$todayStr")
                if (updatedProfile.currentStreak > 1) {
                    awardCoins(100, "Daily Streak Maintained (${updatedProfile.currentStreak} Days)", "streak_${uid}_$todayStr")
                }
                if (updatedProfile.name.isNotBlank() && updatedProfile.phoneNumber.isNotBlank() && updatedProfile.profilePictureUrl.isNotBlank()) {
                    awardCoins(50, "Profile Completion Bonus", "profile_complete_$uid")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }
        try {
            val docRef = db.collection("users").document(uid)
            val updatedProfile = db.runTransaction { tx ->
                val snapshot = tx.get(docRef)
                val profile = snapshot.toObject(UserProfile::class.java) ?: return@runTransaction null
                
                var currentStreak = profile.currentStreak
                var longestStreak = profile.longestStreak
                val lastActiveMs = profile.lastActiveTimeMs
                
                val currentMs = java.lang.System.currentTimeMillis()
                
                val calLast = java.util.Calendar.getInstance()
                val calCurrent = java.util.Calendar.getInstance()
                calCurrent.timeInMillis = currentMs
                
                if (lastActiveMs > 0) {
                    calLast.timeInMillis = lastActiveMs
                    if (calCurrent.get(java.util.Calendar.DAY_OF_YEAR) != calLast.get(java.util.Calendar.DAY_OF_YEAR) || calCurrent.get(java.util.Calendar.YEAR) != calLast.get(java.util.Calendar.YEAR)) {
                        // Different day
                        val calYesterday = java.util.Calendar.getInstance()
                        calYesterday.timeInMillis = currentMs
                        calYesterday.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        val isConsecutive = (calYesterday.get(java.util.Calendar.YEAR) == calLast.get(java.util.Calendar.YEAR) && calYesterday.get(java.util.Calendar.DAY_OF_YEAR) == calLast.get(java.util.Calendar.DAY_OF_YEAR))
                        if (isConsecutive) {
                             currentStreak++
                        } else {
                             currentStreak = 1
                        }
                    } 
                } else {
                    currentStreak = 1
                }
                
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
                
                tx.update(docRef, "currentStreak", currentStreak, "longestStreak", longestStreak, "lastActiveTimeMs", currentMs)
                
                profile.copy(currentStreak = currentStreak, longestStreak = longestStreak)
            }.await()
            
            if (updatedProfile != null) {
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                
                // 1. Daily Login
                awardCoins(5, "Daily Login Reward", "login_${uid}_$todayStr")
                
                // 2. Daily Streak Maintain
                if (updatedProfile.currentStreak > 1) { // 2+ days
                    awardCoins(100, "Daily Streak Maintained (${updatedProfile.currentStreak} Days)", "streak_${uid}_$todayStr")
                }
                
                // 3. Profile Complete (One Time)
                if (updatedProfile.name.isNotBlank() && updatedProfile.phoneNumber.isNotBlank() && updatedProfile.profilePictureUrl.isNotBlank()) {
                     awardCoins(50, "Profile Completion Bonus", "profile_complete_$uid")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open suspend fun uploadProfileImage(uri: android.net.Uri): String {
        val uid = userId
        if (uid.isEmpty()) throw Exception("User not signed in")
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
        storageRef.putFile(uri).await()
        return storageRef.downloadUrl.await().toString()
    }

    open suspend fun uploadProfileImageBytes(bytes: ByteArray): String {
        val uid = userId
        if (uid.isEmpty()) throw Exception("User not signed in")
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
        storageRef.putBytes(bytes).await()
        return storageRef.downloadUrl.await().toString()
    }

    open suspend fun removeProfileImage() {
        val uid = userId
        if (uid.isEmpty()) throw Exception("User not signed in")
        try {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
            storageRef.delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateReferralCodeForEmail(email: String): String {
        val clean = email.substringBefore("@").replace("[^A-Za-z0-9]".toRegex(), "").take(4).uppercase()
        val rand = (1000..9999).random()
        return if (clean.length < 3) "REVI$rand" else "$clean$rand"
    }

    open val referrals: Flow<List<ReferralRecord>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("referrals").whereEqualTo("referrerId", uid)
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(ReferralRecord::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList() ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
    }

    open fun getAllReferrals(): Flow<List<ReferralRecord>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("referrals").whereEqualTo("referrerId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(ReferralRecord::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList() ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
        }
    }

    open val withdrawalRequests: Flow<List<WithdrawalRequest>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("withdrawal_requests").whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(WithdrawalRequest::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList() ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
    }

    open fun getAllWithdrawalRequests(): Flow<List<WithdrawalRequest>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("withdrawal_requests").whereEqualTo("userId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(WithdrawalRequest::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList() ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
        }
    }

    suspend fun submitWithdrawalRequest(req: WithdrawalRequest): Boolean {
        if (userId.isEmpty() || req.coinsDeducted <= 0) return false
        
        val userRef = db.collection("users").document(userId)
        val success = db.runTransaction { tx ->
            val userSnap = tx.get(userRef)
            if (userSnap.exists()) {
                val coins = userSnap.getLong("totalCoins") ?: 0L
                val coinsWithdrawn = userSnap.getLong("totalCoinsWithdrawn") ?: 0L
                if (coins >= req.coinsDeducted) {
                    tx.update(userRef, "totalCoins", coins - req.coinsDeducted)
                    tx.update(userRef, "totalCoinsWithdrawn", coinsWithdrawn + req.coinsDeducted)
                    return@runTransaction true
                }
            }
            false
        }.await()
        
        if (!success) return false
        
        val ref = db.collection("withdrawal_requests").document()
        val finalReq = req.copy(
            id = ref.id,
            userId = userId,
            createdAtMs = System.currentTimeMillis(),
            updatedAtMs = System.currentTimeMillis()
        )
        ref.set(finalReq).await()
        
        val txId = db.collection("coin_history").document().id
        db.collection("coin_history").document(txId).set(CoinTransaction(
            id = txId,
            userId = userId,
            amount = -req.coinsDeducted,
            description = "Withdrawal Requested (${req.method})",
            timestampMs = System.currentTimeMillis(),
            type = "SPENT"
        )).await()
        
        return true
    }

    suspend fun approveWithdrawal(reqId: String, adminId: String, adminEmail: String): Boolean {
        try {
            db.collection("withdrawal_requests").document(reqId).update(
                mapOf(
                    "status" to "APPROVED",
                    "updatedAtMs" to System.currentTimeMillis()
                )
            ).await()
            logSystemAction(adminId, adminEmail, "APPROVE_WITHDRAWAL", "Approved payout for withdrawal request ID: $reqId")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun rejectWithdrawal(reqId: String, reason: String, adminId: String, adminEmail: String): Boolean {
        try {
            val reqRef = db.collection("withdrawal_requests").document(reqId)
            val reqDoc = reqRef.get().await()
            val req = reqDoc.toObject(WithdrawalRequest::class.java) ?: return false
            
            // Refund coins
            val userRef = db.collection("users").document(req.userId)
            db.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                if (userSnap.exists()) {
                    val currentCoins = userSnap.getLong("totalCoins") ?: 0L
                    val currentWithdrawn = userSnap.getLong("totalCoinsWithdrawn") ?: 0L
                    tx.update(userRef, "totalCoins", currentCoins + req.coinsDeducted)
                    tx.update(userRef, "totalCoinsWithdrawn", (currentWithdrawn - req.coinsDeducted).coerceAtLeast(0L))
                }
                null
            }.await()
            
            val txId = db.collection("coin_history").document().id
            db.collection("coin_history").document(txId).set(CoinTransaction(
                id = txId,
                userId = req.userId,
                amount = req.coinsDeducted,
                description = "Refund: Rejected Withdrawal Request",
                timestampMs = System.currentTimeMillis(),
                type = "EARNED"
            )).await()
            
            reqRef.update(
                mapOf(
                    "status" to "REJECTED",
                    "rejectReason" to reason,
                    "updatedAtMs" to System.currentTimeMillis()
                )
            ).await()
            
            logSystemAction(adminId, adminEmail, "REJECT_WITHDRAWAL", "Rejected payout for withdrawal request ID $reqId. Reason: $reason")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun submitReferralCode(code: String): String {
        if (userId.isEmpty()) return "User not signed in"
        
        val result = db.collection("users").whereEqualTo("referralCode", code).get().await()
        if (result.isEmpty) {
            return "Invalid Referral Code"
        }
        
        val referrerSnap = result.documents.first()
        val referrer = referrerSnap.toObject(UserProfile::class.java) ?: return "Error finding referrer"
        
        if (referrer.id == userId) {
            return "You cannot refer yourself!"
        }
        
        val meRef = db.collection("users").document(userId)
        val meSnap = meRef.get().await()
        val me = meSnap.toObject(UserProfile::class.java) ?: return "Error reading profile"
        if (me.referredBy.isNotEmpty()) {
            return "You have already been referred!"
        }
        
        val ref = db.collection("referrals").document()
        val record = ReferralRecord(
            id = ref.id,
            referrerId = referrer.id,
            referrerName = referrer.name.ifEmpty { referrer.email },
            referredId = userId,
            referredEmail = me.email,
            referredName = me.name.ifEmpty { me.email },
            status = "PENDING",
            createdAtMs = System.currentTimeMillis()
        )
        ref.set(record).await()
        
        meRef.update("referredBy", code).await()
        return "Success! Referral registered. Complete 1 hour of study to award referrer."
    }

    suspend fun evaluateReferralPayouts() {
        if (userId.isEmpty()) return
        try {
            val currentProfileDoc = db.collection("users").document(userId).get().await()
            val profile = currentProfileDoc.toObject(UserProfile::class.java) ?: return
            
            if (profile.referredBy.isNotEmpty() && profile.studyTimeMinutes >= 60) {
                val referralQueryResult = db.collection("referrals")
                    .whereEqualTo("referredId", userId)
                    .whereEqualTo("rewardCredited", false)
                    .get().await()
                    
                for (doc in referralQueryResult.documents) {
                    val record = doc.toObject(ReferralRecord::class.java) ?: continue
                    val referrerId = record.referrerId
                    
                    val referrerDocRef = db.collection("users").document(referrerId)
                    db.runTransaction { tx ->
                        val referrerSnap = tx.get(referrerDocRef)
                        if (referrerSnap.exists()) {
                            val currentCoins = referrerSnap.getLong("totalCoins") ?: 0L
                            val currentReferrals = referrerSnap.getLong("referralCount") ?: 0L
                            val currentReferralCoins = referrerSnap.getLong("referralCoinsEarned") ?: 0L
                            
                            tx.update(referrerDocRef, "totalCoins", currentCoins + 1500)
                            tx.update(referrerDocRef, "referralCount", currentReferrals + 1)
                            tx.update(referrerDocRef, "referralCoinsEarned", currentReferralCoins + 1500)
                        }
                        null
                    }.await()
                    
                    val txId = db.collection("coin_history").document().id
                    db.collection("coin_history").document(txId).set(CoinTransaction(
                        id = txId,
                        userId = referrerId,
                        amount = 1500,
                        description = "Referral Bonus: ${profile.name.ifEmpty { profile.email }}",
                        timestampMs = System.currentTimeMillis(),
                        type = "EARNED"
                    )).await()
                    
                    db.collection("referrals").document(record.id).update(
                        mapOf(
                            "status" to "COMPLETED",
                            "rewardCredited" to true,
                            "studyTimeWhenRewardedMins" to profile.studyTimeMinutes
                        )
                    ).await()
                    
                    logSystemAction("System", "system@revisionspace.com", "REFERRAL_CREDITED", "Credited 1500 Coins to User: $referrerId for referral: ${profile.email}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAdminLogs(): Flow<List<AdminLog>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("admin_logs").whereEqualTo("adminId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(AdminLog::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList() ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
        }
    }

    suspend fun logAdminAction(adminEmail: String, action: String, details: String) {
        val docId = db.collection("admin_logs").document().id
        db.collection("admin_logs").document(docId).set(AdminLog(
            id = docId,
            adminId = userId,
            adminEmail = adminEmail,
            action = action,
            details = details,
            timestampMs = System.currentTimeMillis()
        )).await()
    }

    suspend fun sendBroadcastNotification(title: String, message: String, audience: String, adminEmail: String) {
        try {
            val broadcastId = db.collection("broadcast_requests").document().id
            val request = hashMapOf(
                "id" to broadcastId,
                "title" to title,
                "message" to message,
                "audience" to audience,
                "adminEmail" to adminEmail,
                "requesterUid" to userId,
                "timestampMs" to System.currentTimeMillis()
            )
            db.collection("broadcast_requests").document(broadcastId).set(request).await()
            // Realtime listeners in Admin Logs & Analytics will update via the Cloud Function automatically
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    fun getBroadcastAnalytics(): Flow<List<NotificationBroadcast>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("broadcast_analytics")
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc -> try { doc.toObject(NotificationBroadcast::class.java) } catch (e: Exception) { e.printStackTrace(); null } } ?: emptyList() ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
        }
    }

    suspend fun markBroadcastAsOpened(broadcastId: String) {
        if (broadcastId.isEmpty()) return
        try {
            val ref = db.collection("broadcast_analytics").document(broadcastId)
            db.runTransaction { tx ->
                val snap = tx.get(ref)
                if (snap.exists()) {
                    val openedCount = snap.getLong("openedCount") ?: 0L
                    tx.update(ref, "openedCount", openedCount + 1)
                }
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun logSystemAction(adminId: String, email: String, action: String, details: String) {
        try {
            val ref = db.collection("admin_logs").document()
            ref.set(AdminLog(
                id = ref.id,
                adminId = adminId,
                adminEmail = email,
                action = action,
                details = details,
                timestampMs = System.currentTimeMillis()
            )).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val unreadNotificationsCount: Flow<Int> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(
                rDb.appNotificationDao().getNotifications("offline_user")
                    .map { list -> list.count { !it.isRead } }
            )
        } else {
            emit(0)
        }
    }

    val allNotifications: Flow<List<AppNotification>> = flow {
        val rDb = roomDb
        if (rDb != null) {
            emitAll(
                rDb.appNotificationDao().getNotifications("offline_user")
                    .map { list -> list.map { it.toDomain() } }
            )
        } else {
            emit(emptyList())
        }
    }
    
    suspend fun markNotificationAsRead(notificationId: String) {
        if (notificationId.isEmpty()) return
        val rDb = roomDb
        if (rDb != null) {
            rDb.openHelper.writableDatabase.execSQL(
                "UPDATE app_notifications SET isRead = 1 WHERE id = ?",
                arrayOf<Any>(notificationId)
            )
            return
        }
        if (userId.isEmpty()) return
        db.collection("users").document(userId).collection("notifications").document(notificationId).update("isRead", true).await()
    }
    
    suspend fun markAllNotificationsAsRead() {
        val rDb = roomDb
        if (rDb != null) {
            rDb.openHelper.writableDatabase.execSQL("UPDATE app_notifications SET isRead = 1")
            return
        }
        if (userId.isEmpty()) return
        val notifs = db.collection("users").document(userId).collection("notifications").whereEqualTo("isRead", false).get().await()
        val batch = db.batch()
        notifs.documents.forEach {
            batch.update(it.reference, "isRead", true)
        }
        batch.commit().await()
    }

    open suspend fun signUpWithEmail(email: String, password: String, name: String, referralCodeStr: String = "") = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        var validReferrerId = ""
        var validReferrerName = ""
        if (referralCodeStr.isNotBlank()) {
            val referrerDocs = db.collection("users").whereEqualTo("referralCode", referralCodeStr).get().await()
            if (referrerDocs.isEmpty) {
                throw Exception("Invalid referral code.")
            }
            validReferrerId = referrerDocs.documents[0].id
            validReferrerName = referrerDocs.documents[0].getString("name") ?: ""
        }
        
        val authStart = System.currentTimeMillis()
        auth?.createUserWithEmailAndPassword(email, password)?.await()
        android.util.Log.d("AuthPerf", "Firebase Auth Time: ${System.currentTimeMillis() - authStart}ms")
        
        val userId = auth?.currentUser?.uid
        if (userId != null) {
            // Write profile data asynchronously
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val profile = UserProfile(
                        id = userId,
                        name = name,
                        email = email,
                        referralCode = generateReferralCodeForEmail(email),
                        referredBy = if (validReferrerId.isNotBlank()) validReferrerName else "",
                        createdAtMs = System.currentTimeMillis()
                    )
                    db.collection("users").document(userId).set(profile).await()
                    
                    try {
                        db.collection("users").document(userId).collection("data").document("settings").set(
                            mapOf(
                                "dailyTarget" to 0,
                                "notificationsEnabled" to true,
                                "soundEnabled" to true,
                                "vibrationEnabled" to true
                            )
                        ).await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    if (validReferrerId.isNotBlank()) {
                        try {
                            val referralRecord = ReferralRecord(
                                id = userId,
                                referrerId = validReferrerId,
                                referredEmail = email,
                                referredName = name,
                                status = "PENDING",
                                createdAtMs = System.currentTimeMillis()
                            )
                            db.collection("users").document(validReferrerId).collection("referrals").document(userId).set(referralRecord).await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    open suspend fun signInWithEmail(email: String, password: String) {
        auth?.signInWithEmailAndPassword(email, password)?.await()
        val userId = auth?.currentUser?.uid
        if (userId != null) {
            val userDoc = db.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                // Self-healing: if auth is successful but user profile in Firestore doesn't exist, create it on-demand
                val userEmail = auth?.currentUser?.email ?: email
                val userName = userEmail.substringBefore("@")
                val profile = UserProfile(
                    id = userId,
                    name = userName,
                    email = userEmail,
                    referralCode = generateReferralCodeForEmail(userEmail),
                    createdAtMs = System.currentTimeMillis()
                )
                db.collection("users").document(userId).set(profile).await()
                
                try {
                    db.collection("users").document(userId).collection("data").document("settings").set(
                        mapOf(
                            "dailyTarget" to 0,
                            "notificationsEnabled" to true,
                            "soundEnabled" to true,
                            "vibrationEnabled" to true
                        )
                    ).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val isBanned = userDoc.getBoolean("isBanned") ?: false
                if (isBanned) {
                    auth?.signOut()
                    throw Exception("Your account has been suspended by administrator.\nContact support for assistance.")
                }
            }
        }
    }

    open suspend fun signInWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth?.signInWithCredential(credential)?.await()
            val userId = result?.user?.uid
            if (userId != null) {
                val userDoc = db.collection("users").document(userId).get().await()
                if (!userDoc.exists()) {
                    val userEmail = result?.user?.email ?: ""
                    val userName = result?.user?.displayName ?: userEmail.substringBefore("@")
                    val profile = UserProfile(
                        id = userId,
                        name = userName,
                        email = userEmail,
                        profilePictureUrl = result?.user?.photoUrl?.toString() ?: "",
                        referralCode = generateReferralCodeForEmail(userEmail),
                        createdAtMs = System.currentTimeMillis()
                    )
                    db.collection("users").document(userId).set(profile).await()
                } else {
                    val isBanned = userDoc.getBoolean("isBanned") ?: false
                    if (isBanned) {
                        auth?.signOut()
                        throw Exception("Your account has been suspended by administrator.\nContact support for assistance.")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    open suspend fun signInAnonymously() {
        try {
            val result = auth?.signInAnonymously()?.await()
            val userId = result?.user?.uid
            if (userId != null) {
                val userDoc = db.collection("users").document(userId).get().await()
                if (!userDoc.exists()) {
                    val guestEmail = "guest_${userId.take(6)}@revisionspace.app"
                    val profile = UserProfile(
                        id = userId,
                        name = "Guest Student",
                        email = guestEmail,
                        referralCode = generateReferralCodeForEmail(guestEmail),
                        createdAtMs = System.currentTimeMillis()
                    )
                    db.collection("users").document(userId).set(profile).await()
                    try {
                        db.collection("users").document(userId).collection("data").document("settings").set(
                            mapOf(
                                "dailyTarget" to 0,
                                "notificationsEnabled" to true,
                                "soundEnabled" to true,
                                "vibrationEnabled" to true
                            )
                        ).await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("administrators only") || msg.contains("OPERATION_NOT_ALLOWED")) {
                val randomId = java.util.UUID.randomUUID().toString().replace("-", "").take(8)
                val guestEmail = "guest_$randomId@revisionspace.app"
                val guestPassword = java.util.UUID.randomUUID().toString()
                
                val result = auth?.createUserWithEmailAndPassword(guestEmail, guestPassword)?.await()
                val userId = result?.user?.uid
                if (userId != null) {
                    val profile = UserProfile(
                        id = userId,
                        name = "Guest Student",
                        email = guestEmail,
                        referralCode = generateReferralCodeForEmail(guestEmail),
                        createdAtMs = System.currentTimeMillis()
                    )
                    db.collection("users").document(userId).set(profile).await()
                    try {
                        db.collection("users").document(userId).collection("data").document("settings").set(
                            mapOf(
                                "dailyTarget" to 0,
                                "notificationsEnabled" to true,
                                "soundEnabled" to true,
                                "vibrationEnabled" to true
                            )
                        ).await()
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            } else {
                throw e
            }
        }
    }


    suspend fun checkUserExistsAndNotBanned(userId: String): Boolean? {
        val doc = kotlinx.coroutines.withTimeoutOrNull(4000L) {
            db.collection("users").document(userId).get().await()
        } ?: return null
        
        if (!doc.exists()) {
            val profile = UserProfile(
                id = userId,
                name = "Offline Student",
                email = "offline@revisionspace.app",
                referralCode = generateReferralCodeForEmail("offline@revisionspace.app"),
                createdAtMs = System.currentTimeMillis()
            )
            try {
                db.collection("users").document(userId).set(profile).await()
                db.collection("users").document(userId).collection("data").document("settings").set(
                    mapOf(
                        "dailyTarget" to 0,
                        "notificationsEnabled" to true,
                        "soundEnabled" to true,
                        "vibrationEnabled" to true
                    )
                ).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }
        val isBanned = doc.getBoolean("isBanned") ?: false
        if (isBanned) {
            return false
        }
        return true
    }


    suspend fun deleteNotification(notificationId: String) {
        if (notificationId.isEmpty()) return
        val rDb = roomDb
        if (rDb != null) {
            rDb.appNotificationDao().delete(notificationId)
            return
        }
        val uid = auth?.currentUser?.uid ?: ""
        if (uid.isEmpty()) return
        try {
            db.collection("users").document(uid).collection("notifications").document(notificationId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun logBroadcastOpen(topicId: String) {
        if (topicId.isEmpty()) return
        try {
            val broadcastRef = db.collection("broadcast_analytics").document(topicId)
            db.runTransaction { tx ->
                val snap = tx.get(broadcastRef)
                if (snap.exists()) {
                    val openedCount = snap.getLong("openedCount") ?: 0L
                    tx.update(broadcastRef, "openedCount", openedCount + 1)
                }
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open suspend fun getTodayStudyLog(todayStr: String): StudyLog? {
        if (userId.isEmpty()) return null
        val rDb = roomDb
        if (rDb != null) {
            return rDb.studyLogDao().getStudyLogForDate(userId, todayStr)?.toDomain()
                ?: rDb.studyLogDao().getStudyLogForDate("offline_user", todayStr)?.toDomain()
        }
        return try {
            val snap = db.collection("study_logs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("dateString", todayStr)
                .get()
                .await()
            snap.documents.mapNotNull { doc -> try { doc.toObject(StudyLog::class.java) } catch (e: Exception) { e.printStackTrace(); null } }.firstOrNull()
        } catch (e: Exception) {
            throw e
        }
    }

    open suspend fun getTopicById(topicId: String): Topic? {
        if (topicId.isBlank()) return null
        val rDb = roomDb
        if (rDb != null) {
            return rDb.topicDao().getTopicById(topicId)?.toDomain()
        }
        return try {
            val snap = db.collection("topics").document(topicId).get().await()
            if (snap.exists()) snap.toObject(Topic::class.java) else null
        } catch (e: Exception) {
            throw e
        }
    }

    open suspend fun getUserProfile(): UserProfile? {
        if (userId.isEmpty()) return null
        val rDb = roomDb
        if (rDb != null) {
            return rDb.userProfileDao().getProfileSync(userId)?.toDomain()
                ?: rDb.userProfileDao().getProfileSync("offline_user")?.toDomain()
        }
        return try {
            val snap = db.collection("users").document(userId).get().await()
            if (snap.exists()) snap.toObject(UserProfile::class.java) else null
        } catch (e: Exception) {
            throw e
        }
    }

    open suspend fun getRevisionsForTopic(topicId: String): List<Revision> {
        if (userId.isEmpty() || topicId.isBlank()) return emptyList()
        val rDb = roomDb
        if (rDb != null) {
            return rDb.revisionDao().getRevisionsForTopic(topicId).map { it.toDomain() }
        }
        return try {
            val snap = db.collection("users").document(userId)
                .collection("revisions")
                .whereEqualTo("topicId", topicId)
                .get()
                .await()
            snap.documents.mapNotNull { doc -> try { doc.toObject(Revision::class.java) } catch (e: Exception) { e.printStackTrace(); null } }
        } catch (e: Exception) {
            throw e
        }
    }

    open fun getRevisionHistoryFlowForTopic(topicId: String): Flow<List<RevisionHistoryRecord>> {
        val rDb = roomDb
        if (rDb != null) {
            return rDb.revisionHistoryDao().getHistoryForTopic(topicId).map { list ->
                list.map { it.toDomain() }
            }
        }
        return flow {
            emit(getRevisionHistoryForTopic(topicId))
        }
    }

    open suspend fun getRevisionHistoryForTopic(topicId: String): List<RevisionHistoryRecord> {
        if (topicId.isBlank()) return emptyList()
        val rDb = roomDb
        if (rDb != null) {
            return rDb.revisionHistoryDao().getHistoryListForTopic(topicId).map { it.toDomain() }
        }
        if (userId.isEmpty()) return emptyList()
        return try {
            val snap = db.collection("users").document(userId)
                .collection("revision_history")
                .whereEqualTo("topicId", topicId)
                .get()
                .await()
            snap.toObjects(RevisionHistoryRecord::class.java).sortedByDescending { it.completedDateMs }
        } catch (e: Exception) {
            emptyList()
        }
    }

    open suspend fun getChapterById(chapterId: String): Chapter? {
        if (chapterId.isBlank()) return null
        val rDb = roomDb
        if (rDb != null) {
            return rDb.chapterDao().getChapterById(chapterId)?.toDomain()
        }
        return try {
            val snap = db.collection("chapters").document(chapterId).get().await()
            if (snap.exists()) snap.toObject(Chapter::class.java) else null
        } catch (e: Exception) {
            throw e
        }
    }

    open suspend fun getSubjectById(subjectId: String): Subject? {
        if (subjectId.isBlank()) return null
        val rDb = roomDb
        if (rDb != null) {
            return rDb.subjectDao().getSubjectById(subjectId)?.toDomain()
        }
        return try {
            val snap = db.collection("subjects").document(subjectId).get().await()
            if (snap.exists()) snap.toObject(Subject::class.java) else null
        } catch (e: Exception) {
            throw e
        }
    }


    private suspend fun recalculateAllTopicsSrs(uid: String, newPattern: List<Int>) {
        try {
            val topicsSnap = db.collection("topics").whereEqualTo("userId", uid).get().await()
            val ctx = try { com.google.firebase.FirebaseApp.getInstance().applicationContext } catch(e: Exception) { null }
            
            for (doc in topicsSnap.documents) {
                val topic = doc.toObject(Topic::class.java) ?: continue
                if (topic.status == "Complete" && (topic.revisionPattern == null || true)) {
                    // It uses global pattern
                    val revNum = topic.revisionCount
                    val firstStudyMs = topic.firstStudyDate ?: topic.startDateMs ?: topic.createdAt ?: System.currentTimeMillis()
                    val nextRevisionMs = com.example.services.RevisionEngine.calculateNextRevisionTimestamp(
                        revisionCount = revNum,
                        pattern = newPattern,
                        firstStudyMs = firstStudyMs,
                        lastRevisionMs = topic.lastRevisionDateMs
                    )
                    
                    db.collection("topics").document(topic.id).update(
                        "nextRevisionDateMs", nextRevisionMs,
                        "revisionPattern", newPattern,
                        "totalRequiredRevisions", newPattern.size,
                        "revisionStatus", if (revNum >= newPattern.size) "Mastered (Recurring 30-day)" else "Pending Revision"
                    ).await()
                    
                    if (ctx != null) {
                        if (nextRevisionMs != 0L) {
                            com.example.services.SrsNotificationManager.scheduleChapterRevision(
                                ctx, topic.id, topic.subjectId, topic.chapterId, topic.name, nextRevisionMs, topic.estimatedRevisionTimeMins
                            )
                        } else {
                            com.example.services.SrsNotificationManager.cancelChapterRevision(ctx, topic.id)
                        }
                    }
                }
            }
        } catch(e: Exception) { e.printStackTrace() }
    }

    }
