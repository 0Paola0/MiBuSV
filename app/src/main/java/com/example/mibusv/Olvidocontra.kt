package com.example.mibusv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Olvidocontra : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_olvidocontra)
        
        try {
            inicializarVistas()
            configurarListeners()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la actividad: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun inicializarVistas() {
        try {
            emailInput = findViewById(R.id.emailInput)
            resetButton = findViewById(R.id.resetButton)
            
            // Verificar que todos los elementos se encontraron
            if (emailInput == null || resetButton == null) {
                Toast.makeText(this, "Error al cargar elementos de la interfaz", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun configurarListeners() {
        try {
            resetButton.setOnClickListener {
                restablecerContrasena()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar listeners: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun restablecerContrasena() {
        try {
            val email = emailInput.text.toString().trim()

            // Validaciones
            if (email.isEmpty()) {
                emailInput.error = "El correo electrónico es requerido"
                emailInput.requestFocus()
                return
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Ingrese un email válido"
                emailInput.requestFocus()
                return
            }

            // Deshabilitar botón para evitar múltiples intentos
            resetButton.isEnabled = false
            resetButton.text = "Enviando..."

            // Enviar email de restablecimiento
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Se ha enviado un email de restablecimiento a $email", Toast.LENGTH_LONG).show()
                    
                    // Volver al login
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("Olvidocontra", "Error al enviar email: ${exception.message}")
                    Toast.makeText(this, "Error al enviar email: ${exception.message}", Toast.LENGTH_SHORT).show()
                    restaurarBotonReset()
                }
        } catch (e: Exception) {
            android.util.Log.e("Olvidocontra", "Error crítico en restablecerContrasena: ${e.message}")
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_SHORT).show()
            restaurarBotonReset()
        }
    }

    private fun restaurarBotonReset() {
        try {
            resetButton.isEnabled = true
            resetButton.text = "RESTABLECER"
        } catch (e: Exception) {
            android.util.Log.e("Olvidocontra", "Error al restaurar botón: ${e.message}")
        }
    }
}