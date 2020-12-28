package com.example.kotlinchatii.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinchatii.R
import com.example.kotlinchatii.Util.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_get_user_data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class GetUserData : AppCompatActivity() {

    private lateinit var username: String
    private lateinit var status:String
    private lateinit var imageUrl: String
    private var image: Uri? = null
    private var storageReference: StorageReference? = null

    //realtime db
    private var databaseReference: DatabaseReference? = null

    var db: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    private val userCollectionRef = Firebase.firestore.collection("users")

    private val cropActivityForResult = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                    .setAspectRatio(16,9)
                    .getIntent(this@GetUserData)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent).uri
        }

    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_user_data)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        db = FirebaseFirestore.getInstance()

        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance();

        btnDataDone.setOnClickListener {
            if (checkData()) {
                setToFirestore(username, status, image!!)
            }
        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityForResult) {
            it.let {uri ->
                image = uri
                imgUser.setImageURI(image)
            }
        }

        imgPickImage.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

    }


    private fun checkData(): Boolean {
        username = edtUserName.text.toString().trim()
        status = edtUserStatus.text.toString().trim()

        if (username.isEmpty()) {
            edtUserName.error = "Filed is required"
            return false
        } else if (status.isEmpty()) {
            edtUserStatus.error = "Filed is required"
            return false
        } else if (image == null) {
            Toast.makeText(this, "Image required", Toast.LENGTH_SHORT).show()
            return false
        } else return true
        return true
    }

    private fun saveUser(map: Map<String,Any>) = CoroutineScope(Dispatchers.IO).launch {
        try {
           // auth?.uid?.let { userCollectionRef.document(it).set(map).await() }
            userCollectionRef.document(auth!!.uid!!).set(map).await()
        } catch (e : Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@GetUserData, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setToFirestore(name: String, status: String, image: Uri) {

        val riversRef: StorageReference = storageReference!!.child(auth!!.uid + Constant.PATH)

        riversRef.putFile(image)
                .addOnSuccessListener { taskSnapshot -> // Get a URL to the uploaded content
                    val downloadUrl = taskSnapshot.storage.downloadUrl
                    downloadUrl.addOnCompleteListener {
                        imageUrl = it.result.toString()
                        val map = mapOf(
                                "name" to name,
                                "status" to status,
                                "image" to imageUrl,
                                "uid" to auth!!.uid
                        )
                        //db!!.document(auth!!.uid!!).update(map)
//                        db!!.collection("Users").add(map).addOnSuccessListener {
//                            Toast.makeText(this, "Success Upload", Toast.LENGTH_SHORT).show()
//                        }.addOnFailureListener{
//
//                        }
                        databaseReference!!.child(auth!!.uid!!).updateChildren(map)
               //         saveUser(map)
                        //userCollectionRef.add(map)

                        startActivity(Intent(this, DashboardActivity::class.java))
                    }
                }
                .addOnFailureListener {
                    // Handle unsuccessful uploads
                    // ...
                }

    }
}