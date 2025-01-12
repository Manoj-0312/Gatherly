package com.example.eventmanagementapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eventmanagementapp.databinding.ActivitySettingsBinding
import com.example.eventmanagementapp.model.User_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        fetchUserStatistics()
    }

    private fun fetchUserStatistics() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child(userId).get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User_model::class.java)
                if (user != null) {
                    binding.eventsAttendedText.text = "Events Attended: ${user.tickets?.size}"
                    binding.eventsOrganizedText.text = "Events Organized: ${user.tickets?.size}"
                    binding.totalTicketsText.text = "Total Tickets Purchased: ${user.tickets?.size}"
                }
            }.addOnFailureListener {
                binding.eventsAttendedText.text = "Error loading data"
                binding.eventsOrganizedText.text = ""
                binding.totalTicketsText.text = ""
            }
        }
    }
}
