package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OperationEntity
import com.example.data.repository.OpsRepository
import com.example.export.FotogridJpegExporter
import com.example.ui.theme.KdnGold
import com.example.ui.theme.KdnNavy
import com.example.ui.theme.KdnNavyDark
import com.example.ui.theme.OpsRed
import kotlinx.coroutines.launch

@Composable
fun OperationsListScreen(
    repository: OpsRepository,
    onSelectOperation: (OperationEntity) -> Unit,
    onNewOperation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val allOps by repository.allOperations.collectAsState(initial = emptyList())
    val filteredOps = if (searchQuery.isBlank()) {
        allOps
    } else {
        allOps.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.referenceNumber.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true)
        }
    }

    var operationToDelete by remember { mutableStateOf<OperationEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewOperation,
                containerColor = KdnGold,
                contentColor = KdnNavyDark,
                modifier = Modifier.testTag("fab_new_operation")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Operasi Baharu")
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Arkib Operasi KDN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${allOps.size} Rekod Operasi & Fotogrid Tersimpan",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari operasi, no rujukan, lokasi...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = KdnNavy)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_operations_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Operations List
            if (filteredOps.isEmpty()) {
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
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "Tiada rekod operasi." else "Tiada padanan carian.",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Button(
                            onClick = onNewOperation,
                            colors = ButtonDefaults.buttonColors(containerColor = KdnNavy)
                        ) {
                            Text("+ Cipta Fotogrid Baharu")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOps, key = { it.id }) { op ->
                        OperationListItemCard(
                            operation = op,
                            onClick = { onSelectOperation(op) },
                            onExportJpeg = {
                                scope.launch {
                                    try {
                                        val res = FotogridJpegExporter.generateAndSaveJpeg(context, op)
                                        shareJpegFile(context, res.contentUri, op.title)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onDelete = { operationToDelete = op }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (operationToDelete != null) {
        val op = operationToDelete!!
        AlertDialog(
            onDismissRequest = { operationToDelete = null },
            title = { Text("Padam Rekod Operasi?") },
            text = { Text("Adakah anda pasti mahu memadam rekod '${op.title}'? Tindakan ini tidak boleh diundur.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteOperation(op)
                            operationToDelete = null
                            Toast.makeText(context, "Operasi dipadam.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OpsRed)
                ) {
                    Text("Padam")
                }
            },
            dismissButton = {
                TextButton(onClick = { operationToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun OperationListItemCard(
    operation: OperationEntity,
    onClick: () -> Unit,
    onExportJpeg: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("operation_card_${operation.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = operation.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KdnNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "No. Fail: ${operation.referenceNumber}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Quad Badge Indicator
                val photosCount = listOfNotNull(operation.imageUri1, operation.imageUri2, operation.imageUri3).size
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (photosCount == 3) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$photosCount / 3 Foto",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (photosCount == 3) Color(0xFF166534) else Color(0xFF92400E)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = KdnGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = operation.location,
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${operation.operationDate} • ${operation.operationTime} • Pegawai: ${operation.officerName}",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Padam", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }

                OutlinedButton(
                    onClick = onExportJpeg,
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JPEG", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = KdnNavy),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buka Grid", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun shareJpegFile(context: Context, uri: android.net.Uri, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Fotogrid: $title")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Kongsi JPEG"))
}
