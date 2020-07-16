package com.example.chattery.commons

import java.text.SimpleDateFormat
import java.util.*

object TimeFormatter{


    fun messageTimeFormat(timeMillis:Long):String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val formatter:SimpleDateFormat = SimpleDateFormat("hh:mm")

        return formatter.format(calendar.time)
    }

    fun friendsSinceFormat(timeMillis: Long):String{

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val formatter:SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm")

        return formatter.format(calendar.time)
    }
}