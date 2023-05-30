package com.example.chatapp.Adapter

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.ChatActivity
import com.example.chatapp.Model.listMessModel
import com.example.chatapp.PersonalPageActivity
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class listMessAdapter(var listUser: ArrayList<listMessModel>) : RecyclerView.Adapter<listMessAdapter.MyViewHolder>()  {
    private val requestOptions = RequestOptions()
        .override(60,60)
        .centerCrop()
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)
    private val auth = Firebase.auth
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_show_list_chat, parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = listUser[position]
        if(user.isRead > 0) {
           if(user.type == "text") {
                holder.textIsCheckRead.text = user.isRead.toString()
                Glide.with(holder.itemView.context)
                    .load(user.imageURL)
                    .apply(requestOptions)
                    .into(holder.imageUser)
                holder.nameUser.text = user.nameUser
                holder.lastMess.setTypeface(null, Typeface.BOLD)
                holder.lastMess.text = user.lastMess

                holder.timeHour.text = user.timeHour
           } else if(user.type == "image"){
               holder.textIsCheckRead.text = user.isRead.toString()
               Glide.with(holder.itemView.context)
                   .load(user.imageURL)
                   .apply(requestOptions)
                   .into(holder.imageUser)
               holder.nameUser.text = user.nameUser
               holder.lastMess.setTypeface(null, Typeface.BOLD)
               if(auth.currentUser?.uid == user.uidUser) {
                   holder.lastMess.text = "Bạn đã gửi 1 hình ảnh"
               }else {holder.lastMess.text = "${user.nameUser} đã gửi 1 hình ảnh"}

               holder.timeHour.text = user.timeHour
           }

        } else {
            if(user.type == "text") {
                holder.bgCheckIsRead.visibility = View.INVISIBLE
                holder.textIsCheckRead.text = user.isRead.toString()
                Glide.with(holder.itemView.context)
                    .load(user.imageURL)
                    .apply(requestOptions)
                    .into(holder.imageUser)
                holder.nameUser.text = user.nameUser
                holder.lastMess.text = user.lastMess

                holder.timeHour.text = user.timeHour
            } else if(user.type == "image"){
                holder.textIsCheckRead.text = user.isRead.toString()
                Glide.with(holder.itemView.context)
                    .load(user.imageURL)
                    .apply(requestOptions)
                    .into(holder.imageUser)
                holder.nameUser.text = user.nameUser
                if(auth.currentUser?.uid == user.uidUser) {
                    holder.lastMess.text = "Bạn đã gửi 1 hình ảnh"
                }else {holder.lastMess.text = "${user.nameUser} đã gửi 1 hình ảnh"}

                holder.timeHour.text = user.timeHour
            }
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            val bundle = Bundle()
            bundle.putString("uidPersonal", user.uidUser)
            intent.putExtras(bundle)
            holder.itemView.context.startActivity(intent)
        }
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val imageUser : ImageView = itemView.findViewById(R.id.image_user_chat_list)
        val nameUser : TextView = itemView.findViewById(R.id.name_user_item_chat_list)
        val lastMess : TextView = itemView.findViewById(R.id.last_mess_user_chat_list)
        val timeHour : TextView = itemView.findViewById(R.id.text_time_hour)
        val bgCheckIsRead : LinearLayout = itemView.findViewById(R.id.background_check_is_read)
        val textIsCheckRead : TextView = itemView.findViewById(R.id.text_check_is_read)
    }
}