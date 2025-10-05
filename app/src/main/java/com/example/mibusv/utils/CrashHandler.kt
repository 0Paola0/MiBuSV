package com.example.mibusv.utils

import android.content.Context
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

object CrashHandler {
    
    fun handleException(context: Context, throwable: Throwable, tag: String = "CrashHandler") {
        try {
            val stackTrace = StringWriter()
            throwable.printStackTrace(PrintWriter(stackTrace))
            
            Log.e(tag, "Excepción capturada: ${throwable.message}")
            Log.e(tag, "Stack trace: $stackTrace")
            
            // En un entorno de producción, aquí podrías enviar el crash a un servicio como Crashlytics
            // FirebaseCrashlytics.getInstance().recordException(throwable)
            
        } catch (e: Exception) {
            Log.e("CrashHandler", "Error al manejar excepción: ${e.message}")
        }
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        } catch (e: Exception) {
            Log.e("CrashHandler", "Error al registrar log: ${e.message}")
        }
    }
    
    fun logWarning(tag: String, message: String) {
        try {
            Log.w(tag, message)
        } catch (e: Exception) {
            Log.e("CrashHandler", "Error al registrar warning: ${e.message}")
        }
    }
    
    fun logInfo(tag: String, message: String) {
        try {
            Log.i(tag, message)
        } catch (e: Exception) {
            Log.e("CrashHandler", "Error al registrar info: ${e.message}")
        }
    }
}
