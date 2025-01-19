package com.example.eventmanagementapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagementapp.R
import com.squareup.picasso.Picasso

data class FavoriteEvent(val id:String , val title: String, val date: String, val imageUrl: String)

class FavoriteEventAdapter(
    private val context: Context,
    private val events: List<FavoriteEvent>,
    private val onEventClick: (FavoriteEvent) -> Unit
) : RecyclerView.Adapter<FavoriteEventAdapter.FavoriteEventViewHolder>() {

    inner class FavoriteEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
        private val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        private val eventDate: TextView = itemView.findViewById(R.id.eventDate)

        fun bind(event: FavoriteEvent) {
            eventTitle.text = event.title
            eventDate.text = event.date

            // Load the image using Picasso
            Picasso.get()
                .load(event.imageUrl)
                .placeholder(R.drawable.img) // Default placeholder image
                .error(R.drawable.img) // Fallback image in case of error
                .into(eventImage)

            // Set the click listener for the item
            itemView.setOnClickListener { onEventClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_favorite_event, parent, false)
        return FavoriteEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteEventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}
