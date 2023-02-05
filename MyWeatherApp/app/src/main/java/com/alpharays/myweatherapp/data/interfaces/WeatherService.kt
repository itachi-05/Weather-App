package com.alpharays.myweatherapp.data.interfaces

import com.alpharays.myweatherapp.data.dataclasses.WeatherData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.openweathermap.org/"
const val API_KEY = "efa70ac20519053b1f357c9f1e9ebe5c"

interface WeatherInterface {
    @GET(value = "data/2.5/weather?appid=$API_KEY&units=metric")
    fun getWeather(@Query("lat") lat: String, @Query("lon") lon: String): Call<WeatherData>
}

object WeatherService{
    val weatherInstance: WeatherInterface
    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherInstance = retrofit.create(WeatherInterface::class.java)
    }
}