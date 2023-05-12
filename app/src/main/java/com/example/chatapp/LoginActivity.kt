package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.chatapp.databinding.ActivityLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var client : GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth
    private var dbFireStore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        val view = binding.root
        setContentView(view)
        initUI()
    }

    private fun initUI() {
        val clickToResgister : TextView = binding.btnToRegister
        clickToResgister.setOnClickListener {
            val goToRegister = Intent(this, RegisterActivity::class.java)
            startActivity(goToRegister)
            finish()
        }
        val clickToLoginGoogle : Button = binding.btnLoginWithGoogle
        clickToLoginGoogle.setOnClickListener {
            val cle = backClient();
            val intent = cle.signInIntent
            startActivityForResult(intent, 10001)
        }
        val clickToLoginFaceBook : Button = binding.btnLoginWithFacebook
        clickToLoginFaceBook.setOnClickListener {
            loginWithFaceBook()
        }
    }

    private fun backClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.defaut_web_client_id))
            .requestEmail().build()
        client = GoogleSignIn.getClient(this, options)
        return client
    }

    private fun loginWithFaceBook() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile,email"))
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onCancel() {
                        Toast.makeText(this@LoginActivity, "Đăng nhập bị huỷ bởi người dùng", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: FacebookException) {
                        TODO("Not yet implemented")
                    }

                    override fun onSuccess(result: LoginResult) {
                        result.accessToken.let {
                                accessToken ->
                            val credential = FacebookAuthProvider.getCredential(accessToken.token)
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener {
                                        task ->
                                    if (task.isSuccessful) {
                                        if (auth != null) {
                                            val uid = auth.currentUser?.uid.toString()
                                            val docRef = dbFireStore.collection("users").whereEqualTo("uid", uid)
                                            docRef.get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    if (!documentSnapshot.isEmpty) {
                                                        val i = Intent(this@LoginActivity, UserActivity::class.java )
                                                        startActivity(i)
                                                        finish()
                                                    } else {
                                                        val i = Intent(this@LoginActivity, InfoUserActivity::class.java )
                                                        startActivity(i)
                                                        finish()
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(this@LoginActivity, "Lỗi truy vấn dữ liệu", Toast.LENGTH_SHORT).show()
                                                }

                                        }

                                    } else {
                                        Toast.makeText(this@LoginActivity, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("TAG", "signInWithCredential:failure", exception)
                                    Toast.makeText(this@LoginActivity, "Đăng nhập thất bại", Toast.LENGTH_LONG).show()
                                }
                        }
                    }


                }
            )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Gọi hàm onActivityResult của callbackManager
        if(requestCode != 10001) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
        // Kiểm tra nếu là kết quả đăng nhập Google
        if (requestCode == 10001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { tasks ->
                        if (tasks.isSuccessful) {
                            if (auth != null) {
                                val uid = auth.currentUser?.uid.toString()
                                val docRef = dbFireStore.collection("users").whereEqualTo("uid", uid)
                                docRef.get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        if (!documentSnapshot.isEmpty) {
                                            val i = Intent(this@LoginActivity, UserActivity::class.java )
                                            startActivity(i)
                                            finish()
                                        } else {
                                            val i = Intent(this@LoginActivity, InfoUserActivity::class.java )
                                            startActivity(i)
                                            finish()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this@LoginActivity, "Lỗi truy vấn dữ liệu", Toast.LENGTH_SHORT).show()
                                    }

                            }
                        } else {
                            Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("TAG", "signInWithCredential:failure", exception)
                        Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_LONG).show()
                    }
            } catch (e: ApiException) {
                Log.e("TAG", "signInResult:failed code=" + e.statusCode)
                Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null) {
            val i = Intent(this, UserActivity::class.java)
            startActivity(i)
            finish()
        }
    }
}