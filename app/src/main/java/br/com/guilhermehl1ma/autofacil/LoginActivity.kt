package br.com.guilhermehl1ma.autofacil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.guilhermehl1ma.autofacil.databinding.ActivityLoginScreenBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var auth: FirebaseAuth // declare an instance of FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize Firebase Auth
        auth = Firebase.auth

        // user already logged in immediately go to main screen
        if (auth.currentUser?.isEmailVerified == true) {
            goToSuccessLogin()
        }

        binding.btnSignIn.setOnClickListener {
            if (binding.inputEmail.text?.isNotEmpty() == true && binding.inputPassword.text?.isNotEmpty() == true) {
                signInApp(binding.inputEmail.text.toString(), binding.inputPassword.text.toString())
            } else {
                binding.messageError.text = "Fill the fields"
                binding.messageError.visibility = View.VISIBLE
            }
        }

        binding.btnSignUpNewAccount.setOnClickListener {
            goToRegister()
        }

        binding.btnJoinAsGuest.setOnClickListener {
            signInAnonymously()
        }

        binding.textForgotPassword.setOnClickListener {
            val forgotPasswordScreen = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(forgotPasswordScreen)
            finish()
        }
    }

    private fun signInApp(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("signInWithEmail", "SUCCESS")
                    val user = Firebase.auth.currentUser
                    if (user?.isEmailVerified == true) {
                        goToSuccessLogin()
                    } else {
                        binding.messageError.text = "Please verify your email before sign in"
                        binding.messageError.visibility = View.VISIBLE
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("signInWithEmail", "Failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    binding.messageError.text = "User doesn't exist"
                    binding.messageError.visibility = View.VISIBLE
                }
            }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("signInAnonymously", "Success")
                goToLocationMap()
            } else {
                // If sign in fails, display a message to the user.
                Log.w("signInAnonymously", "Failure", task.exception)
                Toast.makeText(
                    baseContext,
                    "Authentication failed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun goToRegister() {
        val registerScreen = Intent(this, SignUpActivity::class.java)
        startActivity(registerScreen)
        finish()
    }

    private fun goToSuccessLogin() {
        val successLoginScreen = Intent(this, MainActivity::class.java)
        startActivity(successLoginScreen)
        finish()
    }

    private fun goToLocationMap() {
        val locationScreen = Intent(this, LocationActivity::class.java)
        startActivity(locationScreen)
        finish()
    }
}