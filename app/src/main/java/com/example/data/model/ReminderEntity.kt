package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationId: Long? = null,
    val operationTitle: String,
    val reminderType: String, // e.g., "Laporan Awal 24 Jam", "Tamat Tempoh Reman", "Serahan Eksibit", "Post-Mortem"
    val note: String = "",
    val targetTimestamp: Long,
    val isTriggered: Boolean = false,
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
