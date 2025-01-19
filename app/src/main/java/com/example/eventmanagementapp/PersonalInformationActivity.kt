package com.example.eventmanagementapp
import com.example.eventmanagementapp.ui.booking.BookingActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagementapp.adapter.FavoriteEvent
import com.example.eventmanagementapp.adapter.FavoriteEventAdapter
import com.example.eventmanagementapp.databinding.ActivityPersonalInformationBinding
import com.example.eventmanagementapp.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PersonalInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInformationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val favoriteEventList = mutableListOf<FavoriteEvent>()
    private lateinit var adapter: FavoriteEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Back button setup
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.editButton.setOnClickListener{
            val intent = Intent(this, UpdateProfileActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        setupRecyclerView()

        // Fetch and display user data
        fetchUserData()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteEventAdapter(this, favoriteEventList) { event ->
            val eventsReference = FirebaseDatabase.getInstance().getReference("events")

            // Fetch the full event details from the database using the event ID
            eventsReference.child(event.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Map the snapshot to the Event model
                        val fullEvent = snapshot.getValue(Event::class.java)
                        if (fullEvent != null) {
                            // Pass the full event object to BookingActivity
                            val intent = Intent(this@PersonalInformationActivity, BookingActivity::class.java)
                            intent.putExtra("eventDetails", fullEvent)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@PersonalInformationActivity, "Event details are incomplete.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@PersonalInformationActivity, "Event not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PersonalInformationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.favoriteEventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.favoriteEventsRecyclerView.adapter = adapter
    }

    private fun fetchUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        val userEmail = snapshot.child("email").getValue(String::class.java) ?: "Unknown"
                        val userPhone = snapshot.child("phoneNum").getValue(String::class.java) ?: "Unknown"
                        val registrationDate = snapshot.child("registrationDate").getValue(Long::class.java) ?: 0L
                        val favoriteEventIds = snapshot.child("favoriteEvents").children.mapNotNull { it.getValue(String::class.java) }

                        // Set user details
                        binding.userName.text = userName
                        binding.userEmail.text = userEmail
                        binding.userPhone.text = userPhone
                        binding.registrationDate.text = "Joined on: " + android.text.format.DateFormat.format("dd MMM yyyy", registrationDate)

                        // Fetch favorite events
                        fetchFavoriteEvents(favoriteEventIds)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PersonalInformationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchFavoriteEvents(favoriteEventIds: List<String>) {
        if (favoriteEventIds.isEmpty()) {
            binding.favoriteEventsRecyclerView.isVisible = false
            binding.noFavoriteEventsMessage.isVisible = true
            return
        }

        val eventsReference = FirebaseDatabase.getInstance().getReference("events")
        for (eventId in favoriteEventIds) {
            eventsReference.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val title = snapshot.child("title").getValue(String::class.java) ?: "Unknown Event"
                        val date = snapshot.child("startDate").getValue(String::class.java) ?: "Unknown Date"
                        val imageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""

                        // Add to favorite event list with the ID included
                        favoriteEventList.add(FavoriteEvent(id = eventId, title = title, date = date, imageUrl = imageUrl))
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PersonalInformationActivity, "Error fetching event details: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
