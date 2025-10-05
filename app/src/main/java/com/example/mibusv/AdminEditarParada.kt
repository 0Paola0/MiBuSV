package com.example.mibusv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mibusv.utils.ValidationUtils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminEditarParada : AppCompatActivity() {
    
    private lateinit var campoNombre: EditText
    private lateinit var campoDireccion: EditText
    private lateinit var campoCoordenadas: EditText
    private lateinit var botonGuardar: Button
    private lateinit var botonCancelar: Button
    
    // Base de datos Firebase
    private lateinit var baseDatos: DatabaseReference
    
    // ID de la parada a editar
    private var paradaId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_editar_parada)
        
        try {
            // Inicializar base de datos Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Obtener datos de la parada desde el intent
            paradaId = intent.getStringExtra("parada_id") ?: ""
            val nombreParada = intent.getStringExtra("parada_nombre") ?: ""
            val direccionParada = intent.getStringExtra("parada_direccion") ?: ""
            val coordenadasParada = intent.getStringExtra("parada_coordenadas") ?: ""
            
            // Inicializar vistas
            campoNombre = findViewById(R.id.campoNombre)
            campoDireccion = findViewById(R.id.campoDireccion)
            campoCoordenadas = findViewById(R.id.campoCoordenadas)
            botonGuardar = findViewById(R.id.botonGuardar)
            botonCancelar = findViewById(R.id.botonCancelar)
            
            // Llenar campos con datos existentes
            campoNombre.setText(nombreParada)
            campoDireccion.setText(direccionParada)
            campoCoordenadas.setText(coordenadasParada)
            
            // Configurar listeners
            configurarListeners()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun configurarListeners() {
        botonGuardar.setOnClickListener {
            actualizarParada()
        }
        
        botonCancelar.setOnClickListener {
            finish()
        }
    }
    
    private fun actualizarParada() {
        try {
            val nombre = campoNombre.text.toString().trim()
            val direccion = campoDireccion.text.toString().trim()
            val coordenadas = campoCoordenadas.text.toString().trim()
            
            // Validaciones
            val camposRequeridos = listOf(
                campoNombre to "El nombre de la parada es requerido",
                campoDireccion to "La dirección es requerida",
                campoCoordenadas to "Las coordenadas son requeridas"
            )
            
            if (!ValidationUtils.validateAllFieldsNotEmpty(camposRequeridos)) {
                return
            }
            
            // Mostrar indicador de carga
            botonGuardar.isEnabled = false
            botonGuardar.text = "Guardando..."
            
            // Actualizar parada en Firebase
            val paradaActualizada = Parada(
                id = paradaId,
                nombreParada = nombre,
                direccion = direccion,
                coordenadas = coordenadas,
                estaOperativa = true,
                fechaRegistro = "", // Mantener fecha original
                rol = "Parada"
            )
            
            baseDatos.child("Paradas").child(paradaId).setValue(paradaActualizada)
                .addOnSuccessListener {
                    Toast.makeText(this, "Parada actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar parada: ${e.message}", Toast.LENGTH_LONG).show()
                    botonGuardar.isEnabled = true
                    botonGuardar.text = "Guardar Cambios"
                }
                
        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar parada: ${e.message}", Toast.LENGTH_LONG).show()
            botonGuardar.isEnabled = true
            botonGuardar.text = "Guardar Cambios"
        }
    }
}
