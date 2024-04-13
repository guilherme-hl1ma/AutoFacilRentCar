package br.com.guilhermehl1ma.autofacil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import br.com.guilhermehl1ma.autofacil.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        if (auth.currentUser?.isAnonymous == true) {
            binding.textLoginCompleted.text = "Anonymous user"
            binding.btnSignUpAnonymous.visibility = View.VISIBLE
        }

        clickSignOut()
        clickSignUp()
    }

    private fun clickSignOut() {
        binding.btnSignOut.setOnClickListener {
            signOutUser()
        }
    }

    private fun clickSignUp() {
        binding.btnSignUpAnonymous.setOnClickListener {
            goToSignUpScreen()
        }
    }

    private fun signOutUser() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            if (currentUser.isAnonymous) {
                deleteUser()
            }

            auth.signOut()
            Log.d("signOut", "Success")
            Toast.makeText(
                requireContext(),
                "User signed out",
                Toast.LENGTH_SHORT
            ).show()
        }

        backToLoginScreen()
    }

    private fun backToLoginScreen() {
        val loginScreen = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(loginScreen)
    }

    private fun goToSignUpScreen() {
        val signUpScreen = Intent(requireActivity(), SignUpActivity::class.java)
        startActivity(signUpScreen)
    }

    private fun deleteUser() {
        val user = Firebase.auth.currentUser!!

        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("deleteUser", "User account deleted.")
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}