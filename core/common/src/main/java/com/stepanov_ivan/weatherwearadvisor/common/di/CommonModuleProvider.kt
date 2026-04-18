package com.stepanov_ivan.weatherwearadvisor.common.di

import android.content.Context
import com.stepanov_ivan.weatherwearadvisor.common.security.EncryptedPreferencesManager

/**
 * Provider для компонентов core:common модуля
 * Другие модули получают доступ к общим компонентам через этот класс
 */
object CommonModuleProvider {
    private var encryptedPreferencesManager: EncryptedPreferencesManager? = null

    /**
     * Инициализация модуля
     */
    fun initialize(context: Context) {
        encryptedPreferencesManager = EncryptedPreferencesManager(context)
    }

    /**
     * Получить менеджер зашифрованных preferences
     */
    fun getEncryptedPreferencesManager(): EncryptedPreferencesManager {
        return encryptedPreferencesManager 
            ?: throw IllegalStateException("CommonModule not initialized. Call initialize(context) first")
    }
}
