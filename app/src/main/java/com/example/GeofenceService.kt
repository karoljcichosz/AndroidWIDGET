package com.example.shopmap.geofence

import android.annotation.SuppressLint
import android.widget.Toast
import android.content.Intent
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.room.Room
import com.example.shopmap.MyDB
import com.example.shopmap.Shop
import com.google.android.gms.location.*
import java.util.*

class GeofenceService : IntentService("geo"), LocationListener {
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(location: Location?) {
        this.updateGeofenceLocations(applicationContext)
    }

    private lateinit var locationManager: LocationManager
    private lateinit var geofencingClient : GeofencingClient
    private lateinit var geofenceList : LinkedList<Geofence>
    private lateinit var shops : List<Shop>
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f
    lateinit private var db: MyDB


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        geofencingClient = LocationServices.getGeofencingClient(this)
        db = Room.databaseBuilder(
            applicationContext,
            MyDB::class.java, "database-name"
        ).allowMainThreadQueries().build()
        shops= db.ProductDAO().all
        shops.forEach{shop -> geofenceList.add(
            Geofence.Builder()
                .setCircularRegion(shop.x, shop.y, shop.promien.toFloat())
                .setRequestId(shop.nazwa)
                .setExpirationDuration(MIN_TIME)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this)
    }

    override fun onHandleIntent(intent: Intent?) {
        val ge = GeofencingEvent.fromIntent(intent)
        val gt = ge.getGeofenceTransition()
        if (gt == Geofence.GEOFENCE_TRANSITION_ENTER || gt == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val tg = ge.getTriggeringGeofences()
            Toast.makeText(applicationContext, tg.get(0).toString(), Toast.LENGTH_SHORT)
        } else {
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
        }
    }




    private fun getGeofencingRequest(geofenceList: ArrayList<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            if(!geofenceList.isEmpty())
                addGeofences(geofenceList)
        }.build()
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun updateGeofenceLocations(context: Context) {
        geofencingClient = LocationServices.getGeofencingClient(context)
        var geofenceList : ArrayList<Geofence> = ArrayList()
        db = Room.databaseBuilder(
            context,
            MyDB::class.java, "database-name"
        ).allowMainThreadQueries().build()
        shops= db.ProductDAO().all
        shops.forEach{shop -> geofenceList.add(
            Geofence.Builder()
                .setCircularRegion(shop.x, shop.y, if(shop.promien==0) 1f else shop.promien.toFloat())
                .setRequestId(shop.nazwa)
                .setExpirationDuration(MIN_TIME)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())
        }
            geofencingClient.removeGeofences(getPendingIntent(context))
        if(!geofenceList.isEmpty())
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), getPendingIntent(context))?.run {
                addOnSuccessListener {
                    // Geofences added
                    // ...
                }
                addOnFailureListener {
                    // Failed to add geofences
                    // ...
                }
            }
        }




}
