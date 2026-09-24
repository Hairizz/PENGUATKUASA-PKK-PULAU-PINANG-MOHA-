package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.OperationEntity
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [OperationEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun operationDao(): OperationDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kdn_ops_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.operationDao(), database.reminderDao())
                }
            }
        }

        private suspend fun populateInitialData(opDao: OperationDao, remDao: ReminderDao) {
            val sampleOpId = opDao.insertOperation(
                OperationEntity(
                    title = "OP BERSEPADU KDN / KAWALAN",
                    referenceNumber = "KDN/BPK/SEL/2026/014",
                    operationDate = "24/09/2026",
                    operationTime = "21:30",
                    location = "Kawasan Perindustrian Bukit Raja, Klang, Selangor",
                    officerName = "PPK Mohd Ridzuan (Ketua Pasukan)",
                    agencyName = "Bahagian Penguatkuasaan & Kawalan KDN",
                    actCategory = "Akta Mesin Cetak dan Penerbitan 1984 [Akta 301]",
                    suspectsCaught = "6 Lelaki (Penyelia & Pekerja Warga Asing)",
                    seizedItemsSummary = "420 Naskhah Penerbitan Tanpa Permit & 2 Mesin Cetak",
                    remarks = "Tindakan pemeriksaan mendapati premis beroperasi mencetak penerbitan berunsur sensitif tanpa lesen sah KDN.",
                    imageLabel1 = "1. SASARAN PREMIS OPERASI",
                    imageLabel2 = "2. PEMERIKSAAN & TANGKAPAN",
                    imageLabel3 = "3. EKSIBIT BARANG RAMPASAN"
                )
            )

            val now = System.currentTimeMillis()
            // 24-hour reminder sample
            remDao.insertReminder(
                ReminderEntity(
                    operationId = sampleOpId,
                    operationTitle = "OP BERSEPADU KDN / KAWALAN",
                    reminderType = "Laporan Awal 24 Jam",
                    note = "Hantar Laporan Kilat Operasi kepada Setiausaha Bahagian Penguatkuasaan KDN Putrajaya.",
                    targetTimestamp = now + (18 * 3600 * 1000L),
                    isTriggered = false,
                    isResolved = false
                )
            )

            // Remand reminder sample
            remDao.insertReminder(
                ReminderEntity(
                    operationId = sampleOpId,
                    operationTitle = "OP BERSEPADU KDN / KAWALAN",
                    reminderType = "Tamat Tempoh Reman (Sek. 117 KPJ)",
                    note = "Permohonan sambung reman suspek di Mahkamah Majistret Klang sebelum jam 10:00 pagi.",
                    targetTimestamp = now + (36 * 3600 * 1000L),
                    isTriggered = false,
                    isResolved = false
                )
            )
        }
    }
}
