package com.example.eventmanagementapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagementapp.AddEvent
import com.example.eventmanagementapp.databinding.FragmentHomeBinding
import com.example.eventmanagementapp.model.Event
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalTime

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFeaturedEvents()
        setupUpcomingEvents()
        setupCreateEventButton()
    }

    private fun setupFeaturedEvents() {
        val database = FirebaseDatabase.getInstance().getReference("events")
        val featuredEvents = mutableListOf<Event>()

        database.orderByChild("ticketPrice").limitToFirst(3)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    featuredEvents.clear()
                    for (eventSnapshot in snapshot.children) {
                        val event = eventSnapshot.getValue(Event::class.java)
                        if (event != null) {
                            featuredEvents.add(event)
                        }
                    }

                    // Set up the adapter for the featured events ViewPager
                    binding.featuredEventsViewPager.adapter = FeaturedEventAdapter(
                        requireContext(), // Pass the context
                        featuredEvents
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Failed to load featured events: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupUpcomingEvents() {
        val database = FirebaseDatabase.getInstance().getReference("events")
        val upcomingEvents = mutableListOf<Event>()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                upcomingEvents.clear()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    if (event != null) {
                        // Parse startTime if present
                        val startTimeString = eventSnapshot.child("startTime").getValue(String::class.java)
                        val startTime: LocalTime? = startTimeString?.let { LocalTime.parse(it) }

                        // Update the event object with parsed startTime
                        val updatedEvent = event.copy(startTime = startTime.toString())
                        upcomingEvents.add(updatedEvent)
                    }
                }

                // Update RecyclerView with fetched data
                binding.upcomingEventsRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.upcomingEventsRecyclerView.adapter = UpcomingEventAdapter(
                    context = requireContext(),
                    events = upcomingEvents,
                    onEventClick = { event ->
                        Toast.makeText(context, "Clicked: ${event.title}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to load upcoming events: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupCreateEventButton() {
        binding.addEventFab.setOnClickListener {
            val intent = Intent(requireContext(), AddEvent::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
