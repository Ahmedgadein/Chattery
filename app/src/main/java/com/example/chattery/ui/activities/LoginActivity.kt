package com.example.chattery.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.chattery.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var mAuth:FirebaseAuth  //FireBase Authentication instance

    lateinit var mEmail:EditText;
    lateinit var mPassword:EditText;
    lateinit var mLoginButton: Button;
    lateinit var mProgressDialog:ProgressDialog

    private val TAG = "LoginActivity"
    private val TITLE = "Log in"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        supportActionBar?.setTitle(TITLE)

        mEmail = findViewById(R.id.login_email)
        mPassword = findViewById(R.id.login_password)

        //Logging in dialoge
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Logging in")
        mProgressDialog.setMessage("Please wait while we log in to your account")
        mProgressDialog.setCanceledOnTouchOutside(false)

        mLoginButton = findViewById(R.id.login_button)
        mLoginButton.setOnClickListener {
            mProgressDialog.show()
            logInUser(mEmail.text.toString(), mPassword.text.toString())
        }
    }

    fun logInUser(email:String,password:String){
        //Validate email and password as non null
        if (!isValidEmailAndPassword(email = email,password = password)){
            mProgressDialog.dismiss()
            Log.d(TAG, "email or password empty")
            Toast.makeText(this,"Couldn't log to your account, check data",Toast.LENGTH_LONG).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
            //Dismiss the dialoge at any state: Failure/Success
            mProgressDialog.dismiss()
            if (it.isSuccessful){
                Log.d(TAG,"Logged in succesfully")

                //When logged send user to main Activity
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }else{
                Log.d(TAG, "Failed to log in")
                Toast.makeText(this, "Couldn't log in, try again",Toast.LENGTH_LONG).show()
            }
        }
    }

    // Check non null email and password
    fun isValidEmailAndPassword(email: String,password: String) =  if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) false else true
}
