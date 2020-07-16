package com.example.chattery.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.R
import com.example.chattery.firebase.FriendsColumns
import com.example.chattery.firebase.RequestColumns
import com.example.chattery.firebase.RequestState
import com.example.chattery.firebase.UsersColumns
import com.example.chattery.model.Request
import com.example.chattery.ui.activities.ProfileActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_requests.view.*
import kotlinx.android.synthetic.main.single_friend_request.view.*
import kotlinx.android.synthetic.main.single_user.view.*
import java.util.*


class RequestsFragment : Fragment() {
    private lateinit var mUsersDatabase:DatabaseReference
    private lateinit var mRootReference:DatabaseReference
    private lateinit var mAuth:FirebaseAuth
    private lateinit var RequestsQuery: Query
    private lateinit var mCurrentUserID:String


    lateinit var mRequestsRecyclerView: RecyclerView
    lateinit var mRequestsAdapter:FirebaseRecyclerAdapter<Request,RequestHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_requests, container, false)

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserID = mAuth.currentUser?.uid!!

        mRootReference = FirebaseDatabase.getInstance().reference
        mUsersDatabase = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users)
        RequestsQuery = FirebaseDatabase.getInstance().reference.child(RequestColumns.Requests).child(mCurrentUserID)

        mRequestsRecyclerView = view.requests_recyclerview
        mRequestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        mRequestsAdapter = object : FirebaseRecyclerAdapter<Request,RequestHolder>(
            FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(RequestsQuery,Request::class.java)
                .setLifecycleOwner(this)
                .build()
        ){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestHolder {
                val view = layoutInflater.inflate(R.layout.single_friend_request,parent,false)
                return RequestHolder(view)
            }

            override fun onBindViewHolder(holder: RequestHolder, position: Int, model: Request) {
                val userID = getRef(position).key!!
                var UserName = "placeHolder"

                mUsersDatabase.child(userID).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userName = snapshot.child(UsersColumns.UserName).value.toString()
                        val userThumb = snapshot.child(UsersColumns.ImageThumbnail).value.toString()
                        UserName = userName

                        holder.bind(userName, userThumb)
                    }

                })

                holder.itemView.setOnClickListener {
                    val profileIntent = ProfileActivity.newIntent(activity!!,userID)
                    startActivity(profileIntent)
                }

                holder.acceptButton.setOnClickListener {
                    addFriendAndRemoveRequest(userID, mCurrentUserID)
                    mRequestsAdapter.notifyDataSetChanged()

                    Toast.makeText(activity!!, "$UserName is added to Friends!", Toast.LENGTH_SHORT).show()
                }

                holder.declineButton.setOnClickListener {
                    removeFromRequest(userID, mCurrentUserID)
                    mRequestsAdapter.notifyDataSetChanged()
                }
            }
        }

        mRequestsRecyclerView.adapter = mRequestsAdapter
    }

    class RequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mUserName = itemView.findViewById<TextView>(R.id.single_requests_username)
        val mUserPic = itemView.findViewById<CircularImageView>(R.id.single_requests_user_pic)

        val acceptButton = itemView.single_requests_accept_button
        val declineButton = itemView.single_requests_decline_button

        fun bind(username:String, userThumb:String){
            mUserName.text = username
            Picasso.get().load(userThumb).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.avatar_empty).into(mUserPic, object : Callback {
                    override fun onSuccess() {
                        //Cool! nothing to do
                    }

                    override fun onError(e: Exception?) {
                        Picasso.get().load(userThumb).placeholder(R.drawable.avatar_empty).into(mUserPic)
                    }

                })
        }
    }

    private fun removeFromRequest(senderID: String, recieverID: String) {
        val data = HashMap<String, Any?>()
        data.put(RequestColumns.Requests + "/" + senderID + "/" + recieverID , null)
        data.put(RequestColumns.Requests + "/" + recieverID + "/" + senderID , null)

        mRootReference.updateChildren(data, object :DatabaseReference.CompletionListener{
            override fun onComplete(error: DatabaseError?, reference: DatabaseReference) {
                // Update request table when no error occurs
                if(error != null){
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
                if (error != null){
                    //TODO: Handle error
                }
            }
        })
    }
}
