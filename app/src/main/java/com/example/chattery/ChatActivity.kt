package com.example.chattery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import com.example.chattery.firebase.OnlineStatus
import com.example.chattery.firebase.UsersColumns
import com.google.firebase.database.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso

class ChatActivity : AppCompatActivity() {
    lateinit var mUsersDatabase: DatabaseReference

    companion object{
        private val EXTRA_ID = "userid"

        fun newIntent(context: Context, userID:String): Intent {
            val chatIntent = Intent(context,ChatActivity::class.java)
            chatIntent.putExtra(EXTRA_ID,userID)

            return chatIntent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val userID = intent.getStringExtra(EXTRA_ID)!!

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.actionbar_chat_activity,null,false)

        val mUserName = view.findViewById<TextView>(R.id.chat_actionbar_username)
        val mUserPic = view.findViewById<CircularImageView>(R.id.chat_actionbar_userpic)
        val mUserLastSeen = view.findViewById<TextView>(R.id.chat_actionbar_last_seen)

        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowCustomEnabled(true)
            customView = view
        }

        mUsersDatabase = FirebaseDatabase.getInstance().reference.child(UsersColumns.Users).child(userID)
        mUsersDatabase.keepSynced(true)
        mUsersDatabase.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child(UsersColumns.UserName).value.toString()
                val userthumb = snapshot.child(UsersColumns.ImageThumbnail).value.toString()
                val onlineStatus = snapshot.child(UsersColumns.Online).value.toString()

                mUserName.text = username
                mUserLastSeen.text = if(onlineStatus.equals(OnlineStatus.Online)) "online" else onlineStatus
                Picasso.get().load(userthumb).placeholder(R.drawable.avatar_empty).into(mUserPic)
            }
        })
    }
}
