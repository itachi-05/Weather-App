package com.alpharays.myweatherapp.`object`

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object TrackingUtility {

    fun hasLocationPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
}