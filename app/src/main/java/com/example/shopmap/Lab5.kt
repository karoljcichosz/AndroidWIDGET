package com.example.shopmap


import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast
import android.media.MediaPlayer




/**
 * Implementation of App Widget functionality.
 */
class Lab5 : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them

        for (appWidgetId in appWidgetIds) {
            // appWidgetManager.updateAppWidget( appWidgetId, views);
            updateAppWidget(context, appWidgetManager, appWidgetId)
            // super.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (ACTION_SIMPLEAPPWIDGET == intent.action) {

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.lab5)
            if (tmp!!)
                views.setImageViewResource(R.id.imageView, R.drawable.a)
            else
                views.setImageViewResource(R.id.imageView, R.drawable.b)
            tmp = (!tmp)!!
            // This time we dont have widgetId. Reaching our widget with that way.
            val appWidget = ComponentName(context, Lab5::class.java)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidget, views)
        }
        if (intent.action == OPEN_BROWSER_ACTION) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com"))
            browserIntent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(browserIntent)
        }
        if (intent.action == PLAY_ACTION) {

                if (mp.isPlaying)
                    mp.pause()
                else
                    mp.start()
        }

        if(intent.action== NEXT_ACTION)
        {
            if (tmp2!!)
                mp = MediaPlayer.create(context.applicationContext, R.raw.ma)
            else
                mp = MediaPlayer.create(context.applicationContext, R.raw.mb)
            tmp2 = (!tmp2)!!
        }

        if(intent.action== STOP_ACTION )
        {
            mp.stop()
            if (tmp2!!)
                mp = MediaPlayer.create(context.applicationContext, R.raw.mb)
            else
                mp = MediaPlayer.create(context.applicationContext, R.raw.ma)
        }
    }

    companion object {
        private val ACTION_SIMPLEAPPWIDGET = "ACTION_BROADCASTWIDGETSAMPLE"
        private val OPEN_BROWSER_ACTION = "ACTION_BROWSER"
        private val PLAY_ACTION = "ACTION_PLAY"
        private val NEXT_ACTION = "NEXT_SONG"
        private val STOP_ACTION = "STOP_SONG"
        private var tmp: Boolean = true
        private var tmp2: Boolean = true
        private lateinit var mp : MediaPlayer
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            if (tmp2!!)
                mp = MediaPlayer.create(context.applicationContext, R.raw.ma)
            else
                mp = MediaPlayer.create(context.applicationContext, R.raw.mb)
            tmp2 = (!tmp2)!!
//            tmp = true
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.lab5)

            views.setImageViewResource(R.id.imageView, R.drawable.a)
            var clickIntent = Intent(context, Lab5::class.java)
            clickIntent.action = ACTION_SIMPLEAPPWIDGET
            var pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.imageView, pendingIntent)
            clickIntent = Intent(context, Lab5::class.java)
            clickIntent.action = OPEN_BROWSER_ACTION
            pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.facebook, pendingIntent)

            clickIntent = Intent(context, Lab5::class.java)
            clickIntent.action = PLAY_ACTION
            pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.play, pendingIntent)

            clickIntent = Intent(context, Lab5::class.java)
            clickIntent.action = NEXT_ACTION
            pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.next, pendingIntent)

            clickIntent = Intent(context, Lab5::class.java)
            clickIntent.action = STOP_ACTION
            pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.stop, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)

        }
    }
}