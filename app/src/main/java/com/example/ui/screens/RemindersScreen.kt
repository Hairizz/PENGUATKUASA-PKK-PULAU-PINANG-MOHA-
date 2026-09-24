package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ReminderEntity
import com.example.data.repository.OpsRepository
import com.example.reminder.ReminderScheduler
import com.example.ui.theme.KdnGold
import com.example.ui.theme.KdnNavy
import com.example.ui.theme.KdnNavyDark
import com.example.ui.theme.OpsAmber
import com.example.ui.theme.OpsGreen
import com.example.ui.theme.OpsRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    repository: OpsRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val reminders by repository.allReminders.collectAsState(initial = emptyList())
    val operations by repository.allOperations.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    // Check notification permission (Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) {
            Toast.makeText(context, "Kebenaran notifikasi automatik diaktifkan!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = KdnGold,
                contentColor = KdnNavyDark,
                modifier = Modifier.testTag("fab_add_reminder")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Peringatan")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Screen Title
            Text(
                text = "Pusat Peringatan Automatik SOP",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Sistem penggera automatik bagi mematuhi tempoh statutori operasi penguatkuasaan.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            // Permission Warning Banner if not granted
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = OpsAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notifikasi Perlu Diaktifkan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KdnNavyDark)
                            Text("Untuk menerima penggera 24 jam & reman automatik.", fontSize = 11.sp, color = Color(0xFF78350F))
                        }
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = KdnNavy),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Aktif", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Overview Counters Card
            val activeCount = reminders.count { !it.isResolved }
            val resolvedCount = reminders.count { it.isResolved }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = KdnNavy)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Peringatan Aktif", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        Text(
                            text = "$activeCount",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = KdnGold
                        )
                        Text("Menunggu Tindakan", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Tindakan Selesai", color = Color(0xFF64748B), fontSize = 11.sp)
                        Text(
                            text = "$resolvedCount",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = OpsGreen
                        )
                        Text("Mematuhi SOP", color = Color(0xFF64748B), fontSize = 10.sp)
                    }
                }
            }

            // Reminders List
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Tiada peringatan dijadualkan.",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KdnNavy)
                        ) {
                            Text("+ Tambah Peringatan SOP")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggleResolved = {
                                scope.launch {
                                    repository.setReminderResolved(reminder.id, !reminder.isResolved)
                                    if (!reminder.isResolved) {
                                        ReminderScheduler.cancelReminder(context, reminder.id)
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    ReminderScheduler.cancelReminder(context, reminder.id)
                                    repository.deleteReminder(reminder)
                                    Toast.makeText(context, "Peringatan dipadam.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Reminder Dialog
    if (showAddDialog) {
        AddReminderDialog(
            operations = operations.map { it.title },
            onDismiss = { showAddDialog = false },
            onSave = { title, type, hoursAhead, note ->
                scope.launch {
                    val targetTime = System.currentTimeMillis() + (hoursAhead * 3600 * 1000L)
                    val reminder = ReminderEntity(
                        operationTitle = title,
                        reminderType = type,
                        note = note,
                        targetTimestamp = targetTime
                    )
                    val id = repository.saveReminder(reminder)
                    ReminderScheduler.scheduleReminder(context, reminder.copy(id = id))
                    Toast.makeText(context, "Peringatan dijadualkan!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: ReminderEntity,
    onToggleResolved: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val timeLeftMillis = reminder.targetTimestamp - now
    val isExpired = timeLeftMillis <= 0

    val timeLeftText = when {
        reminder.isResolved -> "Telah Diselesaikan"
        isExpired -> "TEMPOH TAMAT / LUPUT"
        else -> {
            val hours = timeLeftMillis / (1000 * 60 * 60)
            val minutes = (timeLeftMillis % (1000 * 60 * 60)) / (1000 * 60)
            "Tinggal ${hours}J ${minutes}M"
        }
    }

    val typeColor = when {
        reminder.reminderType.contains("Reman", ignoreCase = true) -> OpsRed
        reminder.reminderType.contains("Laporan", ignoreCase = true) -> OpsAmber
        else -> KdnNavy
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_item_${reminder.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isResolved) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (reminder.isResolved) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = reminder.reminderType,
                        color = typeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Countdown badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                reminder.isResolved -> Color(0xFFDCFCE7)
                                isExpired -> Color(0xFFFEE2E2)
                                else -> Color(0xFFFEF3C7)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = timeLeftText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            reminder.isResolved -> Color(0xFF166534)
                            isExpired -> OpsRed
                            else -> Color(0xFF92400E)
                        }
                    )
                }
            }

            Text(
                text = reminder.operationTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (reminder.isResolved) Color(0xFF64748B) else KdnNavy
            )

            if (reminder.note.isNotBlank()) {
                Text(
                    text = reminder.note,
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }

            val targetDateFormatted = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(Date(reminder.targetTimestamp))
            Text(
                text = "Masa Sasaran: $targetDateFormatted",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Padam", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }

                Button(
                    onClick = onToggleResolved,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (reminder.isResolved) Color(0xFF64748B) else OpsGreen
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = if (reminder.isResolved) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (reminder.isResolved) "Selesai" else "Tanda Selesai", fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    operations: List<String>,
    onDismiss: () -> Unit,
    onSave: (opTitle: String, type: String, hoursAhead: Long, note: String) -> Unit
) {
    var opTitle by remember { mutableStateOf(operations.firstOrNull() ?: "OP BERSEPADU KDN") }
    var reminderType by remember { mutableStateOf("Laporan Awal 24 Jam") }
    var selectedHours by remember { mutableStateOf(24L) }
    var note by remember { mutableStateOf("Hantar flash report kepada Pengarah Penguatkuasa KDN Putrajaya.") }

    var typeExpanded by remember { mutableStateOf(false) }
    val types = listOf(
        "Laporan Awal 24 Jam (Flash Report)",
        "Tamat Tempoh Reman (Seksyen 117 KPJ)",
        "Serahan Eksibit ke Stor Rampasan",
        "Rakaman Percakapan Sasaran",
        "Mesyuarat Post-Mortem Operasi"
    )

    val hourOptions = listOf(1L, 6L, 12L, 24L, 48L, 72L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jadualkan Peringatan SOP Operasi", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = opTitle,
                    onValueChange = { opTitle = it },
                    label = { Text("Nama Operasi Berkaitan") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = reminderType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Peringatan Automatik") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t, fontSize = 12.sp) },
                                onClick = {
                                    reminderType = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Tempoh Peringatan Penggera:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    hourOptions.forEach { h ->
                        val isSel = selectedHours == h
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) KdnNavy else Color(0xFFF1F5F9))
                                .border(1.dp, if (isSel) KdnGold else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .clickable { selectedHours = h }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${h}J",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) KdnGold else Color(0xFF334155)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Arahan / Tindakan Ringkas") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(opTitle, reminderType, selectedHours, note) },
                colors = ButtonDefaults.buttonColors(containerColor = KdnGold, contentColor = KdnNavyDark)
            ) {
                Text("Jadualkan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
