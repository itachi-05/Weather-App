package com.alpharays.myweatherapp.data.dataclasses

data class Coord(val lon: Double, val lat: Double)

data class WeatherClimate(val id: Int, val main: String, val description: String, val icon: String)

data class WeatherMain(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int,
    val sea_level: Int,
    val grnd_level: Int
)

data class Wind(val speed: Double, val deg: Int, val gust: Double)

data class Clouds(val all: Int)

data class Sys(val sunrise: Int, val sunset: Int)

data class WeatherData(
    val coord: Coord,
    val weather: List<WeatherClimate>,
    val base: String,
    val main: WeatherMain,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Int,
    val sys: Sys,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)
