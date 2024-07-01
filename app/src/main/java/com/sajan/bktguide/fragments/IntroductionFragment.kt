package com.sajan.bktguide.fragments

import android.os.Build
import android.os.Bundle
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.sajan.bktguide.R
import com.sajan.bktguide.utils.ImageSwipeAdapter
import com.sajan.bktguide.dataModels.IntroductionDto
import kotlinx.android.synthetic.main.fragment_introduction.view.*
import org.json.JSONArray
import java.nio.charset.Charset

class IntroductionFragment(private var toolbar: Toolbar) : Fragment() {

    private val introductionDtoList = ArrayList<IntroductionDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.title = "Bhaktapur Muncipality"
        readIntroduction()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_introduction, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        for (i in 0 until introductionDtoList.size) {
            val introductionDto = introductionDtoList[i]
            view.tvContent.justificationMode = JUSTIFICATION_MODE_INTER_WORD
            view.tvContent.text = introductionDto.content
            val vpImageSwipe: ViewPager = view.findViewById(R.id.vpImageSwipe)
            val imageSwipeAdapter = ImageSwipeAdapter(context!!)
            vpImageSwipe.adapter = imageSwipeAdapter
            vpImageSwipe.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {

                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) { //this is triggered when the switch to a new page is complete
                        val lastPosition = vpImageSwipe.adapter!!.count - 1
                        val currentPosition = vpImageSwipe.currentItem
                        if (currentPosition == lastPosition) {
                            vpImageSwipe.setCurrentItem(0, false) //false so we don't animate
                        }
                    }
                }
            })
        }
    }

    private fun readIntroduction() {
        val inputStream = context!!.assets.open("introduction.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        val json = String(buffer, Charset.forName("UTF-8"))
        val jsonArrayResponse = JSONArray(json)
        for (i in 0 until jsonArrayResponse.length()) {
            val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
            val id = jsonObjectResponse.getInt("id")
            val title = jsonObjectResponse.getString("title")
            val content = jsonObjectResponse.getString("content")
            val introductionDto = IntroductionDto().apply {
                this.id = id
                this.title = title
                this.content = content
            }
            introductionDtoList.add(introductionDto)
        }
    }
}
