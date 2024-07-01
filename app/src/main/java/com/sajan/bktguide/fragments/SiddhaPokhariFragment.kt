package com.sajan.bktguide.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.sajan.bktguide.R
import com.sajan.bktguide.dataModels.PlaceDataModel
import kotlinx.android.synthetic.main.fragment_places_detail.view.*

class SiddhaPokhariFragment(private var placeDataDtoList: ArrayList<PlaceDataModel>) : Fragment() {

    private var onMapPinClicked: OnMapPinClickListener? = null
    private var siddhaPokhariDtoList = ArrayList<PlaceDataModel>()
    private var placeDataDto = PlaceDataModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in 0 until placeDataDtoList.size) {
            placeDataDto = placeDataDtoList[i]
            if (placeDataDto.placeId == 5) {
                siddhaPokhariDtoList.add(placeDataDto)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_places_detail, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.ivPlaceImage.setImageDrawable(resources.getDrawable(R.drawable.img_siddhapokhari))
        for (i in 0 until siddhaPokhariDtoList.size) {
            val data = siddhaPokhariDtoList[i]
            view.tvPlaceName.text = data.placeName
            view.tvContent.text = data.content
            view.tvContent.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
        }
        view.ivMapPin.setOnClickListener {
            onMapPinClicked!!.onClickedMapPin(27.671938, 85.420718)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMapPinClickListener) {
            onMapPinClicked = context
        }
        super.onAttach(context)
    }

    interface OnMapPinClickListener {
        fun onClickedMapPin(latitude: Double, longitude: Double)
    }
}
