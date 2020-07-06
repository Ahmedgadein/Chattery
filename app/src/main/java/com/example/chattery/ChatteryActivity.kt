package com.example.chattery

import androidx.appcompat.app.AppCompatActivity
import com.example.chattery.firebase.UsersColumns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
            mUsersDatabaseOnlineLabel.setValue(true)
        }

    }

    protected fun setUserOffline() {
        if (userExists()){
            mUsersDatabaseOnlineLabel.setValue(false)
        }
    }


    fun userExists() = if (FirebaseAuth.getInstance().currentUser == null) false else true

}