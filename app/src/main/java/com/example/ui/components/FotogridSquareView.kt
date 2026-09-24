package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.OperationEntity
import com.example.ui.theme.BorderLight
import com.example.ui.theme.KdnGold
import com.example.ui.theme.KdnNavy
import com.example.ui.theme.KdnNavyDark
import com.example.ui.theme.OpsRed

@Composable
fun FotogridSquareView(
    operation: OperationEntity,
    onSlotClicked: (slotNumber: Int) -> Unit = {},
    onRemoveImage: (slotNumber: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("fotogrid_square_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KdnNavyDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "KEMENTERIAN DALAM NEGERI MALAYSIA",
                        color = KdnGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LAPORAN FOTOGRID OPERASI (4-GRID 1:1)",
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(KdnGold)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "1:1 SQUARE",
                        color = KdnNavyDark,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4 Grid layout (2 rows x 2 columns)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(KdnNavy)
                    .padding(2.dp)
            ) {
                // Top Row (Grid 1: Maklumat Operasi & Grid 2: Foto 1)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Grid 1 (Maklumat Operasi)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                    ) {
                        Grid1OperationDetailsCell(operation = operation)
                    }

                    // Grid 2 (Foto 1 - Sasaran / Premis)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                    ) {
                        GridPhotoCell(
                            slotNumber = 2,
                            imageUri = operation.imageUri1,
                            label = operation.imageLabel1.ifBlank { "1. SASARAN PREMIS" },
                            onClick = { onSlotClicked(2) },
                            onRemove = { onRemoveImage(2) }
                        )
                    }
                }

                // Bottom Row (Grid 3: Foto 2 & Grid 4: Foto 3)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Grid 3 (Foto 2 - Tindakan / Tangkapan)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                    ) {
                        GridPhotoCell(
                            slotNumber = 3,
                            imageUri = operation.imageUri2,
                            label = operation.imageLabel2.ifBlank { "2. PEMERIKSAAN" },
                            onClick = { onSlotClicked(3) },
                            onRemove = { onRemoveImage(3) }
                        )
                    }

                    // Grid 4 (Foto 3 - Eksibit / Rampasan)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                    ) {
                        GridPhotoCell(
                            slotNumber = 4,
                            imageUri = operation.imageUri3,
                            label = operation.imageLabel3.ifBlank { "3. EKSIBIT RAMPASAN" },
                            onClick = { onSlotClicked(4) },
                            onRemove = { onRemoveImage(4) }
                        )
                    }
                }
            }

            // Footer bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SULIT • BAHAGIAN PENGUATKUASAAN KDN",
                    color = Color(0xFF94A3B8),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PEG: ${operation.officerName.take(18)}",
                    color = Color(0xFFCBD5E1),
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
private fun Grid1OperationDetailsCell(
    operation: OperationEntity,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, BorderLight, RoundedCornerShape(4.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Cell Header Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KdnNavy)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = KdnGold,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GRID 1: MAKLUMAT OPERASI",
                    color = KdnGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Content List with vertical scroll if cramped
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                OpDetailField(label = "OPERASI", value = operation.title.ifBlank { "OP KDN" }, isBold = true)
                OpDetailField(label = "NO. KES", value = operation.referenceNumber.ifBlank { "-" })
                OpDetailField(label = "TARIKH/MASA", value = "${operation.operationDate} ${operation.operationTime}")
                OpDetailField(label = "LOKASI", value = operation.location.ifBlank { "-" })
                OpDetailField(label = "KETUA OPS", value = operation.officerName.ifBlank { "-" })
                OpDetailField(label = "AKTA", value = operation.actCategory.ifBlank { "-" })

                if (operation.suspectsCaught.isNotBlank()) {
                    OpDetailField(label = "TANGKAPAN", value = operation.suspectsCaught, isBold = true)
                }
                if (operation.seizedItemsSummary.isNotBlank()) {
                    OpDetailField(label = "RAMPASAN", value = operation.seizedItemsSummary, isBold = true)
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Stamp badge
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .border(1.dp, OpsRed, RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "KDN • DISAHKAN",
                        color = OpsRed,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun OpDetailField(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            lineHeight = 9.sp
        )
        Text(
            text = value,
            fontSize = 8.5.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF0F172A),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 10.sp
        )
    }
}

@Composable
private fun GridPhotoCell(
    slotNumber: Int,
    imageUri: String?,
    label: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .testTag("grid_${slotNumber}_cell"),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(imageUri))
                    .crossfade(true)
                    .build(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Remove Button Top-Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC000000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Padam Gambar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else {
            // Empty placeholder with action prompt
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = KdnGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "+ Pilih Gambar",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kamera / Galeri",
                    color = Color(0xFF94A3B8),
                    fontSize = 7.5.sp
                )
            }
        }

        // Slot Badge Top-Left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(KdnGold)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "GRID $slotNumber",
                color = KdnNavyDark,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Label Ribbon Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xCC0A192F))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
