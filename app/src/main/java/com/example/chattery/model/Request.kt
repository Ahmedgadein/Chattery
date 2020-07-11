package com.example.chattery.model

import com.example.chattery.firebase.RequestState

data class Request(
    val state:RequestState = RequestState.NOT_FRIENDS
)