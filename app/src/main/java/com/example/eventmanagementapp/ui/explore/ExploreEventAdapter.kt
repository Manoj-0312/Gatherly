package com.example.eventmanagementapp.ui.explore

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagementapp.R
import com.example.eventmanagementapp.databinding.ItemExploreEventBinding
import com.example.eventmanagementapp.model.Event
import com.example.eventmanagementapp.ui.booking.BookingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ExploreEventAdapter(
    private val context: Context,
    private var events: List<Event>,
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<ExploreEventAdapter.ExploreEventViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreEventViewHolder {
        val binding = ItemExploreEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExploreEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExploreEventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    inner class ExploreEventViewHolder(
        private val binding: ItemExploreEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                eventTitle.text = event.title
                eventDate.text = event.startDate.toString() // Assuming `event.date` is a Date object
                eventLocation.text = event.location
                eventPrice.text = "₹${event.ticketPrice}"

                // Load event image using Picasso
                Picasso.get()
                    .load(event.imageUrl) // Load image URL from the event object
                    .placeholder(R.drawable.event_placeholder) // Placeholder image while loading
                    .error(R.drawable.img) // Error image if loading fails
                    .fit() // Fit the image into ImageView bounds
                    .centerCrop() // Center and crop the image to fill the ImageView
                    .into(eventImageView) // Bind the loaded image to the ImageView

                // Handle like button click
                likeButton.setOnClickListener {
                    toggleFavorite(event.eventId)
                }

                // Update the like button state when binding the event
                updateLikeButton(event.eventId)

                root.setOnClickListener {
                    val intent = Intent(context, BookingActivity::class.java)
                    intent.putExtra("eventDetails", event) // Pass the event object
                    context.startActivity(intent)
                }
            }
        }


        private fun updateLikeButton(eventId: String) {
            if (currentUserId == null) return

            val userRef = dbRef.child(currentUserId).child("favoriteEvents")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Use GenericTypeIndicator to handle the List type
                    val genericTypeIndicator = object : GenericTypeIndicator<List<String>>() {}
                    val favoriteEvents = snapshot.getValue(genericTypeIndicator) ?: emptyList()
                    val isLiked = favoriteEvents.contains(eventId)

                    val likeIcon = if (isLiked) {
                        R.drawable.ic_favourite
                    } else {
                        R.drawable.ic_favourite_border
                    }
                    binding.likeButton.setImageResource(likeIcon)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch like status: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun toggleFavorite(eventId: String) {
            if (currentUserId == null) {
                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val userRef = dbRef.child(currentUserId).child("favoriteEvents")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Use GenericTypeIndicator to handle the List type
                    val genericTypeIndicator = object : GenericTypeIndicator<MutableList<String>>() {}
                    val favoriteEvents = snapshot.getValue(genericTypeIndicator) ?: mutableListOf()
                    val isLiked = favoriteEvents.contains(eventId)

                    if (isLiked) {
                        favoriteEvents.remove(eventId)
                    } else {
                        favoriteEvents.add(eventId)
                    }

                    userRef.setValue(favoriteEvents).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val likeIcon = if (isLiked) R.drawable.ic_favourite_border else R.drawable.ic_favourite
                            binding.likeButton.setImageResource(likeIcon)
                            Toast.makeText(context, "Favorite updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update favorite", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch user data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }
}
