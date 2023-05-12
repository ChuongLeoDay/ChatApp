package com.example.chatapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.chatapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        auth = Firebase.auth
        setContentView(view)
        initUI()
    }

    private fun initUI() {
        val clickToLogin : TextView = binding.btnToLogin
        val clickToRegister : Button = binding.btnUserRegister

        clickToLogin.setOnClickListener {
            val goToLogin = Intent(this, LoginActivity::class.java)
            startActivity(goToLogin)
            finish()
        }

        clickToRegister.setOnClickListener {
            loginWithEmail()
        }

    }
    private fun loginWithEmail() {
        val textEmailRegister : String = binding.editTextUserEmail.text.toString()
        val textPassRegister : String = binding.editTextUserPassword.text.toString()
        if (checkedAllField()) {
            auth.createUserWithEmailAndPassword(textEmailRegister, textPassRegister)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        auth.signOut()
                        dialogCustom("Chúc mừng", "Bạn đã đăng kí thành công!", "Trở về đăng nhập", 2)
                    }
                    else {
                        dialogCustom("Cảnh báo", "Đăng ký thất bại!", "Đã hiểu", 1)
                        Log.e("error: ", it.exception.toString())
                    }
                }
        }
    }
    private fun checkedAllField() : Boolean {
        val textUserEmail = binding.editTextUserEmail
        val textUserPass = binding.editTextUserPassword
        val textUserConfirm = binding.editTextUserConfirmPassword
        if(textUserEmail.text.toString().isBlank()) {
            dialogCustom("Cảnh báo", "Bạn không thể để trống trường email!", "Đã hiểu", 1)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(textUserEmail.text.toString()).matches()){
            dialogCustom("Cảnh báo", "Email không hợp lệ!", "Đã hiểu", 1)
            return false
        }
        if (textUserPass.text.toString().isBlank()) {
            dialogCustom("Cảnh báo", "Bạn không thể để trống trường mật khẩu!", "Đã hiểu", 1)
            return false
        }
        if (textUserPass.length() <= 7) {
            dialogCustom("Cảnh báo", "Yêu cầu mật khẩu tối thiểu 8 kí tự!", "Đã hiểu", 1)
            return false
        }
        if (textUserConfirm.text.toString().isBlank()) {
            dialogCustom("Cảnh báo", "Bạn không thể để trống trường xác nhận mật khẩu!", "Đã hiểu", 1)
            return false
        }
        if(textUserPass.text.toString() != textUserConfirm.text.toString() ) {
            dialogCustom("Cảnh báo", "Mật khẩu không khớp!", "Đã hiểu", 1)
            return false
        }

        return true
    }

    @SuppressLint("ResourceAsColor")
    private fun dialogCustom(mainText : String, notification : String, textButtonSuscess : String, code : Int) {
        val dialog = Dialog(this)
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_alert_custom)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnYes : Button = dialog.findViewById(R.id.btn_alert_dialog_yes)
            val textMainDialog : TextView = dialog.findViewById(R.id.text_alert_main_dialog_custom)
            val textNotificationDialog : TextView = dialog.findViewById(R.id.text_title_notification_dialog_custom)
            val imageIcon : ImageView = dialog.findViewById(R.id.icon_custom_dialog)
            if(code == 1) {
                textMainDialog.text = mainText
                textNotificationDialog.text = notification
                btnYes.text = textButtonSuscess
                btnYes.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
            if (code == 2 ) {
                textMainDialog.text = mainText
                textMainDialog.setTextColor(ContextCompat.getColor(this, R.color.notification_color))
                textNotificationDialog.text = notification
                btnYes.text = textButtonSuscess
                imageIcon.setImageResource(R.drawable.tick_circle_figma)
                imageIcon.drawable.setTint(ContextCompat.getColor(this, R.color.notification_color))
                btnYes.setBackgroundColor(ContextCompat.getColor(this, R.color.notification_color))
                btnYes.setOnClickListener {
                    val i = Intent(this, LoginActivity::class.java)
                    startActivity(i)
                    finish()
                }

                dialog.show()
            }

        }

    }

}