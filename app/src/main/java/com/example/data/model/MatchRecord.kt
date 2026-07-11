package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val candidateName: String,
    val jobTitle: String,
    val matchScore: Int?,
    val resumeText: String,
    val jobDescription: String,
    val keyResponsibilities: String = "",
    val keyAccountabilities: String = "",
    val keyPerformanceIndicators: String = "",
    val qualifications: String = "",
    val analysisResult: String,
    val timestamp: Long = System.currentTimeMillis()
)
