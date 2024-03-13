package br.com.guilhermehl1ma.autofacil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import br.com.guilhermehl1ma.autofacil.databinding.ActivityMainScreenBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    // declare an instance of FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize Firebase Auth
        auth = Firebase.auth

        if (auth.currentUser?.isAnonymous == true) {
            binding.textLoginCompleted.text = "Anonymous user"
            binding.btnSignUpAnonymous.visibility = View.VISIBLE
        }

        clickSignOut()
        clickSignUp()

        binding.btnLocation.setOnClickListener{
            goToLocation()
        }
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
                baseContext,
                "User signed out",
                Toast.LENGTH_SHORT
            ).show()
        }

        backToLoginScreen()
    }


    private fun backToLoginScreen() {
        val loginScreen = Intent(this, LoginActivity::class.java)
        startActivity(loginScreen)
        finish()
    }

    private fun goToSignUpScreen() {
        val signUpScreen = Intent(this, SignUpActivity::class.java)
        startActivity(signUpScreen)
        finish()
    }

    private fun goToLocation() {
        val locationScreen = Intent(this, LocationActivity::class.java)
        startActivity(locationScreen)
        finish()
    }

    private fun deleteUser()
    {
        val user = Firebase.auth.currentUser!!

        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("deleteUser", "User account deleted.")
                }
            }
    }
}