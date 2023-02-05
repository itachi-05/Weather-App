package com.alpharays.myweatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alpharays.myweatherapp.R
import com.alpharays.myweatherapp.databinding.ActivityHomeBinding
import com.alpharays.myweatherapp.viewmodels.WeatherDataViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import pl.droidsonroids.gif.GifImageView
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import kotlin.system.exitProcess

@SuppressLint("SetTextI18n")
class ActivityHome : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var weatherViewModel: WeatherDataViewModel
    private lateinit var dayNight: GifImageView
    private lateinit var weatherImage: GifImageView
    private var permissionsRequested = false

    private lateinit var sharedPref: SharedPreferences

    // permissions
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isFineLocationPermissionGranted = false
    private var isCoarseLocationPermissionGranted = false

    private val permissionList: MutableList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.i("A", "0")

        sharedPref = getSharedPreferences("sharingWeatherData", MODE_PRIVATE)
        binding.placeName.text = sharedPref.getString("place", "Location").toString()
        binding.weatherMain.text = sharedPref.getString("weatherMain", "Weather Status").toString()
        binding.weatherDescription.text =
            sharedPref.getString("weatherDescription", "Weather Status Detailed").toString()
        binding.temperature.text = sharedPref.getString("temperature", "Temperature").toString()
        binding.feelsLike.text = sharedPref.getString("feelsLike", "Feels Like").toString()
        binding.tempMax.text = sharedPref.getString("maxTemp", "Max Temp").toString()
        binding.tempMin.text = sharedPref.getString("minTemp", "Min Temp").toString()
        binding.sunrise.text = sharedPref.getString("sunrise", "Sunrise").toString()
        binding.sunset.text = sharedPref.getString("sunset", "Sunset").toString()
        binding.humidity.text = sharedPref.getString("humidity", "Humidity").toString()
        binding.windSpeed.text = sharedPref.getString("windspeed", "Wind Speed").toString()
        val imageUrlCopy = sharedPref.getString("imageUrl", "11n").toString()

        dayNight = findViewById(R.id.appImageIcon)

        val currentTime = LocalTime.now().toString()
        val hours = currentTime.split(":")[0]
        if (7 > hours.toInt() || 19 <= hours.toInt()) {
            setGif(R.drawable.night, dayNight)
            Picasso.get()
                .load(imageUrlCopy)
                .placeholder(R.drawable.night)
                .error(R.drawable.night)
                .into(binding.myWeatherImage)
        } else {
            Picasso.get()
                .load(imageUrlCopy)
                .placeholder(R.drawable.sun)
                .error(R.drawable.day)
                .into(binding.myWeatherImage)
        }

        Log.i("A", "1")

        requestingPermissions()

        Log.i("A", "7")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("A", "8")
            requestingPermissions()
            Log.i("A", "9")
            return
        }

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.i("Location Not Null", location.toString())
                binding.weatherComing.visibility = View.VISIBLE
                val latitude = location.latitude.toString()
                val longitude = location.longitude.toString()
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses =
                    geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
                Log.i(latitude, longitude)
                binding.placeName.text =
                    addresses!![0].subLocality.toString() + ", " + addresses[0].locality.toString() + "\n" + addresses[0].countryName.toString()

                weatherViewModel = ViewModelProvider(this)[WeatherDataViewModel::class.java]
                weatherViewModel.response.observe(this, Observer {
                    if (it != null) {
                        val weatherMain = weatherViewModel.getWeatherMain()
                        val weatherSys = weatherViewModel.getWeatherSys()
                        val weatherWind = weatherViewModel.getWeatherWind()
                        val weatherClimate = weatherViewModel.getWeatherClimate()!![0]
                        binding.weatherMain.text = weatherClimate.main
                        if (weatherClimate.main.lowercase() != weatherClimate.description.lowercase()) {
                            binding.weatherDescription.text = "About: " + weatherClimate.description
                        } else {
                            binding.weatherDescription.text = ""
                        }
                        binding.temperature.text = "Temperature\n${weatherMain?.temp}°C"
                        binding.tempMax.text = "Max Temp\n${weatherMain?.temp_max}°C"
                        binding.tempMin.text = "Min Temp\n${weatherMain?.temp_min}°C"
                        binding.feelsLike.text = "Feels like\n${weatherMain?.feels_like}°C"
                        binding.humidity.text = "Humidity\n${weatherMain?.humidity}"
                        binding.windSpeed.text = "Wind Speed\n${weatherWind?.speed}"

                        val imageUrl =
                            "https://openweathermap.org/img/wn/${weatherClimate.icon}@2x.png"

                        Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.sun)
                            .error(R.drawable.night)
                            .into(binding.myWeatherImage)

                        // for sun rise
                        val sunriseMilliseconds =
                            weatherSys?.sunrise?.times(1000) //convert to milliseconds
                        val sunriseTime = Date(sunriseMilliseconds?.toLong()!!)
                        val dateFormatRise = SimpleDateFormat("HH:mm:ss a", Locale.getDefault())
                        val sunrise = dateFormatRise.format(sunriseTime)

                        // for sun set
                        val sunsetMilliseconds =
                            weatherSys.sunset.times(1000) //convert to milliseconds
                        val sunsetTime = Date(sunsetMilliseconds.toLong())
                        val dateFormatSet = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                        val sunset = dateFormatSet.format(sunsetTime)

                        binding.sunrise.text = "Sunrise:\n$sunrise"
                        binding.sunset.text = "Sunset:\n$sunset"

                        // sharing all the data in sharedPref
                        sharedPref = getSharedPreferences("sharingWeatherData", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("temperature", binding.temperature.text.toString())
                        editor.putString("place", binding.placeName.text.toString())
                        editor.putString("feelsLike", binding.feelsLike.text.toString())
                        editor.putString("weatherMain", binding.weatherMain.text.toString())
                        editor.putString(
                            "weatherDescription",
                            binding.weatherDescription.text.toString()
                        )
                        editor.putString("maxTemp", binding.tempMax.text.toString())
                        editor.putString("minTemp", binding.tempMin.text.toString())
                        editor.putString("humidity", binding.humidity.text.toString())
                        editor.putString("sunrise", binding.sunrise.text.toString())
                        editor.putString("sunset", binding.sunset.text.toString())
                        editor.putString("windspeed", binding.windSpeed.text.toString())
                        editor.putString("imageUrl", imageUrl)
                        editor.apply()
                    }
                })
                weatherViewModel.getWeather(this, latitude, longitude)
                binding.weatherComing.visibility = View.GONE
            } else {
                Toast.makeText(this,"Please enable Location and restart ",Toast.LENGTH_SHORT).show()
                Log.i("Location is Null", "-1")
            }
        }
    }

    private fun setGif(gifId: Int, dayNight: GifImageView) {
        dayNight.setImageResource(gifId)
    }

    // PERMISSIONS
    private fun requestingPermissions() {
        Log.i("A", "2")
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isFineLocationPermissionGranted =
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                        ?: isFineLocationPermissionGranted
                isCoarseLocationPermissionGranted =
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION]
                        ?: isCoarseLocationPermissionGranted
            }
        Log.i("A", "3")
        requestPermission()
    }


    private fun requestPermission() {
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionLauncher.launch(permissionList.toTypedArray())
        Log.i("A", "4")
    }

    override fun onStart() {
        super.onStart()
        Log.i("B", "1")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("B", "2")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("B", "3")
    }

    override fun onResume() {
        super.onResume()
        Log.i("B", "4")
    }

    override fun onStop() {
        super.onStop()
        Log.i("B", "5")
    }
}