package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase

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
            ).addMigrations(AppDatabase.MIGRATION_3_4)
             .fallbackToDestructiveMigration()
             .build()
        } catch (e: Exception) {
            android.util.Log.e("MyApplication", "Room Database init failed", e)
        }

        try {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId("1:1234567890:android:abcdef")
                .setProjectId("dummy-project-id")
                .setApiKey("dummy-api-key")
                .build()
            com.google.firebase.FirebaseApp.initializeApp(this, options)
        } catch (e: Exception) {
            android.util.Log.w("MyApplication", "Firebase optional init skipped", e)
        }

        try {
            com.example.services.SrsNotificationManager.scheduleDailyMission(this)
            com.example.notification.ReminderScheduler.scheduleDailyReminder(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
