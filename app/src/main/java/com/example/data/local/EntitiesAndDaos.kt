package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- TYPE CONVERTERS ---
class RoomConverters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrEmpty()) return listOf(1, 3, 7, 14, 30)
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(";;;").filter { it.isNotEmpty() }
    }
}

// --- ENTITIES ---

@Entity(tableName = "subjects", indices = [Index(value = ["userId", "isArchived"])])
data class SubjectEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val examId: String = "",
    val name: String = "",
    val colorHex: String = "#0061A4",
    val icon: String = "book",
    val totalTopicsExpected: Int = 0,
    val isArchived: Boolean = false
)

@Entity(tableName = "chapters", indices = [Index(value = ["userId", "subjectId", "isArchived"])])
data class ChapterEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val subjectId: String = "",
    val name: String = "",
    val isCompleted: Boolean = false,
    val revisionCount: Int = 0,
    val progress: Float = 0f,
    val lastRevisionDateMs: Long = 0L,
    val importance: String = "Medium",
    val notes: String = "",
    val scoreObtained: Float? = null,
    val scoreTotal: Float? = null,
    val colorHex: String = "",
    val icon: String = "",
    val isArchived: Boolean = false,
    val firstStudyDateMs: Long? = null,
    val lastStudyDateMs: Long? = null,
    val totalStudyTimeMs: Long = 0L,
    val totalRevisionTimeMs: Long = 0L,
    val nextRevisionDateMs: Long = 0L,
    val status: String = "Upcoming"
)

@Entity(tableName = "topics", indices = [Index(value = ["userId", "chapterId", "subjectId"])])
data class TopicEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val chapterId: String = "",
    val subjectId: String = "",
    val name: String = "",
    val notes: String = "",
    val difficulty: String = "Medium",
    val priority: String = "Medium",
    val estimatedRevisionTimeMins: Int = 30,
    val status: String = "Pending",
    val studyTimeMs: Long = 0L,
    val revisionTimeMs: Long = 0L,
    val revisionCount: Int = 0,
    val lastRevisionDateMs: Long = 0L,
    val nextRevisionDateMs: Long = 0L,
    val masteryScore: Int = 0,
    val examIds: List<String> = emptyList(),
    val revisionPattern: List<Int> = listOf(1, 3, 7, 14, 30),
    val startDateMs: Long? = null,
    val tags: List<String> = emptyList(),
    val firstStudyDate: Long? = null,
    val firstStudyTime: String? = null,
    val firstStudyDuration: Long = 0L,
    val totalRevisions: Int = 0,
    val totalRevisionCount: Int = 0,
    val lastRevisionDate: Long? = null,
    val lastRevisionTime: String? = null,
    val lastRevisionDuration: Long = 0L,
    val nextRevisionDate: Long? = null,
    val revisionStatus: String = "Pending",
    val progressPercentage: Int = 0,
    val expectedRevisionTime: Int = 30,
    val currentRevisionStage: Int = 0,
    val customSpacingPattern: List<Int> = emptyList(),
    val studyCompleted: Boolean = false,
    val completedRevisions: Int = 0,
    val totalRequiredRevisions: Int = 4,
    val progressScore: Int = 0
)

@Entity(tableName = "revisions", indices = [Index(value = ["userId", "topicId"])])
data class RevisionEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val subjectId: String = "",
    val chapterId: String = "",
    val topicId: String = "",
    val subjectName: String = "",
    val chapterName: String = "",
    val topicName: String = "",
    val revisionCount: Int = 0,
    val pattern: List<Int> = listOf(1, 3, 7, 14, 30),
    val lastRevisionDateMs: Long = 0L,
    val nextRevisionDateMs: Long = 0L,
    val status: String = "Pending",
    val notes: String = "",
    val studyDate: String = "",
    val studyTime: String = "",
    val duration: Int = 0,
    val priority: String = "Medium",
    val createdAt: Long = 0L
)

@Entity(tableName = "study_logs", indices = [Index(value = ["userId", "dateString"])])
data class StudyLogEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val dateString: String = "",
    val studyTimeMs: Long = 0L,
    val revisionTimeMs: Long = 0L,
    val focusSessions: Int = 0,
    val deepFocusTimeMs: Long = 0L,
    val longestSessionMs: Long = 0L,
    val totalDistractions: Int = 0,
    val sessionCount: Int = 0
)

@Entity(tableName = "user_profiles", indices = [Index(value = ["id"])])
data class UserProfileEntity(
    @PrimaryKey val id: String = "offline_user",
    val name: String = "Student",
    val bio: String = "",
    val email: String = "user@mobile.local",
    val phoneNumber: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val lastActiveTimeMs: Long = System.currentTimeMillis(),
    val isPremium: Boolean = true,
    val role: String = "User",
    val isBanned: Boolean = false,
    val banReason: String = "",
    val studyTimeMinutes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCoins: Int = 0,
    val totalRevisions: Int = 0,
    val referralCode: String = "",
    val referredBy: String = "",
    val referralCount: Int = 0,
    val referralCoinsEarned: Int = 0,
    val isEmailVerified: Boolean = true,
    val isMobileVerified: Boolean = true,
    val totalCoinsWithdrawn: Int = 0,
    val profilePictureUrl: String = "",
    val profileImageUrl: String = "",
    val totalTestsAttempted: Int = 0,
    val averageAccuracy: Float = 0f,
    val fcmToken: String = "",
    val airRank: Int = 1,
    val topicsCompleted: Int = 0,
    val badge: String = "Scholar",
    val league: String = "Diamond",
    val rankPosition: Int? = 1,
    val isRanked: Boolean = true,
    // Focus timer settings (mode, durations, sound...) persisted as a compact v1 string.
    val focusSettingsStr: String = ""
)

@Entity(tableName = "exams", indices = [Index(value = ["userId"])])
data class ExamEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val name: String = "",
    val examDateMs: Long? = null,
    val targetStudyHours: Int? = null,
    val createdAtMs: Long = System.currentTimeMillis(),
    val progress: Float = 0f,
    val category: String = "",
    val targetRank: String = "",
    val totalSubjects: Int = 0
)

@Entity(tableName = "revision_history", indices = [Index(value = ["userId", "completedDateMs"])])
data class RevisionHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val topicId: String = "",
    val topicName: String = "",
    val completedDateMs: Long = System.currentTimeMillis(),
    val revisionNumber: Int = 0,
    val durationMs: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "subject_tests", indices = [Index(value = ["userId", "subjectId"])])
data class SubjectTestEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val subjectId: String = "",
    val testName: String = "",
    val dateMs: Long = System.currentTimeMillis(),
    val marksObtained: Float = 0f,
    val totalMarks: Float = 0f,
    val remarks: String = ""
)

@Entity(tableName = "quick_tasks", indices = [Index(value = ["userId"])])
data class QuickTaskEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val title: String = "",
    val subject: String = "",
    val isOneTime: Boolean = true,
    val isCarryForward: Boolean = false,
    val isCompleted: Boolean = false,
    val dateCreatedStr: String = "",
    val completedDateStr: String = ""
)

@Entity(tableName = "coin_transactions", indices = [Index(value = ["userId", "timestampMs"])])
data class CoinTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val amount: Int = 0,
    val description: String = "",
    val timestampMs: Long = System.currentTimeMillis(),
    val type: String = "EARNED"
)

@Entity(tableName = "app_notifications", indices = [Index(value = ["userId", "timestampMs"])])
data class AppNotificationEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val topicId: String = "",
    val title: String = "",
    val message: String = "",
    val timestampMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "System",
    val notificationType: String = "",
    val examId: String = "",
    val subjectId: String = "",
    val chapterId: String = "",
    val revisionId: String = ""
)

// --- DAOs ---

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE userId = :userId AND isArchived = 0")
    fun getSubjects(userId: String = "offline_user"): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE userId = :userId AND isArchived = 0")
    suspend fun getSubjectsList(userId: String = "offline_user"): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM subjects WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE userId = :userId AND isArchived = 0")
    fun getChapters(userId: String = "offline_user"): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE userId = :userId AND isArchived = 0")
    suspend fun getChaptersList(userId: String = "offline_user"): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteBySubject(subjectId: String)

    @Query("DELETE FROM chapters WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE userId = :userId")
    fun getTopics(userId: String = "offline_user"): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE userId = :userId")
    suspend fun getTopicsList(userId: String = "offline_user"): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM topics WHERE chapterId = :chapterId")
    suspend fun deleteByChapter(chapterId: String)

    @Query("DELETE FROM topics WHERE subjectId = :subjectId")
    suspend fun deleteBySubject(subjectId: String)

    @Query("DELETE FROM topics WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revisions WHERE userId = :userId")
    fun getRevisions(userId: String = "offline_user"): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE userId = :userId")
    suspend fun getRevisionsList(userId: String = "offline_user"): List<RevisionEntity>

    @Query("SELECT * FROM revisions WHERE topicId = :topicId")
    suspend fun getRevisionsForTopic(topicId: String): List<RevisionEntity>

    @Query("SELECT * FROM revisions WHERE id = :id LIMIT 1")
    suspend fun getRevisionById(id: String): RevisionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(revision: RevisionEntity)

    @Query("DELETE FROM revisions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM revisions WHERE topicId = :topicId")
    suspend fun deleteByTopic(topicId: String)

    @Query("DELETE FROM revisions WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface StudyLogDao {
    @Query("SELECT * FROM study_logs WHERE userId = :userId")
    fun getStudyLogs(userId: String = "offline_user"): Flow<List<StudyLogEntity>>

    @Query("SELECT * FROM study_logs WHERE userId = :userId AND dateString = :dateStr LIMIT 1")
    suspend fun getStudyLogForDate(userId: String = "offline_user", dateStr: String): StudyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: StudyLogEntity)

    @Query("DELETE FROM study_logs WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getProfile(id: String = "offline_user"): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileSync(id: String = "offline_user"): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE userId = :userId")
    fun getExams(userId: String = "offline_user"): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM exams WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface RevisionHistoryDao {
    @Query("SELECT * FROM revision_history WHERE userId = :userId ORDER BY completedDateMs DESC LIMIT 20")
    fun getHistory(userId: String = "offline_user"): Flow<List<RevisionHistoryEntity>>

    @Query("SELECT * FROM revision_history WHERE topicId = :topicId ORDER BY completedDateMs DESC")
    fun getHistoryForTopic(topicId: String): Flow<List<RevisionHistoryEntity>>

    @Query("SELECT * FROM revision_history WHERE topicId = :topicId ORDER BY completedDateMs DESC")
    suspend fun getHistoryListForTopic(topicId: String): List<RevisionHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: RevisionHistoryEntity)

    @Query("DELETE FROM revision_history WHERE topicId = :topicId")
    suspend fun deleteByTopic(topicId: String)

    @Query("DELETE FROM revision_history WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface SubjectTestDao {
    @Query("SELECT * FROM subject_tests WHERE userId = :userId")
    fun getTests(userId: String = "offline_user"): Flow<List<SubjectTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(test: SubjectTestEntity)

    @Query("DELETE FROM subject_tests WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM subject_tests WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface QuickTaskDao {
    @Query("SELECT * FROM quick_tasks WHERE userId = :userId")
    fun getTasks(userId: String = "offline_user"): Flow<List<QuickTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: QuickTaskEntity)

    @Query("DELETE FROM quick_tasks WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM quick_tasks WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface CoinTransactionDao {
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY timestampMs DESC")
    fun getTransactions(userId: String = "offline_user"): Flow<List<CoinTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: CoinTransactionEntity)

    @Query("DELETE FROM coin_transactions WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications WHERE userId = :userId ORDER BY timestampMs DESC")
    fun getNotifications(userId: String = "offline_user"): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotificationEntity)

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM app_notifications WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

@Entity(
    tableName = "flashcards",
    indices = [
        Index(value = ["userId", "topicId"]),
        Index(value = ["userId", "nextReviewDateMs"])
    ]
)
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val userId: String = "offline_user",
    val subjectId: String = "",
    val chapterId: String = "",
    val topicId: String = "",
    val front: String = "",
    val back: String = "",
    val hint: String = "",
    val difficulty: String = "Medium",
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitionCount: Int = 0,
    val lapses: Int = 0,
    val nextReviewDateMs: Long = 0L,
    val lastReviewedMs: Long = 0L,
    val createdAtMs: Long = 0L
)

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY nextReviewDateMs ASC")
    fun getAllFlashcards(userId: String = "offline_user"): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND topicId = :topicId ORDER BY nextReviewDateMs ASC")
    fun getFlashcardsByTopic(userId: String = "offline_user", topicId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND subjectId = :subjectId ORDER BY nextReviewDateMs ASC")
    fun getFlashcardsBySubject(userId: String = "offline_user", subjectId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND nextReviewDateMs <= :nowMs ORDER BY nextReviewDateMs ASC")
    fun getDueFlashcards(userId: String = "offline_user", nowMs: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE userId = :userId AND nextReviewDateMs <= :nowMs")
    fun getDueCount(userId: String = "offline_user", nowMs: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<FlashcardEntity>)

    @Query("DELETE FROM flashcards WHERE id = :id AND userId = :userId")
    suspend fun delete(id: String, userId: String = "offline_user")

    @Query("DELETE FROM flashcards WHERE topicId = :topicId AND userId = :userId")
    suspend fun deleteByTopic(topicId: String, userId: String = "offline_user")

    @Query("DELETE FROM flashcards WHERE userId = :userId")
    fun deleteAllForUserSync(userId: String = "offline_user")
}

