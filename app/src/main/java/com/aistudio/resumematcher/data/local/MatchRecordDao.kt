package com.aistudio.resumematcher.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aistudio.resumematcher.data.model.MatchRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchRecordDao {
    @Query("SELECT * FROM match_records ORDER BY timestamp DESC")
    fun getAllMatchRecords(): Flow<List<MatchRecord>>

    @Query("SELECT * FROM match_records WHERE id = :id")
    suspend fun getMatchRecordById(id: Int): MatchRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchRecord(record: MatchRecord): Long

    @Delete
    suspend fun deleteMatchRecord(record: MatchRecord)

    @Query("DELETE FROM match_records WHERE id = :id")
    suspend fun deleteMatchRecordById(id: Int)

    @Query("DELETE FROM match_records")
    suspend fun clearAllRecords()
}
