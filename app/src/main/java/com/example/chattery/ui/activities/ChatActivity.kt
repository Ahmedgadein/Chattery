package com.example.chattery.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.chattery.commons.AgoTime
import com.example.chattery.commons.ChatteryActivity
import com.example.chattery.R
import com.example.chattery.adapters.MessagesAdapter
import com.example.chattery.firebase.ChatsColumns
import com.example.chattery.firebase.MessageColumns
import com.example.chattery.firebase.OnlineStatus
import com.example.chattery.firebase.UsersColumns
import com.example.chattery.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.actionbar_chat_activity.view.*
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : ChatteryActivity() {
    lateinit var mUsersDatabase: DatabaseReference
    lateinit var mStorage:StorageReference
    lateinit var mRootRef:DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var mCurrentUserID:String
    lateinit var mChatUserID:String

    lateinit var mMessageAddButton: ImageButton
    lateinit var mMessageSendAddButton: ImageButton
    lateinit var mMessage: EditText
    lateinit var mMessagesRecyclerView: RecyclerView
    lateinit var mSwipeRefresh: SwipeRefreshLayout

    private val GET_IMAGE = 11;

    //RecyclerView
    lateinit var messagesList:MutableList<Message>
    lateinit var messagesAdapter: MessagesAdapter

    //Pagination
    private val NUMBER_OF_MESSAGES_PER_REFRESH = 5
    var mCurrentPage = 1
    var itemPos = 0
    var messageKey = " "
    var previousMessageKey = " "

    private val TAG = "ChatActivity"

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
        mChatUserID = userID
        mAuth = FirebaseAuth.getInstance()
        mCurrentUserID = mAuth.currentUser?.uid!!

        mStorage = FirebaseStorage.getInstance().reference
        mUsersDatabase = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users).child(userID)
        mRootRef = FirebaseDatabase.getInstance().reference

        //Chat buttons and edit text
        mMessage = findViewById(R.id.chat_message_edittext)
        mMessageAddButton = findViewById(R.id.chat_add_button)
        mMessageSendAddButton = findViewById(R.id.chat_message_send_button)
        mSwipeRefresh = findViewById(R.id.chat_resfresh_layout)


        //RecyclerView
        messagesList = mutableListOf<Message>()
        messagesAdapter = MessagesAdapter(this,messagesList)
        mMessagesRecyclerView = findViewById(R.id.chat_messages_recyclerview)
        mMessagesRecyclerView.apply {
            adapter = messagesAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
        setMessagesList(mCurrentUserID,userID)

        // --------- Actionbar layout ----------------//>

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.actionbar_chat_activity,null,false)

        //Toolbar Username, last seen, picture
        val mUserName = view.chat_actionbar_username
        val mUserPic = view.chat_actionbar_userpic
        val mUserLastSeen = view.chat_actionbar_last_seen

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

        mMessageAddButton.setOnClickListener {
            val imageIntent = Intent()
            imageIntent.type = "image/*"
            imageIntent.action = Intent.ACTION_GET_CONTENT
            val chooserIntent = Intent.createChooser(imageIntent, "Choose Image")

            Log.i(TAG,"Started Intent")
            startActivityForResult(chooserIntent, GET_IMAGE)
        }

        mSwipeRefresh.setOnRefreshListener {
            mCurrentPage++
            itemPos = 0
            updateMessages(mCurrentUserID, userID)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GET_IMAGE && resultCode == Activity.RESULT_OK){
            val imageUri = data?.data

            Log.i(TAG, "Recieved Uri")
            sendImage(imageUri)
        }
    }

    private fun sendImage(imageUri: Uri?) {
        val ImagePath = mStorage.child(MessageColumns.Messages).child(UUID.randomUUID().toString() + ".jpg")

        Log.i(TAG, "Adding image to storage")
        ImagePath.putFile(imageUri!!).addOnSuccessListener {
            ImagePath.downloadUrl.addOnSuccessListener {
                Log.i(TAG, "Image added successfully & got douwnload URL")
                SendImageMessage(it!!.toString())
            }
        }
    }

    private fun SendImageMessage(imageURL: String) {
        val imageMessageMap = HashMap<String,Any>()
        imageMessageMap[MessageColumns.Message] = imageURL
        imageMessageMap[MessageColumns.Type] = MessageColumns.Image_Type
        imageMessageMap[MessageColumns.Seen] = false
        imageMessageMap[MessageColumns.From] = mCurrentUserID
        imageMessageMap[MessageColumns.TimeStamp] = Calendar.getInstance().timeInMillis

        val push_id = mRootRef.child(MessageColumns.Message).child(mCurrentUserID).child(mChatUserID).push().key!!
        val senderRef = MessageColumns.Messages + "/" + mCurrentUserID + "/" + mChatUserID + "/" + push_id
        val recieverRef = MessageColumns.Messages + "/" + mChatUserID + "/" + mCurrentUserID + "/" + push_id

        val usersMessages = HashMap<String,Any>()
        usersMessages[senderRef] = imageMessageMap
        usersMessages[recieverRef] = imageMessageMap

        mRootRef.updateChildren(usersMessages, object : DatabaseReference.CompletionListener{
            override fun onComplete(error: DatabaseError?, p1: DatabaseReference) {
                if(error != null){
                    //TODO:Handle error
                }
            }
        })

    }

    private fun sendMessage(senderID: String, recieverID:String) {
        val message = mMessage.text.toString()

        if(!TextUtils.isEmpty(message)){

            val push_id = mRootRef.child(MessageColumns.Message).child(senderID).child(recieverID).push().key!!
            val senderRef = MessageColumns.Messages + "/" + senderID + "/" + recieverID + "/" + push_id
            val recieverRef = MessageColumns.Messages + "/" + recieverID + "/" + senderID + "/" + push_id

            val messageMap = HashMap<String, Any>()
            messageMap[MessageColumns.Message] = message
            messageMap[MessageColumns.Type] = MessageColumns.Text_Type
            messageMap[MessageColumns.TimeStamp] = Calendar.getInstance().timeInMillis
            messageMap[MessageColumns.Seen] = false
            messageMap[MessageColumns.From] = senderID

            val usersMessages = HashMap<String,Any>()
            usersMessages[senderRef] = messageMap
            usersMessages[recieverRef] = messageMap

            mMessage.text = null

            mRootRef.updateChildren(usersMessages, object : DatabaseReference.CompletionListener{
                override fun onComplete(error: DatabaseError?, p1: DatabaseReference) {
                    if(error != null){
                        //TODO:Handle error
                    }
                }
            })
        }

    }

    private fun updateMessages(currentUser: String, chatUser: String) {
        val query = mRootRef.child(MessageColumns.Messages).child(currentUser).child(chatUser)
            .orderByKey()
            .endAt(messageKey)
            .limitToLast( NUMBER_OF_MESSAGES_PER_REFRESH)


        query.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                val message = snapshot.getValue(Message::class.java)!!

                // If message key isn't the last add to the list
                if(!snapshot.key.toString().equals(previousMessageKey)){
                    messagesList.add(itemPos++, message)

                // If it's last don't add to list (avoid duplication) & set previous key to current key
                }else{
                    previousMessageKey = messageKey
                }

                if(itemPos == 1){
                    messageKey = snapshot.key.toString()
                }
                messagesAdapter.notifyDataSetChanged()
                mMessagesRecyclerView.scrollToPosition(itemPos)

                mSwipeRefresh.isRefreshing = false
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }


    private fun setMessagesList(currentUser:String, chatUser:String) {
        val query = mRootRef.child(MessageColumns.Messages).child(currentUser).child(chatUser).limitToLast(
            mCurrentPage * NUMBER_OF_MESSAGES_PER_REFRESH)


        query.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                val message = snapshot.getValue(Message::class.java)!!

                itemPos++

                if(itemPos == 1){
                    messageKey = snapshot.key.toString()
                }
                
                //On first load, previous last key is the last key
                previousMessageKey = messageKey

                messagesList.add(message)
                messagesAdapter.notifyDataSetChanged()
                mMessagesRecyclerView.scrollToPosition(messagesList.size - 1)

                mSwipeRefresh.isRefreshing = false
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }
}
