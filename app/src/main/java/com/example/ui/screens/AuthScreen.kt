package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.ui.theme.KdnGold
import com.example.ui.theme.KdnNavy
import com.example.ui.theme.KdnNavyDark

@Composable
fun AuthScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var officerId by remember { mutableStateOf("KDN/OPS/2026/894") }
    var pin by remember { mutableStateOf("1234") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KdnNavyDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Official Emblem Badge
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(KdnNavy),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Lencana KDN",
                tint = KdnGold,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "KEMENTERIAN DALAM NEGERI",
            color = KdnGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Text(
            text = "SISTEM FOTOGRID OPERASI",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Pengesahan Selamat Anggota Penguatkuasa",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Login Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Log Masuk Sesi Bertugas",
                    color = KdnNavyDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = officerId,
                    onValueChange = {
                        officerId = it
                        errorMessage = null
                    },
                    label = { Text("No. Kad Kuasa / No. Badan") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = KdnNavy)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_officer_id"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        errorMessage = null
                    },
                    label = { Text("PIN Keselamatan / Kata Laluan") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = KdnNavy)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_pin"),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (authManager.login(officerId, pin)) {
                            Toast.makeText(context, "Sesi disahkan! Selamat bertugas.", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            errorMessage = "PIN tidak sah. Sila cuba lagi atau gunakan PIN lalai 1234."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KdnNavy),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_auth_submit")
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = KdnGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PENGESAHAN MASUK", fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Quick Demo Login helper
                OutlinedButton(
                    onClick = {
                        officerId = "KDN/OPS/2026/894"
                        pin = "1234"
                        authManager.login(officerId, pin)
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Akses Pantas (Pegawai Bertugas)", fontSize = 12.sp, color = KdnNavy)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DOKUMEN & SISTEM SULIT KERAJAAN MALAYSIA",
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
