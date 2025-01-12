package com.example.eventmanagementapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventmanagementapp.databinding.ActivityUpdateProfileBinding
import com.example.eventmanagementapp.model.User_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        fetchUserDetails()

        binding.updateButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun fetchUserDetails() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User_model::class.java)
                if (user != null) {
                    binding.nameEditText.setText(user.name)
                    binding.emailEditText.setText(user.email)
                    binding.phoneEditText.setText(user.phoneNum)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error fetching user details: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val updatedName = binding.nameEditText.text.toString()
            val updatedEmail = binding.emailEditText.text.toString()
            val updatedPhone = binding.phoneEditText.text.toString()

            if (updatedName.isNotEmpty() && updatedEmail.isNotEmpty() && updatedPhone.isNotEmpty()) {
                val updatedUser = User_model(userId, updatedName, updatedEmail, updatedPhone)

                database.child(userId).setValue(updatedUser).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error updating profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
