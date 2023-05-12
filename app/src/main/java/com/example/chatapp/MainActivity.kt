package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initUI()
    }

    private fun initUI() {
        binding.moveToLogin.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null) {
            val i = Intent(this, UserActivity::class.java)
            startActivity(i)
            finish()
        }
    }

}