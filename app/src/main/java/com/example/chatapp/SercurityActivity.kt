package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.example.chatapp.databinding.ActivitySercurityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SercurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySercurityBinding
    private lateinit var switchPhone: SwitchCompat
    private lateinit var switchEmail: SwitchCompat
    private lateinit var switchBirthday: SwitchCompat
    private val dbFireStore = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySercurityBinding.inflate(layoutInflater)
        val view = binding.root
        auth= Firebase.auth
        initUI()
        setContentView(view)
    }

    private fun initUI() {
        switchPhone = binding.switchPhonePrivate
        switchEmail = binding.switchEmailPrivate
        switchBirthday = binding.switchBirthdayPrivate
        val clickBack : ImageButton = binding.clickToBackFromChatToActivitySercurity

        clickBack.setOnClickListener {
            val i = Intent(this, UserActivity::class.java)
            startActivity(i)
            finish()
        }

        switchPhone.setOnCheckedChangeListener { _, isChecked ->
            val currentUser = auth.currentUser
            val userUid = currentUser?.uid

            if(isChecked) {
                if (userUid != null) {
                    val userRef = dbFireStore.collection("users").whereEqualTo("uid", userUid)

                    userRef.get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentId = documentSnapshot.id
                                val documentRef = dbFireStore.collection("users").document(documentId)

                                documentRef.update("checkPrivatePhone", false)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Thay đổi thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Xử lý khi cập nhật thất bại
                                    }
                            }
                        }
                }
            }else {
                if (userUid != null) {
                    val userRef = dbFireStore.collection("users").whereEqualTo("uid", userUid)

                    userRef.get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentId = documentSnapshot.id
                                val documentRef = dbFireStore.collection("users").document(documentId)

                                documentRef.update("checkPrivatePhone", true)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Thay đổi thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Xử lý khi cập nhật thất bại
                                    }
                            }
                        }
                }
            }
        }

        switchEmail.setOnCheckedChangeListener { _, isChecked ->
            val currentUser = auth.currentUser
            val userUid = currentUser?.uid

            if (isChecked) {
                if (userUid != null) {
                    val userRef = dbFireStore.collection("users").whereEqualTo("uid", userUid)


                    userRef.get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentId = documentSnapshot.id
                                val documentRef = dbFireStore.collection("users").document(documentId)

                                documentRef.update("checkPrivateEmail", false)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Thay đổi thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Xử lý khi cập nhật thất bại
                                    }
                            }
                        }
                }
            } else {
                if (userUid != null) {
                    val userRef = dbFireStore.collection("users").whereEqualTo("uid", userUid)
                    userRef.get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentId = documentSnapshot.id
                                val documentRef = dbFireStore.collection("users").document(documentId)

                                documentRef.update("checkPrivateEmail", true)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Thay đổi thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Xử lý khi cập nhật thất bại
                                    }
                            }
                        }
                }
            }

        }

        switchBirthday.setOnCheckedChangeListener { _, isChecked ->

            val currentUser = auth.currentUser
            val userUid = currentUser?.uid

            if (isChecked) {
                if (userUid != null) {
                    val userRef = dbFireStore.collection("users").whereEqualTo("uid", userUid)
                    val checkPrivatePhone = !isChecked

                    userRef.get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentId = documentSnapshot.id
                                val documentRef = dbFireStore.collection("users").document(documentId)

                                documentRef.update("checkPrivateBirthday", false)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Thay đổi thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Xử lý khi cập nhật thất bại
                                    }
                            }
                        }
                }
            } else {
                if (userUid != null) {
                    val userRef = dbFireStore.collection("users").whereEqualTo("uid", userUid)
                    val checkPrivatePhone = !isChecked

                    userRef.get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentId = documentSnapshot.id
                                val documentRef = dbFireStore.collection("users").document(documentId)

                                documentRef.update("checkPrivateBirthday", true)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Thay đổi thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Xử lý khi cập nhật thất bại
                                    }
                            }
                        }
                }
            }

        }

        checkFirebaseConditions()
    }

    private fun checkFirebaseConditions() {
        val currentUser = auth.currentUser
        val userUid = currentUser?.uid

        if (userUid != null) {
            val userRef = dbFireStore.collection("users").whereEqualTo("uid", userUid)

            userRef.get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        val checkPrivatePhone = documentSnapshot.getBoolean("checkPrivatePhone")
                        val checkPrivateEmail = documentSnapshot.getBoolean("checkPrivateEmail")
                        val checkPrivateBirthday = documentSnapshot.getBoolean("checkPrivateBirthday")

                        // Set trạng thái của các nút
                        switchPhone.isChecked = checkPrivatePhone != true
                        switchEmail.isChecked = checkPrivateEmail != true
                        switchBirthday.isChecked = checkPrivateBirthday != true
                    }
                }
                .addOnFailureListener { e ->
                    // Xử lý khi truy vấn thất bại
                }
        }
    }



}