package com.example.chatapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.databinding.ActivityInfoUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class InfoUserActivity : AppCompatActivity() {

    private val REQUEST_CODE_PICK_IMAGE = 100
    private val REQUEST_CODE_CAPTURE_IMAGE = 101
    private var uriImage : Uri? = null


    private lateinit var binding: ActivityInfoUserBinding
    private lateinit var auth: FirebaseAuth
    private var dbFireStore = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityInfoUserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth
        initUI()
    }


    private fun initUI() {
        val clickToPickImageUser : CardView = binding.cardViewSelectedIamge
        val clickToPickDateUser : Button = binding.btnClickPickDateInfoUser
        val clickStoreDataUser : ImageButton = binding.btnToUserActivity
        val imageDataUser : ImageView = binding.imageViewUserInfo
        val nameDataUser : TextView = binding.editTextNameUserInfo
        val emailDataUser : TextView = binding.editTextEmailUserInfo
        val phoneDataUser : TextView = binding.editTextPhoneUserInfo
        val currentUser = auth.currentUser
        val requestOptions = RequestOptions()
            .override(120,120)
            .centerCrop()
            .placeholder(R.drawable.asset_11)
            .error(R.drawable.danger_triangle_alert_figma)
        if(currentUser != null) {

            for (profile in currentUser?.providerData!!) {
                val providerId = profile.providerId
                // Xử lý theo từng nhà cung cấp đăng nhập
                when (providerId) {
                    "google.com" -> {
                        Glide.with(this)
                            .load(auth.currentUser?.photoUrl)
                            .apply(requestOptions)
                            .into(imageDataUser)
                        nameDataUser.text = currentUser?.displayName.toString()
                        emailDataUser.text = currentUser?.email.toString()
                        emailDataUser.isEnabled = false
                        if (currentUser?.phoneNumber.toString() == "null") {phoneDataUser.text = ""} else {phoneDataUser.text = currentUser?.phoneNumber.toString()}
                    }
                    "facebook.com" -> {
                        Glide.with(this)
                            .load(auth.currentUser?.photoUrl)
                            .apply(requestOptions)
                            .into(imageDataUser)
                        nameDataUser.text = currentUser?.displayName.toString()
                        emailDataUser.text = currentUser?.email.toString()
                        emailDataUser.isEnabled = false
                        if (currentUser?.phoneNumber.toString() == "null") {phoneDataUser.text = ""} else {phoneDataUser.text = currentUser?.phoneNumber.toString()}
                    }

                    "password" -> {
                        emailDataUser.text = currentUser?.email.toString()
                        emailDataUser.isEnabled = false
                    }
                }
            }

        }

        clickToPickImageUser.setOnClickListener {
            showImagePickerDialog()
        }

        clickToPickDateUser.setOnClickListener {
            chooseBirthDay()
        }

        clickStoreDataUser.setOnClickListener {
            if (checkAllField()) {
                getDataUser()
            }
        }
    }

    private fun getDataUser() {
        val clickToPickDateUser : Button = binding.btnClickPickDateInfoUser
        val imageDataUser : ImageView = binding.imageViewUserInfo
        val nameDataUser : TextView = binding.editTextNameUserInfo
        val emailDataUser : TextView = binding.editTextEmailUserInfo
        val phoneDataUser : TextView = binding.editTextPhoneUserInfo
        val radioBtnMale : RadioButton = binding.radioButtonChooseMale
        val radioBtnFemale : RadioButton = binding.radioButtonChooseFemale
        val currentUser = auth.currentUser
        if(currentUser != null) {
            val phoneNumberCheck = PhoneNumberUtil.getInstance()
            val phoneNumberUser = "+84 ${phoneDataUser.text}"
            val e164PhoneNumber = phoneNumberCheck.format(phoneNumberCheck.parse(phoneNumberUser, null), PhoneNumberUtil.PhoneNumberFormat.E164)

            dbFireStore.collection("users")
                .whereEqualTo("phoneNumber", e164PhoneNumber)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.size() > 0) {
                        dialogCustom("Cảnh báo", "Số điện thoại này đã được đăng kí!", "no", "Đã hiểu", 2)
                    } else {
                        val bundle = Bundle()
                        if(uriImage == null) {
                            val drawable = imageDataUser.drawable
                            if(drawable is BitmapDrawable) {
                                val bitmap = drawable.bitmap
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                                val byteArray = stream.toByteArray()
                                bundle.putByteArray("byteImageUser", byteArray)
                            }
                        } else {
                            bundle.putParcelable("UriUser", uriImage)
                        }
                        bundle.putString("NameUser", nameDataUser.text.toString())
                        bundle.putString("BirthdayUser", clickToPickDateUser.text.toString())
                        if(radioBtnMale.isChecked) {
                            bundle.putString("GenderUser", radioBtnMale.text.toString())
                        } else {
                            bundle.putString("GenderUser", radioBtnFemale.text.toString())
                        }
                        bundle.putString("EmailUser", emailDataUser.text.toString())
                        bundle.putString("PhoneUser", e164PhoneNumber)
                        bundle.putString("UidUser", auth.currentUser?.uid.toString())
                        val i = Intent(this@InfoUserActivity, OTPSmsActivity::class.java)
                        i.putExtras(bundle)
                        startActivity(i)
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@InfoUserActivity, "Lỗi truy vấn", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun showImagePickerDialog() {
        dialogCustom("Chọn ảnh", "LB sẽ chọn ảnh của bạn từ", "Camera", "Thư viện", 1)
    }

    @SuppressLint("ResourceAsColor", "ResourceType")
    private fun dialogCustom(mainText : String, notification : String, textButtonReject : String ,textButtonSuscess : String, code : Int) {
        val dialog = Dialog(this)
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)

            if (code == 1 ) {

                dialog.setContentView(R.layout.dialog_log_out_custom)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val btnYes : Button = dialog.findViewById(R.id.btn_alert_dialog_yes)
                val btnNo : Button = dialog.findViewById(R.id.btn_alert_dialog_no)
                val textMainDialog : TextView = dialog.findViewById(R.id.text_alert_main_dialog_custom)
                val textNotificationDialog : TextView = dialog.findViewById(R.id.text_title_notification_dialog_custom)
                val imageIcon : ImageView = dialog.findViewById(R.id.icon_custom_dialog)

                textMainDialog.text = mainText
                textMainDialog.setTextColor(ContextCompat.getColor(this, R.color.warning_notification_color))
                textNotificationDialog.text = notification
                btnYes.text = textButtonSuscess
                btnNo.text = textButtonReject
                imageIcon.setImageResource(R.drawable.scan_icon_figma)
                imageIcon.drawable.setTint(ContextCompat.getColor(this, R.color.warning_notification_color))
                btnYes.setBackgroundResource(R.drawable.custom_button_alert_dialog_yes_yellow)
                btnYes.setOnClickListener {
                    requestReadExternalStoragePermission()
                    dialog.dismiss()
                }
                btnNo.setOnClickListener {
                    takeImageFromCamera()
                    dialog.dismiss()
                }

                dialog.show()
            }

            if(code == 2) {
                dialog.setContentView(R.layout.dialog_alert_custom)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val btnYes : Button = dialog.findViewById(R.id.btn_alert_dialog_yes)
                val textMainDialog : TextView = dialog.findViewById(R.id.text_alert_main_dialog_custom)
                val textNotificationDialog : TextView = dialog.findViewById(R.id.text_title_notification_dialog_custom)

                textMainDialog.text = mainText
                textNotificationDialog.text = notification
                btnYes.text = textButtonSuscess
                btnYes.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }

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

    private fun takeImageFromCamera() {
        val permission = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_CAPTURE_IMAGE)
        }

    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun checkAllField() : Boolean {
        val nameDataUser : TextView = binding.editTextNameUserInfo
        val phoneDataUser : TextView = binding.editTextPhoneUserInfo
        val radioBtnMale : RadioButton = binding.radioButtonChooseMale
        val radioBtnFemale : RadioButton = binding.radioButtonChooseFemale
        val phoneNumberCheck = PhoneNumberUtil.getInstance()

        if(nameDataUser.text.toString().isBlank()) {
            dialogCustom("Cảnh báo", "Hãy nhập tên của bạn vào!", "no", "Đã hiểu", 2)
            return false
        }
        if(!radioBtnMale.isChecked && !radioBtnFemale.isChecked) {
            dialogCustom("Cảnh báo", "Hãy chọn trường giới tính!", "no", "Đã hiểu", 2)
            return false
        }
        if (phoneDataUser.text.toString().isNotBlank()) {
            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneUser = "+84" + phoneDataUser.text.toString()
            val phoneNumber = try {
                phoneNumberUtil.parse(phoneUser, "+84")
            } catch (e: NumberParseException) {
                null
            }
            if (phoneNumber == null || !phoneNumberCheck.isValidNumber(phoneNumber)) {
                dialogCustom("Cảnh báo", "Số điện thoại bạn vừa nhập không đúng!", "no", "Đã hiểu", 2)
                return false
//            } else {
//                val defaultCountry = phoneNumberUtil.getRegionCodeForNumber(phoneNumber)
//                if (!phoneNumberCheck.isValidNumberForRegion(phoneNumber, defaultCountry)) {
//                    dialogCustom("Cảnh báo", "Số điện thoại bạn vừa nhập không đúng!", "no", "Đã hiểu", 2)
//                    return false
//                }
            }
        }


        else {
         dialogCustom("Cảnh báo", "Hãy ghi thông thông tin số điện thoại đầy đủ", "no", "Đã hiểu", 2)
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    val uri = data?.data
                    if (uri != null) {
                        uriImage = uri
                    }
                    binding.imageViewUserInfo.setImageURI(uri)
                }
                REQUEST_CODE_CAPTURE_IMAGE -> {
                    val image = data?.extras?.get("data") as Bitmap
                    binding.imageViewUserInfo.setImageBitmap(image)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PICK_IMAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "Quyền truy cập bộ nhớ ảnh bị từ chối", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_CAPTURE_IMAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeImageFromCamera()
                } else {
                    Toast.makeText(this, "Quyền truy cập camera bị từ chối", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun chooseBirthDay() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this,R.style.MyDatePickerDialogTheme,
            {_, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val simpleDateFormat = SimpleDateFormat("dd/MM/YYYY", Locale.getDefault())
                val selectedDateString = simpleDateFormat.format(selectedDate.time)
                binding.btnClickPickDateInfoUser.text = selectedDateString
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

}