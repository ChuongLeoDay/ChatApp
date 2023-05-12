package com.example.chatapp.Fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.Animation.ProgressDialogUtil
import com.example.chatapp.LoginActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentSettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentSettingBinding
    private lateinit var auth: FirebaseAuth
    private var dbFireStore = Firebase.firestore
    private val requestOptions = RequestOptions()
        .override(120,120)
        .centerCrop()
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =   FragmentSettingBinding.inflate(inflater, container,false)
        auth = Firebase.auth
        initUI()
        return binding.root
    }

    private fun initUI() {
        val clickToLogOut: ConstraintLayout = binding.editLogoutUserSetting
        val imageUser : ImageView = binding.imageUserSetting
        val nameUser : TextView = binding.editNameUserSetting

        getDataUser(imageUser, nameUser)

        clickToLogOut.setOnClickListener {
            dialogCustom("Đăng xuất", "Bạn chắc chắn rằng muốn đăng xuất?", "Không", "Có", 1)
        }
    }

    private fun dialogCustom(mainText : String, notification : String, textButtonReject : String,textButtonSuscess : String,code : Int) {
        val dialog = activity?.let { Dialog(it) }
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_log_out_custom)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnYes : Button = dialog.findViewById(R.id.btn_alert_dialog_yes)
            val btnNo : Button = dialog.findViewById(R.id.btn_alert_dialog_no)
            if(code == 1) {
                btnYes.setOnClickListener {
                    auth.signOut()
                    if(auth.currentUser == null) {
                        val i = Intent(activity, LoginActivity::class.java)
                        startActivity(i)
                    }
                    else {
                        Toast.makeText(activity, "Đăng xuất thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
                btnNo.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }

        }

    }

    private fun getDataUser(imageUser : ImageView, nameUser : TextView) {
        activity?.let { ProgressDialogUtil.showProgressDialog(it) }
        val uidUser = auth.currentUser?.uid
        val userRef = dbFireStore.collection("users")
        userRef.whereEqualTo("uid", uidUser)
            .get()
            .addOnSuccessListener {
                    documents ->
                for (document in documents) {
                    Glide.with(this)
                        .load(document.getString("urlImage"))
                        .apply(requestOptions)
                        .into(imageUser)
                    nameUser.text = document.getString("name")
                }
                ProgressDialogUtil.hideProgressDialog()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Lấy thông tin thất bại", Toast.LENGTH_SHORT).show()
                nameUser.text = ""
                ProgressDialogUtil.hideProgressDialog()
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}