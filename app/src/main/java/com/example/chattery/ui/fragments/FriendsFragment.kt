package com.example.chattery.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.ui.activities.ChatActivity
import com.example.chattery.R
import com.example.chattery.firebase.FriendsColumns
import com.example.chattery.firebase.OnlineStatus
import com.example.chattery.firebase.UsersColumns
import com.example.chattery.model.Friend
import com.example.chattery.ui.activities.MainActivity
import com.example.chattery.ui.activities.ProfileActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.lang.Exception

class FriendsFragment : Fragment() {
    //Firebase
    private lateinit var mFriendsRecyclerView: RecyclerView
    private lateinit var mFriendsAdapter:FirebaseRecyclerAdapter<Friend,FriendHolder>
    private lateinit var mUsersDatabase: DatabaseReference

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mQuery: Query

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends,container,false)

        mAuth = FirebaseAuth.getInstance()

        mQuery = FirebaseDatabase.getInstance().reference.child(FriendsColumns.Friends).child(mAuth.currentUser?.uid!!)
        mUsersDatabase = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users)

        mFriendsRecyclerView = view.findViewById(R.id.friends_recycerview)
        mFriendsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        mFriendsAdapter = object: FirebaseRecyclerAdapter<Friend,FriendHolder>(
            //Firebase options
            FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(mQuery,Friend::class.java)
                .setLifecycleOwner(this)
                .build()
        ){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
                val view = layoutInflater.inflate(R.layout.single_user,parent,false)
                return FriendHolder(view)
            }

            override fun onBindViewHolder(holder: FriendHolder, position: Int, model: Friend) {
                //Friend User ID
                val userID = getRef(position).key!!

                mUsersDatabase.child(userID).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        //TODO: Handle error
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child(UsersColumns.UserName).value.toString()
                        val thumbnail = snapshot.child(UsersColumns.ImageThumbnail).value.toString()
                        val online = snapshot.child(UsersColumns.Online).value.toString()
                        holder.bindDate(model, username,thumbnail, online)
                    }

                })

                holder.itemView.setOnClickListener {
                    val options = arrayOf("Open Profile", "Send Message")
                    val builder = AlertDialog.Builder(context!!)
                        .setTitle("Select Option")
                        .setItems(options,DialogInterface.OnClickListener { dialogInterface, position ->
                            val intent = when(position){
                                0 -> ProfileActivity.newIntent(context!!, userID)
                                1 -> ChatActivity.newIntent(context!!,userID)

                                // Place holder for "else", will not actually happen
                                else -> Intent(context!!, MainActivity::class.java)
                            }
                            startActivity(intent)
                        })
                    builder.create().show()
                }
            }

        }
        mFriendsRecyclerView.adapter = mFriendsAdapter
    }

    class FriendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mDateText = itemView.findViewById<TextView>(R.id.single_user_status)
        private val mUserNameText = itemView.findViewById<TextView>(R.id.single_request_username)
        private val mImage = itemView.findViewById<CircularImageView>(R.id.single_request_user_pic)
        private val mOnlineLabel = itemView.findViewById<ImageView>(R.id.single_user_online)

        fun bindDate(model: Friend, userName:String, thumbnailURL: String, online: String){
            mDateText.text = model.friends_since
            mUserNameText.text = userName
            mOnlineLabel.visibility = if(online.equals(OnlineStatus.Online)) View.VISIBLE else View.INVISIBLE
            Picasso.get().load(thumbnailURL).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.avatar_empty).into(mImage, object : Callback {
                    override fun onSuccess() {
                        //Cool! nothing to do
                    }

                    override fun onError(e: Exception?) {
                        Picasso.get().load(thumbnailURL).placeholder(R.drawable.avatar_empty).into(mImage)
                    }
                })
        }
    }
}
