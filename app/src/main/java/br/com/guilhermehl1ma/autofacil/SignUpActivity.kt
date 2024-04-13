package br.com.guilhermehl1ma.autofacil

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.guilhermehl1ma.autofacil.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import java.util.concurrent.TimeUnit


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions


    var number: String = ""

    private lateinit var callbacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        functions = Firebase.functions("southamerica-east1")

        binding.radioSms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioEmail.isChecked = false
            }
        }

        binding.radioEmail.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.radioSms.isChecked = false
            }
        }

        binding.btnSignUp.setOnClickListener {
            if (validateFields()) {
                number = "+55${binding.inputPhoneNumber.text?.trim().toString()}"
                createUserAccount(
                    binding.inputEmail.text.toString(),
                    binding.inputPassword.text.toString()
                )
            } else {
                binding.messageError.text = "Fill the fields"
                binding.messageError.visibility = View.VISIBLE
            }
        }

        callbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun
                    onVerificationCompleted(
                credential:
                PhoneAuthCredential
            ) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
                Log.d("onVerificationCompleted", "Success")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("onVerificationFailed", "onVerificationFailed  $e")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("onCodeSent", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                val intent = Intent(applicationContext, OtpActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                startActivity(intent)
                finish()
            }
        }
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

        if (!binding.radioSms.isChecked && !binding.radioEmail.isChecked) {
            isValid = false
        }

        return isValid
    }

    private fun createUserAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { register ->
                if (register.isSuccessful) {
                    addNewUser(email, password)
                    if (binding.radioEmail.isChecked) {
                        sendVerificationEmail()
                    } else {
                        sendVerificationCode(number)
                    }
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
        Log.d("sendVerificationCode", "Auth started")
    }

    private fun goToLoginScreen() {
        val loginScreen = Intent(this, LoginActivity::class.java)
        startActivity(loginScreen)
    }

}