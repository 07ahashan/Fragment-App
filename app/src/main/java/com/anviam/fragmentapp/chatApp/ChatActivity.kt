package com.anviam.fragmentapp.chatApp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.anviam.fragmentapp.R
import com.anviam.fragmentapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private var binding: ActivityChatBinding ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding!!.chatBottomNavigate, navController)

        binding?.chatBottomNavigate?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.i_addUser -> navController.navigate(R.id.addUserFragment)
                R.id.i_history -> navController.navigate(R.id.historyFragment)
                R.id.i_chat -> navController.navigate(R.id.chatFragment)
            }
            true
        }
    }
}