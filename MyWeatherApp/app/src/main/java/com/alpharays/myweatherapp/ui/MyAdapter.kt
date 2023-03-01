package com.alpharays.myweatherapp.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alpharays.myweatherapp.R
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class MyAdapter(
    private val context: Context,
    private val weatherList: ArrayList<ArrayList<String>>
) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.placeName)
        val weatherMain: TextView = itemView.findViewById(R.id.weatherMain)
        val weatherDescription: TextView = itemView.findViewById(R.id.weatherDescription)
        val currentWeatherImage: pl.droidsonroids.gif.GifImageView =
            itemView.findViewById(R.id.currentWeatherImage)
        val temperature: TextView = itemView.findViewById(R.id.temperature)
        val feels_like: TextView = itemView.findViewById(R.id.feels_like)
        val temp_max: TextView = itemView.findViewById(R.id.temp_max)
        val temp_min: TextView = itemView.findViewById(R.id.temp_min)
        val humidity: TextView = itemView.findViewById(R.id.humidity)
        val windSpeed: TextView = itemView.findViewById(R.id.windSpeed)
        val sunrise: TextView = itemView.findViewById(R.id.sunrise)
        val sunset: TextView = itemView.findViewById(R.id.sunset)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.weathercountryitem, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentData = weatherList[position]
        holder.placeName.text = currentData[0]
        holder.weatherMain.text = currentData[1]
        holder.weatherDescription.text = "About: " + currentData[2]
        val imageUrl = "https://openweathermap.org/img/wn/${currentData[3]}@2x.png"
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.sun)
            .error(R.drawable.night)
            .into(holder.currentWeatherImage)

        holder.temperature.text = "Temperature\n${currentData[4]}°C"
        holder.feels_like.text = "Feels like\n${currentData[5]}°C"
        holder.temp_max.text = "Max Temp\n${currentData[6]}°C"
        holder.temp_min.text = "Min Temp\n${currentData[7]}°C"
        holder.humidity.text = "Humidity\n${currentData[8]}"
        holder.windSpeed.text = "Wind Speed\n${currentData[9]}"
        holder.sunrise.text = "Sunrise\n${currentData[10]}"
        holder.sunset.text = "Sunset\n${currentData[11]}"

    }

    override fun getItemCount(): Int {
        return weatherList.size
    }
}