package com.example.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.profile.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    lateinit var binding:ActivityProfileBinding
    val data = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var isEdit : Boolean? = false
    var image = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getProfileData()
        binding.btnEdit.setOnClickListener {
            if (isEdit == false){
                binding.edPEmail.isEnabled = true
                binding.edPEmail.isEnabled = true
                binding.edPAddress.isEnabled = true
                binding.edPPhone.isEnabled = true
                binding.edPFullName.isEnabled = true
                isEdit = true
            }else {
                binding.edPEmail.isEnabled = false
                binding.edPEmail.isEnabled = false
                binding.edPAddress.isEnabled = false
                binding.edPPhone.isEnabled = false
                binding.edPFullName.isEnabled = false
                isEdit = false





                val email = binding.edPEmail.text.toString()
                val password =binding.edPEmail.text.toString()
                val address = binding.edPAddress.text.toString()
                val phone = binding.edPPhone.text.toString()
                val fullName = binding.edPFullName.text.toString()

                val user = User(email,password,fullName,address,phone,auth.currentUser!!.uid,image)
                data.collection("DataUser").document(auth.currentUser!!.uid)
                    .set(user)
            }
        }
    }

    private fun getProfileData() {

        data.collection("DataUser")
            .document(auth.currentUser!!.uid)
            .get().addOnSuccessListener {
                val email = it.getString("email")
                val password = it.getString("password")
                val address = it.getString("address")
                val phone = it.getString("phone")
                val fullName = it.getString("fullName")
                val imageProfile = it.getString("imageProfile")

                image = imageProfile.toString()
                binding.edPEmail.setText(email)
                binding.edPEmail.setText(password)
                binding.edPAddress.setText(address)
                binding.edPPhone.setText(phone)
                binding.edPFullName.setText(fullName)

                Picasso.with(this).load(imageProfile).into(binding.imageProfileP)
            }
    }
}