package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.databinding.ActivityPersonalPageBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PersonalPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalPageBinding
    private val dbFireStore = Firebase.firestore
    private val requestOptions = RequestOptions()
        .override(120,120)
        .centerCrop()
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)
    private var uidPersonal: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initUI()
    }

    private fun initUI() {
        val personalImage : ImageView = binding.imageUserPage
        val namePersonal : TextView = binding.editNameUserPage
        val clickToPageMess : Button = binding.btnSendMessUserPage
        val clickToBackActivity : ImageButton = binding.btnBackFromPageToActivity

        val bundle = intent.extras // Lấy bundle từ intent
        if (bundle != null) {
            uidPersonal = bundle.getString("uidPersonal")
        }

        clickToBackActivity.setOnClickListener {
            val i = Intent(this@PersonalPageActivity, UserActivity::class.java)
            startActivity(i)
            finish()
        }

        clickToPageMess.setOnClickListener {
            if (uidPersonal != null) {
                val i = Intent(this@PersonalPageActivity, ChatActivity::class.java)
                i.putExtra("uidPersonal", uidPersonal)
                startActivity(i)
                finish()
            }
        }
        uidPersonal?.let { getDataPersonal(personalImage, namePersonal, it) }
    }

    private fun getDataPersonal(personalImage : ImageView, namePersonal : TextView, uidPersonal : String) {
        dbFireStore.collection("users")
            .whereEqualTo("uid", uidPersonal)
            .get()
            .addOnSuccessListener {
                    document ->
                for (value in document) {
                    Glide.with(this)
                        .load(value.getString("urlImage"))
                        .apply(requestOptions)
                        .into(personalImage)
                    namePersonal.text = value.getString("name")
                }
            }
    }
}
