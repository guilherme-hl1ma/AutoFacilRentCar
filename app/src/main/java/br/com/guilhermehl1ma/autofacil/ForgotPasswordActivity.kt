package br.com.guilhermehl1ma.autofacil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.guilhermehl1ma.autofacil.databinding.ActivityForgotPasswordScreenBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var strEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnReset.setOnClickListener {
            strEmail = binding.edtForgotPasswordEmail.text.toString().trim()
            if (strEmail.isNotEmpty()) {
                sendPasswordResetEmail()
            } else {
                binding.edtForgotPasswordEmail.error = "Email field can't be empty"
            }
        }

        binding.btnForgotPasswordBack.setOnClickListener {
            backToLoginScreen()
        }
    }

    private fun sendPasswordResetEmail() {
        binding.forgetPasswordProgressbar.visibility = View.VISIBLE
        binding.btnReset.visibility = View.INVISIBLE

        auth.sendPasswordResetEmail(strEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Reset Password link has been sent to your registered Email",
                        Toast.LENGTH_SHORT
                    ).show()
                    backToLoginScreen()
                    Log.d("sendPasswordResetEmail", "Reset password email sent.")
                } else {
                    Toast.makeText(
                        baseContext,
                        "Error :- ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.forgetPasswordProgressbar.visibility = View.INVISIBLE
                    binding.btnReset.visibility = View.VISIBLE
                }
            }
    }

    private fun backToLoginScreen() {
        val loginScreen = Intent(this, LoginActivity::class.java)
        startActivity(loginScreen)
        finish()
    }
}