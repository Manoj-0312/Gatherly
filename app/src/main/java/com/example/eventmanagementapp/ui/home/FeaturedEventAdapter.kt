package com.example.eventmanagementapp.ui.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagementapp.R
import com.example.eventmanagementapp.databinding.ItemFeaturedEventBinding
import com.example.eventmanagementapp.model.Event
import com.example.eventmanagementapp.ui.booking.BookingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class FeaturedEventAdapter(
    private val context: Context,
    private val events: List<Event>
) : RecyclerView.Adapter<FeaturedEventAdapter.FeaturedEventViewHolder>() {

    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    inner class FeaturedEventViewHolder(
        private val binding: ItemFeaturedEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                // Set event details
                eventTitle.text = event.title
                eventLocation.text = event.location
                eventPrice.text = "₹${event.ticketPrice}"

                // Load event image using Picasso
                Picasso.get()
                    .load(event.imageUrl)
                    .placeholder(R.drawable.event_placeholder)
                    .error(R.drawable.img)
                    .fit()
                    .centerCrop()
                    .into(eventImageView)

                // Update the like button state based on user's favorite events
                updateLikeButton(event.eventId)

                // Handle favorite button click
                likeButton.setOnClickListener {
                    toggleFavorite(event.eventId)
                }

                // Handle card click
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedEventViewHolder {
        val binding = ItemFeaturedEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeaturedEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeaturedEventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size
}
