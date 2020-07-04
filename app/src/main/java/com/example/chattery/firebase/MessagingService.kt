package com.example.chattery.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.chattery.R
import com.example.chattery.ui.activities.ProfileActivity
import com.example.chattery.ui.activities.UsersActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService: FirebaseMessagingService(){
    lateinit var builder: NotificationCompat.Builder
    val TAG = "MessagingService"

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "Creating channel")
            val name = "my_channel"
            val descriptionText = "my own channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("mChannel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "Message recieved")

        val notification_title = remoteMessage?.notification?.title
        val notification_body = remoteMessage?.notification?.body
        val user_id = remoteMessage?.data?.get("user_from_id")
        val activity_action = remoteMessage?.notification?.clickAction

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel()

            builder = NotificationCompat.Builder(this,"mChannel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
            Log.i(TAG, "Notification created")

        }else{
            builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
        }


        val intent = ProfileActivity.newIntent(this,user_id!!)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationID = System.currentTimeMillis().toInt()

        Log.i(TAG, "Notification manager invoked")
        builder.setContentIntent(pendingIntent)
        notificationManager.notify(notificationID, builder.build())
    }
}