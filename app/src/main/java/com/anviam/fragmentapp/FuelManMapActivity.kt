package com.anviam.fragmentapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import android.view.inputmethod.EditorInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale
import android.widget.FrameLayout
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView

/**
 * FuelManMapActivity handles the map interface for fuel management functionality.
 * It provides features like location search, custom markers, and map controls.
 */
class FuelManMapActivity : AppCompatActivity(), OnMapReadyCallback {

    // region Properties
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    private var centerMarker: Marker? = null
    private var etSearchBar: AppCompatEditText? = null
    private var tvAddress: AppCompatTextView? = null
    private var btnZoomInButton: AppCompatButton? = null
    private var btnzoomOutButton: AppCompatButton? = null
    private var btnMyLocationButton: FloatingActionButton? = null
    private var btnDefaultViewButton: AppCompatButton? = null
    private var btnSatelliteViewButton: AppCompatButton? = null
    private var btnConfirmButton: AppCompatButton? = null
    private var ivBackButton: AppCompatImageView? = null


    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1
        private const val DEFAULT_ZOOM_LEVEL = 15f
    }
    // endregion

    // region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_man_map)

        initializeComponents()
        setupUIControls()
    }
    // endregion

    // region Initialization Methods
    private fun initializeComponents() {
        // Initialize location services

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        etSearchBar = findViewById(R.id.searchEditText)
        tvAddress = findViewById(R.id.tv_addressText)
        btnZoomInButton = findViewById(R.id.zoomInButton)
        btnzoomOutButton = findViewById(R.id.zoomOutButton)
        btnMyLocationButton = findViewById(R.id.myLocationButton)
        btnDefaultViewButton = findViewById(R.id.defaultViewButton)
        btnSatelliteViewButton = findViewById(R.id.satelliteViewButton)
        btnConfirmButton = findViewById(R.id.confirmButton)
        ivBackButton = findViewById(R.id.iv_back_arrow)
        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupUIControls() {
        setupSearchBar()
        setupZoomControls()
        setupMyLocationButton()
        setupMapTypeButtons()
        fetchCurrentLocation()
    }

    private fun setupMapTypeButtons() {
        btnDefaultViewButton?.setOnClickListener {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        btnSatelliteViewButton?.setOnClickListener {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

        btnConfirmButton?.setOnClickListener {
            // TODO: Implement location confirmation handling
        }

        ivBackButton?.setOnClickListener {
            startActivity(Intent(this@FuelManMapActivity, ShortFragment::class.java))
        }
    }
    // endregion

    // region Map Setup and Controls
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configureMapSettings()
        setupMapListeners()

        // Add fixed marker in the center
        addFixedCenterMarker()
    }

    private fun configureMapSettings() {
        if (checkLocationPermission()) {
            mMap.isMyLocationEnabled = true
        }

        // Configure UI settings
        mMap.uiSettings.apply {
            isZoomControlsEnabled = false
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
        }
    }

    private fun setupMapListeners() {
        mMap.setOnCameraIdleListener {
            // Only update address when camera stops moving
            val center = mMap.cameraPosition.target
            updateAddress(center)
        }
    }

    private fun addFixedCenterMarker() {
        // Get the center of the map's view
        val mapView = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view
        mapView?.let { view ->
            // Create an ImageView for the fixed center marker
            val markerImage = ImageView(this).apply {
                setImageResource(R.drawable.ic_map_marker)
                // Position the marker in the center of the map view
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
            }
            // Add the marker ImageView as an overlay to the map
            (view as ViewGroup).addView(markerImage)
        }
        // Remove the previous marker approach
        centerMarker?.remove()
        centerMarker = null
    }
    // endregion

    // region Location and Search Functionality
    private fun setupSearchBar() {
        etSearchBar?.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(textView.text.toString())
                true
            } else false
        }
    }

    private fun searchLocation(query: String) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(query, 1)

            addresses?.firstOrNull()?.let { address ->
                moveMapToLocation(
                    LatLng(address.latitude, address.longitude),
                    address.getAddressLine(0)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // TODO: Implement error handling for user feedback
        }
    }

    private fun moveMapToLocation(latLng: LatLng, address: String) {
        // Remove previous current location marker
        currentMarker?.remove()

        // Update address display
        tvAddress?.text = address
        fetchCurrentLocation()
    }

    private fun fetchCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                    // Move camera to current location
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM_LEVEL)
                    )
                }
            }
        }
    }
    // endregion

    // region Custom Controls
    private fun setupZoomControls() {
        btnZoomInButton?.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }

        btnzoomOutButton?.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    private fun setupMyLocationButton() {
        btnMyLocationButton?.setOnClickListener {
            if (checkLocationPermission()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)

                        // Move camera to current location
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM_LEVEL)
                        )
                    }
                }
            }
        }
    }
    // endregion

    // region Utility Methods
    private fun getBitmapDescriptorFromVector(vectorResId: Int) =
        ContextCompat.getDrawable(this, vectorResId)?.let { vectorDrawable ->
            vectorDrawable.setBounds(
                0, 0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            )
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            Canvas(bitmap).also { vectorDrawable.draw(it) }
            BitmapDescriptorFactory.fromBitmap(bitmap)
        } ?: BitmapDescriptorFactory.defaultMarker()

    private fun updateAddress(latLng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                tvAddress?.text = address.getAddressLine(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // endregion

    // region Permissions Handling
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (checkLocationPermission()) {
                mMap.isMyLocationEnabled = true
            }
        }
    }
    // endregion
}