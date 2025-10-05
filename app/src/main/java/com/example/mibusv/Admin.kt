package com.example.mibusv

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import com.example.mibusv.Login
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog

class Admin : AppCompatActivity() {
    
    private lateinit var tvTabTitle: TextView
    private lateinit var searchInput: EditText
    private lateinit var btnAddItem: Button
    private lateinit var recyclerView: RecyclerView
    
    // Firebase Database
    private lateinit var baseDatos: DatabaseReference
    private lateinit var listaUsuarios: MutableList<User>
    private lateinit var listaConductores: MutableList<Conductor>
    private lateinit var listaRutas: MutableList<Route>
    private lateinit var listaParadas: MutableList<Parada>
    
    // Adaptadores
    private lateinit var userAdapter: UserAdapter
    private lateinit var conductorAdapter: ConductorAdapter
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var paradaAdapter: ParadaAdapter
    
    // Listas filtradas para búsqueda
    private var usuariosFiltrados: MutableList<User> = mutableListOf()
    private var conductoresFiltrados: MutableList<Conductor> = mutableListOf()
    private var rutasFiltradas: MutableList<Route> = mutableListOf()
    private var paradasFiltradas: MutableList<Parada> = mutableListOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        
        // Inicializar Firebase
        try {
            baseDatos = FirebaseDatabase.getInstance().reference
            listaUsuarios = mutableListOf()
            listaConductores = mutableListOf()
            listaRutas = mutableListOf()
            listaParadas = mutableListOf()
            
            android.util.Log.d("Admin", "Firebase inicializado correctamente")
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al inicializar Firebase: ${e.message}")
            Toast.makeText(this, "Error al inicializar Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        recyclerView = findViewById<RecyclerView>(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Referencias a los elementos del header dinámico
        tvTabTitle = findViewById(R.id.tvTabTitle)
        searchInput = findViewById(R.id.searchInput)
        btnAddItem = findViewById(R.id.btnAddItem)
        
        // Verificar que los elementos se encontraron correctamente
        if (tvTabTitle == null || searchInput == null || btnAddItem == null) {
            Toast.makeText(this, "Error al cargar elementos de la interfaz", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Inicializar adaptadores con funcionalidades completas
        try {
        userAdapter = UserAdapter(listaUsuarios) { usuarioEliminar ->
            eliminarUsuario(usuarioEliminar)
        }
        conductorAdapter = ConductorAdapter(listaConductores, 
            { conductor -> abrirEditarConductor(conductor) },
            { conductor -> eliminarConductor(conductor) }
        )
        routeAdapter = RouteAdapter(listaRutas,
            { ruta -> abrirEditarRuta(ruta) },
            { ruta -> eliminarRuta(ruta) }
        )
        paradaAdapter = ParadaAdapter(listaParadas,
            { parada -> abrirEditarParada(parada) },
            { parada -> eliminarParada(parada) }
        )
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al inicializar adaptadores: ${e.message}")
            Toast.makeText(this, "Error al inicializar adaptadores: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        recyclerView.adapter = userAdapter
        
        // Cargar usuarios desde Firebase
        cargarUsuarios(userAdapter)
        updateHeaderContent("Usuarios")

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabName = tab.text.toString()
                updateHeaderContent(tabName)
                
                when (tabName) {
                    "Usuarios" -> {
                        recyclerView.adapter = userAdapter
                        cargarUsuarios(userAdapter)
                        // Limpiar búsqueda al cambiar de pestaña
                        searchInput.text.clear()
                    }
                    "Conductores" -> {
                        recyclerView.adapter = conductorAdapter
                        cargarConductores(conductorAdapter)
                        searchInput.text.clear()
                    }
                    "Rutas" -> {
                        try {
                        recyclerView.adapter = routeAdapter
                        cargarRutas(routeAdapter)
                        searchInput.text.clear()
                        } catch (e: Exception) {
                            android.util.Log.e("Admin", "Error al cambiar a pestaña Rutas: ${e.message}")
                            Toast.makeText(this@Admin, "Error al cargar rutas: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "Paradas" -> {
                        try {
                        recyclerView.adapter = paradaAdapter
                        cargarParadas(paradaAdapter)
                        searchInput.text.clear()
                        } catch (e: Exception) {
                            android.util.Log.e("Admin", "Error al cambiar a pestaña Paradas: ${e.message}")
                            Toast.makeText(this@Admin, "Error al cargar paradas: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        
        // Configurar funcionalidad de búsqueda
        configurarBusqueda()
        
        // Configurar click del botón "Agregar"
        btnAddItem.setOnClickListener {
            val currentTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
            openAddActivity(currentTab)
        }

        val exitButton = findViewById<Button>(R.id.btnSalir)
        if (exitButton == null) {
            Toast.makeText(this, "Error al cargar botón de salir", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        exitButton.setOnClickListener {
            try {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al cerrar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateHeaderContent(tabName: String) {
        try {
            when (tabName) {
                "Usuarios" -> {
                    tvTabTitle.text = "Usuarios Registrados"
                    searchInput.hint = "Buscar usuario"
                    btnAddItem.text = "Agregar Usuario"
                }
                "Conductores" -> {
                    tvTabTitle.text = "Conductores Registrados"
                    searchInput.hint = "Buscar conductor"
                    btnAddItem.text = "Agregar Conductor"
                }
                "Rutas" -> {
                    tvTabTitle.text = "Rutas Disponibles"
                    searchInput.hint = "Buscar ruta"
                    btnAddItem.text = "Agregar Ruta"
                }
                "Paradas" -> {
                    tvTabTitle.text = "Paradas Registradas"
                    searchInput.hint = "Buscar parada"
                    btnAddItem.text = "Agregar Parada"
                }
                else -> {
                    Toast.makeText(this, "Pestaña no reconocida: $tabName", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar header: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAddActivity(tabName: String) {
        try {
            when (tabName) {
                "Usuarios" -> {
                    val intent = Intent(this, AdminRegistroUsuario::class.java)
                    startActivity(intent)
                }
                "Conductores" -> {
                    val intent = Intent(this, AdminRegistroConductor::class.java)
                    startActivity(intent)
                }
                "Rutas" -> {
                    val intent = Intent(this, AdminRegistroRutas::class.java)
                    startActivity(intent)
                }
                "Paradas" -> {
                    val intent = Intent(this, AdminRegistroParada::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Pestaña no reconocida: $tabName", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir actividad: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarUsuarios(adaptador: UserAdapter) {
        try {
            baseDatos.child("Usuarios").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        listaUsuarios.clear()
                        
                        for (usuarioSnapshot in snapshot.children) {
                            try {
                                val usuario = usuarioSnapshot.getValue(User::class.java)
                                usuario?.let {
                                    listaUsuarios.add(it)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Admin", "Error al deserializar usuario: ${e.message}")
                            }
                        }
                        
                        adaptador.submitList(listaUsuarios.toList())
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error al procesar datos de usuarios: ${e.message}")
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("Admin", "Error al cargar usuarios: ${error.message}")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al conectar con Firebase: ${e.message}")
        }
    }

    private fun cargarConductores(adaptador: ConductorAdapter) {
        try {
            baseDatos.child("Conductores").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        listaConductores.clear()
                        
                        for (conductorSnapshot in snapshot.children) {
                            try {
                                val conductor = conductorSnapshot.getValue(Conductor::class.java)
                                conductor?.let {
                                    listaConductores.add(conductor)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Admin", "Error al deserializar conductor: ${e.message}")
                            }
                        }
                        
                        adaptador.submitList(listaConductores.toList())
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error al procesar datos de conductores: ${e.message}")
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("Admin", "Error al cargar conductores: ${error.message}")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al conectar con Firebase: ${e.message}")
        }
    }

    private fun cargarRutas(adaptador: RouteAdapter) {
        try {
            baseDatos.child("Rutas").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        listaRutas.clear()
                        
                        for (rutaSnapshot in snapshot.children) {
                            try {
                                val ruta = rutaSnapshot.getValue(Route::class.java)
                                ruta?.let {
                                    listaRutas.add(it)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Admin", "Error al deserializar ruta: ${e.message}")
                            }
                        }
                        
                        adaptador.submitList(listaRutas.toList())
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error al procesar datos de rutas: ${e.message}")
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("Admin", "Error al cargar rutas: ${error.message}")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al conectar con Firebase: ${e.message}")
        }
    }

    private fun cargarParadas(adaptador: ParadaAdapter) {
        try {
            baseDatos.child("Paradas").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        listaParadas.clear()
                        
                        for (paradaSnapshot in snapshot.children) {
                            try {
                                val parada = paradaSnapshot.getValue(Parada::class.java)
                                parada?.let {
                                    listaParadas.add(it)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("Admin", "Error al deserializar parada: ${e.message}")
                            }
                        }
                        
                        adaptador.submitList(listaParadas.toList())
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error al procesar datos de paradas: ${e.message}")
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("Admin", "Error al cargar paradas: ${error.message}")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al conectar con Firebase: ${e.message}")
        }
    }

    private fun eliminarUsuario(usuario: User) {
        try {
            baseDatos.child("Usuarios").child(usuario.id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al eliminar usuario: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al eliminar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarBusqueda() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val currentTab = findViewById<TabLayout>(R.id.tabLayout).getTabAt(
                    findViewById<TabLayout>(R.id.tabLayout).selectedTabPosition
                )?.text.toString()
                
                when (currentTab) {
                    "Usuarios" -> filtrarUsuarios(query)
                    "Conductores" -> filtrarConductores(query)
                    "Rutas" -> filtrarRutas(query)
                    "Paradas" -> filtrarParadas(query)
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarUsuarios(query: String) {
        usuariosFiltrados.clear()
        if (query.isEmpty()) {
            usuariosFiltrados.addAll(listaUsuarios)
        } else {
            usuariosFiltrados.addAll(listaUsuarios.filter { usuario ->
                usuario.nombre.contains(query, ignoreCase = true) ||
                usuario.email.contains(query, ignoreCase = true) ||
                usuario.telefono.contains(query, ignoreCase = true) ||
                usuario.rol.contains(query, ignoreCase = true)
            })
        }
        userAdapter.submitList(usuariosFiltrados)
    }

    private fun filtrarConductores(query: String) {
        try {
        conductoresFiltrados.clear()
        if (query.isEmpty()) {
            conductoresFiltrados.addAll(listaConductores)
        } else {
            conductoresFiltrados.addAll(listaConductores.filter { conductor ->
                    try {
                conductor.nombreConductor.contains(query, ignoreCase = true) ||
                conductor.email.contains(query, ignoreCase = true) ||
                conductor.telefono.contains(query, ignoreCase = true) ||
                conductor.rutas.contains(query, ignoreCase = true)
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error al filtrar conductor: ${e.message}")
                        false
                    }
            })
        }
        conductorAdapter.submitList(conductoresFiltrados)
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al filtrar conductores: ${e.message}")
            Toast.makeText(this, "Error al filtrar conductores: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filtrarRutas(query: String) {
        try {
        rutasFiltradas.clear()
        if (query.isEmpty()) {
            rutasFiltradas.addAll(listaRutas)
        } else {
            rutasFiltradas.addAll(listaRutas.filter { ruta ->
                    try {
                ruta.numeroRuta.contains(query, ignoreCase = true) ||
                ruta.nombreRuta.contains(query, ignoreCase = true) ||
                ruta.paradaInicial.contains(query, ignoreCase = true) ||
                ruta.paradaFinal.contains(query, ignoreCase = true) ||
                ruta.paradasIntermedias.contains(query, ignoreCase = true)
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error al filtrar ruta: ${e.message}")
                        false
                    }
            })
        }
        routeAdapter.submitList(rutasFiltradas)
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al filtrar rutas: ${e.message}")
            Toast.makeText(this, "Error al filtrar rutas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filtrarParadas(query: String) {
        try {
        paradasFiltradas.clear()
        if (query.isEmpty()) {
            paradasFiltradas.addAll(listaParadas)
        } else {
            paradasFiltradas.addAll(listaParadas.filter { parada ->
                    try {
                parada.nombreParada.contains(query, ignoreCase = true) ||
                parada.direccion.contains(query, ignoreCase = true) ||
                parada.coordenadas.contains(query, ignoreCase = true)
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error al filtrar parada: ${e.message}")
                        false
                    }
            })
        }
        paradaAdapter.submitList(paradasFiltradas)
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al filtrar paradas: ${e.message}")
            Toast.makeText(this, "Error al filtrar paradas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarConductor(conductor: Conductor) {
        try {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Está seguro de que desea eliminar al conductor '${conductor.nombreConductor}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                    try {
                baseDatos.child("Conductores").child(conductor.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Conductor eliminado exitosamente", Toast.LENGTH_SHORT).show()
                                // Recargar conductores de forma segura
                                try {
                        cargarConductores(conductorAdapter)
                                } catch (e: Exception) {
                                    android.util.Log.e("Admin", "Error al recargar conductores: ${e.message}")
                                    // Si falla la recarga, limpiar la lista manualmente
                                    listaConductores.clear()
                                    conductorAdapter.submitList(emptyList())
                                }
                    }
                    .addOnFailureListener { e ->
                                android.util.Log.e("Admin", "Error al eliminar conductor: ${e.message}")
                                Toast.makeText(this, "Error al eliminar conductor: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error crítico al eliminar conductor: ${e.message}")
                        Toast.makeText(this, "Error al eliminar conductor: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al mostrar diálogo de eliminación: ${e.message}")
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarRuta(ruta: Route) {
        try {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Está seguro de que desea eliminar la ruta '${ruta.numeroRuta} - ${ruta.nombreRuta}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                    try {
                baseDatos.child("Rutas").child(ruta.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Ruta eliminada exitosamente", Toast.LENGTH_SHORT).show()
                                // Recargar rutas de forma segura
                                try {
                        cargarRutas(routeAdapter)
                                } catch (e: Exception) {
                                    android.util.Log.e("Admin", "Error al recargar rutas: ${e.message}")
                                    // Si falla la recarga, limpiar la lista manualmente
                                    listaRutas.clear()
                                    routeAdapter.submitList(emptyList())
                                }
                    }
                    .addOnFailureListener { e ->
                                android.util.Log.e("Admin", "Error al eliminar ruta: ${e.message}")
                                Toast.makeText(this, "Error al eliminar ruta: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error crítico al eliminar ruta: ${e.message}")
                        Toast.makeText(this, "Error al eliminar ruta: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al mostrar diálogo de eliminación: ${e.message}")
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun eliminarParada(parada: Parada) {
        try {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Está seguro de que desea eliminar la parada '${parada.nombreParada}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                    try {
                baseDatos.child("Paradas").child(parada.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Parada eliminada exitosamente", Toast.LENGTH_SHORT).show()
                                // Recargar paradas de forma segura
                                try {
                        cargarParadas(paradaAdapter)
                                } catch (e: Exception) {
                                    android.util.Log.e("Admin", "Error al recargar paradas: ${e.message}")
                                    // Si falla la recarga, limpiar la lista manualmente
                                    listaParadas.clear()
                                    paradaAdapter.submitList(emptyList())
                                }
                    }
                    .addOnFailureListener { e ->
                                android.util.Log.e("Admin", "Error al eliminar parada: ${e.message}")
                                Toast.makeText(this, "Error al eliminar parada: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } catch (e: Exception) {
                        android.util.Log.e("Admin", "Error crítico al eliminar parada: ${e.message}")
                        Toast.makeText(this, "Error al eliminar parada: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
        } catch (e: Exception) {
            android.util.Log.e("Admin", "Error al mostrar diálogo de eliminación: ${e.message}")
            Toast.makeText(this, "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirEditarConductor(conductor: Conductor) {
        val intent = Intent(this, AdminEditarConductor::class.java)
        intent.putExtra("conductor_id", conductor.id)
        intent.putExtra("conductor_nombre", conductor.nombreConductor)
        intent.putExtra("conductor_email", conductor.email)
        intent.putExtra("conductor_telefono", conductor.telefono)
        intent.putExtra("conductor_rutas", conductor.rutas)
        startActivity(intent)
    }

    private fun abrirEditarRuta(ruta: Route) {
        val intent = Intent(this, AdminEditarRuta::class.java)
        intent.putExtra("ruta_id", ruta.id)
        intent.putExtra("ruta_numero", ruta.numeroRuta)
        intent.putExtra("ruta_nombre", ruta.nombreRuta)
        intent.putExtra("ruta_distancia", ruta.distancia)
        intent.putExtra("ruta_duracion", ruta.duracion)
        intent.putExtra("ruta_frecuencia", ruta.frecuencia)
        intent.putExtra("ruta_precio", ruta.precio)
        intent.putExtra("ruta_parada_inicial", ruta.paradaInicial)
        intent.putExtra("ruta_paradas_intermedias", ruta.paradasIntermedias)
        intent.putExtra("ruta_parada_final", ruta.paradaFinal)
        intent.putExtra("ruta_horario", ruta.horarioOperacion)
        startActivity(intent)
    }

    private fun abrirEditarParada(parada: Parada) {
        val intent = Intent(this, AdminEditarParada::class.java)
        intent.putExtra("parada_id", parada.id)
        intent.putExtra("parada_nombre", parada.nombreParada)
        intent.putExtra("parada_direccion", parada.direccion)
        intent.putExtra("parada_coordenadas", parada.coordenadas)
        startActivity(intent)
    }
}
