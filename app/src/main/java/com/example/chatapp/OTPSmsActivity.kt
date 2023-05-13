package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.chatapp.Animation.ProgressDialogUtil
import com.example.chatapp.Model.userInfomationModel
import com.example.chatapp.databinding.ActivityOtpsmsBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.concurrent.TimeUnit

class OTPSmsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpsmsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var otpCheck : String
    private var dbFireStore = Firebase.firestore
    private var storageReference = Firebase.storage
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpsmsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth
        storageReference = FirebaseStorage.getInstance()
        resendOTP()
        initUI()
    }

    private fun initUI() {
        val clickCheckOTP: Button = binding.btnAuthenciationOtp
        val phoneNumberTextView : TextView = binding.textPhoneNumberOtp
        val textResendOtp : TextView = binding.resendSmsOtp
        val bundle = intent.extras

        if (bundle != null) {
            val phoneUser = bundle.getString("PhoneUser")
            phoneNumberTextView.text = phoneUser
            if (phoneUser != null) {
                callbackAuthenciationPhone(phoneUser)
            }
        }

        clickCheckOTP.setOnClickListener {
            addDataUser()
        }
        textResendOtp.setOnClickListener {
            if(resendOTP()) {
                callbackAuthenciationPhone(phoneNumberTextView.text.toString())
            }
        }
    }




    @SuppressLint("ResourceAsColor")
    private fun resendOTP() : Boolean {
        val textResendOtp : TextView = binding.resendSmsOtp
        val timeOut = 60

        // Disable the resend button
        textResendOtp.isEnabled = false
        textResendOtp.setTextColor(ContextCompat.getColor(this@OTPSmsActivity, R.color.app_theme_50))

        // Start the countdown timer
        countDownTimer = object : CountDownTimer((timeOut * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the button text with the remaining time
                textResendOtp.text = "Gửi lại mã xác nhận sau ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                // Enable the resend button
                textResendOtp.isEnabled = true
                textResendOtp.setTextColor(ContextCompat.getColor(this@OTPSmsActivity, R.color.app_theme))
                textResendOtp.text = "Gửi lại"
            }
        }.start()

        return true
    }


    private fun addDataUser() {
        val inputNumber1 : TextView = binding.inputCode1
        val inputNumber2 : TextView = binding.inputCode2
        val inputNumber3 : TextView = binding.inputCode3
        val inputNumber4 : TextView = binding.inputCode4
        val inputNumber5 : TextView = binding.inputCode5
        val inputNumber6 : TextView = binding.inputCode6

        val bundle = intent.extras




        val otpInput = "${inputNumber1.text}${inputNumber2.text}${inputNumber3.text}${inputNumber4.text}${inputNumber5.text}${inputNumber6.text}"
        if(otpInput != "") {
            if(otpCheck == otpInput) {
                ProgressDialogUtil.showProgressDialog(this@OTPSmsActivity)
                if (bundle != null) {
                    val bitmapImageUser = bundle.getByteArray("byteImageUser")
                    val uriImage = bundle.getParcelable<Uri>("UriUser")
                    val nameUser = bundle.getString("NameUser")
                    val birthdayUser = bundle.getString("BirthdayUser")
                    val genderUser = bundle.getString("GenderUser")
                    val emailUser = bundle.getString("EmailUser")
                    val phoneUser = bundle.getString("PhoneUser")
                    val uidUser = bundle.getString("UidUser")


                    val storageRef = FirebaseStorage.getInstance().reference.child("ImageUsers/uid_${uidUser}")
                    val fileName = "${System.currentTimeMillis()}.jpg"
                    val imageRef = storageRef.child(fileName)

                    if(bitmapImageUser !=  null) {
                        bitmapImageUser?.let { it1 -> imageRef.putBytes(it1) }
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val urlUser = imageRef.downloadUrl
                                    urlUser.addOnSuccessListener {
                                        val urlFinalImage = it.toString()
                                        if(nameUser != null && birthdayUser != null && genderUser !=null && emailUser != null && phoneUser != null && uidUser != null) {
                                            val dataUser = userInfomationModel(nameUser, urlFinalImage,birthdayUser, genderUser, emailUser, phoneUser, uidUser)
                                            val dataUserUpload = hashMapOf(
                                                "urlImage" to dataUser.imageUserURL,
                                                "name" to dataUser.nameUser,
                                                "birthday" to dataUser.birthdayUser,
                                                "gender" to dataUser.genderUser,
                                                "email" to dataUser.emailUser,
                                                "phoneNumber" to dataUser.phoneNumberUser,
                                                "uid" to dataUser.uidUser,
                                                "checkPrivatePhone" to dataUser.checkPrivatePhone,
                                                "checkPrivateEmail" to dataUser.checkPrivateEmail,
                                                "checkPrivateBirthday" to dataUser.checkPrivateBirthday
                                            )

                                            dbFireStore.collection("users").add(dataUserUpload)
                                                .addOnSuccessListener {
                                                    if(auth.currentUser != null) {
                                                        ProgressDialogUtil.hideProgressDialog()
                                                        val i = Intent(this@OTPSmsActivity, UserActivity::class.java)
                                                        startActivity(i)
                                                        finish()
                                                    }
                                                    else {
                                                        ProgressDialogUtil.hideProgressDialog()
                                                        val i = Intent(this@OTPSmsActivity, LoginActivity::class.java)
                                                        startActivity(i)
                                                        finish()
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    ProgressDialogUtil.hideProgressDialog()
                                                    Toast.makeText(this@OTPSmsActivity, "Thêm thông tin thất bại(add on firebase)", Toast.LENGTH_SHORT).show()
                                                }

                                        } else {
                                            ProgressDialogUtil.hideProgressDialog()
                                            Toast.makeText(this@OTPSmsActivity, "Lỗi nhận thông tin", Toast.LENGTH_SHORT).show()
                                        }

                                    }
                                        .addOnFailureListener {
                                            ProgressDialogUtil.hideProgressDialog()
                                            Toast.makeText(this@OTPSmsActivity, "Lấy link ảnh thất bại", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                else {
                                    ProgressDialogUtil.hideProgressDialog()
                                    Toast.makeText(this@OTPSmsActivity, "Thêm thông tin thất bại(Ảnh)", Toast.LENGTH_SHORT).show()
                                }

                            }
                    }
                    if (uriImage != null) {
                        val inputStream = uriImage?.let { contentResolver.openInputStream(it) }

                        inputStream?.let {
                        imageRef.putFile(uriImage)
                                .addOnCompleteListener { task ->
                                    if(task.isSuccessful) {
                                        val urlUser = imageRef.downloadUrl
                                        urlUser.addOnSuccessListener {
                                            val urlFinalImage = it.toString()
                                            if(nameUser != null && birthdayUser != null && genderUser !=null && emailUser != null && phoneUser != null && uidUser != null) {
                                                val dataUser = userInfomationModel(nameUser, urlFinalImage,birthdayUser, genderUser, emailUser, phoneUser, uidUser)
                                                val dataUserUpload = hashMapOf(
                                                    "urlImage" to dataUser.imageUserURL,
                                                    "name" to dataUser.nameUser,
                                                    "birthday" to dataUser.birthdayUser,
                                                    "gender" to dataUser.genderUser,
                                                    "email" to dataUser.emailUser,
                                                    "phoneNumber" to dataUser.phoneNumberUser,
                                                    "uid" to dataUser.uidUser,
                                                    "checkPrivatePhone" to dataUser.checkPrivatePhone,
                                                    "checkPrivateEmail" to dataUser.checkPrivateEmail,
                                                    "checkPrivateBirthday" to dataUser.checkPrivateBirthday
                                                )
                                                dbFireStore.collection("users").add(dataUserUpload)
                                                    .addOnSuccessListener {
                                                        if(auth.currentUser != null) {
                                                            ProgressDialogUtil.hideProgressDialog()
                                                            val i = Intent(this@OTPSmsActivity, UserActivity::class.java)
                                                            startActivity(i)
                                                            finish()
                                                        }
                                                        else {
                                                            ProgressDialogUtil.hideProgressDialog()
                                                            val i = Intent(this@OTPSmsActivity, LoginActivity::class.java)
                                                            startActivity(i)
                                                            finish()
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        ProgressDialogUtil.hideProgressDialog()
                                                        Toast.makeText(this@OTPSmsActivity, "Thêm thông tin thất bại(add on firebase)", Toast.LENGTH_SHORT).show()
                                                    }

                                            } else {
                                                ProgressDialogUtil.hideProgressDialog()
                                                Toast.makeText(this@OTPSmsActivity, "Lỗi nhận thông tin", Toast.LENGTH_SHORT).show()
                                            }

                                        }
                                            .addOnFailureListener {
                                                ProgressDialogUtil.hideProgressDialog()
                                                Toast.makeText(this@OTPSmsActivity, "Lấy link ảnh thất bại", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    else {
                                        ProgressDialogUtil.hideProgressDialog()
                                        Toast.makeText(this@OTPSmsActivity, "Thêm thông tin thất bại(Ảnh)", Toast.LENGTH_SHORT).show()
                                    }
                                }

                        }
                    }

                }
            }
            else {
                Toast.makeText(this@OTPSmsActivity, "Nhập mã chưa chính xác", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this@OTPSmsActivity, "Hãy nhập mã xác nhận", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callbackAuthenciationPhone(PhoneNumber : String) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                otpCheck = credential.smsCode.orEmpty()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(this@OTPSmsActivity, p0.toString(), Toast.LENGTH_LONG).show()
                Log.e("That Bai", p0.toString())
            }

        }
        authenciationPhoneFireBase(PhoneNumber, callbacks)
    }

    private fun authenciationPhoneFireBase(PhoneNumBer: String, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(PhoneNumBer)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


}