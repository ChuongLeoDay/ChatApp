package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.databinding.ActivityChatBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val dbFireStore = Firebase.firestore
    private val requestOptions = RequestOptions()
        .override(120,120)
        .centerCrop()
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)
    private var uidPersonal: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        val view = binding.root
        initUI()
        setContentView(view)
    }

    private fun initUI() {
        val imageUserChat : ImageView = binding.imageUserChatView
        val nameUserChat : TextView = binding.editTextChatUserName
        val clickToBackActivity : ImageButton = binding.clickToBackFromChatToActivity

        clickToBackActivity.setOnClickListener {
            val i = Intent(this@ChatActivity, UserActivity::class.java)
            startActivity(i)
            finish()
        }

        val bundle = intent.extras // Lấy bundle từ intent
        if (bundle != null) {
            uidPersonal = bundle.getString("uidPersonal")
        }

        getDataUserChat(imageUserChat, nameUserChat, uidPersonal)
    }

    private fun getDataUserChat(
        imageUserChat: ImageView,
        nameUserChat: TextView,
        uidPersonal: String?
    ) {
        dbFireStore.collection("users")
            .whereEqualTo("uid", uidPersonal)
            .get()
            .addOnSuccessListener {
                    document ->
                for (value in document) {
                    Glide.with(this)
                        .load(value.getString("urlImage"))
                        .apply(requestOptions)
                        .into(imageUserChat)
                    nameUserChat.text = value.getString("name")
                }
            }
    }
}