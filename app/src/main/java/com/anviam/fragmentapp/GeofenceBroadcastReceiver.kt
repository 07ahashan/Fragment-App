package com.anviam.fragmentapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                triggeringGeofences?.forEach { geofence ->
                    NotificationHelper.showGeofenceNotification(
                        context,
                        "Entered Geofence",
                        "You have entered: ${geofence.requestId}"
                    )
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                triggeringGeofences?.forEach { geofence ->
                    NotificationHelper.showGeofenceNotification(
                        context,
                        "Exited Geofence",
                        "You have exited: ${geofence.requestId}"
                    )
                }
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                triggeringGeofences?.forEach { geofence ->
                    NotificationHelper.showGeofenceNotification(
                        context,
                        "Dwelling in Geofence",
                        "You are staying in: ${geofence.requestId}"
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
} 