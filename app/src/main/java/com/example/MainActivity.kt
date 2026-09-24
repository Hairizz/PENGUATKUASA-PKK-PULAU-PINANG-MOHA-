package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.repository.OpsRepository
import com.example.ui.MainAppScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.KdnOpsTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var repository: OpsRepository
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getDatabase(applicationContext)
        repository = OpsRepository(database.operationDao(), database.reminderDao())
        authManager = AuthManager(applicationContext)

        setContent {
            KdnOpsTheme {
                val officerState by authManager.officerState.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (officerState.isLoggedIn) {
                        MainAppScreen(
                            repository = repository,
                            authManager = authManager,
                            onLockSession = {
                                authManager.logout()
                            }
                        )
                    } else {
                        AuthScreen(
                            authManager = authManager,
                            onLoginSuccess = {
                                // Handled via state flow
                            }
                        )
                    }
                }
            }
        }
    }
}
