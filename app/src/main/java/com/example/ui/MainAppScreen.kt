package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.auth.AuthManager
import com.example.data.model.OperationEntity
import com.example.data.repository.OpsRepository
import com.example.ui.components.KdnHeader
import com.example.ui.screens.FotogridEditorScreen
import com.example.ui.screens.OperationsListScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsAndProfileScreen
import com.example.ui.theme.KdnGold
import com.example.ui.theme.KdnNavy
import com.example.ui.theme.KdnNavyDark

@Composable
fun MainAppScreen(
    repository: OpsRepository,
    authManager: AuthManager,
    onLockSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val officer by authManager.officerState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingOperation by remember { mutableStateOf<OperationEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            KdnHeader(
                officerName = officer.fullName,
                onLockClicked = onLockSession
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = KdnNavyDark,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Fotogrid") },
                    label = { Text("Fotogrid") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KdnNavyDark,
                        selectedTextColor = KdnGold,
                        indicatorColor = KdnGold,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_fotogrid")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Operasi") },
                    label = { Text("Operasi") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KdnNavyDark,
                        selectedTextColor = KdnGold,
                        indicatorColor = KdnGold,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_operations")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = "Peringatan") },
                    label = { Text("Peringatan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KdnNavyDark,
                        selectedTextColor = KdnGold,
                        indicatorColor = KdnGold,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_reminders")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil/Awan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KdnNavyDark,
                        selectedTextColor = KdnGold,
                        indicatorColor = KdnGold,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_item_profile")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    FotogridEditorScreen(
                        repository = repository,
                        initialOperation = editingOperation,
                        onNavigateToReminders = { selectedTab = 2 }
                    )
                }
                1 -> {
                    OperationsListScreen(
                        repository = repository,
                        onSelectOperation = { op ->
                            editingOperation = op
                            selectedTab = 0
                        },
                        onNewOperation = {
                            editingOperation = null
                            selectedTab = 0
                        }
                    )
                }
                2 -> {
                    RemindersScreen(repository = repository)
                }
                3 -> {
                    SettingsAndProfileScreen(
                        authManager = authManager,
                        onLogout = onLockSession
                    )
                }
            }
        }
    }
}
