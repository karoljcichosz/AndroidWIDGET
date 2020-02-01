package com.example

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.example.shopmap.MyDB
import com.example.shopmap.Shop
import com.example.shopmap.geofence.GeofenceBroadcastReceiver
import com.google.android.gms.location.*
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import java.util.*
import kotlin.collections.ArrayList


class GeofenceService : IntentService("geo") {

    private lateinit var locationManager: LocationManager
    private lateinit var geofencingClient : GeofencingClient
    private var geofenceList = ArrayList<Geofence>()
    private val MIN_TIME: Long = 400
    lateinit private var db : MyDB
    private val binder = LocalBinder()

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        init()
    }

    @SuppressLint("MissingPermission")
    fun init() {
        db= Room.databaseBuilder(
            this.applicationContext,
            MyDB::class.java, "database-name"
        ).allowMainThreadQueries().build()
        geofencingClient = LocationServices.getGeofencingClient(this)
        updateGeofenceLocations(this, true)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

    }

    inner class LocalBinder : Binder() {
        fun getService(): GeofenceService {
            return this@GeofenceService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        if(db==null)
            init()
        return binder
    }

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.i("TRANSITION", errorMessage)

        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            sendNotification( geofenceTransitionDetails, applicationContext)
        }
    }

    private fun getGeofencingRequest(geofenceList: ArrayList<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofences(geofenceList)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).build()
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun updateGeofenceLocations(context: Context, initial: Boolean) {
        val shops= db.ProductDAO().all
        if(!initial && Companion.shops.size==shops.size)
            return
        Companion.shops=shops
        geofenceList.clear()
        shops.forEach{shop -> geofenceList.add(
            Geofence.Builder()
                .setCircularRegion(shop.x, shop.y, if(shop.promien==0) 1f else shop.promien.toFloat())
                .setRequestId(shop.nazwa)
                .setExpirationDuration(-1L)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())
        }
        if(!geofenceList.isEmpty())
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), getPendingIntent(context))?.run {
                addOnSuccessListener {
                }
                addOnFailureListener {
                }
            }
        }


    private val CHANNEL: String = "geofence_channel"
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_ENTER)
            return "ENTER" + ": " + triggeringGeofencesIdsString
        else if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_EXIT)
            return "exit" + ": " + triggeringGeofencesIdsString
        else
            return "blad"
    }

    private fun sendNotification(geofenceTransitionDetails: String, context: Context) {
        createNotificationChannel(context)
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, AlertShop::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        var builder = NotificationCompat.Builder(context, CHANNEL)
            .setContentTitle("geofence")
            .setContentText(geofenceTransitionDetails)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val nm = NotificationManagerCompat.from(context)
        val random = Random()
        val m: Int = random.nextInt(9999 - 1000) + 1000
        nm.notify(m, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL
            val descriptionText = "AAA"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private lateinit var shops : List<Shop>
    }

}
