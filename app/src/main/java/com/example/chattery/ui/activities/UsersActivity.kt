package com.example.chattery.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.commons.ChatteryActivity
import com.example.chattery.firebase.UsersColumns
import com.example.chattery.R
import com.example.chattery.model.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.lang.Exception

class UsersActivity : ChatteryActivity() {
    private lateinit var mQuery: Query
    private lateinit var mAdapter: FirebaseRecyclerAdapter<User, UserHolder>;
    private lateinit var mRecyclerView: RecyclerView

    private val TAG = "AllUserActivity"
    private val TITLE = "All Users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        supportActionBar?.title = TITLE

        mQuery = FirebaseDatabase.getInstance()
            .reference
            .child(UsersColumns.Users)
        Log.i(TAG, "Query: $mQuery")

        mRecyclerView = findViewById(R.id.users_recyclerview)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        mAdapter = object : FirebaseRecyclerAdapter<User, UserHolder>(
            // Firebase options
            FirebaseRecyclerOptions.Builder<User>()
                .setQuery(mQuery, User::class.java)
                .setLifecycleOwner(this)
                .build()
        ) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
                Log.i(TAG,"Creating viewholder")
                val view = layoutInflater.inflate(R.layout.single_user, parent, false)
                return UserHolder(view)
            }

            override fun onBindViewHolder(holder: UserHolder, position: Int, model: User) {
                Log.i(TAG, "Binding user: $position")
                holder.bind((model))

                val UserId = getRef(position).key!!

                //Route to Profile activity when Clicked
                holder.itemView.setOnClickListener {
                    val ProfileIntent = ProfileActivity.newIntent(this@UsersActivity, UserId)
                    startActivity(ProfileIntent)
                }
            }
        }

        mRecyclerView.adapter = mAdapter
    }

    inner class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val Username = itemView.findViewById<TextView>(R.id.single_request_username)
        val Status = itemView.findViewById<TextView>(R.id.single_user_status)
        val Picture = itemView.findViewById<CircularImageView>(R.id.single_request_user_pic)

        fun bind(user: User){
            //Update data
            Username.text = user.username
            Status.text = user.userstatus
            if (user.userimagethumbnail != "default"){
                Picasso.get().load(user.userimagethumbnail).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.avatar_empty).into(Picture, object : Callback {
                        override fun onSuccess() {
                            //Cool! nothing to do
                        }

                        override fun onError(e: Exception?) {
                            Picasso.get().load(user.userimagethumbnail).placeholder(R.drawable.avatar_empty).into(Picture)
                        }
                    })
            }
        }
    }
}


