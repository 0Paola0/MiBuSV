package com.example.mibusv

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.example.mibusv.utils.CrashHandler

class MiBuSVApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Inicializar Firebase con manejo de errores
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("MiBuSVApplication", "Firebase inicializado correctamente")
                
                // Habilitar persistencia offline para Firebase Database
                try {
                    FirebaseDatabase.getInstance().setPersistenceEnabled(true)
                    Log.d("MiBuSVApplication", "Persistencia offline habilitada")
                } catch (e: Exception) {
                    Log.e("MiBuSVApplication", "Error al habilitar persistencia: ${e.message}")
                    // No es crítico, la app puede funcionar sin persistencia
                }
            } else {
                Log.d("MiBuSVApplication", "Firebase ya estaba inicializado")
            }
            
        } catch (e: Exception) {
            Log.e("MiBuSVApplication", "Error crítico en onCreate: ${e.message}")
            CrashHandler.handleException(this, e, "MiBuSVApplication")
        }
    }
}
