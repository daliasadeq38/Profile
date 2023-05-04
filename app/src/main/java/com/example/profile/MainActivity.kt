package com.example.profile

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.profile.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.mo7ammedtabasi.fcm.FirebaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "TagForApp"

class MainActivity : AppCompatActivity() {

    private lateinit var binding :ActivityMainBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        progressDialog = ProgressDialog(this)
        FirebaseMessaging.getInstance().subscribeToTopic("Login")
            .addOnCompleteListener {
                Log.d("TagForApp", "Send")
            }
            .addOnFailureListener{
                Log.d("TagForApp", "Failure")
            }

        if (firebaseAuth.currentUser != null) {
//            startActivity(Intent(this@MainActivity, ShowScreen::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            userLogin()
        }

        binding.tvSignin.setOnClickListener {
            startActivity(Intent(this@MainActivity, SignActivity::class.java))
        }

    }

    private fun userLogin(){
        val emailIsEmpty = binding.edEmail.text.trim().isNotEmpty()
        val passwordIsEmpty = binding.edPassword.text.trim().isNotEmpty()

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            FirebaseService.token = it
            Log.d(TAG, "token : $it")
        }

        FirebaseMessaging.getInstance().subscribeToTopic("LOGIN")

        if (emailIsEmpty && passwordIsEmpty){
            val email= binding.edPassword.text.trim().toString()
            val password = binding.edPassword.text.trim().toString()
            progressDialog.isShowing
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                    Toast.makeText(this@MainActivity, "User login successfully", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this@MainActivity, ShowScreen::class.java))
                    progressDialog.cancel()

            }.addOnFailureListener {
                Log.d("TagForApp", "Failure")
                Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
            }
        } else if (!emailIsEmpty){
            binding.edEmail.error = "The failed is Empty"
        }else if (!passwordIsEmpty){
            binding.edPassword.error = "The failed is Empty"
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(TAG, "Response: $response")
                } else {
                    Log.d(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, "setNotification: ${e.toString()}")
            }
        }
}