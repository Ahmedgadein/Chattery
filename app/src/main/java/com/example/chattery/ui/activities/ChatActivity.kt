package com.example.chattery.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.AgoTime
import com.example.chattery.ChatteryActivity
import com.example.chattery.R
import com.example.chattery.adapters.MessagesAdapter
import com.example.chattery.firebase.ChatsColumns
import com.example.chattery.firebase.MessageColumns
import com.example.chattery.firebase.OnlineStatus
import com.example.chattery.firebase.UsersColumns
import com.example.chattery.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : ChatteryActivity() {
    lateinit var mUsersDatabase: DatabaseReference
    lateinit var mRootRef:DatabaseReference
    lateinit var mAuth: FirebaseAuth

    lateinit var mMessageAddButton: ImageButton
    lateinit var mMessageSendAddButton: ImageButton
    lateinit var mMessage: EditText
    lateinit var mMessagesRecyclerView: RecyclerView

    //RecyclerView
    lateinit var messagesList:MutableList<Message>
    lateinit var messagesAdapter: MessagesAdapter

    companion object{
        private val EXTRA_ID = "userid"

        fun newIntent(context: Context, userID:String): Intent {
            val chatIntent = Intent(context, ChatActivity::class.java)
            chatIntent.putExtra(EXTRA_ID,userID)

            return chatIntent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val userID = intent.getStringExtra(EXTRA_ID)!!
        mAuth = FirebaseAuth.getInstance()
        val mCurrentUserID = mAuth.currentUser?.uid!!

        mUsersDatabase = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users).child(userID)
        mRootRef = FirebaseDatabase.getInstance().reference
        mUsersDatabase.keepSynced(true)

        //Chat buttons and edit text
        mMessage = findViewById(R.id.chat_message_edittext)
        mMessageAddButton = findViewById(R.id.chat_add_button)
        mMessageSendAddButton = findViewById(R.id.chat_message_send_button)
        mMessagesRecyclerView = findViewById(R.id.chat_messages_recyclerview)

        //RecyclerView
        messagesList = mutableListOf()
        messagesAdapter = MessagesAdapter(this,messagesList)
        mMessagesRecyclerView.apply {
            adapter = messagesAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
        setMessagesList(mCurrentUserID,userID)

        // --------- Actionbar layout ----------------//>

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.actionbar_chat_activity,null,false)

        //Toolbar Username, last seen, picture
        val mUserName = view.findViewById<TextView>(R.id.chat_actionbar_username)
        val mUserPic = view.findViewById<CircularImageView>(R.id.chat_actionbar_userpic)
        val mUserLastSeen = view.findViewById<TextView>(R.id.chat_actionbar_last_seen)

        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowCustomEnabled(true)
            customView = view
        }
        // --------- Actionbar layout ----------------//>


        mUsersDatabase.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                //TODO: Do Something
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child(UsersColumns.UserName).value.toString()
                val userthumb = snapshot.child(UsersColumns.ImageThumbnail).value.toString()
                val onlineStatus = snapshot.child(UsersColumns.Online).value.toString()

                mUserName.text = username
                mUserLastSeen.text = if(onlineStatus.equals(OnlineStatus.Online)) "online" else AgoTime.getTimeAgo(onlineStatus.toLong(), application)
                Picasso.get().load(userthumb).placeholder(R.drawable.avatar_empty).into(mUserPic)
            }
        })

        mRootRef.child(ChatsColumns.Chats).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild(mCurrentUserID)){
                    val chatMap = HashMap<String, Any>()
                    chatMap[ChatsColumns.Seen] = false
                    chatMap[ChatsColumns.Timestamp] = Calendar.getInstance().timeInMillis

                    val usersChatMap = HashMap<String, Any>()
                    usersChatMap[ChatsColumns.Chats + "/" + mCurrentUserID + "/" + userID] = chatMap
                    usersChatMap[ChatsColumns.Chats + "/" + userID + "/" + mCurrentUserID] = chatMap

                    mRootRef.updateChildren(usersChatMap, object : DatabaseReference.CompletionListener{
                        override fun onComplete(error: DatabaseError?, p1: DatabaseReference) {
                            if (error != null){
                                //TODO: Handle error
                            }
                        }
                    })
                }
            }
        })

        mMessageSendAddButton.setOnClickListener {
            sendMessage(mCurrentUserID, userID)
        }
    }


    private fun sendMessage(senderID: String, recieverID:String) {
        val message = mMessage.text.toString()

        if(!TextUtils.isEmpty(message)){

            val push_id = mRootRef.child(MessageColumns.Message).child(senderID).child(recieverID).push().key!!
            val senderRef = MessageColumns.Messages + "/" + senderID + "/" + recieverID + "/" + push_id
            val recieverRef = MessageColumns.Messages + "/" + recieverID + "/" + senderID + "/" + push_id

            val messageMap = HashMap<String, Any>()
            messageMap[MessageColumns.Message] = message
            messageMap[MessageColumns.TimeStamp] = Calendar.getInstance().timeInMillis
            messageMap[MessageColumns.Seen] = false

            val usersMessages = HashMap<String,Any>()
            usersMessages.put(senderRef,messageMap)
            usersMessages.put(recieverRef,messageMap)

            mRootRef.updateChildren(usersMessages, object : DatabaseReference.CompletionListener{
                override fun onComplete(error: DatabaseError?, p1: DatabaseReference) {
                    if(error == null){
                        mMessage.text = null
                    }
                }
            })
        }
    }

    private fun setMessagesList(currentUser:String, chatUser:String) {
        mRootRef.child(MessageColumns.Messages).child(currentUser).child(chatUser).addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                val message = snapshot.getValue(Message::class.java)!!
                messagesList.add(message)
                messagesAdapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }
}
