package com.anviam.fragmentapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.anviam.fragmentapp.chatApp.ChatActivity
import com.anviam.fragmentapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private var binding :ActivityMainBinding ?= null
    private var firebaseAuth :FirebaseAuth ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val sideBarNavigation = findViewById<NavigationView>(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)

        onClick()
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_library -> replaceFragment(DateAndTimePickerFragment())
                R.id.nav_subscription -> replaceFragment(SubscriptionFragment())
                R.id.nav_shorts -> replaceFragment(ShortFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
        sideBarNavigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_library -> replaceFragment(DateAndTimePickerFragment())
                R.id.nav_subscription -> replaceFragment(SubscriptionFragment())
                R.id.nav_shorts -> replaceFragment(ShortFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close sidebar after selecting an item
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun onClick() {
        binding?.ivHumburger?.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        binding?.btnReg?.setOnClickListener{
            val intent = Intent(this@MainActivity, ViewBindingActivity::class.java)
            startActivity(intent)
        }

        binding?.btnChat?.setOnClickListener{
            val intent = Intent(this@MainActivity, ChatActivity::class.java)
            startActivity(intent)
        }

        binding?.btnLogout?.setOnClickListener {
            firebaseAuth?.signOut()
            Toast.makeText(this,"Logout Successfully!!", Toast.LENGTH_SHORT).show()
        }

        binding?.btnGoogle?.setOnClickListener {
            replaceFragment(MapsFragment())
        }

    }

}
