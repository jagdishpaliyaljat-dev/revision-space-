package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "offline_notes")
data class OfflineNote(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

@Dao
interface OfflineNoteDao {
    @Query("SELECT * FROM offline_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<OfflineNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: OfflineNote)

    @Delete
    suspend fun deleteNote(note: OfflineNote)
}
