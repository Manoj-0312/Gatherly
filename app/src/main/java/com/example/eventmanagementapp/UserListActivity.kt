package com.example.eventmanagementapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagementapp.databinding.ActivityUserListBinding
import com.example.eventmanagementapp.model.Event
import com.example.eventmanagementapp.model.User_model
import com.google.firebase.database.*

class UserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private lateinit var database: DatabaseReference
    private lateinit var usersList: MutableList<User_model>
    private lateinit var eventId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve eventId passed from EventListActivity
        eventId = intent.getStringExtra("eventId") ?: ""

        if (eventId.isEmpty()) {
            Toast.makeText(this, "Invalid event ID.", Toast.LENGTH_SHORT).show()
            return
        }

        // Initialize Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance().getReference("Users")
        usersList = mutableListOf()

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch users who booked or are attending the event
        fetchUsersForEvent()
    }

    private fun fetchUsersForEvent() {
        // Get the event users from the 'events' node
        val eventsRef = FirebaseDatabase.getInstance().getReference("events").child(eventId)
        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(eventSnapshot: DataSnapshot) {
                val event = eventSnapshot.getValue(Event::class.java)

                if (event != null) {
                    val userIds = event.bookedUsers
                    if (userIds.isNullOrEmpty()) {
                        Toast.makeText(this@UserListActivity, "No users found for this event.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    fetchUserDetails(userIds)
                } else {
                    Toast.makeText(this@UserListActivity, "Event not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, "Error fetching event data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserDetails(userIds: List<String>) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        val usersList = mutableListOf<User_model>() // Initialize the list

        for (userId in userIds) {
            Log.d("manoj", "Fetching data for userID: $userId")

            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User_model::class.java)
                    if (user != null) {
                        usersList.add(user)
                        Log.d("meg", "User fetched: $user")
                    } else {
                        Log.d("meg", "User not found for userID: $userId")
                    }

                    // Update the adapter after fetching all users
                    if (usersList.size == userIds.size) {
                        val adapter = UserListAdapter(usersList)
                        binding.recyclerView.adapter = adapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", "Error fetching user data: ${error.message}")
                    Toast.makeText(this@UserListActivity, "Error fetching user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}
