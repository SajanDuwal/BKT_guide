package com.sajan.bktguide.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.*
import com.sajan.bktguide.rvAdapters.RvCustomIconDecoration
import com.sajan.bktguide.R
import com.sajan.bktguide.dataModels.TouristDto
import com.sajan.bktguide.fragments.*
import com.sajan.bktguide.rvAdapters.RvCustomAdapter
import com.sajan.bktguide.storage.PrefLogin
import com.sajan.bktguide.storage.PrefsImageUri
import com.sajan.bktguide.utils.EndDrawerToggle
import kotlinx.android.synthetic.main.activity_main.*
import android.net.Uri


class MainActivity : AppCompatActivity(),
    DurbarSquareFragment.OnMapPinClickListener,
    TaumadhiSquareFragment.OnMapPinClickListener,
    DattatreyaFragment.OnMapPinClickListener,
    SiddhaPokhariFragment.OnMapPinClickListener,
    KamalPokahriFragment.OnMapPinClickListener,
    PotterySquareFragment.OnMapPinClickListener {

    private val titleList = ArrayList<String>()
    private var rvCustomAdapter: RvCustomAdapter? = null
    private var actionBarDrawerToggle: EndDrawerToggle? = null
    private var position: Int = 1
    private var touristDto: TouristDto? = null

    private val requestCodeLocation = 300
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var latitude = 0.0
    private var longitude = 0.0

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        touristDto = intent.getParcelableExtra("dto")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        setUpActionBar()
        initViews()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
    }

    private fun initViews() {
        actionBarDrawerToggle = EndDrawerToggle(
            this@MainActivity,
            dlDashboard,
            toolbar,
            R.string.open,
            R.string.close
        )
        dlDashboard.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()

        titleList.add(touristDto!!.name!!)
        titleList.add("Home")
        titleList.add("Background")
        titleList.add("Introduction")
        titleList.add("Service")
        titleList.add("Places")
        titleList.add("Taxi Service")
        titleList.add("Logout")
        rvItemList.addItemDecoration(RvCustomIconDecoration())
        rvCustomAdapter = RvCustomAdapter(this@MainActivity, titleList)
        rvItemList.layoutManager = LinearLayoutManager(this@MainActivity)
        rvCustomAdapter!!.onSetClickListener(mOnRvClickListener)
        rvItemList.adapter = rvCustomAdapter
    }

    private val mOnRvClickListener = object : RvCustomAdapter.OnRvClickListener {
        override fun onItemClicked(position: Int) {
            when (position) {
                0 -> {
                    val intent = Intent(this@MainActivity, EditActivity::class.java)
                    intent.putExtra("dto", touristDto)
                    startActivity(intent)
                    finish()
                }

                1 -> {
                    selectFragment(position) // My location
                }

                3 -> {
                    selectFragment(position) // Introduction
                }

                5 -> {
                    selectFragment(position) // Places
                }

                6 -> {
                    Toast.makeText(this@MainActivity, "TaxiService", Toast.LENGTH_SHORT)
                        .show()
                }
                7 -> {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage("Logged out?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                            if (account != null) {
                                googleSignInClient.signOut()
                                    .addOnCompleteListener(this@MainActivity) {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                LoginActivity::class.java
                                            )
                                        )
                                        googleSignInClient.revokeAccess()
                                    }
                                PrefLogin(this@MainActivity).resetLoginPrefs()
                                PrefsImageUri(this@MainActivity).resetImageData()
                                finish()
                            } else {
                                PrefLogin(this@MainActivity).resetLoginPrefs()
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                                finish()
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create().show()
                }
            }
            dlDashboard.closeDrawer(GravityCompat.END)
        }
    }

    private fun selectFragment(position: Int) {
        var fragment = Fragment()
        this.position = position
        when (position) {
            1 -> {
                fragment = MyLocationFragment(toolbar)
                val bundle = Bundle()
                bundle.putDouble("latitude", latitude)
                bundle.putDouble("longitude", longitude)
                fragment.arguments = bundle
            }

            3 -> {
                fragment = IntroductionFragment(toolbar)
            }
            5 -> {
                fragment = PlacesFragment(toolbar)
            }
        }
        setFragment(fragment)
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.flDashboardContainer, fragment)
            .commit()
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                        val fragment = MyLocationFragment(toolbar)
                        val bundle = Bundle()
                        bundle.putDouble("latitude", latitude)
                        bundle.putDouble("longitude", longitude)
                        fragment.arguments = bundle
                        setFragment(fragment)
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            requestCodeLocation
        )
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestCodeLocation -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    getLastLocation()
                } else {
                    requestPermissions()
                }
            }
        }
    }


    //  on getLocationClicked
    override fun onClickedMapPin(latitude: Double, longitude: Double) {
        position = 1
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?daddr=$latitude,$longitude")
        )
        startActivity(intent)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle!!.syncState()
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                        val fragment = MyLocationFragment(toolbar)
                        val bundle = Bundle()
                        bundle.putDouble("latitude", latitude)
                        bundle.putDouble("longitude", longitude)
                        fragment.arguments = bundle
                        setFragment(fragment)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (checkPermission()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                        val fragment = MyLocationFragment(toolbar)
                        val bundle = Bundle()
                        bundle.putDouble("latitude", latitude)
                        bundle.putDouble("longitude", longitude)
                        fragment.arguments = bundle
                        setFragment(fragment)
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        if (dlDashboard.isDrawerOpen(GravityCompat.END)) {
            dlDashboard.closeDrawer(GravityCompat.END)
        } else if (position != 1) {
            position = 1
            selectFragment(position)
        } else {
            if (position == 1) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        super.onBackPressed()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()
            }
        }
    }
}
