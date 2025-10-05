package com.example.mibusv

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class AdminRegistroParada : AppCompatActivity() {
    
    private lateinit var campoNombre: EditText
    private lateinit var campoDepartamento: EditText
    private lateinit var campoCiudad: EditText
    private lateinit var campoCoordenadas: EditText
    private lateinit var botonRegistrar: Button
    private lateinit var botonCancelar: Button
    
    // Base de datos Firebase
    private lateinit var baseDatos: DatabaseReference
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registroparada)
        
        try {
            // Inicializar base de datos Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Inicializar vistas
            inicializarVistas()
            
            // Configurar evento
            configurarListeners()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la actividad: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun inicializarVistas() {
        try {
            campoNombre = findViewById(R.id.etNombreParada)
            campoDepartamento = findViewById(R.id.etDepartamentoParada)
            campoCiudad = findViewById(R.id.etCiudadParada)
            campoCoordenadas = findViewById(R.id.etCoordenadasParada)
            botonRegistrar = findViewById(R.id.btnRegistrarParada)
            botonCancelar = findViewById(R.id.btnCancelarParada)
            
            // Verificar que todos los elementos se encontraron
            if (campoNombre == null || campoDepartamento == null || campoCiudad == null || 
                campoCoordenadas == null || botonRegistrar == null || botonCancelar == null) {
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
            botonRegistrar.setOnClickListener {
                mostrarDialogoConfirmacion()
            }
            
            botonCancelar.setOnClickListener {
                finish()
            }
            
            // Listener para validación en tiempo real de coordenadas
            campoCoordenadas.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validarFormatoCoordenadas(s.toString())
                }
                
                override fun afterTextChanged(s: Editable?) {}
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar listeners: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun validarFormatoCoordenadas(coordenadas: String) {
        if (coordenadas.isNotEmpty()) {
            val regex = Regex("^-?\\d+\\.?\\d*,\\s*-?\\d+\\.?\\d*$")
            if (!regex.matches(coordenadas)) {
                campoCoordenadas.error = "Formato: latitud, longitud (ej: 13.6929, -89.2182)"
            } else {
                campoCoordenadas.error = null
            }
        }
    }
    
    private fun registrarParada() {
        try {
            android.util.Log.d("AdminRegistroParada", "Iniciando registro de parada")
            
            val nombre = campoNombre.text.toString().trim()
            val departamento = campoDepartamento.text.toString().trim()
            val ciudad = campoCiudad.text.toString().trim()
            val coordenadas = campoCoordenadas.text.toString().trim()
            
            android.util.Log.d("AdminRegistroParada", "Datos capturados: nombre=$nombre, departamento=$departamento, ciudad=$ciudad, coordenadas=$coordenadas")
            
            // Validaciones de campos vacíos
            val camposRequeridos = listOf(
                campoNombre to "El nombre de la parada es requerido",
                campoDepartamento to "El departamento es requerido",
                campoCiudad to "La ciudad es requerida",
                campoCoordenadas to "Las coordenadas son requeridas"
            )
            
            if (!ValidationUtils.validateAllFieldsNotEmpty(camposRequeridos)) {
                android.util.Log.d("AdminRegistroParada", "Validación de campos falló")
                return
            }
            
            // Validar formato de coordenadas
            val regex = Regex("^-?\\d+\\.?\\d*,\\s*-?\\d+\\.?\\d*$")
            if (!regex.matches(coordenadas)) {
                android.util.Log.d("AdminRegistroParada", "Formato de coordenadas inválido")
                ValidationUtils.showErrorToast(this, "Formato de coordenadas inválido. Use: latitud, longitud")
                campoCoordenadas.requestFocus()
                return
            }
            
            android.util.Log.d("AdminRegistroParada", "Validaciones pasaron, creando objeto parada")
            
            // Mostrar indicador de carga
            botonRegistrar.isEnabled = false
            botonRegistrar.text = "Registrando..."
            
            // Crear objeto parada
            val idParada = baseDatos.child("Paradas").push().key ?: ""
            val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Crear dirección completa
            val direccionCompleta = "$ciudad, $departamento"
            
            val parada = Parada(
                id = idParada,
                nombreParada = nombre,
                direccion = direccionCompleta,
                coordenadas = coordenadas,
                estaOperativa = true,
                fechaRegistro = fechaRegistro,
                rol = "Parada"
            )
            
            android.util.Log.d("AdminRegistroParada", "Objeto parada creado, guardando en Firebase")
            
            // Guardar en base de datos Firebase Realtime
            baseDatos.child("Paradas").child(idParada).setValue(parada)
                .addOnSuccessListener {
                    android.util.Log.d("AdminRegistroParada", "Parada guardada exitosamente")
                    Toast.makeText(this, "Parada registrada exitosamente", Toast.LENGTH_SHORT).show()
                    
                    // Limpiar formulario
                    campoNombre.text.clear()
                    campoDepartamento.text.clear()
                    campoCiudad.text.clear()
                    campoCoordenadas.text.clear()
                    
                    // Volver a la pantalla anterior
                    finish()
                }
                .addOnFailureListener { excepcion ->
                    android.util.Log.e("AdminRegistroParada", "Error al guardar en Firebase: ${excepcion.message}")
                    Toast.makeText(this, "Error al registrar parada: ${excepcion.message}", Toast.LENGTH_LONG).show()
                    
                    // Restaurar botón
                    botonRegistrar.isEnabled = true
                    botonRegistrar.text = "Registrar"
                }
        } catch (e: Exception) {
            android.util.Log.e("AdminRegistroParada", "Error en registrarParada: ${e.message}", e)
            Toast.makeText(this, "Error al procesar registro: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Restaurar botón
            botonRegistrar.isEnabled = true
            botonRegistrar.text = "Registrar"
        }
    }
    
    private fun mostrarDialogoConfirmacion() {
        try {
            val nombre = campoNombre.text.toString().trim()
            val departamento = campoDepartamento.text.toString().trim()
            val ciudad = campoCiudad.text.toString().trim()
            val coordenadas = campoCoordenadas.text.toString().trim()
            
            AlertDialog.Builder(this)
                .setTitle("Confirmar Registro")
                .setMessage("¿Está seguro de que desea registrar la parada:\n\nNombre: $nombre\nDepartamento: $departamento\nCiudad: $ciudad\nCoordenadas: $coordenadas")
                .setPositiveButton("Sí, Registrar") { _, _ ->
                    registrarParada()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
