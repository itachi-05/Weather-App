package com.alpharays.myweatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpharays.myweatherapp.`object`.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.alpharays.myweatherapp.`object`.TrackingUtility
import com.alpharays.myweatherapp.broadcast.LocationPermissionReceiver
import com.alpharays.myweatherapp.data.dataclasses.Location
import com.alpharays.myweatherapp.data.interfaces.ConnectivityObserver
import com.alpharays.myweatherapp.databinding.ActivityHomeBinding
import com.alpharays.myweatherapp.viewmodels.WeatherDataViewModel
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import pl.droidsonroids.gif.GifImageView
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import com.alpharays.myweatherapp.R

@SuppressLint("SetTextI18n")
class ActivityHome : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var weatherViewModel: WeatherDataViewModel
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefLoc: SharedPreferences
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var locationPermissionReceiver: LocationPermissionReceiver
    private var locationPermissionStatus: MutableLiveData<Boolean> = MutableLiveData(false)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()
        networkFun()
        loadingLastUpdatedData()

        try {
            locationPermissionStatus.observe(this, Observer {
                if (it != null && it == true) {
                    Log.i("locationPermissionStatus", locationPermissionStatus.toString())
                    networkFun()
                }
            })
        } catch (e: Exception) {
            Log.i("errorException1", e.localizedMessage!!.toString())
        }


        try {
            // Registering the receiver to listen for location permission changes
            locationPermissionReceiver = LocationPermissionReceiver(binding.root)
            val intentFilter = IntentFilter().apply {
                addAction(LocationManager.MODE_CHANGED_ACTION)
            }
            registerReceiver(locationPermissionReceiver, intentFilter)
        } catch (e: Exception) {
            Log.i("errorException2", e.localizedMessage!!.toString())

        }


        val locationRequest = LocationRequest.create().apply {
            interval = (60000 * 2) // 2 min
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                // Handle location updates here
                val location = locationResult.lastLocation
                binding.lastUpdated.text =
                    "Last updated at: ${sunTime(System.currentTimeMillis() / 1000)}"
                Log.d(TAG, "Location: ${location?.latitude}, ${location?.longitude}")
                settingData()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        lifecycleScope.launch(Dispatchers.IO) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun networkFun() {
        Log.i("Called", "NETWORK FUN")
        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            connectivityObserver.observe().collect { status ->
                Log.i("NStatus", status.toString())
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Log.i("checkingNS", "Network Status: $status")
                    binding.networkStatus.text = "Network Status: $status"
                    sharedPrefLoc = getSharedPreferences("sharingTimeStatus", MODE_PRIVATE)
                    val timeStatus = sharedPrefLoc.getString("ok", "false")
                    if (status == ConnectivityObserver.Status.Available) {
                        Log.i("Success121212", "TRUE")
                        settingData()
                    } else if (status == ConnectivityObserver.Status.Lost || timeStatus == "false") {
                        Log.i("Success121212", "FALSE")
                        loadingLastUpdatedData()
                    }
                    binding.lastUpdated.text =
                        "Last updated at: ${sunTime(System.currentTimeMillis() / 1000)}"
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("Location updates Stopped", "true")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Location updates Stopped", "true")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        unregisterReceiver(locationPermissionReceiver)
    }

    private fun loadingLastUpdatedData() {
        sharedPref = getSharedPreferences("sharingWeatherData", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPref.getString("updatedData", null)
        val listType = object : TypeToken<ArrayList<ArrayList<String>>>() {}.type
        var list = gson.fromJson<ArrayList<ArrayList<String>>>(json, listType)

        val lastUpdatedTime =
            sharedPref.getString("lastUpdatedTime", System.currentTimeMillis().toString())
        val lut = lastUpdatedTime?.toLong()!! / 1000
        binding.lastUpdated.text = "Last updated at: ${sunTime(lut)}"

        val currentTime = LocalTime.now().toString()
        val hours = currentTime.split(":")[0]
        if (7 > hours.toInt() || 19 <= hours.toInt()) {
            setGif(R.drawable.night, binding.appImageIcon)
        }

        val dummyList: ArrayList<ArrayList<String>> = ArrayList()
        val dList: ArrayList<String> = ArrayList()
        dList.add("No Location Set")
        dList.add("Clear")
        dList.add("Clear Sky")
        dList.add("none")
        dList.add("25°C")
        dList.add("22°C")
        dList.add("26°C")
        dList.add("24°C")
        dList.add("29")
        dList.add("3.02")
        dList.add("5:30 AM")
        dList.add("6:30 PM")
        dummyList.add(dList)

        if (list == null || list.isEmpty()) {
            list = dummyList
//            Snackbar.make(binding.root, "Restart to update location", 3000).show()
//            Handler(Looper.getMainLooper()).postDelayed({
//                Log.i("called after", "10s")
//            }, 5000)
//            Handler(Looper.getMainLooper()).postDelayed({
//                requestingPermissions()
//                settingData()
//                Log.i("called after", "10s")
//            }, 10000)
        }

        binding.differentWeatherRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        Log.i("dummyList", list.toString())
        val adapter = MyAdapter(this, list)
        binding.differentWeatherRecyclerView.adapter = adapter

    }

    private fun settingData() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            Log.i("Entered", "Setting Data")
            val locationClient = LocationServices.getFusedLocationProviderClient(this)
            locationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.i("Non-null", "Location Data")
                    val latitude = location.latitude.toString()
                    val longitude = location.longitude.toString()
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses =
                        geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
                    Log.i(latitude, longitude)
                    val currentLocation =
                        addresses!![0].subLocality.toString() + ", " + addresses[0].locality.toString() + "\n" + addresses[0].countryName.toString()

                    // current location
                    val location1 = Location(latitude, longitude)
                    // mumbai
                    val location2 = Location(
                        (19.095824250281368).toString(),
                        (72.88117643035409).toString()
                    )
                    // new york
                    val location3 =
                        Location((40.679445344902).toString(), (-73.95224706739447).toString())
                    // melbourne
                    val location4 = Location(
                        (-37.80215178279777).toString(),
                        (144.97611570332285).toString()
                    )
                    // singapore
                    val location5 = Location(
                        (1.3517514121053937).toString(),
                        (103.8538530373388).toString()
                    )
                    // sydney
                    val location6 = Location(
                        (-33.860470488386866).toString(),
                        (151.2134683376721).toString()
                    )

                    val locations: ArrayList<Location> = ArrayList()
                    locations.add(location1); locations.add(location2); locations.add(location3)
                    locations.add(location4); locations.add(location5); locations.add(location6)

                    weatherViewModel = ViewModelProvider(this)[WeatherDataViewModel::class.java]

                    weatherViewModel.response.observe(this, Observer { list ->
                        if (list != null) {
                            val recyclerViewList: ArrayList<ArrayList<String>> = ArrayList()
                            for (i in list.indices) {
                                val weatherList: ArrayList<String> = ArrayList()
                                if (i == 0) weatherList.add(currentLocation)
                                if (i == 1) weatherList.add("Mumbai")
                                if (i == 2) weatherList.add("New York")
                                if (i == 3) weatherList.add("Melbourne")
                                if (i == 4) weatherList.add("Singapore")
                                if (i == 5) weatherList.add("Sydney")
                                weatherList.add(list[i].weather[0].main)
                                weatherList.add(list[i].weather[0].description)
                                weatherList.add(list[i].weather[0].icon)
                                weatherList.add(list[i].main.temp.toString())
                                weatherList.add(list[i].main.feels_like.toString())
                                weatherList.add(list[i].main.temp_max.toString())
                                weatherList.add(list[i].main.temp_min.toString())
                                weatherList.add(list[i].main.humidity.toString())
                                weatherList.add(list[i].wind.speed.toString())
                                val sunrise = sunTime(list[i].sys.sunrise.toLong())
                                weatherList.add(sunrise)
                                val sunset = sunTime(list[i].sys.sunset.toLong())
                                weatherList.add(sunset)
                                recyclerViewList.add(weatherList)
                            }
                            binding.weatherComing.visibility = View.VISIBLE
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.differentWeatherRecyclerView.layoutManager =
                                    LinearLayoutManager(
                                        this,
                                        LinearLayoutManager.HORIZONTAL,
                                        false
                                    )
                                Log.i("Success & Found", recyclerViewList.toString())
                                val adapter = MyAdapter(this, recyclerViewList)
                                binding.differentWeatherRecyclerView.adapter = adapter
                                binding.weatherComing.visibility = View.GONE
                            }, 1200)

                            // sharing all the data in sharedPref
                            sharedPref =
                                getSharedPreferences("sharingWeatherData", MODE_PRIVATE)
                            val gson = Gson()
                            val json = gson.toJson(recyclerViewList)
                            val editor = sharedPref.edit()
                            editor.putString("updatedData", json)
                            editor.putString(
                                "lastUpdatedTime",
                                System.currentTimeMillis().toString()
                            )
                            editor.putString("firstInstall", "false")
                            editor.apply()

                            sharedPrefLoc = getSharedPreferences("sharingTimeStatus", MODE_PRIVATE)
                            val editor2 = sharedPrefLoc.edit()
                            editor2.putString("ok", "true")
                            editor2.apply()
                        }
                    })

                    weatherViewModel.response2.observe(this, Observer {
                        if (it != null && it == false) {
                            Log.i("checkingNetwork", "ERROR")
//                            Snackbar.make(binding.root,"Wait...",1000).show()
                            loadingLastUpdatedData()
                        }
                    })

                    weatherViewModel.fetchWeatherData(locations)
                } else {
                    Log.i("Location", "NULL")
                }
            }
        }
    }

    private fun sunTime(ms: Long): String {
        val sunriseMilliseconds = ms.times(1000) //convert to milliseconds
        val sunriseTime = Date(sunriseMilliseconds)
        val dateFormat1 = SimpleDateFormat("h:mm a", Locale.getDefault())
        return dateFormat1.format(sunriseTime)
    }

    private fun setGif(gifId: Int, dayNight: GifImageView) {
        dayNight.setImageResource(gifId)
    }

    // new permission handling:
    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            locationPermissionStatus.postValue(true)
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app.",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        locationPermissionStatus.postValue(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}