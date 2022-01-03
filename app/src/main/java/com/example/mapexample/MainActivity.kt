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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.example.mapexample.databinding.ActivityMainBinding

import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest

import com.google.android.gms.location.LocationSettingsResponse

import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.lang.Exception


class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnLocationRvItemsClicked {

    private lateinit var arrayData: Array<String>
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var placesClient: PlacesClient
    lateinit var adapter: SearchLocationAdapter
    private val recyclerViewList: MutableList<AutocompletePrediction> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initialize(this, "AIzaSyDOGwVObwVRbB7ue1rqkvyFbarZFDBHeuM")
        placesClient = Places.createClient(this)
        binding.btnSearch.setOnClickListener {


        }
        binding.sourceLocation.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.sourceLocation.text.toString().length>2){
                    getSuggestions(binding.sourceLocation.text.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        adapter = SearchLocationAdapter(placesClient, this, recyclerViewList, this)
        binding.rv.adapter = adapter
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Maps", "Place: " + place.name)
            }

            override fun onError(status: Status) {
                Log.d("Maps", "An error occurred: $status")
            }
        })

        enableLocationSettings()
    }

    fun getSuggestions(keyword: String) {

        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(keyword).build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener {
            if (!it.autocompletePredictions.isNullOrEmpty()) {
                recyclerViewList.clear()
                recyclerViewList.addAll(it.autocompletePredictions as MutableList<AutocompletePrediction>)
                adapter.notifyDataSetChanged()
                binding.rv.visibility = View.VISIBLE
            } else {
                binding.rv.visibility = View.GONE
            }
        }
    }

    private fun enableLocationSettings() {
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
                arrayData = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayData, 456)

                    } else {
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
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showMapData()
        } else {
            enableLocationSettings()
        }
    }

    private fun showMapData() {
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
        mMap = p0
    }

    override fun onLocationsItemClicked(place: String, latLng: LatLng?) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng!!))
    }


}