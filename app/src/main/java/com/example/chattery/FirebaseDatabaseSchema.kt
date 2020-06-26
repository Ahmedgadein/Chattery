package com.example.chattery

//This Singleton provides a schema for the Firebase realtime database

open class Columns{
    companion object{
        val Users = "Users"
        val Uid = "uid"
        val UserName = "username"
        val Status = "userstatus"
        val Image = "userimage"
        val ImageThumbnail = "userimagethumbnail"

        //Firebase storage columns
        val ProfileImagesDirectory = "profile_images"
        val ProfileThumbnailDirectory = "thumbnails"
    }
}