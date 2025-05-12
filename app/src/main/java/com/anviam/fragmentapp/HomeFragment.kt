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
import com.anviam.fragmentapp.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private var firebaseAuth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        onClLickRegister()
        return binding?.root
    }

    private fun onClLickRegister() {
        binding?.btnRegister?.setOnClickListener {
            val email = binding?.etEmail?.text?.toString()?.trim()
            val password = binding?.etPassword?.text.toString().trim()
            createUser(email!! , password)
        }
    }

    private fun createUser(email:String, password :String )
    {
        firebaseAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "createUserWithEmail:success")
                    Toast.makeText(requireContext(),"Register Successfully!!",Toast.LENGTH_SHORT).show()
                    updateUI(firebaseAuth?.currentUser)  // Redirect to ChatFragment after successful signup
                } else {
                    Log.w("FirebaseAuth", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
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