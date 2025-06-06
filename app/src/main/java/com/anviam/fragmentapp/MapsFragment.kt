package com.anviam.fragmentapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.anviam.fragmentapp.databinding.FragmentMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var binding: FragmentMapsBinding? = null
    private val locationPermissionRequestCode = 1001
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var geofencingClient: GeofencingClient? = null
    private var destLatLng: LatLng? = null
    private var originLatLng: LatLng? = null
    
    // Geofence properties
    private var currentGeofence: Geofence? = null
    private var geofenceRadius = 100f // Default radius in meters
    private var isGeofenceMode = false
    private val geofencePendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        // Initialize location clients
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        // Create notification channel
        NotificationHelper.createNotificationChannel(requireContext())

        onClick()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isZoomGesturesEnabled = true
        mMap?.uiSettings?.isCompassEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true

        // Enable location and fetch origin
        binding?.mapProgressBar?.visibility = View.VISIBLE
        enableMyLocation()
        binding?.mapProgressBar?.visibility = View.GONE

        // Add map click listener for geofence creation
        mMap?.setOnMapClickListener { latLng ->
            if (isGeofenceMode) {
                createGeofence(latLng)
            }
        }
    }

    private fun onClick() {
        binding?.apply {
            ivSearch?.setOnClickListener {
                searchPlace()
            }

            btnSatellite?.setOnClickListener {
                mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }

            btnTerrain?.setOnClickListener {
                mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }

            btnAddGeofence?.setOnClickListener {
                toggleGeofenceMode()
            }

            btnIncreaseRadius?.setOnClickListener {
                geofenceRadius += 50f
                updateGeofenceRadius()
                updateRadiusText()
            }

            btnDecreaseRadius?.setOnClickListener {
                if (geofenceRadius > 50f) {
                    geofenceRadius -= 50f
                    updateGeofenceRadius()
                    updateRadiusText()
                }
            }
        }
    }

    private fun updateRadiusText() {
        binding?.tvRadius?.text = "${geofenceRadius.toInt()}m"
    }

    private fun searchPlace() {
        val searchedPlace = binding?.etSearch?.text?.toString()?.trim()
        if (!searchedPlace.isNullOrEmpty()) {
            binding?.mapProgressBar?.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    destLatLng = withContext(Dispatchers.IO) {
                        context?.let { getLocationFromAddress(it, searchedPlace) }
                    }

                    if (destLatLng != null) {
                        mMap?.clear()
                        mMap?.addMarker(MarkerOptions().position(destLatLng!!).title(searchedPlace))
                        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(destLatLng!!, 15f))

                    } else {
                        Toast.makeText(context, "Location not found! Please try a different search term.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error searching location: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding?.mapProgressBar?.visibility = View.GONE
                }
            }
        } else {
            Toast.makeText(context, "Please enter a location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLocationFromAddress(context: Context, locationName: String): LatLng? {
        val geocoder = Geocoder(context)
        return try {
            val addressList = geocoder.getFromLocationName(locationName, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                LatLng(address.latitude, address.longitude)
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionRequestCode
            )
            return
        }

        try {
            mMap?.isMyLocationEnabled = true
            mMap?.uiSettings?.isMyLocationButtonEnabled = true

            fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    originLatLng = LatLng(location.latitude, location.longitude)
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(originLatLng!!, 15f))
                } else {
                    Toast.makeText(requireContext(), "Unable to get current location. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }?.addOnFailureListener { e ->
                Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleGeofenceMode() {
        isGeofenceMode = !isGeofenceMode
        binding?.btnAddGeofence?.text = if (isGeofenceMode) "Cancel Geofence" else "Add Geofence"
        
        if (!isGeofenceMode) {
            // Clear any existing geofence visualization
            mMap?.clear()
            currentGeofence = null
        }
    }

    private fun updateGeofenceRadius() {
        currentGeofence?.let { geofence ->
            val center = LatLng(geofence.latitude, geofence.longitude)
            mMap?.clear()
            mMap?.addCircle(
                CircleOptions()
                    .center(center)
                    .radius(geofenceRadius.toDouble())
                    .strokeWidth(2f)
                    .fillColor(0x330000FF.toInt())
                    .strokeColor(0xFF0000FF.toInt())
            )
        }
    }

    private fun createGeofence(latLng: LatLng) {
        if (!hasGeofencePermission()) {
            requestGeofencePermission()
            return
        }

        // Create a unique ID for the geofence
        val geofenceId = "GEOFENCE_${System.currentTimeMillis()}"
        
        // Create the geofence
        currentGeofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(
                latLng.latitude,
                latLng.longitude,
                geofenceRadius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                Geofence.GEOFENCE_TRANSITION_EXIT or
                Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .setNotificationResponsiveness(1000) // 1 second
            .setLoiteringDelay(30000) // 30 seconds for dwell
            .build()

        // Visualize the geofence on the map
        mMap?.clear()
        mMap?.addCircle(
            CircleOptions()
                .center(latLng)
                .radius(geofenceRadius.toDouble())
                .strokeWidth(2f)
                .fillColor(0x330000FF.toInt())
                .strokeColor(0xFF0000FF.toInt())
        )

        // Add a marker at the center
        mMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Geofence Center")
                .snippet("Radius: ${geofenceRadius}m")
        )

        // Add the geofence to monitoring
        addGeofenceToMonitoring()

        // Show confirmation dialog
        showGeofenceConfirmationDialog(latLng)
    }

    private fun addGeofenceToMonitoring() {
        currentGeofence?.let { geofence ->
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            geofencingClient?.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Geofence monitoring started",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to start geofence monitoring: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun hasGeofencePermission(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        // Check for background location permission on Android 10 and above
        val backgroundLocation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        return fineLocation && backgroundLocation
    }

    private fun requestGeofencePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // First request only ACCESS_FINE_LOCATION
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationPermissionRequestCode
                )
            } else {
                // If FINE_LOCATION is granted, request BACKGROUND_LOCATION
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    locationPermissionRequestCode + 1
                )
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required for geofencing",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showGeofenceConfirmationDialog(latLng: LatLng) {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown Location"
            
            Toast.makeText(
                requireContext(),
                "Geofence created at: $address\nRadius: ${geofenceRadius}m",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: IOException) {
            Toast.makeText(
                requireContext(),
                "Geofence created at: (${latLng.latitude}, ${latLng.longitude})\nRadius: ${geofenceRadius}m",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            mMap?.clear()
            mMap = null
            fusedLocationClient = null
            originLatLng = null
            destLatLng = null
            binding = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
