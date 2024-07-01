package com.sajan.bktguide.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.sajan.bktguide.dataModels.PlaceDataModel
import com.sajan.bktguide.fragments.*
import org.json.JSONArray
import java.nio.charset.Charset

class PlacesDetailPageAdapter(
    supportFragmentManager: FragmentManager,
    private val context: Context
) : FragmentStatePagerAdapter(supportFragmentManager) {

    private val placeDataDtoList = ArrayList<PlaceDataModel>()

    override fun getCount() = 7

    override fun getItem(position: Int): Fragment {
        readIntroduction()
        return when (position) {
            0 -> {
                DurbarSquareFragment(placeDataDtoList)
            }
            1 -> {
                TaumadhiSquareFragment(placeDataDtoList)
            }
            2 -> {
                DattatreyaFragment(placeDataDtoList)
            }
            3 -> {
                PotterySquareFragment(placeDataDtoList)
            }
            4 -> {
                SiddhaPokhariFragment(placeDataDtoList)
            }
            5 -> {
                KamalPokahriFragment(placeDataDtoList)
            }

            else -> {
                AroundBhaktapurFragment(placeDataDtoList)
            }
        }
    }

    private fun readIntroduction() {
        val inputStream = context.assets.open("placeDetails.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        val json = String(buffer, Charset.forName("UTF-8"))
        val jsonArrayResponse = JSONArray(json)
        for (i in 0 until jsonArrayResponse.length()) {
            val jsonObjectResponse = jsonArrayResponse.getJSONObject(i)
            val placeId = jsonObjectResponse.getInt("id")
            val placeName = jsonObjectResponse.getString("place_name")
            val content = jsonObjectResponse.getString("content")
            val placeDataDto = PlaceDataModel()
                .apply {
                    this.placeId = placeId
                    this.placeName = placeName
                    this.content = content
                }
            placeDataDtoList.add(placeDataDto)
        }
    }
}