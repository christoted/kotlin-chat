package com.example.kotlinchatii.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kotlinchatii.Fragment.ChatFragment
import com.example.kotlinchatii.Fragment.ContactFragment
import com.example.kotlinchatii.Fragment.ProfileFragment
import com.example.kotlinchatii.model.Friend
import com.example.kotlinchatii.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class DashboardActivity : AppCompatActivity() {

    //realtime db
    private var databaseReference: DatabaseReference? = null
    private var listUsersRealtimeDB: ArrayList<Friend> = ArrayList()


    private lateinit var auth: FirebaseAuth

    var username: String? = null
    var image: String? = null


    private var listUsers: ArrayList<Friend> = ArrayList()
    private lateinit var friend: Friend

    private val userCollectionRef = Firebase.firestore.collection("users")

    private fun retrieveData() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = userCollectionRef.document(auth.uid!!).get().await()

            withContext(Dispatchers.Main) {
                username = querySnapshot["name"].toString()
                image = querySnapshot["image"].toString()
            }

        } catch (e: Exception) {

        }
    }

    private fun retrieveUsers() = CoroutineScope(Dispatchers.IO).launch {
        listUsers = ArrayList()
        try {
            val querySnapshot = userCollectionRef.get().await()
            for (document in querySnapshot.documents) {
                Log.d("size-user document", "retrieveUsers: $document")
                friend = document.toObject<Friend>()!!
                Log.d("size-user friend", "retrieveUsers convert object: $friend")
                listUsers.add(friend)
                Log.d("size-user list", "user: ${listUsers.size}")
            }

        } catch (e: java.lang.Exception) {

        }
    }

    private fun retrieveDataRealtimeDB() = CoroutineScope(Dispatchers.IO).launch {
        val dataUser = databaseReference?.child(auth.uid!!)
        dataUser!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val friend = dataSnapshot.getValue(Friend::class.java)!!
                username = friend.name
                image = friend.image
                Log.d("hehehe", "onDataChange: $username")
                Log.d("hehehe", "onDataChange: $image")
            }
        })
    }
    private fun retrieveUserRealtimeDB() = CoroutineScope(Dispatchers.IO).launch {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val query:Query = databaseReference.orderByKey()
        listUsersRealtimeDB = ArrayList()
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if ( snapshot.exists()) {
                    for (data in snapshot.children) {
                        val friend : Friend = data.getValue(Friend::class.java)!!
                        listUsersRealtimeDB.add(friend)
                        Log.d("sizenya", "onDataChange: $listUsersRealtimeDB")

                    }
                }
            }

        })


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        auth = FirebaseAuth.getInstance()
        //      retrieveData()

        //     retrieveUsers()
        retrieveDataRealtimeDB()

        retrieveUserRealtimeDB()

        Log.d("12345", "onCreate: $listUsersRealtimeDB")

        // bottomNavigationView.setupWithNavController(navHosFragmentContainer.findNavController())

        setCurrentFragment(ChatFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.profileFragment -> {
                    setCurrentFragment(ProfileFragment.newInstance(username!!, image!!))
                }

                R.id.contactFragment -> {

                    //   val fragment : Fragment = ContactFragment.newInstance(listUsers)
                    val fragment: Fragment = ContactFragment.newInstance(listUsersRealtimeDB)

                    setCurrentFragment(fragment)
                }

                R.id.chatFragment -> {
                    val fragment: Fragment = ChatFragment()

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