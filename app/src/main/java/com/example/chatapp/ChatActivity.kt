package com.example.chatapp

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.Model.RoomChat
import com.example.chatapp.Model.messageModel
import com.example.chatapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val dbFireStore = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    private val rtDb = Firebase.database
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
        auth = Firebase.auth
        initUI()
        setContentView(view)
    }

    private fun initUI() {
        val imageUserChat : ImageView = binding.imageUserChatView
        val nameUserChat : TextView = binding.editTextChatUserName
        val clickToBackActivity : ImageButton = binding.clickToBackFromChatToActivity
        val editTextInputMess : EditText = binding.editTextMessUserChat
        val clickToSendMessage : ImageButton = binding.btnSendMessChat

        val bundle = intent.extras // Lấy bundle từ intent
        if (bundle != null) {
            uidPersonal = bundle.getString("uidPersonal")
        }

        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val outRect = Rect()
                editTextInputMess.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    editTextInputMess.clearFocus()
                    hideKeyboard()
                }
            }
            false
        }

        editTextInputMess.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val layoutParams = view.layoutParams as LinearLayout.LayoutParams
                layoutParams.weight = 0f
                view.layoutParams = layoutParams
            } else {
                val layoutParams = view.layoutParams as LinearLayout.LayoutParams
                layoutParams.weight = 1f
                view.layoutParams = layoutParams
            }
        }

        clickToSendMessage.setOnClickListener {
            val contentMessage = editTextInputMess.text.toString()
            addDataMess(contentMessage, editTextInputMess)
        }

        clickToBackActivity.setOnClickListener {
            val i = Intent(this@ChatActivity, UserActivity::class.java)
            startActivity(i)
            finish()
        }



        getDataUserChat(imageUserChat, nameUserChat, uidPersonal)
    }

    private fun addDataMess(contentMessage: String, editTextInputMess : EditText) {
        if (uidPersonal != null) {
            val roomChatId = "${auth.currentUser?.uid}_${uidPersonal}"
            val message = messageModel(
                auth.currentUser?.uid,
                uidPersonal,
                contentMessage,
                "text",
                SimpleDateFormat("HH:mm dd/MM/yyyy").format(Calendar.getInstance().time)
            )

            // Check if room chat exists, if not create one
            dbFireStore.collection("RoomChat")
                .whereIn("idRoomChat", listOf(roomChatId, "${uidPersonal}_${auth.currentUser?.uid}"))
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // Create a new room chat
                        val data = RoomChat(roomChatId, auth.currentUser?.uid, uidPersonal)
                        val roomChatData = hashMapOf(
                            "idRoomChat" to data.idRoomChat,
                            "idUser1" to data.idUser1,
                            "idUser2" to data.idUser2
                        )
                        dbFireStore.collection("RoomChat").add(roomChatData)
                            .addOnSuccessListener { documentReference ->
                                val messageId = rtDb.getReference("Messages/$roomChatId").push().key ?: ""
                                rtDb.getReference("Messages/$roomChatId/$messageId").setValue(message).addOnSuccessListener { editTextInputMess.setText("") }
                            }
                    } else {
                        // Use the existing room chat
                        val messageId = rtDb.getReference("Messages/$roomChatId").push().key ?: ""
                        rtDb.getReference("Messages/$roomChatId/$messageId").setValue(message).addOnSuccessListener { editTextInputMess.setText("")}
                    }
                }
        }
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
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.editTextMessUserChat.clearFocus()
    }
}