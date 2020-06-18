package com.example.chattery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_account)

        val mEmail = findViewById<EditText>(R.id.signup_email)
        val mUsername = findViewById<EditText>(R.id.signup_username)
        val mPassword = findViewById<EditText>(R.id.signup_password)
    }
}
