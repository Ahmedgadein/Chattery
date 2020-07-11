package com.example.chattery.model

import java.sql.Timestamp

data class Chat(
    val seen:Boolean = false,
    val timestamp: Long = 1L
)