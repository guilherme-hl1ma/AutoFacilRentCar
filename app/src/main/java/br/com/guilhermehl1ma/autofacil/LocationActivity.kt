package br.com.guilhermehl1ma.autofacil

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.com.guilhermehl1ma.autofacil.databinding.ActivityLocationBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.functions


// Implement OnMapReadyCallback.
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityLocationBinding
    private lateinit var functions: FirebaseFunctions
    private lateinit var auth: FirebaseAuth
    private var rentalUnits: MutableList<Map<*, *>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get a handle to the fragment and register the callback.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        functions = Firebase.functions("southamerica-east1")
        auth = Firebase.auth
    }

    // Get a handle to the GoogleMap object and display marker.
    override fun onMapReady(googleMap: GoogleMap) {
        getRentalUnits().addOnSuccessListener { result ->
            Log.d("getRentalUnits", "Task Successful")
            val data = result.data as? Map<*, *>
            val payload = data?.get("payload") as? List<*>

            payload?.forEach { rentalUnit ->
                if (rentalUnit is Map<*, *>) {
                    val id = rentalUnit["id"] as String
                    val name = rentalUnit["name"] as String
                    val lat = rentalUnit["lat"] as Double
                    val lng = rentalUnit["lng"] as Double
                    val description = rentalUnit["description"] as String
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title(name)
                    )
                }
            }
        }
    }


    private fun getRentalUnits(): Task<HttpsCallableResult> {
        return functions
            .getHttpsCallable("rentalUnits-getRentalUnits")
            .call()
            .addOnFailureListener { exception ->
                Log.e("Error to read the rental units", exception.toString())
            }
    }
}