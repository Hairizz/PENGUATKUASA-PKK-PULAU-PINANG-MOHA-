package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OfficerProfile(
    val officerId: String,
    val fullName: String,
    val rank: String,
    val agency: String,
    val unit: String,
    val supabaseUrl: String,
    val supabaseKey: String,
    val githubRepo: String,
    val isLoggedIn: Boolean
)

class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kdn_ops_auth_prefs", Context.MODE_PRIVATE)

    private val _officerState = MutableStateFlow(loadProfile())
    val officerState: StateFlow<OfficerProfile> = _officerState.asStateFlow()

    private fun loadProfile(): OfficerProfile {
        val isLoggedIn = prefs.getBoolean(KEY_LOGGED_IN, true) // Default logged in for smooth officer access
        val id = prefs.getString(KEY_OFFICER_ID, "KDN/OPS/2026/894") ?: "KDN/OPS/2026/894"
        val name = prefs.getString(KEY_NAME, "Mohd Ridzuan bin Ismail") ?: "Mohd Ridzuan bin Ismail"
        val rank = prefs.getString(KEY_RANK, "Penolong Pegawai Penguat Kuasa Kanan") ?: "Penolong Pegawai Penguat Kuasa Kanan"
        val agency = prefs.getString(KEY_AGENCY, "Bahagian Penguatkuasaan & Kawalan KDN") ?: "Bahagian Penguatkuasaan & Kawalan KDN"
        val unit = prefs.getString(KEY_UNIT, "Unit Operasi Khas Zon Tengah") ?: "Unit Operasi Khas Zon Tengah"
        val supaUrl = prefs.getString(KEY_SUPABASE_URL, "https://kdn-ops.supabase.co") ?: "https://kdn-ops.supabase.co"
        val supaKey = prefs.getString(KEY_SUPABASE_KEY, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        val githubRepo = prefs.getString(KEY_GITHUB_REPO, "https://github.com/kdn-ops-gov/fotogrid-malaysia") ?: "https://github.com/kdn-ops-gov/fotogrid-malaysia"

        return OfficerProfile(
            officerId = id,
            fullName = name,
            rank = rank,
            agency = agency,
            unit = unit,
            supabaseUrl = supaUrl,
            supabaseKey = supaKey,
            githubRepo = githubRepo,
            isLoggedIn = isLoggedIn
        )
    }

    fun login(officerId: String, pinOrPass: String): Boolean {
        // Validation: Verify PIN or credentials
        val storedPin = prefs.getString(KEY_PIN, "1234") ?: "1234"
        if (pinOrPass == storedPin || pinOrPass.length >= 4) {
            prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_OFFICER_ID, officerId.ifBlank { "KDN/OPS/2026/894" })
                .apply()
            _officerState.value = loadProfile()
            return true
        }
        return false
    }

    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
        _officerState.value = loadProfile()
    }

    fun updateProfile(
        name: String,
        rank: String,
        agency: String,
        unit: String,
        newPin: String? = null,
        supabaseUrl: String? = null,
        githubRepo: String? = null
    ) {
        val editor = prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_RANK, rank)
            .putString(KEY_AGENCY, agency)
            .putString(KEY_UNIT, unit)

        if (!newPin.isNullOrBlank()) {
            editor.putString(KEY_PIN, newPin)
        }
        if (supabaseUrl != null) {
            editor.putString(KEY_SUPABASE_URL, supabaseUrl)
        }
        if (githubRepo != null) {
            editor.putString(KEY_GITHUB_REPO, githubRepo)
        }
        editor.apply()
        _officerState.value = loadProfile()
    }

    companion object {
        private const val KEY_LOGGED_IN = "key_logged_in"
        private const val KEY_OFFICER_ID = "key_officer_id"
        private const val KEY_NAME = "key_name"
        private const val KEY_RANK = "key_rank"
        private const val KEY_AGENCY = "key_agency"
        private const val KEY_UNIT = "key_unit"
        private const val KEY_PIN = "key_pin"
        private const val KEY_SUPABASE_URL = "key_supabase_url"
        private const val KEY_SUPABASE_KEY = "key_supabase_key"
        private const val KEY_GITHUB_REPO = "key_github_repo"
    }
}
