package com.alpharays.myweatherapp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import android.view.View
import com.alpharays.myweatherapp.databinding.ActivityHomeBinding
import com.google.android.material.snackbar.Snackbar

class LocationPermissionReceiver(private val view: View, private val homeBinding: ActivityHomeBinding) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == LocationManager.MODE_CHANGED_ACTION) {
            val locationManager =
                context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val locationEnabled =
                locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            if (locationEnabled) {
                Log.i("location", "enabled")
                homeBinding.appImageText.text = "Shivang"
                Snackbar.make(view, "Location enabled", 2500).show()
            } else {
                Log.i("location", "disabled")
                Snackbar.make(view, "Location disabled", 2500).show()
            }
        }
    }
}
