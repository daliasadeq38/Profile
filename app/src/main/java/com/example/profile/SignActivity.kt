package com.example.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.profile.databinding.ActivitySignBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mo7ammedtabasi.fcm.FirebaseService
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

private const val PICK_IMAGE_REQUEST = 111
private const val TAG = "TagForApp"


class SignActivity : AppCompatActivity() {

    lateinit var firebaseAuth:FirebaseAuth
    lateinit var firebaseFirestore:FirebaseFirestore
    lateinit var progressDialog:ProgressDialog
    lateinit var storageReference: StorageReference
    lateinit var binding : ActivitySignBinding
    private var listener: ActivityResultLauncher<Intent>? = null
    private var imageURI: Uri? = null
    private var url = ""






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference("imagesUsers")

        listener = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageURI = result.data?.data
                binding.imageProfile.setImageURI(imageURI)

                    url = uploadImageProfile(
                        binding.imageProfile,
                        firebaseAuth.currentUser!!.uid
                    )
                }

        }

        binding.imagePick.setOnClickListener {
            choseImageProfile()
        }




        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            FirebaseService.token = it
            Log.d(TAG, "token : $it")
        }

        FirebaseMessaging.getInstance().subscribeToTopic("LOGIN")

        binding.btnSignup.setOnClickListener {
            createAccount()
        }
    }

    fun uploadImageProfile(imageProfile: ImageView, uid: String): String {
        val imageRef: StorageReference = storageReference.child("ImagesProfile")

        val bitmap = (imageProfile.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val childRef = imageRef.child(uid + "imageProfile.png")
        childRef.putBytes(data)
        var uri = ""
        childRef.downloadUrl.addOnCompleteListener{
            uri = it.result.toString()
        }
        return uri
    }

    private fun createAccount() {

        val emailIsEmpty = binding.edEmailSignup.text.trim().isNotEmpty()
        val passwordIsEmpty = binding.edPasswordSignup.text.trim().isNotEmpty()
        val addressIsEmpty = binding.edAddress.text.trim().isNotEmpty()
        val phoneIsEmpty = binding.edPhone.text.trim().isNotEmpty()
        val firstNameIsEmpty = binding.edFullName.text.trim().isNotEmpty()

        if (url == ""){
            Toast.makeText(this, "image is nessesre", Toast.LENGTH_SHORT).show()
            return
        }

        if (emailIsEmpty && passwordIsEmpty && addressIsEmpty && phoneIsEmpty && firstNameIsEmpty){
            val email= binding.edEmailSignup.text.trim().toString()
            val password = binding.edPasswordSignup.text.trim().toString()
            val address = binding.edAddress.text.trim().toString()
            val phone = binding.edPhone.text.trim().toString()
            val fullName = binding.edFullName.text.trim().toString()
            progressDialog.isShowing
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                Toast.makeText(this@SignActivity, "User login successfully", Toast.LENGTH_SHORT).show()

                    val uid = task.result.user!!.uid
                    val user =User(email,password, fullName, address, phone, uid,url)
                    firebaseFirestore.collection("DataUser")
                        .document(uid)
                        .set(user)

//                startActivity(Intent(this@MainActivity, ShowScreen::class.java))
                progressDialog.cancel()

            }.addOnFailureListener {
                Log.d("TagForApp", "Failure")
                Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
            }
        } else if (!emailIsEmpty){
            binding.edEmailSignup.error = "The failed is Empty"
        }else if (!passwordIsEmpty){
            binding.edPasswordSignup.error = "The failed is Empty"
        }else if (!addressIsEmpty){
            binding.edPasswordSignup.error = "The failed is Empty"
        }else if (!phoneIsEmpty){
            binding.edPasswordSignup.error = "The failed is Empty"
        }else if (!firstNameIsEmpty){
            binding.edPasswordSignup.error = "The failed is Empty"
        }

    }

    private fun choseImageProfile() {
        val isGRanted =
          checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (isGRanted != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                PICK_IMAGE_REQUEST
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            listener!!.launch(intent)
        }
    }

}