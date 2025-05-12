package com.anviam.fragmentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anviam.fragmentapp.databinding.ActivityViewBindingBinding
import com.anviam.fragmentapp.model.RegisterDetails
import com.anviam.fragmentapp.services.musicServices

class ViewBindingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityViewBindingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlay.setOnClickListener{
                startService(Intent(this@ViewBindingActivity, musicServices::class.java))
        }
        binding.btnStop.setOnClickListener {
                stopService(Intent(this@ViewBindingActivity, musicServices::class.java))
        }
        binding.btnPause.setOnClickListener {
                onPause()
        }

        val dataObj = RegisterDetails("ali","ali@gmail.com", "1010101")
        binding.registerDetails = dataObj

        binding.btnRegister.setOnClickListener {
            Toast.makeText(this, "sign in successfully", Toast.LENGTH_SHORT).show()
        }

    }
}