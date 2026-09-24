package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val referenceNumber: String,
    val operationDate: String,
    val operationTime: String,
    val location: String,
    val officerName: String,
    val agencyName: String = "Kementerian Dalam Negeri (KDN)",
    val actCategory: String = "Akta Mesin Cetak dan Penerbitan 1984",
    val suspectsCaught: String = "",
    val seizedItemsSummary: String = "",
    val remarks: String = "",
    val imageUri1: String? = null,
    val imageLabel1: String = "1. SASARAN / PREMIS",
    val imageUri2: String? = null,
    val imageLabel2: String = "2. PEMERIKSAAN / SASARAN",
    val imageUri3: String? = null,
    val imageLabel3: String = "3. EKSIBIT / BARANG RAMPASAN",
    val savedJpegPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
