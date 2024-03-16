package br.com.guilhermehl1ma.autofacil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.widget.Toast
import br.com.guilhermehl1ma.autofacil.databinding.ActivityOtpScreenBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        functions = Firebase.functions("southamerica-east1")

        // get storedVerificationId from the intent
        val storedVerificationId = intent.getStringExtra("storedVerificationId")

        // fill otp and call the on click on button
        binding.btnLogin.setOnClickListener {
            val otp = binding.etOtp.text.trim().toString()
            if(otp.isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp)
                linkWithCredential(credential)
            }else{
                Toast.makeText(this,"Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun linkWithCredential(credential: PhoneAuthCredential) {
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("linkWithCredential", "Success")
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Log.w("linkWithCredential", "Failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    setSMSVerified(credential)
                    val intent = Intent(this , MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    Log.d("signInWithPhoneAuthCredential", "SUCCESS")
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this,"Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun setSMSVerified(phoneCredential: PhoneAuthCredential) {
        val user = Firebase.auth.currentUser

        user!!.updatePhoneNumber(phoneCredential)
    }
}