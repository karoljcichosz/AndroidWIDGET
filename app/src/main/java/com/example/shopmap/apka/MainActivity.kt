package com.example.shopmap.apka

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.shopmap.ListActivity
import com.example.shopmap.MapsActivity
import com.example.shopmap.R
import com.example.shopmap.geofence.GeofenceService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult


class MainActivity : AppCompatActivity() {

    lateinit private var intentMap: Intent
    lateinit private var intentList : Intent

    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intentList=Intent(this, ListActivity::class.java)
        intentMap=Intent(this, MapsActivity::class.java)
        val geofenceService : GeofenceService = GeofenceService();
        geofenceService.updateGeofenceLocations(applicationContext)

        this.handleLocationUpdates(geofenceService)
    }
    private fun handleLocationUpdates(geofenceService: GeofenceService) {
        val locationRequest : LocationRequest? = this.createLocationRequest();
        if(locationRequest != null ){
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations){
                    }
                }
            }
            this.startLocationUpdates(locationRequest, this.locationCallback)
        }
    }
    private fun startLocationUpdates(locationRequest: LocationRequest, locationCallback: LocationCallback) {
        val fusedLocationClient = FusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    private fun createLocationRequest() : LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }


    override fun onStart() {
        super.onStart()
        var geofenceHandler = GeofenceService()
    }

    fun clickList(view: View)
    {
        startActivity(intentList)
    }
    fun clickMap(view: View)
    {
        startActivity(intentMap)
    }


}
