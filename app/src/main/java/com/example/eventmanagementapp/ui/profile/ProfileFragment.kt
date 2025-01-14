package com.example.eventmanagementapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.eventmanagementapp.EventListActivity
import com.example.eventmanagementapp.LoginActivity
import com.example.eventmanagementapp.SettingsActivity
import com.example.eventmanagementapp.UpdateProfileActivity
import com.example.eventmanagementapp.databinding.FragmentProfileBinding
import com.example.eventmanagementapp.model.User_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.eventmanagementapp.model.Event
import com.example.eventmanagementapp.model.Ticket

class ProfileFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Adding list variables for the events
    private val attendedEventsList = mutableListOf<Event>()
    private val organizedEventsList = mutableListOf<Event>()
    private val totalTicketsList = mutableListOf<Ticket>()
    private var ticket_size = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference()

        fetchUserDetails()
        setupActionButtons()
    }

    private fun fetchUserDetails() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child("Users").child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User_model::class.java)
                    if (user != null) {
                        displayUserDetails(user)
                        ticket_size = user.tickets.size
                        fetchUserEvents(userId) // Fetch events after fetching user details
                    } else {
                        Toast.makeText(requireContext(), "User details not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserEvents(userId: String) {
        // Fetch events organized by the user
        val eventsRef = database.child("events")
        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                organizedEventsList.clear() // Reset the list of organized events
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    if (event != null && event.createdBy == userId) {
                        organizedEventsList.add(event) // Add event to organized events list
                    }
                }
                updateEventUI() // Update UI after fetching organized events
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching organized events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch attended events (events where the user has booked a ticket)
        eventsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                attendedEventsList.clear() // Reset the list of attended events

                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    if (event != null && event.bookedUsers.contains(userId) && event.status == "completed") {
                        attendedEventsList.add(event) // Add event to attended events list
                    }
                }
                updateEventUI() // Update UI after fetching attended events
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching attended events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch tickets to count total tickets created by the user
        val ticketsRef = database.child("tickets")
        ticketsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalTicketsList.clear() // Reset the list of total tickets
                var activeTickets = 0
                var cancelledTickets = 0

                // Fetch the user's ticket list
                val userRef = database.child("Users").child(userId)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        // Get the list of ticket IDs for the user
                        val userTickets = userSnapshot.child("tickets").children.map { it.value.toString() }

                        // Iterate through all tickets to check if they belong to the user
                        for (ticketSnapshot in snapshot.children) {
                            val ticket = ticketSnapshot.getValue(Ticket::class.java)
                            if (ticket != null && ticket.eventId != null) {
                                // Check if this ticket belongs to the user
                                if (userTickets.contains(ticket.id)) {
                                    // Count active or completed tickets
                                    if (ticket.status == "active" || ticket.status == "completed") {
                                        activeTickets++
                                    } else if (ticket.status == "cancelled") {
                                        cancelledTickets++
                                    }

                                    // Add ticket to the total tickets list
                                    totalTicketsList.add(ticket)
                                }
                            }
                        }
                        updateEventUI() // Update UI after fetching total tickets
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error fetching user tickets: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching tickets: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }




    private fun updateEventUI() {
        binding.apply {
            // Update counts for events attended and organized
            eventsAttended.text = attendedEventsList.size.toString()
            eventsOrganized.text = organizedEventsList.size.toString()

            // Update total tickets
            totalTickets.text = ticket_size.toString()
        }
    }

    private fun displayUserDetails(user: User_model) {
        binding.apply {
            userName.text = user.name
            userEmail.text = user.email
            userPhone.text = user.phoneNum
        }
    }

    private fun setupActionButtons() {
        binding.apply {
            editProfileButton.setOnClickListener {
                // Navigate to edit profile screen
                val intent = Intent(requireActivity(), UpdateProfileActivity::class.java)
                startActivity(intent)
            }

            settingsButton.setOnClickListener {
                // Navigate to settings screen
                val intent = Intent(requireActivity(), SettingsActivity::class.java)
                startActivity(intent)
            }

            logoutButton.setOnClickListener {
                firebaseAuth.signOut()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }

            // Navigate to event list (attended events)
            eventsAttended.setOnClickListener {
                val intent = Intent(requireActivity(), EventListActivity::class.java)
                intent.putExtra("eventType", "attended")
                startActivity(intent)
            }

            // Navigate to event list (organized events)
            eventsOrganized.setOnClickListener {
                val intent = Intent(requireActivity(), EventListActivity::class.java)
                intent.putExtra("eventType", "organized")
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
