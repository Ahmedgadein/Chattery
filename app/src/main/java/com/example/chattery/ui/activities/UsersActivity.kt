package com.example.chattery.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.Columns
import com.example.chattery.R
import com.example.chattery.model.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso

class UsersActivity : AppCompatActivity() {
    // Firebase database
//    lateinit var mDatabase: FirebaseDatabase

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
            .child(Columns.Users)
        Log.i(TAG, "Query: $mQuery")

        mRecyclerView = findViewById(R.id.users_recyclerview)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        mAdapter = object : FirebaseRecyclerAdapter<User, UserHolder>(
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
            }

        }

        mRecyclerView.adapter = mAdapter
    }
}

open class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val Username = itemView.findViewById<TextView>(R.id.single_user_username)
    val Status = itemView.findViewById<TextView>(R.id.single_user_status)
    val Picture = itemView.findViewById<CircularImageView>(R.id.single_user_pic)

    fun bind(user: User){
        with(user){
        Username.text = user.username
        Status.text = user.userstatus
        Picasso.get().load(user.userimagethumbnail).placeholder(R.drawable.avatar_empty).into(Picture)}
    }

}
