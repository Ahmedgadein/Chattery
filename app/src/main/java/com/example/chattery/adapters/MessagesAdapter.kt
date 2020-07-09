package com.example.chattery.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.R
import com.example.chattery.model.Message
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.single_message.view.*

class MessagesAdapter(val context:Context, val messages: List<Message>) : RecyclerView.Adapter<MessagesAdapter.MessageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_message,parent,false)
        return  MessageHolder(view)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }


    inner class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val SenderPic: CircularImageView = itemView.findViewById(R.id.message_sender_pic)
        val Message: TextView = itemView.findViewById(R.id.message_textview)

        fun bind(model:Message){
            Message.text = model.message
            SenderPic.setImageDrawable(context.getDrawable(R.drawable.avatar_empty))
        }
    }
}