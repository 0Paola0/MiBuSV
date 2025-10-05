package com.example.mibusv

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mibusv.adapters.RutaSeleccionAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdminRegistroConductor : AppCompatActivity() {
    
    private lateinit var campoNombre: EditText
    private lateinit var campoEmail: EditText
    private lateinit var campoTelefono: EditText
    private lateinit var campoRutas: EditText
    private lateinit var rvRutasDisponibles: RecyclerView
    private lateinit var tvRutasSeleccionadas: TextView
    private lateinit var botonRegistrar: Button
    private lateinit var botonCancelar: Button
    
    // Base de datos Firebase
    private lateinit var baseDatos: DatabaseReference
    
    // Listas y adaptadores
    private lateinit var listaRutas: MutableList<Route>
    private lateinit var rutasFiltradas: MutableList<Route>
    private lateinit var rutaSeleccionAdapter: RutaSeleccionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registroconductor)
        
        try {
            // Inicializar base de datos Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Inicializar listas
            listaRutas = mutableListOf()
            rutasFiltradas = mutableListOf()
            
            // Inicializar vistas
            inicializarVistas()
            
            // Configurar RecyclerView
            configurarRecyclerView()
            
            // Cargar rutas
            cargarRutas()
            
            // Configurar listener
            configurarListeners()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar la actividad: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun inicializarVistas() {
        try {
            campoNombre = findViewById(R.id.etNombreConductor)
            campoEmail = findViewById(R.id.etEmailConductor)
            campoTelefono = findViewById(R.id.etTelefonoConductor)
            campoRutas = findViewById(R.id.etRutasConductor)
            rvRutasDisponibles = findViewById(R.id.rvRutasDisponibles)
            tvRutasSeleccionadas = findViewById(R.id.tvRutasSeleccionadas)
            botonRegistrar = findViewById(R.id.btnRegistrarConductor)
            botonCancelar = findViewById(R.id.btnCancelarConductor)
            
            // Verificar que todos los elementos se encontraron
            if (campoNombre == null || campoEmail == null || campoTelefono == null || 
                campoRutas == null || rvRutasDisponibles == null || tvRutasSeleccionadas == null ||
                botonRegistrar == null || botonCancelar == null) {
                Toast.makeText(this, "Error al cargar elementos de la interfaz", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun configurarRecyclerView() {
        try {
            rvRutasDisponibles.layoutManager = LinearLayoutManager(this)
            rutaSeleccionAdapter = RutaSeleccionAdapter(rutasFiltradas) { ruta, isSelected ->
                actualizarRutasSeleccionadas()
            }
            rvRutasDisponibles.adapter = rutaSeleccionAdapter
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar RecyclerView: ${e.message}", Toast.LENGTH_LONG).show()
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
            
            // Listener para el campo de búsqueda de rutas
            campoRutas.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    filtrarRutas(s.toString())
                }
                
                override fun afterTextChanged(s: Editable?) {}
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar listeners: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun cargarRutas() {
        try {
            baseDatos.child("Rutas").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        listaRutas.clear()
                        for (rutaSnapshot in snapshot.children) {
                            val ruta = rutaSnapshot.getValue(Route::class.java)
                            ruta?.let { listaRutas.add(it) }
                        }
                        // Inicializar con todas las rutas
                        rutasFiltradas.clear()
                        rutasFiltradas.addAll(listaRutas)
                        rutaSeleccionAdapter.submitList(rutasFiltradas)
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminRegistroConductor, "Error al cargar rutas: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Toast.makeText(this@AdminRegistroConductor, "Error al cargar rutas: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error al conectar con Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun filtrarRutas(texto: String) {
        try {
            if (texto.isEmpty()) {
                // Mostrar todas las rutas si no hay texto
                rutasFiltradas.clear()
                rutasFiltradas.addAll(listaRutas)
                rvRutasDisponibles.visibility = android.view.View.GONE
            } else {
                // Filtrar rutas que contengan el texto
                rutasFiltradas.clear()
                rutasFiltradas.addAll(listaRutas.filter { ruta ->
                    ruta.numeroRuta.contains(texto, ignoreCase = true) ||
                    ruta.nombreRuta.contains(texto, ignoreCase = true)
                })
                rvRutasDisponibles.visibility = android.view.View.VISIBLE
            }
            rutaSeleccionAdapter.submitList(rutasFiltradas)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al filtrar rutas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun actualizarRutasSeleccionadas() {
        try {
            val rutasSeleccionadas = rutaSeleccionAdapter.getRutasSeleccionadasTexto()
            tvRutasSeleccionadas.text = "Rutas seleccionadas: $rutasSeleccionadas"
        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar rutas seleccionadas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun registrarConductor() {
        try {
            val nombre = campoNombre.text.toString().trim()
            val email = campoEmail.text.toString().trim()
            val telefono = campoTelefono.text.toString().trim()
            val rutas = rutaSeleccionAdapter.getRutasSeleccionadasTexto()
            
            // Validaciones básicas
            if (nombre.isEmpty()) {
                campoNombre.error = "El nombre del conductor es requerido"
                campoNombre.requestFocus()
                return
            }
            
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                campoEmail.error = "Ingrese un email válido"
                campoEmail.requestFocus()
                return
            }
            
            if (telefono.isEmpty()) {
                campoTelefono.error = "El teléfono es requerido"
                campoTelefono.requestFocus()
                return
            }
            
            if (rutas == "Ninguna") {
                Toast.makeText(this, "Debe seleccionar al menos una ruta", Toast.LENGTH_SHORT).show()
                campoRutas.requestFocus()
                return
            }
            
            // Mostrar indicador de carga
            botonRegistrar.isEnabled = false
            botonRegistrar.text = "Registrando..."
            
            // Crear objeto conductor
            val idConductor = baseDatos.child("Conductores").push().key ?: ""
            val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            val conductor = Conductor(
                id = idConductor,
                nombreConductor = nombre,
                email = email,
                telefono = telefono,
                rutas = rutas,
                fechaRegistro = fechaRegistro,
                estaOperativo = true,
                rol = "Conductor"
            )
            
            // Guardar en base de datos Firebase Realtime
            baseDatos.child("Conductores").child(idConductor).setValue(conductor)
                .addOnSuccessListener {
                    Toast.makeText(this, "Conductor registrado exitosamente", Toast.LENGTH_SHORT).show()
                    
                    // Limpiar formulario
                    campoNombre.text.clear()
                    campoEmail.text.clear()
                    campoTelefono.text.clear()
                    campoRutas.text.clear()
                    tvRutasSeleccionadas.text = "Rutas seleccionadas: Ninguna"
                    
                    // Volver a la pantalla anterior
                    finish()
                }
                .addOnFailureListener { excepcion ->
                    Toast.makeText(this, "Error al registrar conductor: ${excepcion.message}", Toast.LENGTH_LONG).show()
                    
                    // Restaurar botón
                    botonRegistrar.isEnabled = true
                    botonRegistrar.text = "Registrar"
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar registro: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Restaurar botón
            botonRegistrar.isEnabled = true
            botonRegistrar.text = "Registrar"
        }
    }
    
    private fun mostrarDialogoConfirmacion() {
        try {
            val nombre = campoNombre.text.toString().trim()
            val email = campoEmail.text.toString().trim()
            val rutas = rutaSeleccionAdapter.getRutasSeleccionadasTexto()
            
            AlertDialog.Builder(this)
                .setTitle("Confirmar Registro")
                .setMessage("¿Está seguro de que desea registrar al conductor:\n\nNombre: $nombre\nEmail: $email\nRutas: $rutas")
                .setPositiveButton("Sí, Registrar") { _, _ ->
                    registrarConductor()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}