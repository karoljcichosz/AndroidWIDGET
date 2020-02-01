package com.example.shopmap.apka

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.shopmap.ListActivity
import com.example.shopmap.MapsActivity
import com.example.shopmap.R
import com.example.GeofenceService

class MainActivity : AppCompatActivity() {

    lateinit private var intentMap: Intent
    lateinit private var intentList : Intent
    private var mBound: Boolean = false
    companion object {
        lateinit var mService: GeofenceService
    }
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as GeofenceService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intentList=Intent(this, ListActivity::class.java)
        intentMap=Intent(this, MapsActivity::class.java)
        val intent =Intent(this, GeofenceService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(mBound)
            mService.updateGeofenceLocations(this, false)
        else {
            val intent = Intent(this, GeofenceService::class.java)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }


        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mBound)
        {
            mBound=false
            unbindService(connection)
        }
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
