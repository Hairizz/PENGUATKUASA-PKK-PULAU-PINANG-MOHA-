package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class OperationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)
        val opTitle = intent.getStringExtra(EXTRA_OP_TITLE) ?: "Operasi KDN"
        val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: "Peringatan SOP"
        val note = intent.getStringExtra(EXTRA_NOTE) ?: "Sila ambil tindakan segera mengikut arahan operasi."

        showNotification(context, reminderId.toInt(), opTitle, reminderType, note)
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        opTitle: String,
        reminderType: String,
        note: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "kdn_ops_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Peringatan Operasi KDN",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi automatik peringatan tempoh dan SOP operasi penguatkuasaan"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ PERINGATAN KDN: $reminderType")
            .setContentText("$opTitle - $note")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$opTitle\n\n$note\n\nSila kemaskini laporan fotogrid dalam sistem."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_OP_TITLE = "extra_op_title"
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
        const val EXTRA_NOTE = "extra_note"
    }
}
