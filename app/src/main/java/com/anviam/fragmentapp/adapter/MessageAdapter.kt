package com.anviam.fragmentapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anviam.fragmentapp.databinding.MessageItemBinding
import com.anviam.fragmentapp.model.message
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar


class MessageAdapter(private val messageList: List<message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val calender = Calendar.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messageList.size

    inner class MessageViewHolder(private val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: message) {
            val hour = calender.get(Calendar.HOUR_OF_DAY ).toString()
            val min = calender.get(Calendar.MINUTE).toString()

            if (message.senderId == currentUserId) {
                binding.tvMessage.visibility = View.GONE
                binding.ivReceiverProfile.visibility = View.GONE
                binding.receiverDateAndTime.visibility = View.GONE
                binding.tvMessage1.text = message.message
                binding.tvMessage1.visibility = View.VISIBLE
                binding.senderDateAndTime.text = "$hour:$min"
            } else {
                binding.tvMessage1.visibility = View.GONE
                binding.ivSenderProfile.visibility = View.GONE
                binding.senderDateAndTime.visibility = View.GONE
                binding.tvMessage.text = message.message
                binding.tvMessage.visibility = View.VISIBLE
                binding.receiverDateAndTime.text = "$hour:$min"
            }
        }
    }
}
