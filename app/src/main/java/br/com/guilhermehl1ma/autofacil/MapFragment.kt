package br.com.guilhermehl1ma.autofacil

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.guilhermehl1ma.autofacil.databinding.FragmentMapBinding
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var functions: FirebaseFunctions
    private lateinit var auth: FirebaseAuth
    private var rentalUnits: MutableList<Map<*, *>> = mutableListOf()
    private var map: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        functions = Firebase.functions("southamerica-east1")
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        loadRentalUnits()
    }

    private fun loadRentalUnits() {
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
                    map?.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title(name)
                            .snippet(description)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
