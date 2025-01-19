package com.example.eventmanagementapp.ui.booking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventmanagementapp.R
import com.example.eventmanagementapp.databinding.ActivityBookingBinding
import com.example.eventmanagementapp.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding
    private var event: Event? = null
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the passed event details
        event = intent.getParcelableExtra("eventDetails")

        if (event == null) {
            Toast.makeText(this, "Event data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayEventDetails()

        // Handle booking button click
        binding.bookButton.setOnClickListener {
            bookEvent()
        }

        // Handle map link click
        binding.mapLink.setOnClickListener {
            openMap(event?.mapUrl)
        }

        // Handle share button click
        binding.shareButton.setOnClickListener {
            shareEventDetails()
        }

        // Handle back button click
        binding.backButton.setOnClickListener {
            onBackPressed() // Calls the default back navigation behavior
        }
    }

    private fun displayEventDetails() {
        event?.let { event ->
            binding.eventTitle.text = event.title
            binding.eventLocation.text = event.location
            binding.eventCategory.text = "Category: ${event.category}"
            binding.eventDateTime.text = "Date: ${event.startDate} | Time: ${event.startTime}"
            binding.eventDuration.text = "Duration: ${event.duration} hours"
            binding.participantLimit.text = "Participants Allowed: ${event.participantLimit}"
            binding.eventDescription.text = event.description
            binding.eventPrice.text = "Price: ₹${event.ticketPrice}"

            // Load event image
            Picasso.get()
                .load(event.imageUrl)
                .placeholder(R.drawable.event_placeholder)
                .error(R.drawable.img)
                .fit()
                .centerCrop()
                .into(binding.eventImageView)
        }
    }

    private fun openMap(mapUrl: String?) {
        mapUrl?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        } ?: run {
            Toast.makeText(this, "Map URL not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bookEvent() {
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        event?.let { event ->
            val ticketsRef = FirebaseDatabase.getInstance().getReference("tickets")

            // Check if the user has already booked this event
            ticketsRef.orderByChild("userId")
                .equalTo(currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var alreadyBooked = false

                        for (ticketSnapshot in snapshot.children) {
                            val bookedEventId = ticketSnapshot.child("eventId").getValue(String::class.java)
                            if (bookedEventId == event.eventId) {
                                alreadyBooked = true
                                break
                            }
                        }

                        if (alreadyBooked) {
                            Toast.makeText(this@BookingActivity, "You have already booked this event!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Proceed with booking
                            val ticketId = ticketsRef.push().key
                            if (ticketId == null) {
                                Toast.makeText(this@BookingActivity, "Failed to generate ticket ID", Toast.LENGTH_SHORT).show()
                                return
                            }

                            val ticketData = mapOf(
                                "ticketId" to ticketId,
                                "userId" to currentUserId,
                                "eventId" to event.eventId,
                                "status" to "active"
                            )

                            ticketsRef.child(ticketId).setValue(ticketData).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@BookingActivity, "Booking successful!", Toast.LENGTH_SHORT).show()
                                    finish() // Optionally close the booking activity
                                } else {
                                    Toast.makeText(this@BookingActivity, "Failed to book event", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@BookingActivity, "Failed to check booking status: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun shareEventDetails() {
        event?.let { event ->
            val shareText = """
                Check out this event:
                Title: ${event.title}
                Location: ${event.location}
                Date: ${event.startDate}
                Time: ${event.startTime}
                Duration: ${event.duration} hours
                Price: ₹${event.ticketPrice}
                Description: ${event.description}
                Map: ${event.mapUrl}
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Event Details: ${event.title}")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Event Details"))
        } ?: run {
            Toast.makeText(this, "Event details not available to share", Toast.LENGTH_SHORT).show()
        }
    }
}
