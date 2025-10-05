package com.example.mibusv

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.example.mibusv.utils.ValidationUtils
import java.text.SimpleDateFormat
import java.util.*

class Admin_Registrorutas : AppCompatActivity() {
    
    // Referencias a los elementos del formulario
    private lateinit var etNumeroRuta: EditText
    private lateinit var etNombreRuta: EditText
    private lateinit var etParadaInicial: EditText
    private lateinit var etParadasIntermedias: EditText
    private lateinit var etParadaFinal: EditText
    private lateinit var etDistancia: EditText
    private lateinit var etDuracion: EditText
    private lateinit var etFrecuencia: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etHorario: EditText
    private lateinit var btnRegistrarRuta: Button
    private lateinit var btnCancelarRuta: Button
    
    // RecyclerViews para las listas desplegables
    private lateinit var rvParadasIniciales: RecyclerView
    private lateinit var rvParadasIntermedias: RecyclerView
    private lateinit var rvParadasFinales: RecyclerView
    
    // Base de datos Firebase
    private lateinit var baseDatos: DatabaseReference
    
    // Listas de datos
    private lateinit var listaParadas: MutableList<Parada>
    private lateinit var paradasIntermediasSeleccionadas: MutableList<Parada>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrorutas)
        
        try {
            // Inicializar Firebase
            baseDatos = FirebaseDatabase.getInstance().reference
            
            // Inicializar listas
            listaParadas = mutableListOf()
            paradasIntermediasSeleccionadas = mutableListOf()
            
            // Inicializar vistas
            inicializarVistas()
            
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
    
    private fun inicializarVistas() {
        try {
            etNumeroRuta = findViewById(R.id.etNumeroRuta)
            etNombreRuta = findViewById(R.id.etNombreRuta)
            etParadaInicial = findViewById(R.id.etParadaInicial)
            etParadasIntermedias = findViewById(R.id.etParadasIntermedias)
            etParadaFinal = findViewById(R.id.etParadaFinal)
            etDistancia = findViewById(R.id.etDistancia)
            etDuracion = findViewById(R.id.etDuracion)
            etFrecuencia = findViewById(R.id.etFrecuencia)
            etPrecio = findViewById(R.id.etPrecio)
            etHorario = findViewById(R.id.etHorario)
            btnRegistrarRuta = findViewById(R.id.btnRegistrarRuta)
            btnCancelarRuta = findViewById(R.id.btnCancelarRuta)
            rvParadasIniciales = findViewById(R.id.rvParadasIniciales)
            rvParadasIntermedias = findViewById(R.id.rvParadasIntermedias)
            rvParadasFinales = findViewById(R.id.rvParadasFinales)
            
            // Verificar que todos los elementos se encontraron
            if (etNumeroRuta == null || etNombreRuta == null || 
                etParadaInicial == null || etParadasIntermedias == null || etParadaFinal == null ||
                etDistancia == null || etDuracion == null || etFrecuencia == null ||
                etPrecio == null || etHorario == null || btnRegistrarRuta == null || btnCancelarRuta == null) {
                Toast.makeText(this, "Error al cargar elementos de la interfaz", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun configurarRecyclerViews() {
        try {
            rvParadasIniciales.layoutManager = LinearLayoutManager(this)
            rvParadasIntermedias.layoutManager = LinearLayoutManager(this)
            rvParadasFinales.layoutManager = LinearLayoutManager(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar RecyclerViews: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun configurarListeners() {
        try {
            btnRegistrarRuta.setOnClickListener {
                mostrarDialogoConfirmacion()
            }
            
            btnCancelarRuta.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al configurar listeners: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    
    private fun cargarParadas() {
        try {
            baseDatos.child("Paradas").addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        listaParadas.clear()
                        for (paradaSnapshot in snapshot.children) {
                            val parada = paradaSnapshot.getValue(Parada::class.java)
                            parada?.let { listaParadas.add(it) }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@Admin_Registrorutas, "Error al cargar paradas: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Toast.makeText(this@Admin_Registrorutas, "Error al cargar paradas: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error al conectar con Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun registrarRuta() {
        try {
            val numeroRuta = etNumeroRuta.text.toString().trim()
            val nombreRuta = etNombreRuta.text.toString().trim()
            val paradaInicial = etParadaInicial.text.toString().trim()
            val paradasIntermedias = etParadasIntermedias.text.toString().trim()
            val paradaFinal = etParadaFinal.text.toString().trim()
            val distancia = etDistancia.text.toString().trim()
            val duracion = etDuracion.text.toString().trim()
            val frecuencia = etFrecuencia.text.toString().trim()
            val precio = etPrecio.text.toString().trim()
            val horario = etHorario.text.toString().trim()
            
            // Validaciones de campos vacíos
            val camposRequeridos = listOf(
                etNumeroRuta to "El número de ruta es requerido",
                etNombreRuta to "El nombre de la ruta es requerido",
                etParadaInicial to "La parada inicial es requerida",
                etParadaFinal to "La parada final es requerida",
                etDistancia to "La distancia es requerida",
                etDuracion to "La duración es requerida",
                etFrecuencia to "La frecuencia es requerida",
                etPrecio to "El precio es requerido",
                etHorario to "El horario es requerido"
            )
            
            if (!ValidationUtils.validateAllFieldsNotEmpty(camposRequeridos)) {
                return
            }
            
            // Validar formato de horario
            if (!ValidationUtils.formatHorario(etHorario)) {
                return
            }
            
            
            // Validar que las paradas existen en Firebase
            if (!validarParadaExiste(paradaInicial, "inicial")) {
                etParadaInicial.error = "La parada '$paradaInicial' no existe en el sistema"
                etParadaInicial.requestFocus()
                return
            }
            
            if (!validarParadaExiste(paradaFinal, "final")) {
                etParadaFinal.error = "La parada '$paradaFinal' no existe en el sistema"
                etParadaFinal.requestFocus()
                return
            }
            
            // Validar paradas intermedias si se especificaron
            if (paradasIntermedias.isNotEmpty()) {
                val paradasIntermediasList = paradasIntermedias.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                for (parada in paradasIntermediasList) {
                    if (!validarParadaExiste(parada, "intermedia")) {
                        etParadasIntermedias.error = "La parada '$parada' no existe en el sistema"
                        etParadasIntermedias.requestFocus()
                        return
                    }
                }
            }
            
            // Mostrar indicador de carga
            btnRegistrarRuta.isEnabled = false
            btnRegistrarRuta.text = "Registrando..."
            
            // Crear objeto ruta
            val idRuta = baseDatos.child("Rutas").push().key ?: ""
            val fechaRegistro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            
            // Verificar valores antes de crear el objeto
            android.util.Log.d("Admin_Registrorutas", "=== VALORES DE LOS CAMPOS ===")
            android.util.Log.d("Admin_Registrorutas", "etParadaInicial.text: '${etParadaInicial.text.toString()}'")
            android.util.Log.d("Admin_Registrorutas", "etParadasIntermedias.text: '${etParadasIntermedias.text.toString()}'")
            android.util.Log.d("Admin_Registrorutas", "etParadaFinal.text: '${etParadaFinal.text.toString()}'")
            android.util.Log.d("Admin_Registrorutas", "Parada Inicial (trimmed): '$paradaInicial'")
            android.util.Log.d("Admin_Registrorutas", "Paradas Intermedias (trimmed): '$paradasIntermedias'")
            android.util.Log.d("Admin_Registrorutas", "Parada Final (trimmed): '$paradaFinal'")
            android.util.Log.d("Admin_Registrorutas", "================================")
            
            // Validar que al menos una parada esté llena
            if (paradaInicial.isEmpty() && paradasIntermedias.isEmpty() && paradaFinal.isEmpty()) {
                Toast.makeText(this, "Debe especificar al menos una parada", Toast.LENGTH_SHORT).show()
                return
            }
            
            val ruta = Route(
                id = idRuta,
                numeroRuta = numeroRuta,
                nombreRuta = nombreRuta,
                distancia = distancia,
                duracion = duracion,
                frecuencia = frecuencia,
                paradaInicial = paradaInicial,
                paradasIntermedias = paradasIntermedias,
                paradaFinal = paradaFinal,
                precio = precio,
                horarioOperacion = horario,
                estaOperativa = true,
                fechaRegistro = fechaRegistro,
                rol = "Ruta"
            )
            
            // Verificar el objeto creado
            android.util.Log.d("Admin_Registrorutas", "Objeto Route creado:")
            android.util.Log.d("Admin_Registrorutas", "  - paradaInicial: '${ruta.paradaInicial}'")
            android.util.Log.d("Admin_Registrorutas", "  - paradasIntermedias: '${ruta.paradasIntermedias}'")
            android.util.Log.d("Admin_Registrorutas", "  - paradaFinal: '${ruta.paradaFinal}'")
            
            // Guardar en base de datos Firebase Realtime
            baseDatos.child("Rutas").child(idRuta).setValue(ruta)
                .addOnSuccessListener {
                    android.util.Log.d("Admin_Registrorutas", "Ruta guardada exitosamente en Firebase")
                    
                    // Verificar que se guardó correctamente leyendo de Firebase
                    verificarDatosGuardados(idRuta)
                    
                    Toast.makeText(this, "Ruta registrada exitosamente", Toast.LENGTH_SHORT).show()
                    
                    // Limpiar formulario
                    limpiarFormulario()
                    
                    // Volver a la pantalla anterior
                    finish()
                }
                .addOnFailureListener { excepcion ->
                    Toast.makeText(this, "Error al registrar ruta: ${excepcion.message}", Toast.LENGTH_LONG).show()
                    
                    // Restaurar botón
                    btnRegistrarRuta.isEnabled = true
                    btnRegistrarRuta.text = "Registrar Ruta"
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar registro: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Restaurar botón
            btnRegistrarRuta.isEnabled = true
            btnRegistrarRuta.text = "Registrar Ruta"
        }
    }
    
    private fun limpiarFormulario() {
        try {
            etNumeroRuta.text.clear()
            etNombreRuta.text.clear()
            etParadaInicial.text.clear()
            etParadasIntermedias.text.clear()
            etParadaFinal.text.clear()
            etDistancia.text.clear()
            etDuracion.text.clear()
            etFrecuencia.text.clear()
            etPrecio.text.clear()
            etHorario.text.clear()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al limpiar formulario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun mostrarDialogoConfirmacion() {
        try {
            val numeroRuta = etNumeroRuta.text.toString().trim()
            val nombreRuta = etNombreRuta.text.toString().trim()
            
            AlertDialog.Builder(this)
                .setTitle("Confirmar Registro")
                .setMessage("¿Está seguro de que desea registrar la ruta:\n\nNúmero: $numeroRuta\nNombre: $nombreRuta")
                .setPositiveButton("Sí, Registrar") { _, _ ->
                    registrarRuta()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validarParadaExiste(nombreParada: String, tipo: String): Boolean {
        return listaParadas.any { it.nombreParada.equals(nombreParada, ignoreCase = true) }
    }
    
    private fun verificarDatosGuardados(idRuta: String) {
        baseDatos.child("Rutas").child(idRuta).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val rutaGuardada = snapshot.getValue(Route::class.java)
                    android.util.Log.d("Admin_Registrorutas", "=== DATOS VERIFICADOS EN FIREBASE ===")
                    android.util.Log.d("Admin_Registrorutas", "ID: ${rutaGuardada?.id}")
                    android.util.Log.d("Admin_Registrorutas", "Número Ruta: ${rutaGuardada?.numeroRuta}")
                    android.util.Log.d("Admin_Registrorutas", "Nombre Ruta: ${rutaGuardada?.nombreRuta}")
                    android.util.Log.d("Admin_Registrorutas", "Parada Inicial: '${rutaGuardada?.paradaInicial}'")
                    android.util.Log.d("Admin_Registrorutas", "Paradas Intermedias: '${rutaGuardada?.paradasIntermedias}'")
                    android.util.Log.d("Admin_Registrorutas", "Parada Final: '${rutaGuardada?.paradaFinal}'")
                    android.util.Log.d("Admin_Registrorutas", "=====================================")
                } else {
                    android.util.Log.e("Admin_Registrorutas", "ERROR: No se encontró la ruta en Firebase")
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Admin_Registrorutas", "Error al verificar datos: ${error.message}")
            }
        })
    }
}