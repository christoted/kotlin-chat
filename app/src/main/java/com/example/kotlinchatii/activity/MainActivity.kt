package com.example.kotlinchatii.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kotlinchatii.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

//        id_btn_signIn.setBackgroundColor( ContextCompat.getColor(this@MainActivity, R.color.colorAccent));


        id_btn_signIn.setOnClickListener{
            login()
        }
        id_tv_sign_up.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        if ( auth.currentUser != null) {
           // startActivity(Intent(this, DashboardActivity::class.java))
        }

    }

    private fun login() {
        val email = id_tv_email.text.toString()
        val password = id_tv_password.text.toString()

        if ( email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email,password).await()
                    withContext(Dispatchers.Main) {
                        checkloggedState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

    }

    private fun checkloggedState() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, GetUserData::class.java))
        }
    }


}