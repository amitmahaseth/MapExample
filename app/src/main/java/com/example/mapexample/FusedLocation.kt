package com.example.mapexample

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.webkit.PermissionRequest
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.lang.ref.WeakReference
import java.util.*
import java.util.jar.Manifest

object FusedLocation {

    private var fusedLocationClient: WeakReference<FusedLocationProviderClient>? = null

    /** Initialize Fused Location For Get Lat Long */
    fun initializeFusedLocation(activity: Activity, locationSaveData: (LocationSaveData) -> Unit) {
        fusedLocationClient =
            WeakReference(LocationServices.getFusedLocationProviderClient(activity))

        checkPermission(activity, locationSaveData)
    }


    /** Check Permission */
    private fun checkPermission(activity: Activity, locationSaveData: (LocationSaveData) -> Unit) {
        try {
            Dexter.withContext(activity)
                .withPermissions(
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0?.areAllPermissionsGranted()!!) {
                            if (isLocationEnabled(activity)) {
                                getCurrentLocation(activity, locationSaveData)
                            } else {
                                showGPSNotEnabledDialog(activity)
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }

                }).check()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Show GPS Alert
     * */
    fun showGPSNotEnabledDialog(context: Context) {
//        context.customAlert(context.getString() {
//            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//        }
    }


    /** Location Not Enabled */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    /** Get Current Location */
    fun getCurrentLocation(activity: Activity, latLong: (LocationSaveData) -> Unit) {

        if (ActivityCompat.checkSelfPermission(
                activity,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest =
            LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationClient?.get()?.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        getAddress(activity, location.latitude, location.longitude, latLong)
                    }
                }
            }, Looper.getMainLooper()
        )
    }

    /**
     * Get Address
     * */
    fun getAddress(
        activity: Activity,
        latitude: Double,
        longitude: Double,
        locationSaveData: (LocationSaveData) -> Unit
    ) {
        try {
            val addresses: List<Address>
            val geocode = Geocoder(activity, Locale.getDefault())

            addresses = geocode.getFromLocation(
                latitude,
                longitude,
                1
            )

            val address: String =
                addresses[0].getAddressLine(0)
            val city: String = addresses[0].locality
            val state: String = addresses[0].adminArea
            val country: String = addresses[0].countryName
            val postalCode: String = addresses[0].postalCode
            val knownName: String = addresses[0].featureName
            locationSaveData(
                LocationSaveData(
                    address,
                    city,
                    state,
                    country,
                    postalCode,
                    knownName,
                    latitude.toString(),
                    longitude.toString()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Location Save Data
     * */
    data class LocationSaveData(
        var address: String = "",
        var city: String = "",
        var state: String = "",
        var country: String = "",
        var postalCode: String = "",
        var knownName: String = "",
        var latitude: String = "",
        var longitude: String = ""
    )


}