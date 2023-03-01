package com.alpharays.myweatherapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alpharays.myweatherapp.data.dataclasses.Location
import com.alpharays.myweatherapp.data.dataclasses.WeatherData
import com.alpharays.myweatherapp.data.interfaces.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WeatherDataViewModel(application: Application) : AndroidViewModel(application) {

    private val _response = MutableLiveData<List<WeatherData>>()
    val response: LiveData<List<WeatherData>> = _response

    private val _response2 = MutableLiveData<Boolean>()
    val response2: LiveData<Boolean> = _response2

    fun fetchWeatherData(locations: ArrayList<Location>) {
        viewModelScope.launch {
            val responses = mutableListOf<WeatherData>()
            withContext(Dispatchers.IO) {
                try {
                    _response2.postValue(true)
                    for (location in locations) {
                        val response =
                            WeatherService.weatherInstance.getWeather(location.lat, location.lon)
                                .execute()
                        if (response.isSuccessful) {
                            response.body()?.let { weatherData ->
                                responses.add(weatherData)
                            }
                        }
                    }
                } catch (e: Exception) {
                    _response2.postValue(false)
                    Log.i("NETWORK","ERROR")
                }
            }
            _response.value = responses
        }
    }

}