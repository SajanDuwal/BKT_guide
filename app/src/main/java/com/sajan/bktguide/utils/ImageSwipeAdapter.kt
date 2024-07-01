package com.sajan.bktguide.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.sajan.bktguide.R

class ImageSwipeAdapter(private val context: Context) : PagerAdapter() {

    private val galImages = intArrayOf(
        R.drawable.img_bkt_map,
        R.drawable.img_durbar_sq,
        R.drawable.img_taumadhi,
        R.drawable.img_dattatri,
        R.drawable.img_potterysquare,
        R.drawable.img_siddhapokhari,
        R.drawable.img_kamalpokhari,
        R.drawable.img_bkt_map
    )

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as ImageView
    }

    override fun getCount() = galImages.size

    override fun instantiateItem(container: ViewGroup, position: Int): ImageView {
        val imageView = ImageView(context)
        imageView.setImageResource(galImages[position])
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        (container as ViewPager).addView(imageView, 0)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as ImageView)
    }
}