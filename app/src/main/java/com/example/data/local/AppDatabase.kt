package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        OfflineNote::class,
        SubjectEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        RevisionEntity::class,
        StudyLogEntity::class,
        UserProfileEntity::class,
        ExamEntity::class,
        RevisionHistoryEntity::class,
        SubjectTestEntity::class,
        QuickTaskEntity::class,
        CoinTransactionEntity::class,
        AppNotificationEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offlineNoteDao(): OfflineNoteDao
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun topicDao(): TopicDao
    abstract fun revisionDao(): RevisionDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun examDao(): ExamDao
    abstract fun revisionHistoryDao(): RevisionHistoryDao
    abstract fun subjectTestDao(): SubjectTestDao
    abstract fun quickTaskDao(): QuickTaskDao
    abstract fun coinTransactionDao(): CoinTransactionDao
    abstract fun appNotificationDao(): AppNotificationDao

    companion object {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE topics ADD COLUMN firstStudyDate INTEGER")
                db.execSQL("ALTER TABLE topics ADD COLUMN firstStudyTime TEXT")
                db.execSQL("ALTER TABLE topics ADD COLUMN firstStudyDuration INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN totalRevisions INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN totalRevisionCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN lastRevisionDate INTEGER")
                db.execSQL("ALTER TABLE topics ADD COLUMN lastRevisionTime TEXT")
                db.execSQL("ALTER TABLE topics ADD COLUMN lastRevisionDuration INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN nextRevisionDate INTEGER")
                db.execSQL("ALTER TABLE topics ADD COLUMN revisionStatus TEXT NOT NULL DEFAULT 'Pending'")
                db.execSQL("ALTER TABLE topics ADD COLUMN progressPercentage INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN expectedRevisionTime INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE topics ADD COLUMN currentRevisionStage INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN customSpacingPattern TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE topics ADD COLUMN studyCompleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN completedRevisions INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE topics ADD COLUMN totalRequiredRevisions INTEGER NOT NULL DEFAULT 4")
                db.execSQL("ALTER TABLE topics ADD COLUMN progressScore INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN focusSettingsStr TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
