package br.com.guilhermehl1ma.autofacil

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import br.com.guilhermehl1ma.autofacil.databinding.ActivityVerifyAccountBinding

class VerifyAccount : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityVerifyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}