package com.example.eventmanagementapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagementapp.databinding.ActivityEventListBinding
import com.example.eventmanagementapp.model.Event
import com.example.eventmanagementapp.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EventListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventListBinding
    private lateinit var database: DatabaseReference
    private lateinit var eventsList: MutableList<Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance().getReference("events")

        // Fetch the event type (attended or organized)
        val eventType = intent.getStringExtra("eventType")

        eventsList = mutableListOf() // Initialize the list

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch events from Firebase based on the event type (attended or organized)
        fetchEvents(eventType)
    }

    private fun fetchEvents(eventType: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val ticketsRef = FirebaseDatabase.getInstance().getReference("tickets")

        // Fetch events from the database
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(eventSnapshot: DataSnapshot) {
                eventsList.clear() // Clear previous data in the list

                if (eventType == "organized") {
                    // Filter events created by the logged-in user
                    for (event in eventSnapshot.children.mapNotNull { it.getValue(Event::class.java) }) {
                        if (event.createdBy == userId) {
                            eventsList.add(event)
                        }
                    }

                    // Update the adapter after filtering
                    val adapter = EventListAdapter(eventsList) { event ->
                        // Handle the event item click (show event details)
                        showUsersForEvent(event)
                    }
                    binding.recyclerView.adapter = adapter
                } else if (eventType == "attended") {
                    // Fetch all tickets to filter attended events
                    ticketsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(ticketSnapshot: DataSnapshot) {
                            val userCompletedTickets = ticketSnapshot.children.mapNotNull { it.getValue(Ticket::class.java) }
                                .filter { it.status.lowercase() == "completed" && it.eventId != null }

                            // Get the list of event IDs for the user's completed tickets
                            val userEventIds = userCompletedTickets.map { it.eventId }

                            for (event in eventSnapshot.children.mapNotNull { it.getValue(Event::class.java) }) {
                                // Add events attended by the user (with completed tickets)
                                if (event.bookedUsers.contains(userId) && userEventIds.contains(event.eventId)) {
                                    eventsList.add(event)
                                }
                            }

                            // Update the adapter after filtering
                            val adapter = EventListAdapter(eventsList) { event ->
                                // Handle the event item click (show event details)
                                showUsersForEvent(event)
                            }
                            binding.recyclerView.adapter = adapter
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@EventListActivity, "Error fetching tickets: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    // If eventType is null or unsupported
                    Toast.makeText(this@EventListActivity, "Invalid event type.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EventListActivity, "Error fetching events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Method to show users for the event
    private fun showUsersForEvent(event: Event) {
        // Assuming you navigate to another activity to display event details and users
        val intent = Intent(this, UserListActivity::class.java)
        intent.putExtra("eventId", event.eventId) // Passing the event ID
        startActivity(intent)
    }
}
