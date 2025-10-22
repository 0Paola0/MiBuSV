package com.example.mibusv // Asegúrate que sea tu paquete

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color // Para colores de marcadores y polyline
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log // Para logs de depuración
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory // Para colores de marcadores
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds // Para ajustar el zoom
import com.google.android.gms.maps.model.Marker // Para guardar marcadores
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline // <-- Para la línea de ruta
import com.google.android.gms.maps.model.PolylineOptions // <-- Para dibujar la línea
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
// Imports para Directions API
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil // Para decodificar polilíneas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DriverDashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    // --- Variables de Mapas y Ubicación ---
    private var mMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var baseDatos: DatabaseReference
    private lateinit var conductorId: String

    // --- UI Elements ---
    private lateinit var spinnerRoutes: Spinner
    private lateinit var switchShareLocation: SwitchMaterial
    private lateinit var btnToggleTrip: Button
    private lateinit var btnLogout: Button

    // --- State Variables ---
    private var isSharingLocation = false
    private var isTripActive = false
    private var selectedRoute: Route? = null
    private val availableRoutes = mutableListOf<Route>()
    private val routeNames = mutableListOf<String>()

    // --- Para manejo de paradas y RUTA ---
    private val allStopsMap = mutableMapOf<String, Parada>() // Mapa para buscar paradas por nombre rápido
    private val stopMarkers = mutableListOf<Marker>()       // Lista para guardar los marcadores de parada
    private var routePolyline: Polyline? = null // Variable para guardar la línea de la ruta dibujada

    // --- Contexto para Directions API ---
    private lateinit var geoApiContext: GeoApiContext

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        // --- Inicialización ---
        baseDatos = FirebaseDatabase.getInstance().reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        conductorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Inicializar GeoApiContext con tu API Key
        // ¡¡¡RECUERDA REEMPLAZAR "TU_API_KEY_AQUI"!!!
        geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyAZ6KfOCzxiQSXyzOcr9zxg2zsxebIK6lQ")
            .build()

        if (conductorId.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo identificar al conductor.", Toast.LENGTH_LONG).show()
            logoutDriver()
            return
        }

        // --- Referencias UI ---
        spinnerRoutes = findViewById(R.id.spinnerRoutes)
        switchShareLocation = findViewById(R.id.switchShareLocation)
        btnToggleTrip = findViewById(R.id.btnToggleTrip)
        btnLogout = findViewById(R.id.btnLogoutDriver)

        // --- Configuración Mapa ---
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // --- Configuración Inicial ---
        setupLocationUpdates()
        loadAllStops() // Cargar paradas primero
        loadAvailableRoutes() // Luego cargar rutas
        setupButtonClickListeners()
        updateTripButtonState()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.uiSettings?.isZoomControlsEnabled = true
        checkLocationPermission()
        // Si ya hay una ruta seleccionada cuando el mapa está listo, dibujarla
        selectedRoute?.let { drawRouteOnMap(it) }
    }

    // --- Carga de Datos (Rutas y Paradas) ---

    // Carga todas las paradas en un mapa para acceso rápido
    private fun loadAllStops() {
        baseDatos.child("Paradas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allStopsMap.clear()
                for (stopSnapshot in snapshot.children) {
                    val parada = stopSnapshot.getValue(Parada::class.java)
                    parada?.let { allStopsMap[it.nombreParada.lowercase().trim()] = it }
                }
                // Si la ruta ya estaba seleccionada, intenta dibujar de nuevo ahora que las paradas cargaron
                selectedRoute?.let { drawRouteOnMap(it) }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DriverDashboardActivity, "Error al cargar paradas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Carga las rutas disponibles para el Spinner
    private fun loadAvailableRoutes() {
        baseDatos.child("Rutas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availableRoutes.clear()
                routeNames.clear()
                routeNames.add("Seleccione una ruta...") // Default option

                for (routeSnapshot in snapshot.children) {
                    val route = routeSnapshot.getValue(Route::class.java)
                    route?.let {
                        availableRoutes.add(it)
                        routeNames.add("${it.numeroRuta} - ${it.nombreRuta}")
                    }
                }
                // Setup Spinner adapter
                val adapter = ArrayAdapter(this@DriverDashboardActivity, android.R.layout.simple_spinner_item, routeNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerRoutes.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DriverDashboardActivity, "Error al cargar rutas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Listener para cuando se selecciona una ruta en el Spinner
        spinnerRoutes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Si no es la opción "Seleccione..."
                    selectedRoute = availableRoutes[position - 1] // Guarda la ruta seleccionada
                    drawRouteOnMap(selectedRoute!!) // Dibuja la ruta en el mapa
                } else {
                    selectedRoute = null
                    clearStopsAndRoute() // Limpia el mapa si no hay ruta seleccionada
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedRoute = null
                clearStopsAndRoute()
            }
        }
    }

    // --- Lógica para Dibujar la Ruta en el Mapa ---

    // Limpia marcadores de paradas y la línea de ruta del mapa
    private fun clearStopsAndRoute() {
        stopMarkers.forEach { it.remove() }
        stopMarkers.clear()
        routePolyline?.remove()
        routePolyline = null
    }

    // Dibuja los marcadores de parada y la ruta (usando Directions API)
    private fun drawRouteOnMap(route: Route) {
        if (mMap == null || allStopsMap.isEmpty()) {
            Log.w("drawRouteOnMap", "Map or stops not ready yet.")
            return // No hacer nada si el mapa o las paradas no están listos
        }
        clearStopsAndRoute() // Limpiar mapa antes de dibujar lo nuevo

        // 1. Obtener nombres de paradas en orden
        val stopNamesInOrder = mutableListOf<String>()
        stopNamesInOrder.add(route.paradaInicial.trim())
        route.paradasIntermedias.split(',')
            .map { it.trim() }.filter { it.isNotEmpty() }
            .forEach { stopNamesInOrder.add(it) }
        stopNamesInOrder.add(route.paradaFinal.trim())

        val waypointsLatLng = mutableListOf<LatLng>() // Coordenadas para la API
        val boundsBuilder = LatLngBounds.Builder()

        // 2. Dibujar marcadores y obtener coordenadas
        stopNamesInOrder.forEach { stopName ->
            val parada = allStopsMap[stopName.lowercase()]
            if (parada != null) {
                try {
                    val coords = parada.coordenadas.split(",").map { it.trim().toDouble() }
                    if (coords.size == 2) {
                        val stopLatLng = LatLng(coords[0], coords[1])
                        val marker = mMap?.addMarker(
                            MarkerOptions()
                                .position(stopLatLng)
                                .title(parada.nombreParada)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                        marker?.let {
                            stopMarkers.add(it)
                            boundsBuilder.include(stopLatLng)
                            waypointsLatLng.add(stopLatLng)
                        }
                    }
                } catch (e: Exception) { Log.e("drawRouteOnMap", "Invalid coordinates for stop: ${parada.nombreParada}", e) }
            } else {
                Log.w("drawRouteOnMap", "Stop not found in allStopsMap: $stopName")
            }
        }

        // 3. Llamar a Directions API si hay al menos 2 puntos
        if (waypointsLatLng.size >= 2) {
            val origin = waypointsLatLng.first()
            val destination = waypointsLatLng.last()
            // Waypoints intermedios (todos excepto el primero y el último)
            val intermediateWaypoints = if (waypointsLatLng.size > 2) waypointsLatLng.subList(1, waypointsLatLng.size - 1) else emptyList()

            // Convertir a formato de la librería google-maps-services
            val apiOrigin = com.google.maps.model.LatLng(origin.latitude, origin.longitude)
            val apiDestination = com.google.maps.model.LatLng(destination.latitude, destination.longitude)
            val apiWaypoints = intermediateWaypoints.map { com.google.maps.model.LatLng(it.latitude, it.longitude) }.toTypedArray()

            Log.d("DirectionsApi", "Requesting directions from ${apiOrigin} to ${apiDestination} via ${apiWaypoints.size} waypoints")

            // Ejecutar en hilo secundario
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = DirectionsApi.newRequest(geoApiContext)
                        .origin(apiOrigin)
                        .destination(apiDestination)
                        .waypoints(*apiWaypoints) // El '*' es importante aquí
                        .mode(com.google.maps.model.TravelMode.DRIVING) // Modo de viaje
                        .await() // Ejecuta la llamada

                    // Volver al hilo principal para actualizar UI (mapa)
                    withContext(Dispatchers.Main) {
                        if (result.routes.isNotEmpty() && result.routes[0].overviewPolyline != null) {
                            val decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)
                            Log.d("DirectionsApi", "Route found with ${decodedPath.size} points")
                            val polylineOptions = PolylineOptions()
                                .addAll(decodedPath)
                                .color(Color.BLUE) // Color de la ruta
                                .width(12f)       // Grosor
                            routePolyline = mMap?.addPolyline(polylineOptions)

                            // Ajustar zoom a los marcadores (ya se hace abajo)
                            // val bounds = boundsBuilder.build()
                            // mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

                        } else {
                            Log.w("DirectionsApi", "No routes found in API response.")
                            Toast.makeText(this@DriverDashboardActivity, "No se encontró ruta por calles", Toast.LENGTH_SHORT).show()
                            drawStraightPolyline(waypointsLatLng) // Fallback a línea recta
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DirectionsApi", "Error fetching directions", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DriverDashboardActivity, "Error al obtener ruta: ${e.message}", Toast.LENGTH_LONG).show()
                        drawStraightPolyline(waypointsLatLng) // Fallback a línea recta
                    }
                }
            }
        } else if (waypointsLatLng.size == 1) {
            // Si solo hay una parada, simplemente centrar en ella
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(waypointsLatLng[0], 15f))
        }


        // 4. Ajustar cámara para mostrar todos los marcadores (se ejecuta siempre)
        if (stopMarkers.isNotEmpty()) {
            try {
                val bounds = boundsBuilder.build()
                // Usar post para asegurar que el mapa ya tenga dimensiones
                mMap?.let { map ->
                    // Damos un pequeño delay para asegurar que el layout esté listo
                    map.setOnMapLoadedCallback {
                        try { // Doble try-catch por si acaso
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)) // 150px padding
                        } catch (e: Exception) {
                            Log.e("MapZoom", "Error animating camera bounds after map loaded", e)
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                Log.e("MapZoom", "Error building bounds or animating camera", e)
                // Fallback si falla el bounds (ej, solo 1 punto)
                if (stopMarkers.size == 1) {
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(stopMarkers[0].position, 15f))
                }
            }
        }
    }

    // Dibuja una línea recta como fallback
    private fun drawStraightPolyline(points: List<LatLng>) {
        if (mMap != null && points.size >= 2) {
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .color(Color.GRAY) // Color distinto para indicar fallback
                .width(8f)
            routePolyline = mMap?.addPolyline(polylineOptions)
        }
    }

    // --- Configuración de Botones ---
    private fun setupButtonClickListeners() {
        switchShareLocation.setOnCheckedChangeListener { _, isChecked ->
            isSharingLocation = isChecked
            if (isSharingLocation) {
                startLocationUpdatesIfPermitted()
            } else {
                stopLocationUpdates()
            }
        }

        btnToggleTrip.setOnClickListener {
            if (selectedRoute == null) {
                Toast.makeText(this, "Por favor, seleccione una ruta primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isTripActive = !isTripActive
            updateTripButtonState()

            if (isTripActive) {
                if (!isSharingLocation) {
                    switchShareLocation.isChecked = true // Activa compartir al iniciar
                }
                Toast.makeText(this, "Viaje iniciado para la ruta ${selectedRoute?.numeroRuta}", Toast.LENGTH_SHORT).show()
                // Guardar estado en Firebase
                baseDatos.child("Conductores").child(conductorId).child("viajeActivo").setValue(true)
                baseDatos.child("Conductores").child(conductorId).child("rutaActualId").setValue(selectedRoute?.id)

            } else {
                if (isSharingLocation) {
                    switchShareLocation.isChecked = false // Desactiva compartir al finalizar
                }
                Toast.makeText(this, "Viaje finalizado", Toast.LENGTH_SHORT).show()
                // Actualizar estado en Firebase
                baseDatos.child("Conductores").child(conductorId).child("viajeActivo").setValue(false)
                baseDatos.child("Conductores").child(conductorId).child("rutaActualId").setValue(null)
                // Limpiar selección local y mapa
                selectedRoute = null
                spinnerRoutes.setSelection(0)
                clearStopsAndRoute()
            }
        }

        btnLogout.setOnClickListener {
            logoutDriver()
        }
    }

    // Actualiza el texto y color del botón Iniciar/Finalizar
    private fun updateTripButtonState() {
        if (isTripActive) {
            btnToggleTrip.text = "Finalizar Viaje"
            try { btnToggleTrip.setBackgroundColor(ContextCompat.getColor(this, R.color.design_default_color_error)) }
            catch (e: Exception) { btnToggleTrip.setBackgroundColor(Color.RED) }
            spinnerRoutes.isEnabled = false // No cambiar ruta durante viaje
        } else {
            btnToggleTrip.text = "Iniciar Viaje"
            try { btnToggleTrip.setBackgroundColor(ContextCompat.getColor(this, R.color.design_default_color_primary)) }
            catch (e: Exception) { btnToggleTrip.setBackgroundColor(Color.BLUE) }
            spinnerRoutes.isEnabled = true // Sí cambiar ruta antes de iniciar
        }
    }

    // --- Lógica de Ubicación ---
    // Configura el callback para recibir actualizaciones de ubicación
    private fun setupLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    // No centrar automáticamente si el viaje está activo para no perder la vista de la ruta
                    if (!isTripActive && mMap != null) {
                        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                    }

                    // Guardar en Firebase solo si se está compartiendo
                    if (isSharingLocation && conductorId.isNotEmpty()) {
                        val locationData = mapOf(
                            "latitud" to location.latitude,
                            "longitud" to location.longitude,
                            "timestamp" to ServerValue.TIMESTAMP,
                            "rutaId" to selectedRoute?.id // Guarda la ruta actual
                        )
                        baseDatos.child("UbicacionesConductores").child(conductorId).setValue(locationData)
                    }
                }
            }
        }
    }

    // Inicia las actualizaciones si tiene permiso
    private fun startLocationUpdatesIfPermitted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            // Si intenta iniciar sin permiso, revierte el switch y pide permiso
            isSharingLocation = false
            if(::switchShareLocation.isInitialized) switchShareLocation.isChecked = false // Check if initialized
            checkLocationPermission()
        }
    }

    // Comienza a pedir actualizaciones de ubicación
    private fun startLocationUpdates() {
        // Configuración de la petición de ubicación
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Intervalo deseado
            fastestInterval = 3000 // Intervalo mínimo
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Máxima precisión
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            mMap?.isMyLocationEnabled = true // Muestra punto azul
            Log.d("LocationUpdates", "Starting location updates")
            // Toast.makeText(this, "Compartiendo ubicación...", Toast.LENGTH_SHORT).show() // Evitar spam de toasts
        } catch (e: SecurityException) {
            Log.e("LocationUpdates", "SecurityException starting updates", e)
            Toast.makeText(this, "Error de seguridad al iniciar ubicación.", Toast.LENGTH_SHORT).show()
            isSharingLocation = false
            if(::switchShareLocation.isInitialized) switchShareLocation.isChecked = false
        }
    }

    // Detiene las actualizaciones de ubicación
    private fun stopLocationUpdates() {
        try{
            fusedLocationClient.removeLocationUpdates(locationCallback)
            mMap?.isMyLocationEnabled = false // Oculta punto azul (opcional)
            Log.d("LocationUpdates", "Stopping location updates")
            Toast.makeText(this, "Se dejó de compartir ubicación", Toast.LENGTH_SHORT).show()
        } catch (e: Exception){
            Log.e("LocationUpdates", "Exception stopping updates", e)
        }
        // Opcional: Eliminar última ubicación de Firebase
        // baseDatos.child("UbicacionesConductores").child(conductorId).removeValue()
    }


    // --- Permisos y Logout ---
    // Verifica si tiene permiso de ubicación, si no, lo pide
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Si tiene permiso, habilita la capa "Mi Ubicación" y centra si no hay viaje activo
            try {
                mMap?.isMyLocationEnabled = true
                if (isSharingLocation) {
                    startLocationUpdates() // Si ya estaba compartiendo, inicia de nuevo
                } else {
                    // Mueve la cámara a la última ubicación conocida solo si no hay viaje activo
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            if (!isTripActive && mMap != null) {
                                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                            }
                        }
                    }
                }
            } catch (se: SecurityException) {
                Log.e("LocationPermission", "SecurityException enabling location layer", se)
                Toast.makeText(this, "No se pudo habilitar 'Mi Ubicación'", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Maneja la respuesta del usuario a la petición de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si concede permiso, habilita capa y reinicia updates si es necesario
                try {
                    mMap?.isMyLocationEnabled = true
                    if (isSharingLocation) { // Si el switch estaba on, intenta iniciar updates
                        startLocationUpdates()
                    } else { // Si no, solo centra si no hay viaje
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                if (!isTripActive && mMap != null){
                                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                                }
                            }
                        }
                    }
                } catch (se: SecurityException) { Log.e("LocationPermission", "SecurityException after permission granted", se) }
            } else {
                // Si niega permiso, informa y asegura que el switch esté apagado
                Toast.makeText(this, "Permiso de ubicación denegado. Funcionalidad limitada.", Toast.LENGTH_LONG).show()
                isSharingLocation = false
                if(::switchShareLocation.isInitialized) switchShareLocation.isChecked = false
            }
        }
    }

    // Cierra sesión y vuelve al Login
    private fun logoutDriver() {
        stopLocationUpdates() // Detiene updates antes de salir
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- Ciclo de Vida ---
    // Detiene updates en pausa si se está compartiendo
    override fun onPause() {
        super.onPause()
        if (isSharingLocation) {
            stopLocationUpdates()
        }
    }

    // Reanuda updates al volver si se estaba compartiendo
    override fun onResume() {
        super.onResume()
        // Solo intenta reanudar si el switch está activado
        if (isSharingLocation) {
            startLocationUpdatesIfPermitted()
        }
    }
}