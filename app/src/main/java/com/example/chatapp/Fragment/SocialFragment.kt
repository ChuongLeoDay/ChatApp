package com.example.chatapp.Fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapter.infoSocialAdapter
import com.example.chatapp.Model.userInfomationModel
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentProFileBinding
import com.example.chatapp.databinding.FragmentSocialBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SocialFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SocialFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding : FragmentSocialBinding
    private val dbFireStore = Firebase.firestore
    private val infoSocialAdapter = infoSocialAdapter(ArrayList())
    private lateinit var auth : FirebaseAuth

    private val handler = Handler(Looper.getMainLooper())

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
        binding = FragmentSocialBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        initUI()
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initUI() {
        val rvUsers : RecyclerView = binding.recyclerSocialInfo
        val searchUsers : SearchView = binding.btnSearchViewSocial


        rvUsers.layoutManager = LinearLayoutManager(context)

        rvUsers.apply { adapter = infoSocialAdapter }

        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val outRect = Rect()
                    searchUsers.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    searchUsers.clearFocus()
                    hideKeyboard()
                }
            }
            false
        }


        searchUsers.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query.let { searchInfoUsers(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchRunnable = Runnable {
                    searchInfoUsers(newText)
                }
                handler.removeCallbacks(searchRunnable)
                handler.postDelayed(searchRunnable, 1000)
                return false
            }

        })

        searchUsers.setOnFocusChangeListener {  _, hasFocus ->
            if (!hasFocus) {
                infoSocialAdapter.infoUsers.clear()
                infoSocialAdapter.notifyDataSetChanged()
            }
        }


    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        binding.btnSearchViewSocial.clearFocus()
    }


    private fun searchInfoUsers(query: String?) {
        val queryRef = if (query.isNullOrEmpty()) {
            dbFireStore.collection("users")
        } else {
            dbFireStore.collection("users")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", "${query}\uf8ff")
                .orderBy("name")
                .orderBy("urlImage")
                .orderBy("birthday")
                .orderBy("gender")
                .orderBy("email")
                .orderBy("phoneNumber")
                .orderBy("uid")
                .orderBy("checkPrivatePhone")
                .orderBy("checkPrivateEmail")
                .orderBy("checkPrivateBirthday")
        }

        queryRef.get()
            .addOnSuccessListener { result ->
                val infoUserSocial = ArrayList<userInfomationModel>()
                var uid: String?
                for (document in result) {
                    uid = document.getString("uid")
                    if (uid != auth.currentUser?.uid) {
                        val infoUser = document.toObject(userInfomationModel::class.java)
                        infoUserSocial.add(infoUser)
                    }
                }

                infoSocialAdapter.apply {
                    this.infoUsers = infoUserSocial
                    notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Không tìm thấy người dùng nào", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SocialFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SocialFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}