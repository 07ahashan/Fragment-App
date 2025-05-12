package com.anviam.fragmentapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.anviam.fragmentapp.adapter.MessageAdapter
import com.anviam.fragmentapp.databinding.FragmentChatBinding
import com.anviam.fragmentapp.model.message
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private var messageList = mutableListOf<message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("messages")

        // Setup RecyclerView
        messageAdapter = MessageAdapter(messageList)
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = messageAdapter

        // Load messages from Firebase
        loadMessages()


        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.etMessage.text?.clear()
            }
        }
    }

    private fun loadMessages() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(message::class.java)
                    message?.let { messageList.add(it) }
                }
                messageAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendMessage(text: String) {
        val messageId = database.push().key ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

        val message = message(senderId = userId, message = text, timestamp = System.currentTimeMillis())

        database.child(messageId).setValue(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }
}
