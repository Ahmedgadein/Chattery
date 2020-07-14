package com.example.chattery.ui.fragments

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.R
import com.example.chattery.firebase.ChatsColumns
import com.example.chattery.firebase.MessageColumns
import com.example.chattery.firebase.UsersColumns
import com.example.chattery.model.Chat
import com.example.chattery.model.Message
import com.example.chattery.ui.activities.ChatActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_chats.view.*
import kotlinx.android.synthetic.main.single_friend_request.view.*
import kotlinx.android.synthetic.main.single_user.view.*

class ChatsFragment : Fragment() {
    lateinit var mAuth: FirebaseAuth
    lateinit var ChatsQuery:Query
    lateinit var mRootRef:DatabaseReference
    lateinit var ChatsDatabase:DatabaseReference
    lateinit var mCurrentUserID:String

    lateinit var mChatsRecyclerView:RecyclerView
    lateinit var mChatsAdapter:FirebaseRecyclerAdapter<Chat,ChatHolder>

    val TAG = "ChatsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.fragment_chats, container,false)

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserID = mAuth.currentUser?.uid!!

        mRootRef = FirebaseDatabase.getInstance().reference
        ChatsDatabase = FirebaseDatabase.getInstance().reference.child(ChatsColumns.Chats).child(mCurrentUserID)
        ChatsQuery = ChatsDatabase.orderByChild(ChatsColumns.Timestamp)

        mChatsRecyclerView = view.chats_recyclerview
        mChatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity!!)
            setHasFixedSize(true)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        mChatsAdapter = object : FirebaseRecyclerAdapter<Chat, ChatHolder>(
            FirebaseRecyclerOptions.Builder<Chat>()
                .setQuery(ChatsQuery,Chat::class.java)
                .setLifecycleOwner(this)
                .build()
        ){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                val view = layoutInflater.inflate(R.layout.single_user, parent, false)
                return ChatHolder(view)
            }

            override fun onBindViewHolder(holder: ChatHolder, position: Int, model: Chat) {
                val userID = getRef(position).key!!
                var userName = "placeholder"
                var userThumb = "placeholder"

                mRootRef.child(UsersColumns.Users).child(userID).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        userName = snapshot.child(UsersColumns.UserName).value.toString()
                        userThumb = snapshot.child(UsersColumns.ImageThumbnail).value.toString()
                    }
                })

                val lastMessage:Query = mRootRef.child(MessageColumns.Messages).child(mCurrentUserID).child(userID).limitToLast(1)
                lastMessage.addChildEventListener(object : ChildEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    }

                    override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                        val message = snapshot.getValue(Message::class.java)!!

                        holder.bind(userName, userThumb, message)
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                    }
                })

                holder.itemView.setOnClickListener {
                    val chatIntent = ChatActivity.newIntent(activity!!,userID)
                    startActivity(chatIntent)
                }
            }
        }

        mChatsRecyclerView.adapter = mChatsAdapter
    }

    class ChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mUserName = itemView.single_request_username
        val mUserPic = itemView.single_request_user_pic
        val mMessage = itemView.single_user_status

        fun bind(username:String, userPic:String, model:Message){
            mUserName.text = username
            mMessage.filters += InputFilter.LengthFilter(20)
            
            if(model.type == MessageColumns.Text_Type){
                mMessage.text = model.message
            }else{
                mMessage.text = "Image"
            }

            Picasso.get().load(userPic).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.avatar_empty).into(mUserPic, object : Callback {
                    override fun onSuccess() {
                        //Cool! nothing to do
                    }

                    override fun onError(e: Exception?) {
                        Picasso.get().load(userPic).placeholder(R.drawable.avatar_empty).into(mUserPic)
                    }

                })

        }

    }


}
