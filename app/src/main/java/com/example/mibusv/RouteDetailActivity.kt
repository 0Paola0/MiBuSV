package com.example.mibusv

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.database.*

class RouteDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var baseDatos: DatabaseReference
    private var mMap: GoogleMap? = null
    private var routeId: String? = null
    private var currentRoute: Route? = null // Para almacenar datos de la ruta

    // Vistas del layout
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var tvRouteNumberName: TextView
    private lateinit var tvRouteSchedule: TextView
    private lateinit var tvRouteFrequency: TextView
    private lateinit var tvRoutePrice: TextView
    private lateinit var tvRouteStopsList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Botón de regreso

        collapsingToolbar = findViewById(R.id.toolbar_layout)
        tvRouteNumberName = findViewById(R.id.detailRouteNumberName)
        tvRouteSchedule = findViewById(R.id.detailRouteSchedule)
        tvRouteFrequency = findViewById(R.id.detailRouteFrequency)
        tvRoutePrice = findViewById(R.id.detailRoutePrice)
        tvRouteStopsList = findViewById(R.id.detailRouteStopsList)

        baseDatos = FirebaseDatabase.getInstance().reference
        routeId = intent.getStringExtra("ROUTE_ID")

        if (routeId == null) {
            Toast.makeText(this, "Error: No se recibió ID de ruta", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar datos de la ruta
        loadRouteDetails()

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.detail_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun loadRouteDetails() {
        baseDatos.child("Rutas").child(routeId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentRoute = snapshot.getValue(Route::class.java)
                if (currentRoute != null) {
                    displayRouteDetails(currentRoute!!)
                    // Si tienes coordenadas de paradas, cárgalas aquí para el mapa
                } else {
                    Toast.makeText(this@RouteDetailActivity, "Ruta no encontrada", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RouteDetailActivity, "Error al cargar ruta", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayRouteDetails(route: Route) {
        val title = "${route.numeroRuta} - ${route.nombreRuta}"
        collapsingToolbar.title = title // Título en la barra colapsada
        tvRouteNumberName.text = title
        tvRouteSchedule.text = "Horario: ${route.horarioOperacion}"
        tvRouteFrequency.text = "Frecuencia: ${route.frecuencia} Minutos"
        tvRoutePrice.text = "Precio: ${route.precio}" // Asumiendo que tienes 'precio'
        // Formatear la lista de paradas (esto es básico, podrías mejorarlo)
        val stopsText = "Inicio: ${route.paradaInicial}\nIntermedias:\n${route.paradasIntermedias.replace(",", "\n")}\nFinal: ${route.paradaFinal}"
        tvRouteStopsList.text = stopsText

        // Intentar centrar el mapa en la primera parada si hay datos
        centerMapOnFirstStop(route.paradaInicial)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.uiSettings?.isMapToolbarEnabled = false // Deshabilitar botones de Google Maps

        // Si los datos de la ruta ya cargaron, centra el mapa
        currentRoute?.let {
            centerMapOnFirstStop(it.paradaInicial)
            // Aquí podrías añadir marcadores para todas las paradas
        }
    }

    // Función básica para centrar mapa (necesitarás coordenadas reales)
    private fun centerMapOnFirstStop(stopName: String) {
        // !!! IMPORTANTE: Necesitarás buscar las coordenadas de la parada en tu nodo "Paradas" !!!
        // Este es solo un ejemplo con coordenadas fijas.
        val defaultLocation = LatLng(13.7942, -88.8965) // Coordenadas genéricas de El Salvador
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
        // Cuando tengas las coordenadas reales de la parada, úsalas aquí.
        // mMap?.addMarker(MarkerOptions().position(coordenadasReales).title(stopName))
    }

    // Para el botón de regreso en la barra
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}