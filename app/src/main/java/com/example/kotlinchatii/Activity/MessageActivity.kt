package com.example.kotlinchatii.Activity

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.example.kotlinchatii.Model.ChatList
import com.example.kotlinchatii.Model.Friend
import com.example.kotlinchatii.Model.Message
import com.example.kotlinchatii.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_message.*


class MessageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var chatId: String? = null

    private var hisId: String? = null
    private var hisImage: String? = null

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var myId: String
    private lateinit var myName: String
    private lateinit var myImage: String

    private val chatCollectionRef = Firebase.firestore.collection("chatList")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        auth = FirebaseAuth.getInstance()
        val friend: Friend? = intent.getParcelableExtra("friend")

        hisId = friend?.uid
        hisImage = friend?.image
        Glide.with(this)
                .load(hisImage)
                .into(msgImage)

        myId = auth.uid.toString()
        Log.d("my-id", "onCreate: $myId")
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
        val firestoneReference = Firebase.firestore.collection("chatList").document(myId)

        Firebase.firestore.collection("chatList").get().addOnCompleteListener {
            OnCompleteListener<QuerySnapshot> { task ->

                if (task.isSuccessful) {
                    val queryData: String = firestoneReference.get().result?.get("member").toString()
                    if (queryData == hisId) {
                        firestoneReference.addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w("12345", "Listen failed.", e)
                                return@addSnapshotListener
                            }

                            if (snapshot != null && snapshot.exists()) {
                                val member = queryData
                                if (member == queryData) {
                                    chatId = snapshot.id

                                }
                            } else {
                                Log.d("12345", "Current data: null")
                            }
                        }
                    }

                }

            }

        }
    }


    private fun createChat(message: String) {
        var chatCollectionRef = Firebase.firestore.collection("chatList").document(myId)
        chatId = chatCollectionRef.id

        val myChatList = ChatList(chatId, message, System.currentTimeMillis().toString(), hisId)

        chatCollectionRef.collection(chatId!!).add(myChatList)

        chatCollectionRef = Firebase.firestore.collection("chatList").document(hisId!!)

        val hisChatList = ChatList(chatId, message, System.currentTimeMillis().toString(), myId)

        chatCollectionRef.collection(chatId!!).add(hisChatList)

        // For Chat Collection
        chatCollectionRef = Firebase.firestore.collection("chat").document(chatId!!)
        val message = Message(myId, hisId!!, message, System.currentTimeMillis().toString(), type = "text")

        chatCollectionRef.set(message)

    }

    private fun sendMessage(message: String) {
        if (chatId == null) {
            createChat(message)
        } else {

            var firestoreReference = Firebase.firestore.collection("chat").document(chatId!!)

            val messageModel = Message(myId, hisId!!, message, System.currentTimeMillis().toString(), type = "text")

            firestoreReference.set(messageModel)

            val map: MutableMap<String, Any> = HashMap()

            map["lastMessage"] = message
            map["date"] = System.currentTimeMillis().toString()

            firestoreReference = Firebase.firestore.collection("chatList").document(myId).firestore.document(chatId!!)
            firestoreReference.update(map)

            firestoreReference = Firebase.firestore.collection("chatList").document(hisId!!).firestore.document(chatId!!)
            firestoreReference.update(map)


        }
    }
}