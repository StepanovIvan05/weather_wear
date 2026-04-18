package com.stepanov_ivan.weatherwearadvisor.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Управление зашифрованными чувствительными данными.
 * Использует AES-256 для шифрования SharedPreferences
 */
class EncryptedPreferencesManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserId(userId: String) {
        encryptedPrefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? = encryptedPrefs.getString(KEY_USER_ID, null)

    fun saveUserEmail(email: String) {
        encryptedPrefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? = encryptedPrefs.getString(KEY_USER_EMAIL, null)

    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "weather_wear_encrypted_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
    }
}
