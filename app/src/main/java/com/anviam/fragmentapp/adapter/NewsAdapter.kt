package com.anviam.fragmentapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anviam.fragmentapp.databinding.ItemNewsBinding
import com.anviam.fragmentapp.model.ResultsItem
import com.bumptech.glide.Glide

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    private var newsList: List<ResultsItem> = emptyList()

    fun setNews(list: List<ResultsItem>) {
        newsList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun getItemCount(): Int = newsList.size

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    class NewsViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ResultsItem) {
            binding.tvHeading.text = item.title ?: "No Title"
            binding.tvNewsDetails.text = item.body ?: "No Details"
            val imageUrl = item.image
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(binding.ivNews.context)
                    .load(imageUrl)
                    .into(binding.ivNews)
            } else {
                binding.ivNews.setImageResource(com.anviam.fragmentapp.R.drawable.chat_icon_com)
            }
        }
    }
}