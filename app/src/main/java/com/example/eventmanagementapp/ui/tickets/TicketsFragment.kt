package com.example.eventmanagementapp.ui.tickets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagementapp.databinding.FragmentTicketsBinding
import com.example.eventmanagementapp.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TicketsFragment : Fragment() {

    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    private lateinit var ticketsAdapter: TicketAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        fetchTickets()
    }

    private fun setupRecyclerView() {
        ticketsAdapter = TicketAdapter()
        binding.recyclerViewTickets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ticketsAdapter
        }
    }

    private fun fetchTickets() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        database.child("Users").child(userId).child("tickets")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ticketIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    loadTicketDetails(ticketIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load tickets: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadTicketDetails(ticketIds: List<String>) {
        val ticketsList = mutableListOf<Ticket>()

        for (ticketId in ticketIds) {
            database.child("tickets").child(ticketId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.getValue(Ticket::class.java)?.let { ticketsList.add(it) }

                        if (ticketsList.size == ticketIds.size) {
                            ticketsAdapter.submitList(ticketsList)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Failed to load ticket details", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
