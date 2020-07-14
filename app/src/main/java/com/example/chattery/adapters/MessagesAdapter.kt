package com.example.chattery.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattery.R
import com.example.chattery.firebase.MessageColumns
import com.example.chattery.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.single_message.view.*

class MessagesAdapter(val context:Context, val messages: List<Message>) : RecyclerView.Adapter<MessagesAdapter.MessageHolder>() {
    val mCurrentUserID = FirebaseAuth.getInstance().currentUser?.uid!!

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
        val Message: TextView = itemView.findViewById(R.id.message_textview)
        val Image: ImageView = itemView.findViewById(R.id.message_imageview);


        fun bind(model:Message){
            when(model.type){
                //Text Message
                MessageColumns.Text_Type -> {
                    Image.visibility = View.GONE

                    when(model.from){
                        mCurrentUserID -> {
                            Message.text = model.message
                            Message.setBackgroundResource(R.drawable.my_message_background)
                            Message.setTextColor(context.resources.getColor(R.color.colorPrimaryDark))

                            val rules = Message.layoutParams as RelativeLayout.LayoutParams
                            rules.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE)
                            rules.addRule(RelativeLayout.ALIGN_PARENT_START,0)
                            Message.layoutParams = rules
                        }

                        else -> {
                            Message.text = model.message
                            Message.setBackgroundResource(R.drawable.user_message_background)
                            Message.setTextColor(context.resources.getColor(R.color.design_default_color_on_primary))

                            val rules = Message.layoutParams as RelativeLayout.LayoutParams
                            rules.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0)
                            rules.addRule(RelativeLayout.ALIGN_PARENT_START,RelativeLayout.TRUE)
                            Message.layoutParams = rules
                        }
                    }
                }

                //Image Message
                MessageColumns.Image_Type -> {
                    Message.visibility = View.GONE

                    when(model.from){
                        mCurrentUserID -> {
                            Picasso.get().load(model.message).into(Image)

                            val rules = Image.layoutParams as RelativeLayout.LayoutParams
                            rules.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE)
                            rules.addRule(RelativeLayout.ALIGN_PARENT_START,0)
                            Image.layoutParams = rules
                        }

                        else -> {
                            Picasso.get().load(model.message).into(Image)

                            val rules = Image.layoutParams as RelativeLayout.LayoutParams
                            rules.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0)
                            rules.addRule(RelativeLayout.ALIGN_PARENT_START,RelativeLayout.TRUE)
                            Image.layoutParams = rules
                        }
                    }
                }
            }


        }
    }
}