package com.example.chatapp

import ChatAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.Animation.ProgressDialogUtil
import com.example.chatapp.Model.RoomChat
import com.example.chatapp.Model.messageModel
import com.example.chatapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : AppCompatActivity() {
    private val REQUEST_CODE_PICK_IMAGE = 100
    private var uriImage : Uri? = null
    private lateinit var binding: ActivityChatBinding
    private val dbFireStore = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    private val rtDb = Firebase.database
    private val requestOptions = RequestOptions()
        .override(120,120)
        .centerCrop()
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)
    private var checkRoomChat : Boolean? = null
    private var idRoomChat : String? = null
    private var uidPersonal: String? = null
    private val chatAdapter = ChatAdapter(ArrayList(), this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        val view = binding.root
        auth = Firebase.auth
        initUI()
        checkRoomChat()
        setContentView(view)
    }

    private fun initUI() {
        val imageUserChat : ImageView = binding.imageUserChatView
        val nameUserChat : TextView = binding.editTextChatUserName
        val clickToBackActivity : ImageButton = binding.clickToBackFromChatToActivity
        val clickToSendImageMess : ImageButton = binding.imageBtnChooseMess
        val editTextInputMess : EditText = binding.editTextMessUserChat
        val clickToSendMessage : ImageButton = binding.btnSendMessChat
        val clickToChooseFile : ImageButton = binding.imageChooseFileContent

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


        editTextInputMess.addTextChangedListener {
            if(editTextInputMess.text.toString() != "") {
                clickToChooseFile.visibility = View.GONE
                clickToSendImageMess.visibility = View.GONE
                clickToSendMessage.visibility = View.VISIBLE
            }  else {
                clickToChooseFile.visibility = View.VISIBLE
                clickToSendImageMess.visibility = View.VISIBLE
                clickToSendMessage.visibility = View.GONE
            }
        }

        clickToSendMessage.setOnClickListener {
            val contentMessage = editTextInputMess.text.toString()
            addDataMessTypeText(contentMessage, editTextInputMess)
        }

        clickToSendImageMess.setOnClickListener {
            requestReadExternalStoragePermission()
        }

        clickToBackActivity.setOnClickListener {
            val i = Intent(this@ChatActivity, UserActivity::class.java)
            startActivity(i)
            finish()
        }



        getDataUserChat(imageUserChat, nameUserChat, uidPersonal)
    }

    private fun checkRoomChat() {
        if(uidPersonal != null) {
            dbFireStore.collection("RoomChat")
                .whereIn("idRoomChat", listOf("${auth.currentUser?.uid}_${uidPersonal}", "${uidPersonal}_${auth.currentUser?.uid}"))
                .get()
                .addOnSuccessListener { documents ->
                    if(documents.isEmpty) {
                        checkRoomChat = false
                    }
                    else {
                        for (doc in documents) {
                           idRoomChat = doc.getString("idRoomChat")
                            checkRoomChat = true
                            readChat(idRoomChat)
                        }
                    }
                }
        }
    }

    private fun readChat(idRoomChat: String?) {
        checkAndUpdateReadStatus()
        val rvChat : RecyclerView = binding.rvMessChat
        rvChat.apply { adapter = chatAdapter }
        rvChat.layoutManager = LinearLayoutManager(this)
        val messagesRef = rtDb.getReference("Messages")
        val roomRef = idRoomChat?.let { messagesRef.child(it) }
        roomRef?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val messageList = ArrayList<messageModel>()
                    for (snapMess in snapshot.children) {
                        val message = snapMess.getValue(messageModel::class.java)
                        messageList.add(message!!)
                    }
                    chatAdapter.apply {
                        this.message = messageList
                        notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun checkAndUpdateReadStatus() {
        if (idRoomChat != null) {
            val messageRef = rtDb.getReference("Messages").child(idRoomChat!!)
            messageRef
                .limitToLast(15)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val messages = mutableListOf<messageModel>()

                        // Kiểm tra xem có tồn tại tin nhắn trong node không
                        if (dataSnapshot.exists()) {
                            // Lặp qua danh sách 15 tin nhắn gần nhất (đã được sắp xếp theo thứ tự ngược)
                            for (messageSnapshot in dataSnapshot.children.reversed()) {
                                val message = messageSnapshot.getValue(messageModel::class.java)

                                if (message != null) {
                                    messages.add(message)
                                }
                                // Kiểm tra nếu danh sách tin nhắn không rỗng
                                if (messages.isNotEmpty()) {
                                    val childUpdates = HashMap<String, Any>()
                                    for (message in messages) {
                                        if (message.isRead == "false_${uidPersonal}") {
                                            val messageId = messageSnapshot.key
                                            if (messageId != null) {
                                                childUpdates["/$messageId/read"] = "true_${uidPersonal}"
                                            }
                                        }
                                    }
                                    messageRef.updateChildren(childUpdates)
                                } else {
                                    // Node không có tin nhắn
                                    // Xử lý trường hợp không có tin nhắn theo yêu cầu
                                }
                            }

                        } else {
                            // Node không có tin nhắn
                            // Xử lý trường hợp không có tin nhắn theo yêu cầu
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Xử lý lỗi nếu có
                    }
                })
        }
    }

    private fun addDataMessTypeText(contentMessage: String, editTextInputMess: EditText) {
        if (idRoomChat != null && checkRoomChat == true) {
            val database = rtDb.getReference("Messages").child(idRoomChat!!)
            database.orderByChild("lastSend")
                .equalTo("true_${auth.currentUser?.uid}")
                .limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()) {
                            for (messageSnapshot in dataSnapshot.children) {
                                val lastMessageId = messageSnapshot.key
                                if (lastMessageId != null) {
                                    // Sửa trường lastSend thành "false_${auth.currentUser?.uid}"
                                    database.child(lastMessageId).child("lastSend")
                                        .setValue("false_${auth.currentUser?.uid}")
                                        .addOnSuccessListener {
                                            // Gửi tin nhắn mới lên cơ sở dữ liệu
                                            val currentTime = Calendar.getInstance().time
                                            val timeFormat = SimpleDateFormat("HH:mm")
                                            val dateFormat = SimpleDateFormat("dd/MM/yyyy")

                                            val message = messageModel(
                                                auth.currentUser?.uid,
                                                uidPersonal,
                                                contentMessage,
                                                "text",
                                                timeFormat.format(currentTime), // Trường chỉ có giờ và phút
                                                dateFormat.format(currentTime),
                                                "false_${auth.currentUser?.uid}",
                                                "true_${auth.currentUser?.uid}"
                                            )
                                            val messageId = rtDb.getReference("Messages").child(idRoomChat!!).push().key ?: ""
                                            rtDb.getReference("Messages").child(idRoomChat!!).child(messageId).setValue(message)
                                                .addOnSuccessListener {
                                                    editTextInputMess.setText("")
                                                    readChat(idRoomChat)
                                                }
                                        }
                                        .addOnFailureListener { error ->
                                            // Xử lý khi sửa trường thất bại
                                            // ...
                                        }
                                }
                            }
                        } else {
                            // Gửi tin nhắn mới lên cơ sở dữ liệu
                            val currentTime = Calendar.getInstance().time
                            val timeFormat = SimpleDateFormat("HH:mm")
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy")

                            val message = messageModel(
                                auth.currentUser?.uid,
                                uidPersonal,
                                contentMessage,
                                "text",
                                timeFormat.format(currentTime), // Trường chỉ có giờ và phút
                                dateFormat.format(currentTime),
                                "true_${auth.currentUser?.uid}",
                                "true_${auth.currentUser?.uid}"
                            )
                            val messageId = rtDb.getReference("Messages").child(idRoomChat!!).push().key ?: ""
                            rtDb.getReference("Messages").child(idRoomChat!!).child(messageId).setValue(message)
                                .addOnSuccessListener {
                                    editTextInputMess.setText("")
                                    readChat(idRoomChat)
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Xử lý lỗi
                    }
                })

        }
        else if(checkRoomChat == false) {
            val roomChatId = "${auth.currentUser?.uid}_${uidPersonal}"
            val data = RoomChat(roomChatId, auth.currentUser?.uid, uidPersonal)
            val roomChatData = hashMapOf(
                "idRoomChat" to data.idRoomChat,
                "idUser1" to data.idUser1,
                "idUser2" to data.idUser2
            )
            val currentTime = Calendar.getInstance().time
            val timeFormat = SimpleDateFormat("HH:mm")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")

            val message = messageModel(
                auth.currentUser?.uid,
                uidPersonal,
                contentMessage,
                "text",
                timeFormat.format(currentTime), // Trường chỉ có giờ và phút
                dateFormat.format(currentTime),
                "true_${auth.currentUser?.uid}",
                "true_${auth.currentUser?.uid}"
            )
            dbFireStore.collection("RoomChat").add(roomChatData)
                .addOnSuccessListener {
                    idRoomChat = roomChatId

                    // Gửi tin nhắn mới lên cơ sở dữ liệu
                    val messageId = rtDb.getReference("Messages").child(roomChatId).push().key ?: ""
                    rtDb.getReference("Messages").child(roomChatId).child(messageId).setValue(message)
                        .addOnSuccessListener {
                            editTextInputMess.setText("")
                            readChat(roomChatId)
                            checkRoomChat = true
                        }
                }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun requestReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PICK_IMAGE
            )
        } else {
            pickImageFromGallery()
        }
    }

    private fun addDataMessTypeImage(UriImages : Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("ImageMessages/mess_${idRoomChat}")
        val fileName = "${System.currentTimeMillis()}.jpg"
        val imageRef = storageRef.child(fileName)
        if(UriImages != null) {
            val inputStream = UriImages?.let { contentResolver.openInputStream(it) }
            inputStream?.let {
                imageRef.putFile(UriImages!!)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            val urlUser = imageRef.downloadUrl
                            urlUser.addOnSuccessListener {
                                val urlFinalImage = it.toString()
                                if (idRoomChat != null && checkRoomChat == true) {
                                    val database = rtDb.getReference("Messages").child(idRoomChat!!)
                                    database.orderByChild("lastSend")
                                        .equalTo("true_${auth.currentUser?.uid}")
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    for (messageSnapshot in dataSnapshot.children) {
                                                        val lastMessageId = messageSnapshot.key
                                                        if (lastMessageId != null) {
                                                            // Sửa trường lastSend thành "false_${auth.currentUser?.uid}"
                                                            database.child(lastMessageId).child("lastSend")
                                                                .setValue("false_${auth.currentUser?.uid}")
                                                                .addOnSuccessListener {
                                                                    // Gửi tin nhắn mới lên cơ sở dữ liệu
                                                                    val currentTime = Calendar.getInstance().time
                                                                    val timeFormat = SimpleDateFormat("HH:mm")
                                                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy")

                                                                    val message = messageModel(
                                                                        auth.currentUser?.uid,
                                                                        uidPersonal,
                                                                        urlFinalImage,
                                                                        "image",
                                                                        timeFormat.format(currentTime), // Trường chỉ có giờ và phút
                                                                        dateFormat.format(currentTime),
                                                                        "false_${auth.currentUser?.uid}",
                                                                        "true_${auth.currentUser?.uid}"
                                                                    )
                                                                    val messageId = rtDb.getReference("Messages").child(idRoomChat!!).push().key ?: ""
                                                                    rtDb.getReference("Messages").child(idRoomChat!!).child(messageId).setValue(message)
                                                                        .addOnSuccessListener {
                                                                            readChat(idRoomChat)
                                                                        }
                                                                }
                                                                .addOnFailureListener { error ->
                                                                    // Xử lý khi sửa trường thất bại
                                                                    // ...
                                                                }
                                                        }
                                                    }
                                                } else {
                                                    // Gửi tin nhắn mới lên cơ sở dữ liệu
                                                    val currentTime = Calendar.getInstance().time
                                                    val timeFormat = SimpleDateFormat("HH:mm")
                                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy")

                                                    val message = messageModel(
                                                        auth.currentUser?.uid,
                                                        uidPersonal,
                                                        urlFinalImage,
                                                        "text",
                                                        timeFormat.format(currentTime), // Trường chỉ có giờ và phút
                                                        dateFormat.format(currentTime),
                                                        "true_${auth.currentUser?.uid}",
                                                        "true_${auth.currentUser?.uid}"
                                                    )
                                                    val messageId = rtDb.getReference("Messages").child(idRoomChat!!).push().key ?: ""
                                                    rtDb.getReference("Messages").child(idRoomChat!!).child(messageId).setValue(message)
                                                        .addOnSuccessListener {
                                                            readChat(idRoomChat)
                                                        }
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // Xử lý lỗi
                                            }
                                        })

                                }
                                else if(checkRoomChat == false) {
                                    val roomChatId = "${auth.currentUser?.uid}_${uidPersonal}"
                                    val data = RoomChat(roomChatId, auth.currentUser?.uid, uidPersonal)
                                    val roomChatData = hashMapOf(
                                        "idRoomChat" to data.idRoomChat,
                                        "idUser1" to data.idUser1,
                                        "idUser2" to data.idUser2
                                    )
                                    val currentTime = Calendar.getInstance().time
                                    val timeFormat = SimpleDateFormat("HH:mm")
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy")

                                    val message = messageModel(
                                        auth.currentUser?.uid,
                                        uidPersonal,
                                        urlFinalImage,
                                        "image",
                                        timeFormat.format(currentTime), // Trường chỉ có giờ và phút
                                        dateFormat.format(currentTime),
                                        "true_${auth.currentUser?.uid}",
                                        "true_${auth.currentUser?.uid}"
                                    )
                                    dbFireStore.collection("RoomChat").add(roomChatData)
                                        .addOnSuccessListener {
                                            idRoomChat = roomChatId

                                            // Gửi tin nhắn mới lên cơ sở dữ liệu
                                            val messageId = rtDb.getReference("Messages").child(roomChatId).push().key ?: ""
                                            rtDb.getReference("Messages").child(roomChatId).child(messageId).setValue(message)
                                                .addOnSuccessListener {
                                                    readChat(roomChatId)
                                                    checkRoomChat = true
                                                }
                                        }
                                }

                            }
                                .addOnFailureListener {

                                }
                        }
                        else {

                        }
                    }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    val uri = data?.data
                    if (uri != null) {
                        uriImage = uri
                        addDataMessTypeImage(uriImage!!)
                    }

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