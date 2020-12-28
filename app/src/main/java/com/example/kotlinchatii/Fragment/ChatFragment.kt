package com.example.kotlinchatii.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinchatii.R
import com.example.kotlinchatii.activity.MessageActivity
import com.example.kotlinchatii.databinding.ChatItemLayoutBinding
import com.example.kotlinchatii.databinding.FragmentChatBinding
import com.example.kotlinchatii.model.Chat
import com.example.kotlinchatii.model.ChatList
import com.example.kotlinchatii.model.Friend
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

val auth : FirebaseAuth = FirebaseAuth.getInstance()

private lateinit var binding : FragmentChatBinding

private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<ChatList, ChatFragment.ViewHolder>

class ChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        readMessages()
        return binding.root
   //     return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun readMessages() {
        val query = FirebaseDatabase.getInstance().getReference("ChatList").child(auth.uid!!)
        val firebaseRecyclerOptions = FirebaseRecyclerOptions.Builder<com.example.kotlinchatii.model.ChatList>()
            .setLifecycleOwner(this)
            .setQuery(query, com.example.kotlinchatii.model.ChatList::class.java)
            .build()

        firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<ChatList, ViewHolder>(firebaseRecyclerOptions) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val chatItemLayoutBinding: ChatItemLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.chat_item_layout, parent, false
                )

                return ViewHolder(chatItemLayoutBinding)
            }

            override fun onBindViewHolder(holder: ViewHolder, p1: Int, chatlist: ChatList) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(chatlist.member!!)

                databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if ( snapshot.exists()) {
                            val user = snapshot.getValue(Friend::class.java)

                            val chatModel = Chat(
                                chatlist.chatId,
                                user?.name,
                                chatlist.lastMessage,
                                user?.image,
                                "date",
                                user?.online
                            )

                            holder.chatItemLayoutBinding.chatModel = chatModel
                            holder.itemView.setOnClickListener {
                                val intent = Intent(activity, MessageActivity::class.java)
                                intent.putExtra("chat-id", chatlist.chatId)
                                intent.putExtra("hisId", user?.uid)
                                intent.putExtra("hisImage", user?.image)
                                startActivity(intent)
                            }
                        }
                    }

                })
            }
        }
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewChat.setHasFixedSize(false)
        binding.recyclerViewChat.adapter = firebaseRecyclerAdapter

    }

    class ViewHolder(val chatItemLayoutBinding: ChatItemLayoutBinding) :
        RecyclerView.ViewHolder(chatItemLayoutBinding.root)

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}