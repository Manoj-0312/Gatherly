package com.example.eventmanagementapp.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagementapp.databinding.FragmentExploreBinding
import com.example.eventmanagementapp.model.Event
import com.google.android.material.chip.Chip
import com.google.firebase.database.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private var searchJob: Job? = null
    private var allEvents: List<Event> = emptyList()
    private lateinit var database: DatabaseReference
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("events")

        setupEventsRecyclerView()
        setupCategoryChips()
        setupSearchBar()
        fetchEventsFromFirebase()
    }

    private fun fetchEventsFromFirebase() {
        binding.searchProgressBar.isVisible = true
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventsList = mutableListOf<Event>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    if (event != null) {
                        eventsList.add(event)
                    }
                }
                allEvents = eventsList
                updateRecyclerView()
                binding.searchProgressBar.isVisible = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load events: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.searchProgressBar.isVisible = false
            }
        })
    }

    private fun updateRecyclerView(filteredList: List<Event> = allEvents) {
        (binding.eventsRecyclerView.adapter as? ExploreEventAdapter)?.updateEvents(filteredList)
    }

    private fun setupSearchBar() {
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ExploreEventAdapter(requireContext(), emptyList()) { event ->
                Toast.makeText(context, "Clicked: ${event.title}", Toast.LENGTH_SHORT).show()
                binding.searchView.hide()
            }
        }

        binding.searchView.editText.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300) // Debounce typing
                editable?.toString()?.let { query ->
                    if (query.isNotEmpty()) {
                        binding.searchProgressBar.isVisible = true
                        searchEvents(query)
                    }
                }
            }
        }
    }

    private fun searchEvents(query: String) {
        val filteredEvents = allEvents.filter { event ->
            event.title.contains(query, ignoreCase = true) ||
                    event.description.contains(query, ignoreCase = true) ||
                    event.location.contains(query, ignoreCase = true)
        }
        updateRecyclerView(filteredEvents)
        binding.searchProgressBar.isVisible = false
    }

    private fun setupCategoryChips() {
        val categories = listOf("Music", "Education", "Health", "Technology", "Fitness")
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setOnClickListener {
                    if (isChecked) {
                        selectedCategory = category
                        filterByCategory(category)
                    } else {
                        selectedCategory = null
                        updateRecyclerView() // Show all events
                    }
                }
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    private fun filterByCategory(category: String) {
        val filteredEvents = allEvents.filter { event ->
            event.category.equals(category, ignoreCase = true) ||
                    event.title.contains(category, ignoreCase = true) ||
                    event.description.contains(category, ignoreCase = true)
        }
        updateRecyclerView(filteredEvents)
    }

    private fun setupEventsRecyclerView() {
        binding.eventsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 1)
            adapter = ExploreEventAdapter(requireContext(), allEvents) { event ->
                Toast.makeText(context, "Clicked: ${event.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
