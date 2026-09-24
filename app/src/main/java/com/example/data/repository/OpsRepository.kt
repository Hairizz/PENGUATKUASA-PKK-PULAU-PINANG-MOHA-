package com.example.data.repository

import com.example.data.local.OperationDao
import com.example.data.local.ReminderDao
import com.example.data.model.OperationEntity
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

class OpsRepository(
    private val operationDao: OperationDao,
    private val reminderDao: ReminderDao
) {
    val allOperations: Flow<List<OperationEntity>> = operationDao.getAllOperations()
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()

    suspend fun getOperationById(id: Long): OperationEntity? = operationDao.getOperationById(id)

    suspend fun saveOperation(operation: OperationEntity): Long {
        return if (operation.id == 0L) {
            operationDao.insertOperation(operation)
        } else {
            operationDao.updateOperation(operation)
            operation.id
        }
    }

    suspend fun deleteOperation(operation: OperationEntity) {
        operationDao.deleteOperation(operation)
    }

    fun searchOperations(query: String): Flow<List<OperationEntity>> {
        return operationDao.searchOperations(query)
    }

    suspend fun saveReminder(reminder: ReminderEntity): Long {
        return if (reminder.id == 0L) {
            reminderDao.insertReminder(reminder)
        } else {
            reminderDao.updateReminder(reminder)
            reminder.id
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun setReminderResolved(id: Long, resolved: Boolean) {
        reminderDao.setReminderResolved(id, resolved)
    }

    fun getRemindersForOperation(opId: Long): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersForOperation(opId)
    }
}
