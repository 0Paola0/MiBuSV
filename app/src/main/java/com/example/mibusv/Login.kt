package com.example.mibusv

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {
    private lateinit var passwordInput: EditText
    private lateinit var togglePassword: ImageView
    private lateinit var emailInput: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var registerLink: TextView
    private lateinit var guestButtonContainer: LinearLayout
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_login)

            passwordInput = findViewById(R.id.passwordInput)
            togglePassword = findViewById(R.id.togglePassword)
            emailInput = findViewById(R.id.emailInput)
            loginButton = findViewById(R.id.loginButton)
            forgotPassword = findViewById(R.id.forgotPassword)
            registerLink = findViewById(R.id.registerLink)
            guestButtonContainer = findViewById(R.id.guestButtonContainer)
            
            // Verificar que todos los elementos se encontraron
            if (passwordInput == null || togglePassword == null || emailInput == null || 
                loginButton == null || forgotPassword == null || registerLink == null || 
                guestButtonContainer == null) {
                Toast.makeText(this, "Error al cargar elementos de la interfaz", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la actividad: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordInput.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ojo)
            } else {
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.esconder)
            }
            passwordInput.setSelection(passwordInput.text.length)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            iniciarSesion(email, password)
        }

        // Configurar click listener para "¿Olvidaste tu contraseña?"
        forgotPassword.setOnClickListener {
            val intent = Intent(this, Olvidocontra::class.java)
            startActivity(intent)
        }

        // Configurar click listener para "Regístrate"
        registerLink.setOnClickListener {
            mostrarDialogoSeleccionRol()
        }

        // Configurar click listener para "Entrar como Invitado"
        guestButtonContainer.setOnClickListener {
            val intent = Intent(this, PrincipalUsuarios::class.java)
            startActivity(intent)
        }

    }
    private fun iniciarSesion(email: String, password: String) {
        try {
            // Validaciones de campos vacíos
            if (email.isEmpty()) {
                emailInput.error = "El correo electrónico es requerido"
                emailInput.requestFocus()
                return
            }
            
            if (password.isEmpty()) {
                passwordInput.error = "La contraseña es requerida"
                passwordInput.requestFocus()
                return
            }
            
            // Validación de formato de email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Ingrese un email válido (ejemplo@gmail.com)"
                emailInput.requestFocus()
                return
            }
            
            // Validación específica de Gmail
            if (!email.endsWith("@gmail.com")) {
                emailInput.error = "Debe usar una cuenta de Gmail (@gmail.com)"
                emailInput.requestFocus()
                return
            }
            
            // Deshabilitar botón para evitar múltiples intentos
            loginButton.isEnabled = false
            loginButton.text = "Iniciando sesión..."
            
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    try {
                        val uid = authResult.user?.uid
                        if (uid != null) {
                            verificarRolYRedirigir(uid)
                        } else {
                            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
                            restaurarBotonLogin()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Login", "Error en success listener: ${e.message}")
                        Toast.makeText(this, "Error al procesar autenticación: ${e.message}", Toast.LENGTH_SHORT).show()
                        restaurarBotonLogin()
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("Login", "Error de autenticación: ${exception.message}")
                    Toast.makeText(this, "Error de autenticación: ${exception.message}", Toast.LENGTH_SHORT).show()
                    restaurarBotonLogin()
                }
        } catch (e: Exception) {
            android.util.Log.e("Login", "Error crítico en iniciarSesion: ${e.message}")
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_SHORT).show()
            restaurarBotonLogin()
        }
    }
    
    private fun restaurarBotonLogin() {
        try {
            loginButton.isEnabled = true
            loginButton.text = "Iniciar Sesión"
        } catch (e: Exception) {
            android.util.Log.e("Login", "Error al restaurar botón: ${e.message}")
        }
    }

    private fun verificarRolYRedirigir(uid: String) {
        try {
            // Verificar que Firebase esté inicializado
            if (FirebaseApp.getApps(this).isEmpty()) {
                Toast.makeText(this, "Error: Firebase no está inicializado", Toast.LENGTH_SHORT).show()
                restaurarBotonLogin()
                return
            }
            
            val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)

            ref.get().addOnSuccessListener { snapshot ->
                try {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").value?.toString() ?: "Pasajero"
                        android.util.Log.d("Login", "Rol del usuario: $role")
                        
                        when (role) {
                            "admin" -> {
                                val intent = Intent(this, Admin::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            "Pasajero" -> {
                                val intent = Intent(this, PrincipalUsuarios::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            else -> {
                                Toast.makeText(this, "Rol desconocido: $role", Toast.LENGTH_SHORT).show()
                                restaurarBotonLogin()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show()
                        restaurarBotonLogin()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Login", "Error al procesar rol: ${e.message}")
                    Toast.makeText(this, "Error al procesar rol: ${e.message}", Toast.LENGTH_SHORT).show()
                    restaurarBotonLogin()
                }
            }.addOnFailureListener { exception ->
                android.util.Log.e("Login", "Error al obtener rol: ${exception.message}")
                Toast.makeText(this, "Error al obtener rol: ${exception.message}", Toast.LENGTH_SHORT).show()
                restaurarBotonLogin()
            }
        } catch (e: Exception) {
            android.util.Log.e("Login", "Error crítico en verificarRolYRedirigir: ${e.message}")
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_SHORT).show()
            restaurarBotonLogin()
        }
    }

    private fun mostrarDialogoSeleccionRol() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_role_selection, null)
            
            val btnPasajero = dialogView.findViewById<Button>(R.id.btnPasajero)
            val btnConductor = dialogView.findViewById<Button>(R.id.btnConductor)
            val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
            
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()
            
            // Configurar listeners
            btnPasajero.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(this, Registro::class.java)
                startActivity(intent)
            }
            
            btnConductor.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(this, Registroconductor::class.java)
                startActivity(intent)
            }
            
            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}