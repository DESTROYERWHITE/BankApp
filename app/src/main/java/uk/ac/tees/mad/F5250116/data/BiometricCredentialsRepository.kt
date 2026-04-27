package uk.ac.tees.mad.F5250116.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

data class BiometricCredentials(
    val email: String,
    val pin: String
)

class BiometricCredentialsRepository(context: Context) {

    private val sharedPreferences = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(email: String, pin: String) {
        sharedPreferences.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PIN, pin)
            .apply()
    }

    fun getCredentials(): BiometricCredentials? {
        val email = sharedPreferences.getString(KEY_EMAIL, null)
        val pin = sharedPreferences.getString(KEY_PIN, null)
        return if (email.isNullOrBlank() || pin.isNullOrBlank()) {
            null
        } else {
            BiometricCredentials(email = email, pin = pin)
        }
    }

    fun enrolledEmail(): String? = sharedPreferences.getString(KEY_EMAIL, null)

    fun hasCredentials(): Boolean = getCredentials() != null

    fun clearCredentials() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "biometric_credentials"
        private const val KEY_EMAIL = "email"
        private const val KEY_PIN = "pin"
    }
}
