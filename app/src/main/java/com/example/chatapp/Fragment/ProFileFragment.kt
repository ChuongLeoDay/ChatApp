package com.example.chatapp.Fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.Animation.ProgressDialogUtil
import com.example.chatapp.Model.Constants
import com.example.chatapp.Model.infoSocialUser
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentProFileBinding
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
 * Use the [ProFileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProFileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentProFileBinding
    private var dbFireStore = Firebase.firestore
    private lateinit var auth : FirebaseAuth
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
        binding = FragmentProFileBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        initUI()
        return binding.root
    }

    private fun initUI() {
        val imageUser : ImageView = binding.imageviewProfileUser
        val nameUser : TextView = binding.editUserNameProfile
        val genderUser : TextView = binding.editGenderUserProfile
        val dateUser : TextView = binding.editBirthdayUserProfile
        val emailUser : TextView = binding.editEmailUserProfile
        val phoneUser : TextView = binding.editPhoneUserProfile

        val addSocialInfo : ImageButton = binding.btnAddSocialProfile

        addSocialInfo.setOnClickListener {
            addInfoSocial()
        }

        getDataUser(imageUser, nameUser, genderUser, dateUser, emailUser, phoneUser)
    }


    private fun addInfoSocial() {
        val dialog = context?.let { Dialog(it) }

        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_add_social_user_profile)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnDismiss: Button = dialog.findViewById(R.id.btn_dismiss)
            val btnConfirm: Button = dialog.findViewById(R.id.btn_confirm)

            val spinnerChooseSocial: Spinner = dialog.findViewById(R.id.spinnerChooseSocial)
            val nameAccount: EditText = dialog.findViewById(R.id.name_acccount)
            val linkAccount: EditText = dialog.findViewById(R.id.link_acccount)

            val options = arrayOf(Constants.FACEBOOK_OPTION, Constants.INSTAGRAM_OPTION, Constants.TIKTOK_OPTION)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerChooseSocial.adapter = adapter
            var optionChooseSpinner: String? = null
            spinnerChooseSocial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    optionChooseSpinner = options[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }

            btnDismiss.setOnClickListener {
                dialog.dismiss()
            }
            btnConfirm.setOnClickListener {
                if (optionChooseSpinner != null && nameAccount.text.toString() != "" && linkAccount.text.toString() != "") {
                    val uid = auth.currentUser?.uid
                    if (uid != null && uid.isNotEmpty()) {
                        val data = infoSocialUser(uid, nameAccount.text.toString(), linkAccount.text.toString())
                        val updateSocial = hashMapOf(
                            "uid" to data.uid,
                            "nameAccount" to data.nameAccount,
                            "linkAccount" to data.linkAccount
                        )

                        // Lưu thông tin lên Firebase
                        if(optionChooseSpinner == Constants.FACEBOOK_OPTION) {
                            dbFireStore.collection("FacebookSocialInfoUsers").add(updateSocial)
                                .addOnSuccessListener {
                                    Toast.makeText(activity, "Thêm thành công!", Toast.LENGTH_SHORT).show()
                                    binding.btnSocialFbProfile.visibility = View.VISIBLE
                                    dialog.dismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(activity, "Thêm thất bại!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                        } else if (optionChooseSpinner == Constants.INSTAGRAM_OPTION) {
                            dbFireStore.collection("InstagramSocialInfoUsers").add(updateSocial)
                                .addOnSuccessListener {
                                    Toast.makeText(activity, "Thêm thành công!", Toast.LENGTH_SHORT).show()
                                    binding.btnSocialInsProfile.visibility = View.VISIBLE
                                    dialog.dismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(activity, "Thêm thất bại!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                        } else {
                            dbFireStore.collection("TikTokSocialInfoUsers").add(updateSocial)
                                        .addOnSuccessListener {
                                            Toast.makeText(activity, "Thêm thành công!", Toast.LENGTH_SHORT).show()
                                            binding.btnSocialTiktokProfile.visibility = View.VISIBLE
                                            dialog.dismiss()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(activity, "Thêm thất bại!", Toast.LENGTH_SHORT).show()
                                            dialog.dismiss()
                                        }
                        }
                    }
                } else {
                    Toast.makeText(activity, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        } else {
            Toast.makeText(activity, "Nulll", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getDataUser(imageUser : ImageView ,nameUser : TextView, genderUser : TextView, dateUser : TextView, emailUser : TextView, phoneUser : TextView) {
        activity?.let { ProgressDialogUtil.showProgressDialog(it) }
        val uidUser = auth.currentUser?.uid
        val userRef = dbFireStore.collection("users")
        val userInfoTikTok = dbFireStore.collection("TikTokSocialInfoUsers")
        val userInfoFacebook = dbFireStore.collection("FacebookSocialInfoUsers")
        val userInfoInstagram = dbFireStore.collection("InstagramSocialInfoUsers")

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
                    genderUser.text = document.getString("gender")
                    dateUser.text = document.getString("birthday")
                    emailUser.text = document.getString("email")
                    phoneUser.text = document.getString("phoneNumber")
                }
                ProgressDialogUtil.hideProgressDialog()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Lấy dữ liệu thất bại", Toast.LENGTH_SHORT).show()
                ProgressDialogUtil.hideProgressDialog()
                nameUser.text = ""
                genderUser.text = ""
                dateUser.text = ""
                emailUser.text = ""
                phoneUser.text = ""

            }
        userInfoFacebook.whereEqualTo("uid", uidUser)
            .get()
            .addOnSuccessListener {
                document ->
                if (!document.isEmpty) {
                    binding.btnSocialFbProfile.visibility = View.VISIBLE
                }
            }
        userInfoTikTok.whereEqualTo("uid", uidUser)
            .get()
            .addOnSuccessListener {
                document ->
                if (!document.isEmpty) {
                    binding.btnSocialTiktokProfile.visibility = View.VISIBLE
                }
            }
        userInfoInstagram.whereEqualTo("uid", uidUser)
            .get()
            .addOnSuccessListener {
                document ->
                if(!document.isEmpty) {
                    binding.btnSocialInsProfile.visibility = View.VISIBLE
                }
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProFileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProFileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}