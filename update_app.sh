#!/bin/bash
sed -i 's/import com.google.firebase.FirebaseApp/import androidx.room.Room\nimport com.example.data.local.AppDatabase\nimport com.google.firebase.FirebaseApp/g' app/src/main/java/com/example/MyApplication.kt

sed -i 's/class MyApplication : Application() {/class MyApplication : Application() {\n    companion object {\n        lateinit var database: AppDatabase\n            private set\n    }\n/g' app/src/main/java/com/example/MyApplication.kt

sed -i '/FirebaseFirestore.getInstance().firestoreSettings = settings/a \
            \
            // Initialize Room Database\
            database = Room.databaseBuilder(\
                applicationContext,\
                AppDatabase::class.java, "offline_database"\
            ).fallbackToDestructiveMigration().build()' app/src/main/java/com/example/MyApplication.kt
