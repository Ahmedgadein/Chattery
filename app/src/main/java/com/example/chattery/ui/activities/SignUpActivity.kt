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
import com.example.chattery.R
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth;
    lateinit var mEmail: EditText
    lateinit var mUserName: EditText
    lateinit var mPassword: EditText
    lateinit var mProgress: ProgressDialog

    private val TAG: String = "SignUpActivity"
    private val TITLE = "Sign up"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_account)
        //initiate authenticate object
        mAuth = FirebaseAuth.getInstance()

        supportActionBar?.setTitle(TITLE)

        mEmail = findViewById(R.id.signup_email)
        mUserName = findViewById(R.id.signup_username)
        mPassword = findViewById(R.id.signup_password)

        //Create a loading dialoge
        mProgress = ProgressDialog(this)
        mProgress.setTitle("Creating account")
        mProgress.setMessage("Please wait while we create your account")
        mProgress.setCanceledOnTouchOutside(false)
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        val mSignupButton = findViewById<Button>(R.id.signup_button)
        mSignupButton.setOnClickListener {
            mProgress.show()
            signUpUser(mUserName.text.toString(),mEmail.text.toString(),mPassword.text.toString())
        }

    }
    fun signUpUser(name:String, email:String, password:String ){

        //Validate entries text, if not valid abandon account creation
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            mProgress.dismiss()
            Toast.makeText(this,"Couldn't create account, check your data",Toast.LENGTH_LONG).show()
            return
        }


        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this){
            mProgress.dismiss()    //Dismiss at any situation Failure/Success

            if(it.isSuccessful){
                Log.d(TAG,"Created account succesfuly")
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }else{
                Log.d(TAG,"Couldn't create account")
                Toast.makeText(this,"Failed to create account, try again",Toast.LENGTH_LONG).show()
            }
        }
    }
}
