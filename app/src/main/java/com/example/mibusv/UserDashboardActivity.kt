package com.example.mibusv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        searchInput = findViewById(R.id.searchRoutesInput)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Listener para la barra de navegación
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    searchInput.visibility = View.GONE // Ocultar búsqueda en el mapa
                    currentFragment = UserMapFragment()
                }
                R.id.nav_routes -> {
                    searchInput.visibility = View.VISIBLE // Mostrar búsqueda en la lista
                    currentFragment = RouteListFragment()
                }
            }
            if (currentFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, currentFragment!!).commit()
            }
            true
        }

        // Carga inicial
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_map // Empezar en el mapa
        }

        // Listener para la barra de búsqueda
        searchInput.addTextChangedListener { text ->
            // Si el fragmento actual es la lista de rutas, le pasamos la búsqueda
            if (currentFragment is RouteListFragment) {
                (currentFragment as RouteListFragment).filterRoutes(text.toString())
            }
        }
    }
}

private fun RouteListFragment.filterRoutes(toString: String) {}
