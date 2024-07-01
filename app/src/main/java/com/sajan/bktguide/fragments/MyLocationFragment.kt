package com.sajan.bktguide.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.sajan.bktguide.R
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.sajan.bktguide.utils.log
import com.google.android.gms.maps.CameraUpdateFactory

class MyLocationFragment(private var toolbar: Toolbar) : Fragment() {

    private var mMap: GoogleMap? = null
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.title = "My Location"
        latitude = arguments?.getDouble("latitude")!!
        longitude = arguments?.getDouble("longitude")!!
        this@MyLocationFragment::class.java.log("$latitude, $longitude")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(mOnMapCallBacks)
    }

    private val mOnMapCallBacks = OnMapReadyCallback { googleMap ->
        if (googleMap != null) {
            mMap = googleMap
            val durbarSquare = LatLng(27.672075, 85.428104)
            val nyatapoloTemple = LatLng(27.671359, 85.429328)
            val siddhapokhari = LatLng(27.671938, 85.420718)
            val potterySquare = LatLng(27.669898, 85.427781)
            val dattatreyaTemple = LatLng(27.673397, 85.435359)
            val kamalPokhari = LatLng(27.676866, 85.438387)

            mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings?.isCompassEnabled = true
            mMap!!.uiSettings?.isZoomGesturesEnabled = true
            mMap!!.uiSettings?.isRotateGesturesEnabled = true

            mMap!!.addMarker(MarkerOptions().position(durbarSquare).title("Bhaktapur Durbar Square"))
                .showInfoWindow()
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(durbarSquare))

            mMap!!.addMarker(MarkerOptions().position(nyatapoloTemple).title("Nyatapolo Temple"))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(nyatapoloTemple))

            mMap!!.addMarker(MarkerOptions().position(siddhapokhari).title("SiddhaPokhari"))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(siddhapokhari))

            mMap!!.addMarker(MarkerOptions().position(potterySquare).title("Pottery Square"))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(potterySquare))

            mMap!!.addMarker(MarkerOptions().position(dattatreyaTemple).title("Dattatreya Temple"))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(dattatreyaTemple))

            mMap!!.addMarker(MarkerOptions().position(kamalPokhari).title("Kamal Pokhari"))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(kamalPokhari))

            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude), 16f
                )
            )
        }
    }
}