package com.example.chattery.ui.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.chattery.*
import com.example.chattery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.*

class ProfileActivity : AppCompatActivity() {
    lateinit var mUserPic:ImageView
    lateinit var mUserName:TextView
    lateinit var mUserStatus:TextView
    lateinit var mRequestButton:Button
    lateinit var mProgress:ProgressDialog;

    // Databases
    lateinit var mRequestsDatabaseRef:DatabaseReference    //Friend Requests database
    lateinit var mUsersDatabaseRef: DatabaseReference      //Users Requests database
    lateinit var mFriendsDatabase: DatabaseReference       //Friends Requests database

    lateinit var mCurrentUserId:String

    var mRequestState = RequestState.NOT_FRIENDS  // Default value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        mUserPic = findViewById(R.id.profile_user_image)
        mUserName = findViewById(R.id.profile_user_name)
        mUserStatus = findViewById(R.id.profile_user_status)
        initiateDialog()

        // Profile User Id & Current User Id
        val UserID = intent.getStringExtra(UsersActivity.EXTRA_USER_ID)
        mCurrentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        mRequestsDatabaseRef = FirebaseDatabase.getInstance().reference.child(RequestColumns.Requests)
        mFriendsDatabase = FirebaseDatabase.getInstance().reference.child(FriendsColumns.Friends)

        mUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users).child(UserID)
        showDialog()
        mUsersDatabaseRef.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(error: DatabaseError) {
                //TODO: Handle error
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userimage = snapshot.child(UsersColumns.Image).value.toString()
                val username = snapshot.child(UsersColumns.UserName).value.toString()
                val userstatus = snapshot.child(UsersColumns.Status).value.toString()

                mUserName.text = username
                mUserStatus.text = userstatus
                Picasso.get().load(userimage).placeholder(R.drawable.avatar_empty).into(mUserPic)


                mRequestsDatabaseRef.child(mCurrentUserId).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        dissmisDialog()
                        // Is the profile user in request database?
                        if (snapshot.hasChild(UserID)){
                            val request_state = enumValueOf<RequestState>(snapshot.child(UserID).child(RequestColumns.Request_state).getValue().toString())

                            if (request_state.equals(RequestState.RECIEVED)){
                                mRequestState = RequestState.RECIEVED
                                mRequestButton.text = RequestLabel.ACCEPT_REQUEST

                            }else if(request_state.equals(RequestState.SENT)){
                                mRequestState = RequestState.SENT
                                mRequestButton.text = RequestLabel.CANCEL_REQUEST
                            }
                        }
                    }

                })
            }

        })

        mRequestButton = findViewById(R.id.profile_send_request)
        mRequestButton.setOnClickListener {
            mRequestButton.isEnabled = false
            when {
                mRequestState.equals(RequestState.NOT_FRIENDS) -> {
                    AddToRequestsDatabase(mCurrentUserId,UserID)
                }

                mRequestState.equals(RequestState.SENT) ->{
                    removeFromRequestDatabase(mCurrentUserId,UserID)
                }

                mRequestState.equals(RequestState.RECIEVED) ->{
                    removeFromRequestDatabase(mCurrentUserId,UserID)
                    addToFriendsDatabase(mCurrentUserId,UserID)
                }
            }
        }

    }

    private fun addToFriendsDatabase(senderID: String, recieverID: String) {
        val currentDate = Date().toString()

        mFriendsDatabase.child(senderID).child(recieverID).child(FriendsColumns.FriendsSince).setValue(currentDate).addOnSuccessListener {
            mFriendsDatabase.child(recieverID).child(senderID).child(FriendsColumns.FriendsSince).setValue(currentDate).addOnSuccessListener {
                mRequestButton.isEnabled = true
                mRequestButton.text = RequestLabel.UNFRIEND
                mRequestState = RequestState.FRIENDS
            }
        }
    }

    private fun AddToRequestsDatabase(senderID: String, recieverID: String) {
        mRequestsDatabaseRef.child(senderID)
            .child(recieverID).child(RequestColumns.Request_state).setValue(RequestState.SENT).addOnCompleteListener {
                if (it.isSuccessful){

                    // Add request {received} to profile user id
                    mRequestsDatabaseRef.child(recieverID).child(senderID)
                        .child(RequestColumns.Request_state).setValue(RequestState.RECIEVED).addOnSuccessListener {
                            mRequestButton.isEnabled = true
                            mRequestButton.text = RequestLabel.CANCEL_REQUEST
                            mRequestState = RequestState.SENT
                        }

                }else{
                    //TODO: Handle error
                }
            }

    }

    private fun removeFromRequestDatabase(senderID: String, recieverID: String) {
        val sender = senderID
        val reciever = recieverID

        mRequestsDatabaseRef.child(sender).child(reciever).removeValue().addOnSuccessListener {

            //remove current user id from request
            mRequestsDatabaseRef.child(reciever).child(sender).removeValue().addOnSuccessListener {

                mRequestButton.isEnabled = true
                mRequestButton.text = RequestLabel.SEND_REQUEST
                mRequestState = RequestState.NOT_FRIENDS
            }
        }
    }

    private fun initiateDialog() {
        mProgress = ProgressDialog(this)
        mProgress.setTitle("Loading")
        mProgress.setMessage("Please wait while we load the user profile")
    }

    private fun showDialog(){
        mProgress.show()
    }

    private fun dissmisDialog(){
        mProgress.dismiss()
    }
}
