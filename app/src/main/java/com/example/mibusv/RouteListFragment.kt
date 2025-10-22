package com.example.mibusv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class RouteListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var routeAdapter: UserRouteAdapter
    private val fullRouteList = mutableListOf<Route>() // Lista completa
    private val filteredRouteList = mutableListOf<Route>() // Lista para mostrar
    private lateinit var baseDatos: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_route_list, container, false)

        recyclerView = view.findViewById(R.id.routesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // El adaptador ahora usa la lista filtrada y tiene un listener de clic
        routeAdapter = UserRouteAdapter(filteredRouteList) { route ->
            // Acción al hacer clic en una ruta
            val intent = Intent(activity, RouteDetailActivity::class.java)
            intent.putExtra("ROUTE_ID", route.id) // Pasamos el ID de la ruta
            startActivity(intent)
        }
        recyclerView.adapter = routeAdapter

        baseDatos = FirebaseDatabase.getInstance().reference
        cargarRutas()

        return view
    }

    private fun cargarRutas() {
        baseDatos.child("Rutas").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullRouteList.clear()
                for (routeSnapshot in snapshot.children) {
                    val route = routeSnapshot.getValue(Route::class.java)
                    route?.let { fullRouteList.add(it) }
                }
                filterRoutes("") // Mostrar todas al principio
            }
            override fun onCancelled(error: DatabaseError) { /* Manejar error */ }
        })
    }

    // Nueva función para filtrar la lista
    fun filterRoutes(query: String) {
        filteredRouteList.clear()
        if (query.isEmpty()) {
            filteredRouteList.addAll(fullRouteList)
        } else {
            filteredRouteList.addAll(fullRouteList.filter {
                it.nombreRuta.contains(query, ignoreCase = true) ||
                        it.numeroRuta.contains(query, ignoreCase = true) ||
                        it.paradaInicial.contains(query, ignoreCase = true) ||
                        it.paradaFinal.contains(query, ignoreCase = true)
            })
        }
        routeAdapter.notifyDataSetChanged()
    }
}