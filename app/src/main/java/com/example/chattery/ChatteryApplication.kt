package com.example.chattery

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.chattery.firebase.UsersColumns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso


class ChatteryApplication: Application() {

override fun onCreate() {
        super.onCreate()
        //Firebase Offline features
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        //Picasso Offline
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
        val built = builder.build()
        built.setIndicatorsEnabled(true)
        built.isLoggingEnabled = true
        Picasso.setSingletonInstance(built)

        //Firebase Offline/Online Feature
        if(FirebaseAuth.getInstance().currentUser != null){
            val mCurrentUser = FirebaseAuth.getInstance().currentUser

            val mUsersDatabaseOnlineLabel = FirebaseDatabase.getInstance()
                .reference.child(UsersColumns.Users).child(mCurrentUser!!.uid)

            mUsersDatabaseOnlineLabel.child(UsersColumns.Online).onDisconnect().setValue(false)
        }

    }

}