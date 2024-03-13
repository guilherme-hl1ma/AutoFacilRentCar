package br.com.guilhermehl1ma.autofacil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import br.com.guilhermehl1ma.autofacil.databinding.ActivitySignUpScreenBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions

    var number: String = ""

    // we will use this to match the sent otp from firebase
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        functions = Firebase.functions("southamerica-east1")

        clickOnSignUp()

        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
                Log.d("GFG", "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("GFG", "onVerificationFailed  $e")
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("GFG", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
                val intent = Intent(applicationContext, OtpActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun clickOnSignUp() {
        binding.btnSignUp.setOnClickListener {
            if (validateFields()) {
                number = "+55${binding.inputPhoneNumber.text?.trim().toString()}"

                if (auth.currentUser?.isAnonymous == true) {
                    linkWithCredential()
                } else {
                    createUserAccount(
                        binding.inputEmail.text.toString(),
                        binding.inputPassword.text.toString()
                    )
                }
                //sendVerificationCode(number)
                addNewUser(
                    binding.inputEmail.text.toString(),
                    binding.inputPassword.text.toString()
                )
            } else {
                binding.messageError.text = "Fill the fields"
                binding.messageError.visibility = View.VISIBLE
            }
        }
    }

    private fun createUserAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { register ->
                if (register.isSuccessful) {
                    sendVerificationEmail()
                } else {
                    Log.e("createUserAccount", "Failure", register.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun linkWithCredential() {
        val credential = EmailAuthProvider.getCredential(
            binding.inputEmail.text.toString(),
            binding.inputPassword.text.toString()
        )

        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("linkWithCredential", "Success")
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

    private fun sendVerificationEmail() {
        val user = auth.currentUser

        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Please verify your email, clicking on the link sent.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("sendEmailVerification", "Verification Email sent")
                    goToLoginScreen()
                } else {
                    Log.e("sendEmailVerification", "Failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun addNewUser(email: String, password: String) {
        val user = auth.currentUser
        val completeName = binding.inputName.text.toString() + binding.inputSurname.text.toString()
        val data = hashMapOf(
            "userUid" to user?.uid,
            "email" to email,
            "senha" to password,
            "nome" to completeName,
            "cpf" to binding.inputCpf.text.toString(),
            "dataNascimento" to binding.inputBirthDate.text.toString(),
            "telefone" to binding.inputPhoneNumber.text.toString()
        )

        functions
            .getHttpsCallable("users-addNewUser")
            .call(data)
            .addOnSuccessListener { result ->
                val dataMap = result.data as? Map<*, *>

                if (dataMap != null) {
                    val status = dataMap["status"] as? String
                    val message = dataMap["message"] as? String

                    if (status == "SUCCESS") {
                        // User inserted
                        Log.d("addNewUser", "User successfully inserted")
                        // Access payload if necessary
                    } else {
                        // Error to insert user
                        Log.w("addNewUser", "Error to insert user: $message")
                    }
                } else {
                    Log.w("addNewUser", "Error to process Cloud Function data")
                }
            }
            .addOnFailureListener { exception ->
                // Error cloud function
                Log.e("addNewUser", "Error to call addNewUser Function", exception)
            }

    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("GFG", "Auth started")
    }


    private fun validateFields(): Boolean {
        var isValid = true
        if (binding.inputName.text.toString().isEmpty()) {
            isValid = false
        }

        if (binding.inputSurname.text.toString().isEmpty()) {
            isValid = false
        }

        if (binding.inputCpf.text.toString().isEmpty()) {
            isValid = false
        }

        if (binding.inputBirthDate.text.toString().isEmpty()) {
            isValid = false
        }

        if (binding.inputPhoneNumber.text.toString().isEmpty()) {
            isValid = false
        }

        if (binding.inputEmail.text.toString().isEmpty()) {
            isValid = false
        }

        if (binding.inputPassword.text.toString().isEmpty()) {
            isValid = false
        }

        return isValid
    }

    private fun goToLoginScreen() {
        val loginScreen = Intent(this, LoginActivity::class.java)
        startActivity(loginScreen)
    }
}