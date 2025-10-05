package com.example.mibusv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mibusv.adapters.BusquedaAdapter
import com.example.mibusv.utils.ValidationUtils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminEditarRuta : AppCompatActivity() {
    
    private lateinit var campoNumeroRuta: EditText
    private lateinit var campoNombreRuta: EditText
    private lateinit var etParadaInicial: EditText
    private lateinit var rvParadasIniciales: RecyclerView
    private lateinit var etParadasIntermedias: EditText
    private lateinit var rvParadasIntermedias: RecyclerView
    private lateinit var etParadaFinal: EditText
    private lateinit var rvParadasFinales: RecyclerView
    private lateinit var campoDistancia: EditText
    private lateinit var campoDuracion: EditText
    private lateinit var campoFrecuencia: EditText
    private lateinit var campoPrecio: EditText
    private lateinit var campoHorario: EditText
    private lateinit var botonGuardar: Button
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
    
    // ID de la ruta a editar
    private var rutaId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_editar_ruta)
        
        try {
            // Inicializar base de datos Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Obtener datos de la ruta desde el intent
            rutaId = intent.getStringExtra("ruta_id") ?: ""
            val numeroRuta = intent.getStringExtra("ruta_numero") ?: ""
            val nombreRuta = intent.getStringExtra("ruta_nombre") ?: ""
            val distancia = intent.getStringExtra("ruta_distancia") ?: ""
            val duracion = intent.getStringExtra("ruta_duracion") ?: ""
            val frecuencia = intent.getStringExtra("ruta_frecuencia") ?: ""
            val precio = intent.getStringExtra("ruta_precio") ?: ""
            val paradaInicial = intent.getStringExtra("ruta_parada_inicial") ?: ""
            val paradasIntermedias = intent.getStringExtra("ruta_paradas_intermedias") ?: ""
            val paradaFinal = intent.getStringExtra("ruta_parada_final") ?: ""
            val horario = intent.getStringExtra("ruta_horario") ?: ""
            
            // Inicializar vistas
            campoNumeroRuta = findViewById(R.id.campoNumeroRuta)
            campoNombreRuta = findViewById(R.id.campoNombreRuta)
            etParadaInicial = findViewById(R.id.etParadaInicial)
            rvParadasIniciales = findViewById(R.id.rvParadasIniciales)
            etParadasIntermedias = findViewById(R.id.etParadasIntermedias)
            rvParadasIntermedias = findViewById(R.id.rvParadasIntermedias)
            etParadaFinal = findViewById(R.id.etParadaFinal)
            rvParadasFinales = findViewById(R.id.rvParadasFinales)
            campoDistancia = findViewById(R.id.campoDistancia)
            campoDuracion = findViewById(R.id.campoDuracion)
            campoFrecuencia = findViewById(R.id.campoFrecuencia)
            campoPrecio = findViewById(R.id.campoPrecio)
            campoHorario = findViewById(R.id.campoHorario)
            botonGuardar = findViewById(R.id.botonGuardar)
            botonCancelar = findViewById(R.id.botonCancelar)
            
            // Llenar campos con datos existentes
            campoNumeroRuta.setText(numeroRuta)
            campoNombreRuta.setText(nombreRuta)
            campoDistancia.setText(distancia)
            campoDuracion.setText(duracion)
            campoFrecuencia.setText(frecuencia)
            campoPrecio.setText(precio)
            campoHorario.setText(horario)
            etParadaInicial.setText(paradaInicial)
            etParadasIntermedias.setText(paradasIntermedias)
            etParadaFinal.setText(paradaFinal)
            
            // Configurar RecyclerViews
            configurarRecyclerViews()
            
        // Cargar datos
        cargarParadas()
            
            // Configurar listeners
            configurarListeners()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun configurarRecyclerViews() {
        rvParadasIniciales.layoutManager = LinearLayoutManager(this)
        rvParadasIntermedias.layoutManager = LinearLayoutManager(this)
        rvParadasFinales.layoutManager = LinearLayoutManager(this)
        
        
        paradasInicialesSearchAdapter = BusquedaAdapter(emptyList()) { paradaNombre ->
            paradaInicialSeleccionada = paradasList.find { it.nombreParada == paradaNombre }
            etParadaInicial.setText(paradaNombre)
            rvParadasIniciales.visibility = android.view.View.GONE
        }
        
        paradasIntermediasSearchAdapter = BusquedaAdapter(emptyList()) { paradaNombre ->
            val parada = paradasList.find { it.nombreParada == paradaNombre }
            if (parada != null && !paradasIntermediasSeleccionadas.contains(parada)) {
                paradasIntermediasSeleccionadas.add(parada)
                actualizarTextoParadasIntermedias()
            }
            etParadasIntermedias.setText("")
            rvParadasIntermedias.visibility = android.view.View.GONE
        }
        
        paradasFinalesSearchAdapter = BusquedaAdapter(emptyList()) { paradaNombre ->
            paradaFinalSeleccionada = paradasList.find { it.nombreParada == paradaNombre }
            etParadaFinal.setText(paradaNombre)
            rvParadasFinales.visibility = android.view.View.GONE
        }
        
        rvParadasIniciales.adapter = paradasInicialesSearchAdapter
        rvParadasIntermedias.adapter = paradasIntermediasSearchAdapter
        rvParadasFinales.adapter = paradasFinalesSearchAdapter
    }
    
    private fun configurarListeners() {
        botonGuardar.setOnClickListener {
            actualizarRuta()
        }
        
        botonCancelar.setOnClickListener {
            finish()
        }
        
        
        etParadaInicial.addTextChangedListener(createTextWatcher { query ->
            val paradasFiltradas = paradasList.filter { 
                it.nombreParada.contains(query, ignoreCase = true) 
            }
            paradasInicialesSearchAdapter.updateItems(paradasFiltradas.map { it.nombreParada })
            rvParadasIniciales.visibility = if (query.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        })
        
        etParadasIntermedias.addTextChangedListener(createTextWatcher { query ->
            val paradasFiltradas = paradasList.filter { 
                it.nombreParada.contains(query, ignoreCase = true) 
            }
            paradasIntermediasSearchAdapter.updateItems(paradasFiltradas.map { it.nombreParada })
            rvParadasIntermedias.visibility = if (query.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        })
        
        etParadaFinal.addTextChangedListener(createTextWatcher { query ->
            val paradasFiltradas = paradasList.filter { 
                it.nombreParada.contains(query, ignoreCase = true) 
            }
            paradasFinalesSearchAdapter.updateItems(paradasFiltradas.map { it.nombreParada })
            rvParadasFinales.visibility = if (query.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        })
    }
    
    private fun createTextWatcher(onTextChanged: (String) -> Unit): android.text.TextWatcher {
        return object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged(s.toString().trim())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
    }
    
    
    private fun cargarParadas() {
        baseDatos.child("Paradas").addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                paradasList.clear()
                for (paradaSnapshot in snapshot.children) {
                    val parada = paradaSnapshot.getValue(Parada::class.java)
                    if (parada != null) {
                        paradasList.add(parada)
                    }
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }
    
    private fun actualizarTextoParadasIntermedias() {
        val texto = if (paradasIntermediasSeleccionadas.isEmpty()) {
            "Ninguna"
        } else {
            paradasIntermediasSeleccionadas.joinToString(", ") { it.nombreParada }
        }
    }
    
    private fun actualizarRuta() {
        try {
            val numeroRuta = campoNumeroRuta.text.toString().trim()
            val nombreRuta = campoNombreRuta.text.toString().trim()
            val distancia = campoDistancia.text.toString().trim()
            val duracion = campoDuracion.text.toString().trim()
            val frecuencia = campoFrecuencia.text.toString().trim()
            val precio = campoPrecio.text.toString().trim()
            val horario = campoHorario.text.toString().trim()
            
            // Validaciones
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
            
            if (!ValidationUtils.formatHorario(campoHorario)) {
                return
            }
            
            
            if (paradaInicialSeleccionada == null) {
                ValidationUtils.showErrorToast(this, "Seleccione una parada inicial")
                return
            }
            
            if (paradaFinalSeleccionada == null) {
                ValidationUtils.showErrorToast(this, "Seleccione una parada final")
                return
            }
            
            // Mostrar indicador de carga
            botonGuardar.isEnabled = false
            botonGuardar.text = "Guardando..."
            
            // Actualizar ruta en Firebase
            val rutaActualizada = Route(
                id = rutaId,
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
                fechaRegistro = "", // Mantener fecha original
                rol = "Ruta"
            )
            
            baseDatos.child("Rutas").child(rutaId).setValue(rutaActualizada)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ruta actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar ruta: ${e.message}", Toast.LENGTH_LONG).show()
                    botonGuardar.isEnabled = true
                    botonGuardar.text = "Guardar Cambios"
                }
                
        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar ruta: ${e.message}", Toast.LENGTH_LONG).show()
            botonGuardar.isEnabled = true
            botonGuardar.text = "Guardar Cambios"
        }
    }
}
