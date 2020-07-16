package com.example.chattery.commons

import androidx.appcompat.app.AppCompatActivity
import com.example.chattery.firebase.OnlineStatus.Companion.Online
import com.example.chattery.firebase.UsersColumns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.util.*

open class ChatteryActivity : AppCompatActivity(){
    private var mUsersDatabaseOnlineLabel = if(FirebaseAuth.getInstance().currentUser!= null){
        FirebaseDatabase.getInstance().reference.child(UsersColumns.Users).child(FirebaseAuth.getInstance().currentUser?.uid!!).child(UsersColumns.Online)
    }else{
        FirebaseDatabase.getInstance().reference
    }


    override fun onResume() {
        super.onResume()
        if(userExists()){
            setUserOnline()
        }
    }

    override fun onUserLeaveHint() {
        if (userExists()){
            setUserOffline()
        }
        super.onUserLeaveHint()
    }

    protected fun setUserOnline() {
        if (userExists()){
            mUsersDatabaseOnlineLabel.setValue(Online)
        }

    }

    protected fun setUserOffline() {
        if (userExists()){
            mUsersDatabaseOnlineLabel.setValue(ServerValue.TIMESTAMP)
        }
    }


    fun userExists() = FirebaseAuth.getInstance().currentUser != null

}