package com.example.eventmanagementapp.ui.tickets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagementapp.R
import com.example.eventmanagementapp.databinding.ItemTicketBinding
import com.example.eventmanagementapp.model.Ticket

class TicketAdapter : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    private val tickets = mutableListOf<Ticket>()

    fun submitList(ticketList: List<Ticket>) {
        tickets.clear()
        tickets.addAll(ticketList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position])
    }

    override fun getItemCount(): Int = tickets.size

    class TicketViewHolder(private val binding: ItemTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ticket: Ticket) {
            binding.textViewEventTitle.text = ticket.eventTitle
            binding.textViewEventDate.text = ticket.eventDate
            binding.textViewEventLocation.text = ticket.eventLocation
            binding.textViewPrice.text = "$${ticket.price}"

            // Set icon and color based on ticket status
            when (ticket.status) {
                "Completed" -> {
                    binding.imageViewStatusIcon.setImageResource(R.drawable.ic_completed)
                    binding.imageViewStatusIcon.setColorFilter(
                        itemView.context.getColor(R.color.status_active),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                "Active" -> {
                    binding.imageViewStatusIcon.setImageResource(R.drawable.ic_active)
                    binding.imageViewStatusIcon.setColorFilter(
                        itemView.context.getColor(R.color.status_completed),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                "Cancelled" -> {
                    binding.imageViewStatusIcon.setImageResource(R.drawable.ic_cancelled)
                    binding.imageViewStatusIcon.setColorFilter(
                        itemView.context.getColor(R.color.status_cancelled),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
            }
        }
    }
}
