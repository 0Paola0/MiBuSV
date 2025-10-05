package com.example.mibusv

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class Registro : AppCompatActivity() {
    private lateinit var nombreInput: EditText
    private lateinit var apellidoInput: EditText
    private lateinit var telefonoInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var togglePassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView
    private lateinit var termsCheckbox: CheckBox
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        
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
            nombreInput = findViewById(R.id.nombreInput)
            apellidoInput = findViewById(R.id.apellidoInput)
            telefonoInput = findViewById(R.id.telefonoInput)
            emailInput = findViewById(R.id.emailInput)
            passwordInput = findViewById(R.id.passwordInput)
            confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
            togglePassword = findViewById(R.id.togglePassword)
            toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword)
            termsCheckbox = findViewById(R.id.termsCheckbox)
            registerButton = findViewById(R.id.registerButton)
            loginLink = findViewById(R.id.loginLink)
            
            // Verificar que todos los elementos se encontraron
            if (nombreInput == null || apellidoInput == null || telefonoInput == null ||
                emailInput == null || passwordInput == null || confirmPasswordInput == null ||
                togglePassword == null || toggleConfirmPassword == null || termsCheckbox == null ||
                registerButton == null || loginLink == null) {
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
            // Toggle password visibility
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

            // Toggle confirm password visibility
            toggleConfirmPassword.setOnClickListener {
                isConfirmPasswordVisible = !isConfirmPasswordVisible
                if (isConfirmPasswordVisible) {
                    confirmPasswordInput.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    toggleConfirmPassword.setImageResource(R.drawable.ojo)
                } else {
                    confirmPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    toggleConfirmPassword.setImageResource(R.drawable.esconder)
                }
                confirmPasswordInput.setSelection(confirmPasswordInput.text.length)
            }

            // Register button
            registerButton.setOnClickListener {
                registrarUsuario()
            }

            // Login link
            loginLink.setOnClickListener {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar listeners: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun registrarUsuario() {
        try {
            val nombre = nombreInput.text.toString().trim()
            val apellido = apellidoInput.text.toString().trim()
            val telefono = telefonoInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Validaciones de campos vacíos
            if (nombre.isEmpty()) {
                nombreInput.error = "El nombre es requerido"
                nombreInput.requestFocus()
                return
            }

            if (apellido.isEmpty()) {
                apellidoInput.error = "El apellido es requerido"
                apellidoInput.requestFocus()
                return
            }

            if (telefono.isEmpty()) {
                telefonoInput.error = "El teléfono es requerido"
                telefonoInput.requestFocus()
                return
            }

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

            if (confirmPassword.isEmpty()) {
                confirmPasswordInput.error = "Confirme su contraseña"
                confirmPasswordInput.requestFocus()
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

            // Validación de formato de teléfono (0000-0000)
            val telefonoPattern = Regex("^\\d{4}-\\d{4}$")
            if (!telefonoPattern.matches(telefono)) {
                telefonoInput.error = "Formato de teléfono inválido. Use: 0000-0000"
                telefonoInput.requestFocus()
                return
            }

            // Validación de longitud de contraseña
            if (password.length < 6) {
                passwordInput.error = "La contraseña debe tener al menos 6 caracteres"
                passwordInput.requestFocus()
                return
            }

            // Validación de coincidencia de contraseñas
            if (password != confirmPassword) {
                confirmPasswordInput.error = "Las contraseñas no coinciden"
                confirmPasswordInput.requestFocus()
                return
            }

            if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return
            }

            // Deshabilitar botón para evitar múltiples intentos
            registerButton.isEnabled = false
            registerButton.text = "Registrando..."

            // Crear usuario en Firebase Auth
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    try {
                        val uid = authResult.user?.uid
                        if (uid != null) {
                            guardarUsuarioEnBaseDatos(uid, nombre, apellido, telefono, email)
                        } else {
                            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
                            restaurarBotonRegistro()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Registro", "Error en success listener: ${e.message}")
                        Toast.makeText(this, "Error al procesar registro: ${e.message}", Toast.LENGTH_SHORT).show()
                        restaurarBotonRegistro()
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("Registro", "Error de registro: ${exception.message}")
                    Toast.makeText(this, "Error de registro: ${exception.message}", Toast.LENGTH_SHORT).show()
                    restaurarBotonRegistro()
                }
        } catch (e: Exception) {
            android.util.Log.e("Registro", "Error crítico en registrarUsuario: ${e.message}")
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_SHORT).show()
            restaurarBotonRegistro()
        }
    }

    private fun guardarUsuarioEnBaseDatos(uid: String, nombre: String, apellido: String, telefono: String, email: String) {
        try {
            val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Combinar nombre y apellido en un solo campo
            val nombreCompleto = "$nombre $apellido"
            
            val usuario = User(
                id = uid,
                nombre = nombreCompleto,
                telefono = telefono,
                email = email,
                rol = "Pasajero",
                fechaRegistro = fechaRegistro,
                estaActivo = true
            )

            val ref = FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
            ref.setValue(usuario)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                    
                    // Limpiar formulario
                    limpiarFormulario()
                    
                    // Ir a login
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("Registro", "Error al guardar usuario: ${exception.message}")
                    Toast.makeText(this, "Error al guardar usuario: ${exception.message}", Toast.LENGTH_SHORT).show()
                    restaurarBotonRegistro()
                }
        } catch (e: Exception) {
            android.util.Log.e("Registro", "Error crítico en guardarUsuarioEnBaseDatos: ${e.message}")
            Toast.makeText(this, "Error crítico: ${e.message}", Toast.LENGTH_SHORT).show()
            restaurarBotonRegistro()
        }
    }

    private fun limpiarFormulario() {
        try {
            nombreInput.text.clear()
            apellidoInput.text.clear()
            telefonoInput.text.clear()
            emailInput.text.clear()
            passwordInput.text.clear()
            confirmPasswordInput.text.clear()
            termsCheckbox.isChecked = false
        } catch (e: Exception) {
            android.util.Log.e("Registro", "Error al limpiar formulario: ${e.message}")
        }
    }

    private fun restaurarBotonRegistro() {
        try {
            registerButton.isEnabled = true
            registerButton.text = "REGISTRARSE"
        } catch (e: Exception) {
            android.util.Log.e("Registro", "Error al restaurar botón: ${e.message}")
        }
    }
}