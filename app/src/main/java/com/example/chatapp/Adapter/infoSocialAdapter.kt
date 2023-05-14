package com.example.chatapp.Adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.Model.userInfomationModel
import com.example.chatapp.PersonalPageActivity
import com.example.chatapp.R

class infoSocialAdapter(var infoUsers : ArrayList<userInfomationModel>) : RecyclerView.Adapter<infoSocialAdapter.MyViewHolder>() {
    private val requestOptions = RequestOptions()
        .override(60,60)
        .centerCrop()
        .placeholder(R.drawable.asset_11)
        .error(R.drawable.danger_triangle_alert_figma)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_social, parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return infoUsers.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = infoUsers[position]
        holder.nameUserInfo.text = currentItem.nameUser
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUserURL)
            .apply(requestOptions)
            .into(holder.imageUserInfo)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PersonalPageActivity::class.java)
            val bundle = Bundle()
            bundle.putString("uidPersonal", currentItem.uidUser)
            intent.putExtras(bundle)
            holder.itemView.context.startActivity(intent)
        }
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val imageUserInfo : ImageView = itemView.findViewById(R.id.image_user_info_social)
        val nameUserInfo : TextView = itemView.findViewById(R.id.name_user_item_profile_social)
    }


}