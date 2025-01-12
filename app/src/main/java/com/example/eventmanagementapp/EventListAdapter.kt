package com.example.eventmanagementapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagementapp.databinding.ItemEventBinding
import com.example.eventmanagementapp.model.Event
import com.squareup.picasso.Picasso

class EventListAdapter(
    private val events: List<Event>
) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    // ViewHolder class to hold individual event item views
    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.apply {
                // Set event data to the views in the item layout
                eventTitle.text = event.title
                eventDate.text = event.startDate
                eventLocation.text = event.location
                eventPrice.text = "$${event.ticketPrice}"

                // Load the image using Picasso
                Picasso.get()
                    .load(event.imageUrl) // Load image URL
                    .placeholder(android.R.drawable.progress_horizontal) // Placeholder while loading
                    .into(eventImageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = events.size
}
