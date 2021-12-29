package com.example.mapexample

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.jar.Manifest
import android.content.IntentSender
import android.content.IntentSender.SendIntentException
import android.util.Log
import com.example.mapexample.databinding.ActivityMainBinding

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest

import com.google.android.gms.location.LocationSettingsResponse

import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.lang.Exception


class MainActivity : AppCompatActivity() ,OnMapReadyCallback{

    private lateinit var arrayData: Array<String>
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Places.initialize(getApplicationContext(), "AIzaSyDzWXXSTbALY_4iKkVY4ZMtUaZcsnh2dbA");

        binding.btnSearch.setOnClickListener {
            binding.sourceLocation.text.toString().trim()
            binding.destinationLocation.text.toString().trim()

            if (binding.sourceLocation.equals("") && binding.destinationLocation.equals("")){
                Toast.makeText(this,"please enter both location",Toast.LENGTH_SHORT).show()
            }else{

            }
        }




        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.d("Maps", "Place: "+ place.name)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.d("Maps", "An error occurred: $status")
            }
        })
       showMapData()
    }

    protected fun enableLocationSettings() {
        val locationRequest = LocationRequest.create()
            .setInterval((10 * 1000).toLong())
            .setFastestInterval((2 * 1000).toLong())
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(
                this
            ) { response: LocationSettingsResponse? ->
               arrayData= arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayData, 456)

                    }else
                    {
                        showMapData()
                    }
                } else {
                    TODO("VERSION.SDK_INT < M")
                    showMapData()
                }



            }
            .addOnFailureListener(
                this
            ) { ex: Exception? ->
                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        ex.startResolutionForResult(
                            this@MainActivity,
                            123
                        )
                    } catch (sendEx: SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            enableLocationSettings()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            showMapData()
        }else
        {
            enableLocationSettings()
        }
    }

    private fun showMapData()
    {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment)
                as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            mMap = it

            val location = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(location).title("My Locations"))
            mMap.animateCamera(CameraUpdateFactory.newLatLng(location))
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

            }
            mMap.isMyLocationEnabled = true
        })

    }

    override fun onMapReady(p0: GoogleMap) {
        mMap=p0
    }


}