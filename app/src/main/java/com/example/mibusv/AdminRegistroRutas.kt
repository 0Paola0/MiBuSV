package com.example.mibusv

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mibusv.adapters.BusquedaAdapter
import com.example.mibusv.utils.ValidationUtils
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdminRegistroRutas : AppCompatActivity() {
    
    private lateinit var campoNumeroRuta: EditText
    private lateinit var campoNombreRuta: EditText
    private lateinit var etParadaInicial: EditText
    private lateinit var rvParadasIniciales: RecyclerView
    private lateinit var etParadasIntermedias: EditText
    private lateinit var rvParadasIntermedias: RecyclerView
    private lateinit var tvParadasIntermediasSeleccionadas: TextView
    private lateinit var etParadaFinal: EditText
    private lateinit var rvParadasFinales: RecyclerView
    private lateinit var campoDistancia: EditText
    private lateinit var campoDuracion: EditText
    private lateinit var campoFrecuencia: EditText
    private lateinit var campoPrecio: EditText
    private lateinit var campoHorario: EditText
    private lateinit var botonRegistrar: Button
    private lateinit var botonCancelar: Button
    
    // Base de datos Firebase
    private lateinit var baseDatos: DatabaseReference
    
    // Listas de datos
    private var paradasList: MutableList<Parada> = mutableListOf()
    
    // Adaptadores para búsqueda
    private lateinit var paradasInicialesSearchAdapter: BusquedaAdapter
    private lateinit var paradasIntermediasSearchAdapter: BusquedaAdapter
    private lateinit var paradasFinalesSearchAdapter: BusquedaAdapter
    
    // Selecciones
    private var paradaInicialSeleccionada: Parada? = null
    private var paradasIntermediasSeleccionadas: MutableList<Parada> = mutableListOf()
    private var paradaFinalSeleccionada: Parada? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrorutas)
        
        try {
            // Inicializar base de datos Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Inicializar vistas
            campoNumeroRuta = findViewById(R.id.etNumeroRuta)
            campoNombreRuta = findViewById(R.id.etNombreRuta)
            etParadaInicial = findViewById(R.id.etParadaInicial)
            rvParadasIniciales = findViewById(R.id.rvParadasIniciales)
            etParadasIntermedias = findViewById(R.id.etParadasIntermedias)
            rvParadasIntermedias = findViewById(R.id.rvParadasIntermedias)
            tvParadasIntermediasSeleccionadas = findViewById(R.id.tvParadasIntermediasSeleccionadas)
            etParadaFinal = findViewById(R.id.etParadaFinal)
            rvParadasFinales = findViewById(R.id.rvParadasFinales)
            campoDistancia = findViewById(R.id.etDistancia)
            campoDuracion = findViewById(R.id.etDuracion)
            campoFrecuencia = findViewById(R.id.etFrecuencia)
            campoPrecio = findViewById(R.id.etPrecio)
            campoHorario = findViewById(R.id.etHorario)
            botonRegistrar = findViewById(R.id.btnRegistrarRuta)
            botonCancelar = findViewById(R.id.btnCancelarRuta)
            
            // Configurar RecyclerViews
            configurarRecyclerViews()
            
            // Configurar búsquedas
            configurarBusquedaParadas()
            
            // Cargar datos desde Firebase
            cargarParadas()
            
            // Configurar escuchadores
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
    
    
    private fun configurarRecyclerViews() {
        // RecyclerView de paradas iniciales
        rvParadasIniciales.layoutManager = LinearLayoutManager(this)
        paradasInicialesSearchAdapter = BusquedaAdapter(emptyList()) { paradaNombre ->
            paradaInicialSeleccionada = paradasList.find { it.nombreParada == paradaNombre }
            etParadaInicial.setText(paradaNombre)
            rvParadasIniciales.visibility = View.GONE
        }
        rvParadasIniciales.adapter = paradasInicialesSearchAdapter
        
        // RecyclerView de paradas intermedias
        rvParadasIntermedias.layoutManager = LinearLayoutManager(this)
        paradasIntermediasSearchAdapter = BusquedaAdapter(emptyList()) { paradaNombre ->
            val parada = paradasList.find { it.nombreParada == paradaNombre }
            parada?.let {
                if (!paradasIntermediasSeleccionadas.contains(it)) {
                    paradasIntermediasSeleccionadas.add(it)
                    actualizarTextoParadasIntermedias()
                }
            }
            etParadasIntermedias.setText("")
            rvParadasIntermedias.visibility = View.GONE
        }
        rvParadasIntermedias.adapter = paradasIntermediasSearchAdapter
        
        // RecyclerView de paradas finales
        rvParadasFinales.layoutManager = LinearLayoutManager(this)
        paradasFinalesSearchAdapter = BusquedaAdapter(emptyList()) { paradaNombre ->
            paradaFinalSeleccionada = paradasList.find { it.nombreParada == paradaNombre }
            etParadaFinal.setText(paradaNombre)
            rvParadasFinales.visibility = View.GONE
        }
        rvParadasFinales.adapter = paradasFinalesSearchAdapter
    }
    
    
    private fun configurarBusquedaParadas() {
        // Búsqueda de parada inicial
        etParadaInicial.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    val paradasFiltradas = paradasList
                        .filter { it.nombreParada.lowercase().contains(query.lowercase()) }
                        .map { it.nombreParada }
                    paradasInicialesSearchAdapter.updateItems(paradasFiltradas)
                    rvParadasIniciales.visibility = if (paradasFiltradas.isNotEmpty()) View.VISIBLE else View.GONE
                } else {
                    rvParadasIniciales.visibility = View.GONE
                }
            }
        })
        
        // Búsqueda de paradas intermedias
        etParadasIntermedias.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    val paradasFiltradas = paradasList
                        .filter { it.nombreParada.lowercase().contains(query.lowercase()) }
                        .map { it.nombreParada }
                    paradasIntermediasSearchAdapter.updateItems(paradasFiltradas)
                    rvParadasIntermedias.visibility = if (paradasFiltradas.isNotEmpty()) View.VISIBLE else View.GONE
                } else {
                    rvParadasIntermedias.visibility = View.GONE
                }
            }
        })
        
        // Búsqueda de parada final
        etParadaFinal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    val paradasFiltradas = paradasList
                        .filter { it.nombreParada.lowercase().contains(query.lowercase()) }
                        .map { it.nombreParada }
                    paradasFinalesSearchAdapter.updateItems(paradasFiltradas)
                    rvParadasFinales.visibility = if (paradasFiltradas.isNotEmpty()) View.VISIBLE else View.GONE
                } else {
                    rvParadasFinales.visibility = View.GONE
                }
            }
        })
    }
    
    
    private fun cargarParadas() {
        baseDatos.child("Paradas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                paradasList.clear()
                for (paradaSnapshot in snapshot.children) {
                    try {
                        val parada = paradaSnapshot.getValue(Parada::class.java)
                        parada?.let {
                            paradasList.add(it)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminRegistroRutas", "Error al cargar parada: ${e.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminRegistroRutas, "Error al cargar paradas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun actualizarTextoParadasIntermedias() {
        val textoParadas = paradasIntermediasSeleccionadas.joinToString(", ") { it.nombreParada }
        if (paradasIntermediasSeleccionadas.isEmpty()) {
            tvParadasIntermediasSeleccionadas.text = "Paradas intermedias: Ninguna"
        } else {
            tvParadasIntermediasSeleccionadas.text = "Paradas intermedias: $textoParadas (${paradasIntermediasSeleccionadas.size} paradas)"
        }
    }
    
    private fun registrarRuta() {
        try {
            val numeroRuta = campoNumeroRuta.text.toString().trim()
            val nombreRuta = campoNombreRuta.text.toString().trim()
            val distancia = campoDistancia.text.toString().trim()
            val duracion = campoDuracion.text.toString().trim()
            val frecuencia = campoFrecuencia.text.toString().trim()
            val precio = campoPrecio.text.toString().trim()
            val horario = campoHorario.text.toString().trim()
            
            // Validaciones de campos vacíos
            val camposRequeridos = listOf(
                campoNumeroRuta to "El número de ruta es requerido",
                campoNombreRuta to "El nombre de la ruta es requerido",
                campoDistancia to "La distancia es requerida",
                campoDuracion to "La duración es requerida",
                campoFrecuencia to "La frecuencia es requerida",
                campoPrecio to "El precio es requerido",
                campoHorario to "El horario es requerido"
            )
            
            if (!ValidationUtils.validateAllFieldsNotEmpty(camposRequeridos)) {
                return
            }
            
            // Validar formato de horario
            if (!ValidationUtils.formatHorario(campoHorario)) {
                return
            }
            
            // Validar selecciones
            if (paradaInicialSeleccionada == null) {
                ValidationUtils.showErrorToast(this, "Seleccione una parada inicial")
                return
            }
            
            if (paradaFinalSeleccionada == null) {
                ValidationUtils.showErrorToast(this, "Seleccione una parada final")
                return
            }
            
            // Mostrar indicador de carga
            botonRegistrar.isEnabled = false
            botonRegistrar.text = "Registrando..."
            
            // Crear objeto ruta
            val idRuta = baseDatos.child("Rutas").push().key ?: ""
            val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Crear string de todas las paradas
            val ruta = Route(
                id = idRuta,
                numeroRuta = numeroRuta,
                nombreRuta = nombreRuta,
                distancia = distancia,
                duracion = duracion,
                frecuencia = frecuencia,
                paradaInicial = paradaInicialSeleccionada!!.nombreParada,
                paradasIntermedias = if (paradasIntermediasSeleccionadas.isEmpty()) "Ninguna" else paradasIntermediasSeleccionadas.joinToString(", ") { it.nombreParada },
                paradaFinal = paradaFinalSeleccionada!!.nombreParada,
                precio = precio,
                horarioOperacion = horario,
                estaOperativa = true,
                fechaRegistro = fechaRegistro,
                rol = "Ruta"
            )
            
            // Guardar en base de datos Firebase Realtime
            baseDatos.child("Rutas").child(idRuta).setValue(ruta)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ruta registrada exitosamente", Toast.LENGTH_SHORT).show()
                    
                    // Limpiar formulario
                    limpiarFormulario()
                    
                    // Volver a la pantalla anterior
                    finish()
                }
                .addOnFailureListener { excepcion ->
                    Toast.makeText(this, "Error al registrar ruta: ${excepcion.message}", Toast.LENGTH_LONG).show()
                    
                    // Restaurar botón
                    botonRegistrar.isEnabled = true
                    botonRegistrar.text = "Registrar Ruta"
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar registro: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Restaurar botón
            botonRegistrar.isEnabled = true
            botonRegistrar.text = "Registrar Ruta"
        }
    }
    
    private fun limpiarFormulario() {
        campoNumeroRuta.text.clear()
        campoNombreRuta.text.clear()
        etParadaInicial.text.clear()
        etParadasIntermedias.text.clear()
        etParadaFinal.text.clear()
        campoDistancia.text.clear()
        campoDuracion.text.clear()
        campoFrecuencia.text.clear()
        campoPrecio.text.clear()
        campoHorario.text.clear()
        
        paradaInicialSeleccionada = null
        paradasIntermediasSeleccionadas.clear()
        paradaFinalSeleccionada = null
        
        rvParadasIniciales.visibility = View.GONE
        rvParadasIntermedias.visibility = View.GONE
        rvParadasFinales.visibility = View.GONE
        
        actualizarTextoParadasIntermedias()
    }
    
    private fun mostrarDialogoConfirmacion() {
        try {
            val numeroRuta = campoNumeroRuta.text.toString().trim()
            val nombreRuta = campoNombreRuta.text.toString().trim()
            val conductor = "No asignado" // Ya no se requiere conductor
            val paradaInicial = paradaInicialSeleccionada?.nombreParada ?: "No seleccionada"
            val paradasIntermedias = if (paradasIntermediasSeleccionadas.isEmpty()) "Ninguna" else paradasIntermediasSeleccionadas.joinToString(", ") { it.nombreParada }
            val paradaFinal = paradaFinalSeleccionada?.nombreParada ?: "No seleccionada"
            
            AlertDialog.Builder(this)
                .setTitle("Confirmar Registro de Ruta")
                .setMessage("¿Está seguro de que desea registrar la ruta:\n\nNúmero: $numeroRuta\nNombre: $nombreRuta\nConductor: $conductor\n\nParada Inicial: $paradaInicial\nParadas Intermedias: $paradasIntermedias\nParada Final: $paradaFinal")
                .setPositiveButton("Sí, Registrar") { _, _ ->
                    registrarRuta()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
