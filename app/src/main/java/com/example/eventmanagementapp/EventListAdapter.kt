package com.example.eventmanagementapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagementapp.databinding.ItemEventBinding
import com.example.eventmanagementapp.model.Event
import com.squareup.picasso.Picasso

class EventListAdapter(
    private val events: List<Event>,
    private val onViewDetailsClick: (Event) -> Unit
) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.apply {
                eventTitle.text = event.title
                eventDate.text = event.startDate
                eventLocation.text = event.location
                eventPrice.text = "₹${event.ticketPrice}"

                // Load image
                Picasso.get()
                    .load(event.imageUrl)
                    .placeholder(android.R.drawable.progress_horizontal)
                    .into(eventImageView)

                // Set click listener for the button
                viewDetailsButton.setOnClickListener {
                    onViewDetailsClick(event)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}
