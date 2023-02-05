package com.alpharays.myweatherapp.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpharays.myweatherapp.data.dataclasses.WeatherData
import com.alpharays.myweatherapp.data.interfaces.WeatherService
import com.alpharays.myweatherapp.data.dataclasses.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class WeatherDataViewModel : ViewModel() {
    private val _response = MutableLiveData<WeatherData>()

    val response: LiveData<WeatherData> get() = _response

    fun getWeather(context: Context, lat: String, lon: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
//            WeatherService.weatherInstance.getWeather(lat,lon).execute() : execute blocks the current thread until it gets the response from the server
            WeatherService.weatherInstance.getWeather(lat, lon).enqueue(object :
                Callback<WeatherData> {
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if (response.isSuccessful) {
                        _response.postValue(response.body())
                        Log.i("Response", "Found")
                    } else {
                        // handle error
                        Log.i("Response", "Not Found")
                    }
                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong!!", Toast.LENGTH_SHORT).show()
                    when (t) {
                        is IOException -> {
                            Log.i("ErrorRetrofit", "Network Failure")
                            // handle network failure
                        }
                        is HttpException -> {
                            Log.i("ErrorRetrofit", "HTTP status code Failure")
                            // handle HTTP status code failure
                        }
                        else -> {
                            Log.i("ErrorRetrofit", t.toString())
                            // handle other failures
                        }
                    }
                }
            })
        }
    }

    fun getWeatherMain(): WeatherMain? = _response.value?.main

    fun getWeatherSys(): Sys? = _response.value?.sys

    fun getWeatherWind(): Wind? = _response.value?.wind

    fun getWeatherClimate(): List<WeatherClimate>? = _response.value?.weather

}