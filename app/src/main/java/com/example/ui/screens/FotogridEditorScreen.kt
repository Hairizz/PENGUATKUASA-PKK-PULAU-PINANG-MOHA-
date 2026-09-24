package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.OperationEntity
import com.example.data.model.ReminderEntity
import com.example.data.repository.OpsRepository
import com.example.export.ExportResult
import com.example.export.FotogridJpegExporter
import com.example.reminder.ReminderScheduler
import com.example.ui.components.FotogridSquareView
import com.example.ui.theme.KdnGold
import com.example.ui.theme.KdnNavy
import com.example.ui.theme.KdnNavyDark
import com.example.ui.theme.OpsGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotogridEditorScreen(
    repository: OpsRepository,
    initialOperation: OperationEntity? = null,
    onOperationSaved: (OperationEntity) -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form states
    var opId by remember { mutableStateOf(initialOperation?.id ?: 0L) }
    var title by remember { mutableStateOf(initialOperation?.title ?: "OP BERSEPADU KDN / KAWALAN") }
    var refNo by remember { mutableStateOf(initialOperation?.referenceNumber ?: "KDN/BPK/SEL/2026/014") }
    var opDate by remember {
        mutableStateOf(
            initialOperation?.operationDate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        )
    }
    var opTime by remember {
        mutableStateOf(
            initialOperation?.operationTime ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
    }
    var location by remember { mutableStateOf(initialOperation?.location ?: "Kawasan Perindustrian Bukit Raja, Klang") }
    var officerName by remember { mutableStateOf(initialOperation?.officerName ?: "PPK Mohd Ridzuan (Ketua Pasukan)") }
    var agencyName by remember { mutableStateOf(initialOperation?.agencyName ?: "Bahagian Penguatkuasaan & Kawalan KDN") }
    var actCategory by remember { mutableStateOf(initialOperation?.actCategory ?: "Akta Mesin Cetak dan Penerbitan 1984 [Akta 301]") }
    var suspectsCaught by remember { mutableStateOf(initialOperation?.suspectsCaught ?: "6 Lelaki (Pekerja Asing & Penyelia)") }
    var seizedItemsSummary by remember { mutableStateOf(initialOperation?.seizedItemsSummary ?: "420 Naskhah Penerbitan & 2 Mesin Cetak") }

    // Photos
    var imageUri1 by remember { mutableStateOf(initialOperation?.imageUri1) }
    var imageLabel1 by remember { mutableStateOf(initialOperation?.imageLabel1 ?: "1. SASARAN PREMIS OPERASI") }

    var imageUri2 by remember { mutableStateOf(initialOperation?.imageUri2) }
    var imageLabel2 by remember { mutableStateOf(initialOperation?.imageLabel2 ?: "2. PEMERIKSAAN & TANGKAPAN") }

    var imageUri3 by remember { mutableStateOf(initialOperation?.imageUri3) }
    var imageLabel3 by remember { mutableStateOf(initialOperation?.imageLabel3 ?: "3. EKSIBIT BARANG RAMPASAN") }

    var isExporting by remember { mutableStateOf(false) }
    var exportResultDialog by remember { mutableStateOf<ExportResult?>(null) }
    var showQuickReminderDialog by remember { mutableStateOf(false) }
    var activeSlotForPicker by remember { mutableIntStateOf(2) }

    // Act Categories Dropdown
    val actOptions = listOf(
        "Akta Mesin Cetak dan Penerbitan 1984 [Akta 301]",
        "Akta Imigresen 1959/63 [Akta 155]",
        "Akta Pasport 1966 [Akta 150]",
        "Akta Pertubuhan 1966 [Akta 335]",
        "Akta Penapisan Filem 2002 [Akta 620]",
        "Akta Dadah Berbahaya 1952 [Akta 234]",
        "Akta Pencegahan Jenayah 1959 (POCA)"
    )
    var actExpanded by remember { mutableStateOf(false) }

    // Current transient OperationEntity for live preview
    val currentOperation = OperationEntity(
        id = opId,
        title = title,
        referenceNumber = refNo,
        operationDate = opDate,
        operationTime = opTime,
        location = location,
        officerName = officerName,
        agencyName = agencyName,
        actCategory = actCategory,
        suspectsCaught = suspectsCaught,
        seizedItemsSummary = seizedItemsSummary,
        imageUri1 = imageUri1,
        imageLabel1 = imageLabel1,
        imageUri2 = imageUri2,
        imageLabel2 = imageLabel2,
        imageUri3 = imageUri3,
        imageLabel3 = imageLabel3
    )

    // Image Picker launcher (Zero-permission Android Photo Picker compliant with Google Play)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            when (activeSlotForPicker) {
                2 -> imageUri1 = uri.toString()
                3 -> imageUri2 = uri.toString()
                4 -> imageUri3 = uri.toString()
            }
        }
    }

    val openPickerForSlot = { slot: Int ->
        activeSlotForPicker = slot
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val saveCurrentOperation = suspend {
        val savedId = repository.saveOperation(currentOperation)
        opId = savedId
        opId
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner / Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Editor Fotogrid Operasi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Format Rasmi 4-Grid Segi Empat Sama (1:1)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Button(
                onClick = { showQuickReminderDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = KdnGold, contentColor = KdnNavyDark),
                modifier = Modifier.testTag("btn_quick_reminder")
            ) {
                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Peringatan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live 4-Grid Preview Card (Square 1:1)
        Text(
            text = "PRATONTON FOTOGRID 1:1 (SEGI EMPAT SAMA):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )

        FotogridSquareView(
            operation = currentOperation,
            onSlotClicked = { slot -> openPickerForSlot(slot) },
            onRemoveImage = { slot ->
                when (slot) {
                    2 -> imageUri1 = null
                    3 -> imageUri2 = null
                    4 -> imageUri3 = null
                }
            }
        )

        // Primary Export Button: SAVE AS JPEG
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = KdnNavy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tindakan Eksport Dokumen Rasmi",
                    color = KdnGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Jana composite 4-grid saiz segi empat sama resolusi tinggi dalam format JPEG untuk simpanan arkib atau penyampaian laporan operasi.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                try {
                                    saveCurrentOperation()
                                    val result = FotogridJpegExporter.generateAndSaveJpeg(context, currentOperation)
                                    exportResultDialog = result
                                    Toast.makeText(context, "Fotogrid berjaya disimpan sebagai JPEG!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Ralat menjana JPEG: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KdnGold, contentColor = KdnNavyDark),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_as_jpeg_button"),
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = KdnNavyDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Menjana JPEG...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE AS JPEG", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                saveCurrentOperation()
                                Toast.makeText(context, "Draf operasi disimpan!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.testTag("save_draft_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", fontSize = 13.sp)
                    }
                }
            }
        }

        // Section: Grid 1 - Maklumat Operasi Inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(KdnNavy)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("GRID 1", color = KdnGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Maklumat & Butir-Butir Operasi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama / Kod Operasi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_operation_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    label = { Text("No. Rujukan Kes / Fail") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_reference_number"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = opDate,
                        onValueChange = { opDate = it },
                        label = { Text("Tarikh") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = opTime,
                        onValueChange = { opTime = it },
                        label = { Text("Masa (24J)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi Serbuan / Premis Sasaran") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_location")
                )

                OutlinedTextField(
                    value = officerName,
                    onValueChange = { officerName = it },
                    label = { Text("Ketua Pasukan / Pegawai Serbuan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_officer_name"),
                    singleLine = true
                )

                // Dropdown for Act Category
                ExposedDropdownMenuBox(
                    expanded = actExpanded,
                    onExpandedChange = { actExpanded = !actExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = actCategory,
                        onValueChange = { actCategory = it },
                        label = { Text("Kategori Akta / Peruntukan Undang-Undang") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = actExpanded,
                        onDismissRequest = { actExpanded = false }
                    ) {
                        actOptions.forEach { act ->
                            DropdownMenuItem(
                                text = { Text(act, fontSize = 13.sp) },
                                onClick = {
                                    actCategory = act
                                    actExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = suspectsCaught,
                    onValueChange = { suspectsCaught = it },
                    label = { Text("Tangkapan / Sasaran Diperiksa") },
                    placeholder = { Text("Contoh: 6 Lelaki Warga Asing") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = seizedItemsSummary,
                    onValueChange = { seizedItemsSummary = it },
                    label = { Text("Ringkasan Rampasan / Eksibit") },
                    placeholder = { Text("Contoh: 420 Unit Penerbitan & Mesin") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section: Grid 2, 3, 4 - Foto Operasi Setup
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pengurusan Foto Operasi (Grid 2 hingga 4)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pilih gambar dari galeri atau kamera untuk setiap slot grid dan tetapkan label rujukan rasmi.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                // Grid 2 Slot Controls
                PhotoSlotRow(
                    slotNumber = 2,
                    currentLabel = imageLabel1,
                    onLabelChange = { imageLabel1 = it },
                    hasImage = !imageUri1.isNullOrBlank(),
                    onSelectImage = { openPickerForSlot(2) },
                    onClearImage = { imageUri1 = null }
                )

                // Grid 3 Slot Controls
                PhotoSlotRow(
                    slotNumber = 3,
                    currentLabel = imageLabel2,
                    onLabelChange = { imageLabel2 = it },
                    hasImage = !imageUri2.isNullOrBlank(),
                    onSelectImage = { openPickerForSlot(3) },
                    onClearImage = { imageUri2 = null }
                )

                // Grid 4 Slot Controls
                PhotoSlotRow(
                    slotNumber = 4,
                    currentLabel = imageLabel3,
                    onLabelChange = { imageLabel3 = it },
                    hasImage = !imageUri3.isNullOrBlank(),
                    onSelectImage = { openPickerForSlot(4) },
                    onClearImage = { imageUri3 = null }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Export Result Dialog
    if (exportResultDialog != null) {
        val res = exportResultDialog!!
        AlertDialog(
            onDismissRequest = { exportResultDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = OpsGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Fotogrid Berjaya Dijana!", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Fail fotogrid 4-grid segi empat sama (1:1 JPEG) telah berjaya disimpan ke peranti:")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = res.file.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KdnNavy
                        )
                    }
                    Text(
                        text = "Saiz: ${res.width} x ${res.height} px • Format: JPEG Kualiti Tinggi",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareJpeg(context, res.contentUri, title)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KdnNavy)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kongsi JPEG")
                }
            },
            dismissButton = {
                TextButton(onClick = { exportResultDialog = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Quick Reminder Dialog
    if (showQuickReminderDialog) {
        QuickReminderModal(
            operationTitle = title,
            onDismiss = { showQuickReminderDialog = false },
            onAddReminder = { remType, hoursAhead, note ->
                scope.launch {
                    val savedOpId = saveCurrentOperation()
                    val targetTime = System.currentTimeMillis() + (hoursAhead * 3600 * 1000L)
                    val reminder = ReminderEntity(
                        operationId = savedOpId,
                        operationTitle = title,
                        reminderType = remType,
                        note = note,
                        targetTimestamp = targetTime
                    )
                    val remId = repository.saveReminder(reminder)
                    ReminderScheduler.scheduleReminder(context, reminder.copy(id = remId))
                    Toast.makeText(context, "Peringatan $remType ($hoursAhead jam) dijadualkan!", Toast.LENGTH_SHORT).show()
                    showQuickReminderDialog = false
                }
            }
        )
    }
}

@Composable
private fun PhotoSlotRow(
    slotNumber: Int,
    currentLabel: String,
    onLabelChange: (String) -> Unit,
    hasImage: Boolean,
    onSelectImage: () -> Unit,
    onClearImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(KdnGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("GRID $slotNumber", color = KdnNavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasImage) "Foto Terpilih" else "Tiada Foto",
                    fontSize = 12.sp,
                    color = if (hasImage) OpsGreen else Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onSelectImage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasImage) KdnNavy else KdnGold,
                        contentColor = if (hasImage) Color.White else KdnNavyDark
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (hasImage) "Tukar" else "Pilih", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (hasImage) {
                    OutlinedButton(
                        onClick = onClearImage,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Buang", fontSize = 11.sp, color = Color.Red)
                    }
                }
            }
        }

        OutlinedTextField(
            value = currentLabel,
            onValueChange = onLabelChange,
            label = { Text("Label Grid $slotNumber (Dipaparkan dalam JPEG)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun QuickReminderModal(
    operationTitle: String,
    onDismiss: () -> Unit,
    onAddReminder: (type: String, hours: Long, note: String) -> Unit
) {
    var selectedPreset by remember { mutableStateOf("Laporan Awal 24 Jam") }
    var hoursAhead by remember { mutableStateOf(24L) }
    var customNote by remember { mutableStateOf("Hantar flash report kepada Pengarah Penguatkuasa KDN.") }

    val presets = listOf(
        Triple("Laporan Awal 24 Jam", 24L, "Hantar Laporan Kilat 24 Jam kepada Setiausaha Bahagian KDN Putrajaya."),
        Triple("Tamat Tempoh Reman (Sek. 117)", 24L, "Permohonan sambung reman atau pertuduhan di mahkamah sebelum tamat tempoh."),
        Triple("Serahan Eksibit ke Stor", 48L, "Daftar dan serahkan semua barang rampasan kes ke Stor Eksibit berpusat."),
        Triple("Mesyuarat Post-Mortem", 72L, "Sesi bedah siasat operasi bersama semua agensi terlibat.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = KdnGold, modifier = Modifier.size(32.dp))
        },
        title = {
            Text("Set Peringatan Automatik SOP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tetapkan penggera peringatan automatik bagi mematuhi tempoh masa tindakan undang-undang:",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )

                presets.forEach { (type, hours, defaultNote) ->
                    val isSelected = selectedPreset == type
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFEBF3FF) else Color(0xFFF8FAFC))
                            .border(
                                1.dp,
                                if (isSelected) KdnNavy else Color(0xFFCBD5E1),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) KdnNavy else Color(0xFF0F172A)
                                )
                                Button(
                                    onClick = {
                                        selectedPreset = type
                                        hoursAhead = hours
                                        customNote = defaultNote
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) KdnNavy else Color(0xFFE2E8F0),
                                        contentColor = if (isSelected) Color.White else Color(0xFF334155)
                                    ),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(if (isSelected) "Dipilih" else "Pilih", fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = "Tempoh: $hours Jam",
                                fontSize = 11.sp,
                                color = KdnGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text("Nota Tindakan") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddReminder(selectedPreset, hoursAhead, customNote) },
                colors = ButtonDefaults.buttonColors(containerColor = KdnGold, contentColor = KdnNavyDark)
            ) {
                Text("Jadualkan Peringatan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun shareJpeg(context: Context, uri: Uri, title: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Fotogrid Operasi KDN: $title")
        putExtra(Intent.EXTRA_TEXT, "Berikut dilampirkan Laporan Fotogrid Rasmi Operasi Kementerian Dalam Negeri (KDN):\n$title")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Kongsi Fotogrid JPEG"))
}
