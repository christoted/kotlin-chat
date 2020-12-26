package com.example.kotlinchatii.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinchatii.Fragment.ChatFragment
import com.example.kotlinchatii.Fragment.ContactFragment
import com.example.kotlinchatii.Fragment.ProfileFragment
import com.example.kotlinchatii.Model.Friend
import com.example.kotlinchatii.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    var username : String ? = null
    var image: String ? = null


    private var listUsers : ArrayList<Friend> = ArrayList()
    private lateinit var friend: Friend

    private val userCollectionRef = Firebase.firestore.collection("users")

    private fun retrieveData() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = userCollectionRef.document(auth.uid!!).get().await()

            withContext(Dispatchers.Main) {
                username = querySnapshot["name"].toString()
                image = querySnapshot["image"].toString()
            }

        } catch (e : Exception) {

        }
    }

    private fun retrieveUsers() = CoroutineScope(Dispatchers.IO).launch {
        listUsers = ArrayList()
        try {
            val querySnapshot = userCollectionRef.get().await()
            for ( document in querySnapshot.documents){
                Log.d("size-user document", "retrieveUsers: $document")
                friend = document.toObject<Friend>()!!
                Log.d("size-user friend", "retrieveUsers convert object: $friend")
                listUsers.add(friend)
                Log.d("size-user list", "user: ${listUsers.size}")
            }

        } catch (e : java.lang.Exception) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        retrieveData()

        retrieveUsers()

     // bottomNavigationView.setupWithNavController(navHosFragmentContainer.findNavController())

        setCurrentFragment(ChatFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.profileFragment -> {
                    setCurrentFragment(ProfileFragment.newInstance(username!!, image!!))
                }

                R.id.contactFragment -> {

                    val fragment : Fragment = ContactFragment.newInstance(listUsers)

                    setCurrentFragment(fragment)
                }

                R.id.chatFragment -> {
                    val fragment : Fragment = ChatFragment()

                    setCurrentFragment(fragment)
                }
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) = supportFragmentManager.beginTransaction()
        .apply {
        replace(R.id.flFragment, fragment)
        commit()
    }
}