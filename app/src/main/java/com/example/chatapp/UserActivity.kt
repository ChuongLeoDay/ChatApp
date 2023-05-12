package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatapp.Fragment.MessageFragment
import com.example.chatapp.Fragment.ProFileFragment
import com.example.chatapp.Fragment.SettingFragment
import com.example.chatapp.Fragment.SocialFragment
import com.example.chatapp.databinding.ActivityUserBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarMenuView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initUI()
    }

    private fun initUI() {
        val viewMenu : BottomNavigationView = binding.viewMenu
        val fragHomeMess =MessageFragment()
        val fragHomeProfile = ProFileFragment()
        val fragHomeSocial = SocialFragment()
        val fragHomeSetting = SettingFragment()

        replaceFragment(fragHomeMess)
        viewMenu.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home_message -> replaceFragment(fragHomeMess)
                R.id.home_user_profile -> replaceFragment(fragHomeProfile)
                R.id.home_setting -> replaceFragment(fragHomeSetting)
                R.id.home_social_profile -> replaceFragment(fragHomeSocial)
            }
            true
        }
    }

    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.frame_real_scroll, fragment)
            commit()
        }
    }


}