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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.chattery.UsersColumns

class SignUpActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth;                //Firebase Authentication
    lateinit var mDataBaseRef: DatabaseReference      //Firebase Database Reference
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
            signUpUser(mUserName.text.toString(), mEmail.text.toString(), mPassword.text.toString())
        }

    }

    fun signUpUser(name: String, email: String, password: String) {

        //Validate entries text, if not valid abandon account creation
        if (!isValidUserNameEmailAndPassword(username = name, email = email, password = password)) {
            mProgress.dismiss()
            Toast.makeText(this, "Couldn't create account, check your data", Toast.LENGTH_LONG)
                .show()
            return
        }


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Log.d(TAG, "Created account succesfuly")
                AddUserToDataBase(name)
            } else {
                Log.d(TAG, "Couldn't create account")
                Toast.makeText(this, "Failed to create account, try again", Toast.LENGTH_LONG)
                    .show()
                mProgress.dismiss()
            }
        }
    }

    private fun AddUserToDataBase(name: String) {
        val mUserId = mAuth.currentUser?.uid    //Current created user id
        mDataBaseRef = FirebaseDatabase.getInstance().reference
        mDataBaseRef = mDataBaseRef.child(UsersColumns.Users).child(mUserId!!)    //  Database/Users/userid


        //Database entries for current user
        val data = HashMap<String, Any?>()
        data[UsersColumns.UserName] = name
        data[UsersColumns.Status] = "Hey there, I'm on Chattery!"    //inspired by a famous Software, Lolz :-)
        data[UsersColumns.Image] = "default"
        data[UsersColumns.ImageThumbnail] = "default"

        mDataBaseRef.setValue(data).addOnCompleteListener {
            mProgress.dismiss()    //Dismiss dialoge at any situation Failure/Success

            if (it.isSuccessful) {
                Log.d(TAG, "Added user to database successfully")
                //When added to database, route user to Main Activity
                sendToMainPage()
            } else {
                Log.d(TAG, "Failed to add user to database")
                Toast.makeText(this, "Failed to create account, try again", Toast.LENGTH_LONG).show()
            }

        }
    }

    // Send to Main Activity
    private fun sendToMainPage() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    //Validate non null email and password
    private fun isValidUserNameEmailAndPassword(username: String, email: String, password: String) =
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) false else true
}
