package com.example.mibusv;



import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class AdminMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var baseDatos: DatabaseReference
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapa) // Usa el layout generado para el mapa

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        baseDatos = FirebaseDatabase.getInstance().reference

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar el mapa para que se pueda hacer zoom
        mMap.uiSettings.isZoomControlsEnabled = true

        // 1. Pedir permisos y centrar en la ubicación del usuario
        enableMyLocation()

        // 2. Cargar las paradas desde Firebase y dibujarlas
        cargarYDibujarParadas()
    }

    private fun cargarYDibujarParadas() {
        baseDatos.child("Paradas").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (paradaSnapshot in snapshot.children) {
                    val parada = paradaSnapshot.getValue(Parada::class.java) // Reemplaza Parada::class.java con tu modelo de datos
                    parada?.let {
                        try {
                            // Suponiendo que las coordenadas están guardadas como "latitud,longitud"
                            val coords = it.coordenadas.split(",").map { str -> str.trim().toDouble() }
                            if (coords.size == 2) {
                                val paradaLatLng = LatLng(coords[0], coords[1])
                                mMap.addMarker(
                                        MarkerOptions()
                                                .position(paradaLatLng)
                                                .title(it.nombreParada) // Título del marcador
                                                .snippet(it.direccion) // Subtítulo
                                )
                            }
                        } catch (e: Exception) {
                            // Ignorar paradas con coordenadas mal formateadas
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminMapActivity, "Error al cargar paradas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            // Centrar la cámara en la última ubicación conocida
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } else {
            // Si no hay permiso, solicitarlo
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario concede el permiso, habilitamos la ubicación
                enableMyLocation()
            } else {
                Toast.makeText(this, "El permiso de ubicación es necesario para mostrar el mapa.", Toast.LENGTH_LONG).show()
            }
        }
    }
}