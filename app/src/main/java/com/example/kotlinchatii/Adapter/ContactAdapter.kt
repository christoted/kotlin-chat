package com.example.kotlinchatii.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinchatii.model.Friend
import com.example.kotlinchatii.R
import kotlinx.android.synthetic.main.contact_item_layout.view.*

class ContactAdapter(
        val listFriend: ArrayList<Friend>,
        val context : Context,
        val itemListener : ContactItemListener
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view){

        init {
            itemView.setOnClickListener {
                itemListener.onContactItemClick(adapterPosition)
            }
        }

        fun bind(friend: Friend) {
            itemView.txtContactName.text = friend.name
            itemView.txtContactStatus.text = friend.status
            Glide.with(itemView)
                    .load(friend.image)
                    .into(itemView.imgContactUserInfo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item_layout, parent, false)

        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listFriend.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val friend : Friend = listFriend[position]
        holder.bind(friend)
    }

    interface ContactItemListener {
        fun onContactItemClick(position: Int)
    }

}