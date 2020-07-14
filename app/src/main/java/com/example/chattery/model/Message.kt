package com.example.chattery.model

data class Message(
    var message:String = "",
    var seen:Boolean = false,
    var timestamp: Long = 1L,
    var type: String = "",
    var from:String = ""
)