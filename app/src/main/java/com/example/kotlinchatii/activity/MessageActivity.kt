package com.example.kotlinchatii.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinchatii.BR
import com.example.kotlinchatii.model.ChatList
import com.example.kotlinchatii.model.Friend
import com.example.kotlinchatii.model.Message
import com.example.kotlinchatii.R
import com.example.kotlinchatii.databinding.ActivityMessageBinding
import com.example.kotlinchatii.databinding.LeftItemLayoutBinding
import com.example.kotlinchatii.databinding.RightItemLayoutBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_message.*


class MessageActivity : AppCompatActivity() {

    private lateinit var activityMessageBinding: ActivityMessageBinding

    private lateinit var auth: FirebaseAuth

    private var chatId: String? = null

    private var hisId: String? = null
    private var hisImage: String? = null

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var myId: String
    private lateinit var myName: String
    private lateinit var myImage: String

    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Message, ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // activityMessageBinding = DataBindingUtil.setContentView(this, R.layout.activity_message)
        activityMessageBinding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(activityMessageBinding.root)
        //  setContentView(R.layout.activity_message)


        if ( intent.hasExtra("chat-id")) {
            chatId = intent.getStringExtra("chat-id")
            hisId = intent.getStringExtra("hisId")
            Log.d("hisIdBro", "onCreate: test12345 $hisId")
            hisImage = intent.getStringExtra("hisImage")
            readMessage(chatId!!)
        } else {
            hisId = intent.getStringExtra("hisId")
            Log.d("hisIdBro", "onCreate: test12345 $hisId")
            hisImage = intent.getStringExtra("hisImage")
        }

        auth = FirebaseAuth.getInstance()
        val friend: Friend? = intent.getParcelableExtra("friend")

        hisId = friend?.uid
        Log.d("his-id", "onCreate: $hisId")
        hisImage = friend?.image
        Glide.with(this)
            .load(hisImage)
            .into(msgImage)

        myId = auth.uid.toString()

        Log.d("my-id", "onCreate: $myId")
        Log.d("my-id", "his Id: $hisId")
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        myName = sharedPreferences.getString("name", null).toString()
        myImage = sharedPreferences.getString("image", null).toString()

        Log.d("my-name", "onCreate: $myName")
        Log.d("my-name", "onCreate: $myImage")

        btnSend.setOnClickListener {
            if (msgText.text.isEmpty()) {
                Toast.makeText(this, "isi dong", Toast.LENGTH_SHORT).show()

            } else {
                val message: String = msgText.text.toString()
                sendMessage(message)
            }

        }

        if (chatId == null)
            checkChat(hisId!!)
    }

    //If chatId equal to null, we must check whether there's a chat or not
    //  if chat exists then we just send message, if not exists we create the chat then send message
    // We need create chat for on both user side
    // Nanti DB nya kek ada
    // Collection chat yang berisi chatId, di dalam chatId nanti bakal nyimpen kumpulan id-chat yang lebih dalam isinya (date, message, receiver, sender, type)
    // lalu ada collection chat-list, dimana document id dari chat-list diambil dari uuid, nanti di dlm nya baru ada ChatId
    private fun checkChat(hisId: String) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myId)
        val query = databaseReference.orderByChild("member").equalTo(hisId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val member = data.child("member").value.toString()
                        if (member == hisId) {
                            chatId = data.key
                            readMessage(chatId!!)
                        }
                    }
                }
            }

        })
    }


    private fun createChat(message: String) {

        var databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myId)
        chatId = databaseReference.push().key
        val myChatList = ChatList(chatId, message, System.currentTimeMillis().toString(), hisId)
        databaseReference.child(chatId!!).setValue(myChatList)

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisId!!)
        val hisChatList = ChatList(chatId, message, System.currentTimeMillis().toString(), myId)
        databaseReference.child(chatId!!).setValue(hisChatList)

        // For Chat Collection
        databaseReference = FirebaseDatabase.getInstance().getReference("chat").child(chatId!!)
        val message =
            Message(myId, hisId!!, message, System.currentTimeMillis().toString(), type = "text")
        databaseReference.push().setValue(message)

    }

    private fun sendMessage(message: String) {
        hisId = intent.getStringExtra("hisId")
        Log.d("hisIdSend", "sendMessage: $hisId")
        if (chatId == null) {
            createChat(message)
        } else {
            var databaseReference =
                FirebaseDatabase.getInstance().getReference("chat").child(chatId!!)

            val messageModel = Message(
                myId,
                hisId!!,
                message,
                System.currentTimeMillis().toString(),
                type = "text"
            )

            databaseReference.push().setValue(messageModel)

            val map: MutableMap<String, Any> = HashMap()

            map["lastMessage"] = message
            map["date"] = System.currentTimeMillis().toString()

            databaseReference =
                FirebaseDatabase.getInstance().getReference("ChatList").child(myId).child(chatId!!)
            databaseReference.updateChildren(map)

            databaseReference =
                FirebaseDatabase.getInstance().getReference("ChatList").child(hisId!!)
                    .child(chatId!!)
            databaseReference.updateChildren(map)
        }
    }

    private fun readMessage(chatId: String) {
        val query = FirebaseDatabase.getInstance().getReference("chat").child(chatId)

        val firebaseRecyclerOptions = FirebaseRecyclerOptions.Builder<Message>()
            .setLifecycleOwner(this)
            .setQuery(query, Message::class.java)
            .build()

        query.keepSynced(true)

        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<Message, ViewHolder>(firebaseRecyclerOptions) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

                    var viewDataBinding: ViewDataBinding? = null

                    if (viewType == 0) {
                        viewDataBinding = RightItemLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    }

                    if (viewType == 1) {
                        viewDataBinding = LeftItemLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    }

                    return ViewHolder(viewDataBinding!!)
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int, message: Message) {
                    if (getItemViewType(position) == 0) {
                        holder.viewDataBinding.setVariable(BR.message, message)
                        holder.viewDataBinding.setVariable(BR.messageImage, myImage)
                    }

                    if (getItemViewType(position) == 1) {
                        holder.viewDataBinding.setVariable(BR.message, message)
                        holder.viewDataBinding.setVariable(BR.messageImage, hisImage)
                    }
                }

                override fun getItemViewType(position: Int): Int {
                    val message = getItem(position)
                    return if (message.senderId == myId)
                        0
                    else
                        1
                }
            }

        activityMessageBinding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        activityMessageBinding.messageRecyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter!!.startListening()
    }

    class ViewHolder(var viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root)
}

