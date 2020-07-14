package com.example.chattery.ui.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import com.example.chattery.commons.ChatteryActivity
import com.example.chattery.R
import com.example.chattery.adapters.TabsAdapter
import com.example.chattery.firebase.UsersColumns
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ChatteryActivity() {
    lateinit var mAuth: FirebaseAuth     //Firebase Authentication
    lateinit var mViewPager:ViewPager
    lateinit var mTabs:TabLayout
    lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    private lateinit var mUsersDatabaseOnlineLabel:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        mToolbar = findViewById(R.id.main_page_toolbar)
        mToolbar.setTitleTextColor(resources.getColor(R.color.design_default_color_on_primary))
        setSupportActionBar(mToolbar)

        mViewPager = findViewById(R.id.view_pager)
        mViewPager.adapter =
            TabsAdapter(supportFragmentManager)
        mViewPager.currentItem = 1   //Chats page

        mTabs = findViewById(R.id.main_tab_layout)
        mTabs.setupWithViewPager(mViewPager)

        if(!isNetworkAvailableAndConnected()) {
            Toast.makeText(this,"No Network Connection", Toast.LENGTH_LONG).show()
        }

    }

    private fun isNetworkAvailableAndConnected():Boolean {
        val connectivitymanager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return !(connectivitymanager.activeNetworkInfo == null || !connectivitymanager.activeNetworkInfo.isConnected)
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        if (user == null && isNetworkAvailableAndConnected()) {
            sendToWelcomeScreen()
            finish()
        }else{
            mUsersDatabaseOnlineLabel = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users).child(FirebaseAuth.getInstance().currentUser!!.uid).child(UsersColumns.Online)
            setUserOnline()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (userExists()){
            setUserOffline()
        }
    }

    private fun sendToWelcomeScreen() {
        val mIntent = Intent(this, WelcomeActivity::class.java)
        startActivity(mIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId){

            R.id.menu_allusers -> {
                val intent = Intent(this,UsersActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menu_settings -> {
                val settingsIntent = Intent(this,
                    SettingsActivity::class.java)
                startActivity(settingsIntent)
                 true
            }

            R.id.menu_sign_out ->{
                mAuth.signOut()
                sendToWelcomeScreen()
                true
            }

            else ->  super.onOptionsItemSelected(item)

        }


}
