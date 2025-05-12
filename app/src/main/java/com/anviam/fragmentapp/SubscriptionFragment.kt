package com.anviam.fragmentapp
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.anviam.fragmentapp.adapter.ImagePagerAdapter

class SubscriptionFragment : Fragment() {

    private var viewPager: ViewPager2 ?= null

    // Sample images from drawable
    private val imageList = listOf(
        R.drawable.image1,
        R.drawable.image2,
        R.drawable.image3
    )
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_subscription, container, false)

        viewPager = view.findViewById(R.id.imageViewPager)
        viewPager?.adapter = ImagePagerAdapter(imageList)
        return view
    }
}