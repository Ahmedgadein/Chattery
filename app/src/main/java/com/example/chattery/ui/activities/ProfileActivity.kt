package com.example.chattery.ui.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.chattery.commons.ChatteryActivity
import com.example.chattery.R
import com.example.chattery.firebase.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.util.*

class ProfileActivity : ChatteryActivity() {
    lateinit var mUserPic:ImageView
    lateinit var mUserName:TextView
    lateinit var mUserStatus:TextView
    lateinit var mRequestText:TextView
    lateinit var mRequestButton:Button
    lateinit var mDeclineButton: Button
    lateinit var mProgress:ProgressDialog;

    // Databases
    lateinit var mRootReference: DatabaseReference
    lateinit var mRequestsDatabaseRef:DatabaseReference    //Friend Requests database
    lateinit var mUsersDatabaseRef: DatabaseReference      //Users database
    lateinit var mFriendsDatabase: DatabaseReference       //Friends database
    lateinit var mNotificationsDatabase:DatabaseReference  //Notifications database

    lateinit var mCurrentUserId:String

    lateinit var mRequestState:RequestState // Default value


    companion object{
        private val EXTRA_ID = "Extra_ID"

        fun newIntent(context: Context, userID: String): Intent{
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(EXTRA_ID,userID)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mRequestState = RequestState.NOT_FRIENDS

        mUserPic = findViewById(R.id.profile_user_image)
        mUserName = findViewById(R.id.profile_user_name)
        mUserStatus = findViewById(R.id.profile_user_status)
        mDeclineButton = findViewById(R.id.profile_decline_request)
        mRequestButton = findViewById(R.id.profile_send_request)
        mRequestText = findViewById(R.id.request_state)
        mRequestText.text = "not friends"

        mDeclineButton.visibility = View.INVISIBLE
        mDeclineButton.isEnabled = false

        initiateDialog()

        // Profile User Id & Current User Id
        val UserID = intent.getStringExtra(EXTRA_ID)
        mCurrentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        mRootReference = FirebaseDatabase.getInstance().reference
        mRequestsDatabaseRef = FirebaseDatabase.getInstance().reference.child(RequestColumns.Requests)
        mFriendsDatabase = FirebaseDatabase.getInstance().reference.child(FriendsColumns.Friends)
        mNotificationsDatabase = FirebaseDatabase.getInstance().reference.child(NotificationsColumns.Notification)

        mUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users).child(UserID)
        mUsersDatabaseRef.keepSynced(true)
        showDialog()
        mUsersDatabaseRef.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(error: DatabaseError) {
                //TODO: Handle error
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //Update data
                val userimage = snapshot.child(UsersColumns.Image).value.toString()
                val username = snapshot.child(UsersColumns.UserName).value.toString()
                val userstatus = snapshot.child(UsersColumns.Status).value.toString()

                mUserName.text = username
                mUserStatus.text = userstatus

                Picasso.get().load(userimage).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.avatar_empty).into(mUserPic, object :Callback{
                        override fun onSuccess() {
                            //Cool! nothing to do
                        }

                        override fun onError(e: Exception?) {
                            Picasso.get().load(userimage).placeholder(R.drawable.avatar_empty).into(mUserPic)
                        }

                    })

                mRequestsDatabaseRef.child(mCurrentUserId).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        dissmisDialog()
                        // Is the profile user in request database?
                        if (snapshot.hasChild(UserID)){
                            val request_state = enumValueOf<RequestState>(snapshot.child(UserID).child(
                                RequestColumns.Request_state).value.toString())

                            if (request_state.equals(RequestState.RECIEVED)){
                                mRequestState = RequestState.RECIEVED

                                updateUI()

                            }else if(request_state.equals(RequestState.SENT)){
                                mRequestState = RequestState.SENT

                                updateUI()
                            }
                        }
                    }

                })

                mFriendsDatabase.child(mCurrentUserId).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        dissmisDialog()
                        // Is the profile user in request database?
                        if (snapshot.hasChild(UserID)){
                            mRequestState = RequestState.FRIENDS

                            updateUI()
                        }
                    }

                })
            }

        })

        mRequestButton.setOnClickListener {
            mRequestButton.isEnabled = false
            when {
                mRequestState.equals(RequestState.NOT_FRIENDS) -> {
                    AddRequestAndNotification(mCurrentUserId,UserID)
                }

                mRequestState.equals(RequestState.SENT) ->{
                    removeFromRequest(mCurrentUserId,UserID)
                }

                mRequestState.equals(RequestState.RECIEVED) ->{
                    addFriendAndRemoveRequest(mCurrentUserId, UserID)
                }

                // If request state is FRIENDS
                else ->{
                    removeFriend(mCurrentUserId,UserID)
                }
            }
        }

        mDeclineButton.setOnClickListener {
            removeFromRequest(mCurrentUserId, UserID)
        }
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun updateUI() {

        when(mRequestState){

            RequestState.NOT_FRIENDS ->{
                mRequestButton.isEnabled = true
                mRequestButton.text = RequestLabel.SEND_REQUEST
                mRequestText.text = "not friends"

                mDeclineButton.visibility = View.INVISIBLE
                mDeclineButton.isEnabled = false
            }

            RequestState.SENT ->{
                mRequestButton.isEnabled = true
                mRequestButton.text = RequestLabel.CANCEL_REQUEST
                mRequestText.text = "sent"

                mDeclineButton.visibility = View.INVISIBLE
                mDeclineButton.isEnabled = false
            }

            RequestState.RECIEVED ->{
                mRequestButton.isEnabled = true
                mRequestButton.text = RequestLabel.ACCEPT_REQUEST
                mRequestText.text = "recieved"

                mDeclineButton.visibility = View.VISIBLE
                mDeclineButton.isEnabled = true
            }

            RequestState.FRIENDS ->{
                mRequestButton.isEnabled = true
                mRequestButton.text = RequestLabel.UNFRIEND
                mRequestText.text = "friends"

                mDeclineButton.visibility = View.INVISIBLE
                mDeclineButton.isEnabled = false
            }
        }
    }

    private fun AddRequestAndNotification(senderID: String, recieverID: String) {
        // get Key
        val notificationID = mNotificationsDatabase.child(recieverID).push().key

        val notificationData = HashMap<String, Any?>()
        notificationData.put(NotificationsColumns.From, senderID)
        notificationData.put(NotificationsColumns.type,"notification")

        // Update requests and notification tables
        val data = HashMap<String, Any?>()
        data.put(RequestColumns.Requests + "/" + senderID + "/" + recieverID + "/" + RequestColumns.Request_state, RequestState.SENT)
        data.put(RequestColumns.Requests + "/" + recieverID + "/" + senderID + "/" + RequestColumns.Request_state, RequestState.RECIEVED)
        data.put(NotificationsColumns.Notification + "/" + recieverID + "/" + notificationID!!, notificationData)

        mRootReference.updateChildren(data, object: DatabaseReference.CompletionListener{
            override fun onComplete(error: DatabaseError?, reference: DatabaseReference) {
                //Update button when no error occurs
                if(error == null){
                    updateUI()
                }else{
                    //TODO: Handle error
                }
            }
        })
    }

    private fun removeFromRequest(senderID: String, recieverID: String) {
        val data = HashMap<String, Any?>()
        data.put(RequestColumns.Requests + "/" + senderID + "/" + recieverID , null)
        data.put(RequestColumns.Requests + "/" + recieverID + "/" + senderID , null)

        mRootReference.updateChildren(data, object :DatabaseReference.CompletionListener{
            override fun onComplete(error: DatabaseError?, reference: DatabaseReference) {
                // Update request table when no error occurs
                if(error == null){
                    mRequestState = RequestState.NOT_FRIENDS

                    updateUI()
                }else{
                    //TODO: Handle error
                }
            }
        })
    }

    private fun addFriendAndRemoveRequest(senderID: String, recieverID: String) {
        val date = Date().toString()

        val data = HashMap<String, Any?>()
        data.put(FriendsColumns.Friends + "/" + senderID + "/" + recieverID + "/" + FriendsColumns.FriendsSince, date)
        data.put(FriendsColumns.Friends + "/" + recieverID + "/" + senderID + "/" + FriendsColumns.FriendsSince, date)
        data.put(RequestColumns.Requests + "/" + senderID + "/" + recieverID , null)
        data.put(RequestColumns.Requests + "/" + recieverID + "/" + senderID , null)

        mRootReference.updateChildren(data, object : DatabaseReference.CompletionListener{
            override fun onComplete(error: DatabaseError?, reference: DatabaseReference) {
                if (error == null){
                    mRequestState = RequestState.FRIENDS

                    updateUI()
                }else{
                    //TODO: Handle error
                }
            }
        })
    }

    private fun removeFriend(senderID: String, recieverID: String) {
        val data = HashMap<String, Any?>()
        data.put(FriendsColumns.Friends + "/" + senderID + "/" + recieverID, null)
        data.put(FriendsColumns.Friends + "/" + recieverID + "/" + senderID, null)

        mRootReference.updateChildren(data, object : DatabaseReference.CompletionListener{
            override fun onComplete(error: DatabaseError?, reference: DatabaseReference) {
                if (error == null){
                    mRequestState = RequestState.NOT_FRIENDS

                    updateUI()
                }else{
                    //TODO: Handle error
                }
            }
        })
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
