package com.example.chattery.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.ChatteryActivity
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
    lateinit var mQuery: Query
    lateinit var mAdapter: FirebaseRecyclerAdapter<User, UserHolder>;
    lateinit var mRecyclerView: RecyclerView

    val TAG = "AllUserActivity"
    val TITLE = "All Users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        supportActionBar?.setTitle(TITLE)

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
                Log.i(TAG, "Binding user: " + position )
                holder.bind((model))

                //Extract the user ID
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

        val Username = itemView.findViewById<TextView>(R.id.single_user_username)
        val Status = itemView.findViewById<TextView>(R.id.single_user_status)
        val Picture = itemView.findViewById<CircularImageView>(R.id.single_user_pic)

        fun bind(user: User){
            Username.text = user.username
            Status.text = user.userstatus
            if (user.userimagethumbnail != "default"){
                Picasso.get().load(user.userimagethumbnail.toString()).networkPolicy(NetworkPolicy.OFFLINE)
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


