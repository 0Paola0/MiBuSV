package com.example.mibusv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mibusv.adapters.RutaSeleccionAdapter
import com.example.mibusv.utils.ValidationUtils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdminEditarConductor : AppCompatActivity() {
    
    private lateinit var campoNombre: EditText
    private lateinit var campoEmail: EditText
    private lateinit var campoTelefono: EditText
    private lateinit var campoRutas: EditText
    private lateinit var rvRutasDisponibles: RecyclerView
    private lateinit var tvRutasSeleccionadas: TextView
    private lateinit var botonGuardar: Button
    private lateinit var botonCancelar: Button
    
    // Base de datos Firebase
    private lateinit var baseDatos: DatabaseReference
    
    // Listas y adaptadores
    private lateinit var listaRutas: MutableList<Route>
    private lateinit var rutaSeleccionAdapter: RutaSeleccionAdapter
    
    // ID del conductor a editar
    private var conductorId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_editar_conductor)
        
        try {
            // Inicializar base de datos Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Obtener datos del conductor desde el intent
            conductorId = intent.getStringExtra("conductor_id") ?: ""
            val nombreConductor = intent.getStringExtra("conductor_nombre") ?: ""
            val emailConductor = intent.getStringExtra("conductor_email") ?: ""
            val telefonoConductor = intent.getStringExtra("conductor_telefono") ?: ""
            val rutasConductor = intent.getStringExtra("conductor_rutas") ?: ""
            
            // Inicializar listas
            listaRutas = mutableListOf()
            
            // Inicializar vistas
            campoNombre = findViewById(R.id.campoNombre)
            campoEmail = findViewById(R.id.campoEmail)
            campoTelefono = findViewById(R.id.campoTelefono)
            campoRutas = findViewById(R.id.campoRutas)
            rvRutasDisponibles = findViewById(R.id.rvRutasDisponibles)
            tvRutasSeleccionadas = findViewById(R.id.tvRutasSeleccionadas)
            botonGuardar = findViewById(R.id.botonGuardar)
            botonCancelar = findViewById(R.id.botonCancelar)
            
            // Configurar RecyclerView
            rvRutasDisponibles.layoutManager = LinearLayoutManager(this)
            rutaSeleccionAdapter = RutaSeleccionAdapter(listaRutas) { ruta, isSelected ->
                actualizarTextoRutasSeleccionadas()
            }
            rvRutasDisponibles.adapter = rutaSeleccionAdapter
            
            // Llenar campos con datos existentes
            campoNombre.setText(nombreConductor)
            campoEmail.setText(emailConductor)
            campoTelefono.setText(telefonoConductor)
            campoRutas.setText(rutasConductor)
            
            // Cargar rutas disponibles
            cargarRutasDisponibles()
            
            // Configurar listeners
            configurarListeners()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun configurarListeners() {
        botonGuardar.setOnClickListener {
            actualizarConductor()
        }
        
        botonCancelar.setOnClickListener {
            finish()
        }
        
        // Listener para búsqueda de rutas
        campoRutas.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    val rutasFiltradas = listaRutas.filter { ruta ->
                        ruta.nombreRuta.contains(query, ignoreCase = true) ||
                        ruta.numeroRuta.contains(query, ignoreCase = true)
                    }
                    rutaSeleccionAdapter.submitList(rutasFiltradas)
                } else {
                    rutaSeleccionAdapter.submitList(listaRutas)
                }
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
    
    private fun cargarRutasDisponibles() {
        baseDatos.child("Rutas").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                listaRutas.clear()
                for (rutaSnapshot in snapshot.children) {
                    val ruta = rutaSnapshot.getValue(Route::class.java)
                    if (ruta != null) {
                        listaRutas.add(ruta)
                    }
                }
                rutaSeleccionAdapter.submitList(listaRutas)
            }
            
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(this@AdminEditarConductor, "Error al cargar rutas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun actualizarTextoRutasSeleccionadas() {
        val rutasSeleccionadas = rutaSeleccionAdapter.getRutasSeleccionadasTexto()
        tvRutasSeleccionadas.text = "Rutas seleccionadas: $rutasSeleccionadas"
    }
    
    private fun actualizarConductor() {
        try {
            val nombre = campoNombre.text.toString().trim()
            val email = campoEmail.text.toString().trim()
            val telefono = campoTelefono.text.toString().trim()
            val rutas = rutaSeleccionAdapter.getRutasSeleccionadasTexto()
            
            // Validaciones
            val camposRequeridos = listOf(
                campoNombre to "El nombre del conductor es requerido",
                campoEmail to "El email es requerido",
                campoTelefono to "El teléfono es requerido"
            )
            
            if (!ValidationUtils.validateAllFieldsNotEmpty(camposRequeridos)) {
                return
            }
            
            if (!ValidationUtils.validateGmailFormat(campoEmail)) {
                return
            }
            
            if (!ValidationUtils.validatePhoneFormat(campoTelefono)) {
                return
            }
            
            if (rutas == "Ninguna") {
                ValidationUtils.showErrorToast(this, "Debe seleccionar al menos una ruta")
                campoRutas.requestFocus()
                return
            }
            
            // Mostrar indicador de carga
            botonGuardar.isEnabled = false
            botonGuardar.text = "Guardando..."
            
            // Actualizar conductor en Firebase
            val conductorActualizado = Conductor(
                id = conductorId,
                nombreConductor = nombre,
                email = email,
                telefono = telefono,
                rutas = rutas,
                fechaRegistro = "",
                estaOperativo = true,
                rol = "Conductor"
            )
            
            baseDatos.child("Conductores").child(conductorId).setValue(conductorActualizado)
                .addOnSuccessListener {
                    Toast.makeText(this, "Conductor actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar conductor: ${e.message}", Toast.LENGTH_LONG).show()
                    botonGuardar.isEnabled = true
                    botonGuardar.text = "Guardar Cambios"
                }
                
        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar conductor: ${e.message}", Toast.LENGTH_LONG).show()
            botonGuardar.isEnabled = true
            botonGuardar.text = "Guardar Cambios"
        }
    }
}
