package com.anviam.fragmentapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.anviam.fragmentapp.chatApp.ChatActivity
import com.anviam.fragmentapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var binding : FragmentProfileBinding ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        onClick()
        return binding?.root
    }

    private fun onClick() {
        binding?.btnLogin?.setOnClickListener {
            signInUser()
        }

        binding?.tvRegister?.setOnClickListener {
            val fragmentHome = HomeFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentHome)
                .addToBackStack(null)
                .commit()
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
//            reload()
        }
    }

    private fun signInUser() {
        val email = binding?.etEmail?.text.toString().trim()
        val password = binding?.etPassword?.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Log.d("FirebaseAuth", "signInWithEmail:success")
                        updateUI(auth.currentUser)
                    } else {
                        Log.w("FirebaseAuth", "signInWithEmail:failure", task.exception)
//                        createUser(email, password)  // Try to create an account if sign-in fails
                        Toast.makeText(requireContext(),"User Doesn't Exist, Register First", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
           val intent = Intent(requireActivity(), ChatActivity::class.java )
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "User authentication failed!", Toast.LENGTH_SHORT).show()
        }
    }

}
