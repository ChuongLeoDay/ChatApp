package com.example.chatapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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

        checkSocialMedia()
        checkFirebaseConditions()
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

    private fun checkSocialMedia() {
        val userInfoTikTok = dbFireStore.collection("TikTokSocialInfoUsers")
        val userInfoFacebook = dbFireStore.collection("FacebookSocialInfoUsers")
        val userInfoInstagram = dbFireStore.collection("InstagramSocialInfoUsers")

        val checkEmptyText : TextView = binding.editTextCheckInfoPage

        val editFace : TextView = binding.accountNameFacebookPage
        val editIns : TextView = binding.accountInstagramPage
        val editTikTok : TextView = binding.accountTiktokPage

        val layoutFace : ConstraintLayout = binding.layoutClickToFacebookPage
        val layoutIns : ConstraintLayout = binding.layoutClickToInstagramPage
        val layoutTikTok : ConstraintLayout = binding.layoutClickToTiktokPage

        var linkFace : String? = null
        var linkIns : String? = null
        var linkTikTok : String? = null

        userInfoFacebook.whereEqualTo("uid", uidPersonal).get()
                        .addOnSuccessListener {
                            document ->
                            if(!document.isEmpty){
                                for (doc in document) {
                                    checkEmptyText.visibility = View.GONE
                                    editFace.text = doc.getString("nameAccount")
                                    linkFace = doc.getString("linkAccount")
                                    layoutFace.visibility = View.VISIBLE
                                }
                            }
                        }

        userInfoInstagram.whereEqualTo("uid", uidPersonal).get()
            .addOnSuccessListener {
                    document ->
                if(!document.isEmpty){
                    for (doc in document) {
                        checkEmptyText.visibility = View.GONE
                        editIns.text = doc.getString("nameAccount")
                        linkIns = doc.getString("linkAccount")
                        layoutIns.visibility = View.VISIBLE
                    }
                }
            }

        userInfoTikTok.whereEqualTo("uid", uidPersonal).get()
            .addOnSuccessListener {
                    document ->
                if(!document.isEmpty){
                    for (doc in document) {
                        checkEmptyText.visibility = View.GONE
                        editTikTok.text = doc.getString("nameAccount")
                        linkTikTok = doc.getString("linkAccount")
                        layoutTikTok.visibility = View.VISIBLE
                    }
                }
            }

        layoutFace.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkFace))
            startActivity(intent)
        }

        layoutIns.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkIns))
            startActivity(intent)
        }

        layoutTikTok.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkTikTok))
            startActivity(intent)
        }
    }

    private fun checkFirebaseConditions() {

        val checkEmptyText : TextView = binding.editTextCheckInfoPage

        val editPhone : TextView = binding.editPhoneUserPage
        val editEmail : TextView = binding.editEmailUserPage
        val editBirthday : TextView = binding.editBirthdayUserPage

        val layoutPhone : ConstraintLayout = binding.layoutClickToPhonePage
        val layoutEmail : ConstraintLayout = binding.layoutClickToEmailPage
        val layoutBirthday : ConstraintLayout = binding.layoutClickToBirthday
        val lineUI : View = binding.lineLayoutInfo2Page

        if (uidPersonal != null) {
            val userRef = dbFireStore.collection("users").whereEqualTo("uid", uidPersonal)

            userRef.get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        val checkPrivatePhone = documentSnapshot.getBoolean("checkPrivatePhone")
                        val checkPrivateEmail = documentSnapshot.getBoolean("checkPrivateEmail")
                        val checkPrivateBirthday = documentSnapshot.getBoolean("checkPrivateBirthday")

                        if(checkPrivatePhone == false || checkPrivateEmail == false || checkPrivateBirthday == false) {
                            lineUI.visibility = View.VISIBLE
                            checkEmptyText.visibility = View.GONE
                        }

                        if(checkPrivatePhone == false) {
                            dbFireStore.collection("users").whereEqualTo("uid", uidPersonal)
                                .get().addOnSuccessListener {
                                    document ->
                                    for (doc in document) {
                                        editPhone.text = doc.getString("phoneNumber")
                                        layoutPhone.visibility = View.VISIBLE
                                    }
                                }

                        }

                        if(checkPrivateEmail == false) {
                            dbFireStore.collection("users").whereEqualTo("uid", uidPersonal)
                                .get().addOnSuccessListener {
                                        document ->
                                    for (doc in document) {
                                        editEmail.text = doc.getString("email")
                                        layoutEmail.visibility = View.VISIBLE
                                    }
                                }

                        }

                        if(checkPrivateBirthday == false) {
                            dbFireStore.collection("users").whereEqualTo("uid", uidPersonal)
                                .get().addOnSuccessListener {
                                        document ->
                                    for (doc in document) {
                                        editBirthday.text = doc.getString("birthday")
                                        layoutBirthday.visibility = View.VISIBLE
                                    }
                                }

                        }

                    }
                }
                .addOnFailureListener { _ ->
                    // Xử lý khi truy vấn thất bại
                }
        }
    }
}
