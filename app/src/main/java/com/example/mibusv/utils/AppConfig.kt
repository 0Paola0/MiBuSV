package com.example.mibusv.utils

import android.content.Context
import android.util.Log

object AppConfig {
    
    // Configuración de logging
    const val DEBUG_MODE = true
    const val LOG_TAG = "MiBuSV"
    
    // Configuración de Firebase
    const val FIREBASE_TIMEOUT_MS = 10000L
    const val MAX_RETRY_ATTEMPTS = 3
    
    // Configuración de UI
    const val TOAST_DURATION_SHORT = 2000
    const val TOAST_DURATION_LONG = 4000
    
    /**
     * Configura el logging de la aplicación
     */
    fun setupLogging() {
        if (DEBUG_MODE) {
            Log.d(LOG_TAG, "Modo debug activado")
        }
    }
    
    /**
     * Verifica si la aplicación está en modo debug
     */
    fun isDebugMode(): Boolean {
        return DEBUG_MODE
    }
    
    /**
     * Obtiene el tag de logging estándar
     */
    fun getLogTag(): String {
        return LOG_TAG
    }
    
    /**
     * Log seguro que solo funciona en modo debug
     */
    fun debugLog(message: String) {
        if (DEBUG_MODE) {
            Log.d(LOG_TAG, message)
        }
    }
    
    /**
     * Log de error que siempre funciona
     */
    fun errorLog(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(LOG_TAG, message, throwable)
        } else {
            Log.e(LOG_TAG, message)
        }
    }
}
