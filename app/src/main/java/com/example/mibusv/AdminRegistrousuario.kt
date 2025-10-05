package com.example.mibusv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mibusv.utils.ValidationUtils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdminRegistroUsuario : AppCompatActivity() {
    
    private lateinit var campoNombre: EditText
    private lateinit var campoEmail: EditText
    private lateinit var campoTelefono: EditText
    private lateinit var campoContrasena: EditText
    private lateinit var campoConfirmarContrasena: EditText
    private lateinit var botonRegistrar: Button
    private lateinit var botonCancelar: Button
    
    // Base de datos Firebase
    private lateinit var baseDatos: DatabaseReference
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrousuario)
        
        try {
            // Inicializar base de datos Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Inicializar vistas
            campoNombre = findViewById(R.id.etNombre)
            campoEmail = findViewById(R.id.etEmail)
            campoTelefono = findViewById(R.id.etTelefono)
            campoContrasena = findViewById(R.id.etPassword)
            campoConfirmarContrasena = findViewById(R.id.etConfirmPassword)
            botonRegistrar = findViewById(R.id.btnRegistrar)
            botonCancelar = findViewById(R.id.btnCancelar)
            
            // Verificar que todos los elementos se encontraron
            if (campoNombre == null || campoEmail == null || campoTelefono == null || 
                campoContrasena == null || campoConfirmarContrasena == null || 
                botonRegistrar == null || botonCancelar == null) {
                Toast.makeText(this, "Error al cargar elementos de la interfaz", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            
            // Configurar evento setOnClickListener
            botonRegistrar.setOnClickListener {
                mostrarDialogoConfirmacion()
            }
            
            botonCancelar.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la actividad: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun registrarUsuario() {
        try {
            val nombre = campoNombre.text.toString().trim()
            val email = campoEmail.text.toString().trim()
            val telefono = campoTelefono.text.toString().trim()
            val contrasena = campoContrasena.text.toString().trim()
            val confirmarContrasena = campoConfirmarContrasena.text.toString().trim()
            
            // Validaciones de campos vacíos
            val camposRequeridos = listOf(
                campoNombre to "El nombre es requerido",
                campoEmail to "El email es requerido",
                campoTelefono to "El teléfono es requerido",
                campoContrasena to "La contraseña es requerida",
                campoConfirmarContrasena to "Confirme la contraseña"
            )
            
            if (!ValidationUtils.validateAllFieldsNotEmpty(camposRequeridos)) {
                return
            }
            
            // Validar formato de email (@gmail.com)
            if (!ValidationUtils.validateGmailFormat(campoEmail)) {
                return
            }
            
            // Validar formato de teléfono (0000-0000)
            if (!ValidationUtils.validatePhoneFormat(campoTelefono)) {
                return
            }
            
            // Validar longitud de contraseña
            if (contrasena.length < 6) {
                campoContrasena.error = "La contraseña debe tener al menos 6 caracteres"
                campoContrasena.requestFocus()
                return
            }
            
            // Validar que las contraseñas coincidan
            if (contrasena != confirmarContrasena) {
                campoConfirmarContrasena.error = "Las contraseñas no coinciden"
                campoConfirmarContrasena.requestFocus()
                return
            }
            
            // Mostrar indicador de carga
            botonRegistrar.isEnabled = false
            botonRegistrar.text = "Registrando..."
            
            // Crear objeto usuario
            val idUsuario = baseDatos.child("Usuarios").push().key ?: ""
            val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val usuario = User(
                id = idUsuario,
                nombre = nombre,
                email = email,
                telefono = telefono,
                estaActivo = true,
                fechaRegistro = fechaRegistro,
                rol = "Pasajero"
            )
            
            // Guardar en base de datos Firebase Realtime
            baseDatos.child("Usuarios").child(idUsuario).setValue(usuario)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                    
                    // Limpiar formulario
                    campoNombre.text.clear()
                    campoEmail.text.clear()
                    campoTelefono.text.clear()
                    campoContrasena.text.clear()
                    campoConfirmarContrasena.text.clear()
                    
                    // Volver a la pantalla anterior
                    finish()
                }
                .addOnFailureListener { excepcion ->
                    Toast.makeText(this, "Error al registrar usuario: ${excepcion.message}", Toast.LENGTH_LONG).show()
                    
                    // Boton para resetear
                    botonRegistrar.isEnabled = true
                    botonRegistrar.text = "Registrar"
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar registro: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Boton para resetear
            botonRegistrar.isEnabled = true
            botonRegistrar.text = "Registrar"
        }
    }
    
    private fun mostrarDialogoConfirmacion() {
        try {
            val nombre = campoNombre.text.toString().trim()
            val email = campoEmail.text.toString().trim()
            
            AlertDialog.Builder(this)
                .setTitle("Confirmar Registro")
                .setMessage("¿Está seguro de que desea registrar al usuario:\n\nNombre: $nombre\nEmail: $email")
                .setPositiveButton("Sí, Registrar") { _, _ ->
                    registrarUsuario()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}