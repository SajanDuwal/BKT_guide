package com.sajan.bktguide.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.sajan.bktguide.R
import com.sajan.bktguide.adapters.PlacesDetailPageAdapter
import kotlinx.android.synthetic.main.fragment_places.*

class PlacesFragment(private val toolbar: Toolbar) : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.title = "Places"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vpPlaces.adapter = PlacesDetailPageAdapter(childFragmentManager, context!!)
    }
}
