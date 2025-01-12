package com.example.eventmanagementapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventmanagementapp.databinding.ActivityAddEventBinding
import com.example.eventmanagementapp.model.Event
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AddEvent : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityAddEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("events")

        // Set up DatePickerDialog for the event date input field
        binding.eventDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        binding.saveEventButton.setOnClickListener {
            saveEvent()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create and show the DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Format the selected date to match "yyyy-MM-dd" format
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth)
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                binding.eventDateEditText.setText(selectedDate.format(dateFormatter))
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun saveEvent() {
        val title = binding.eventNameEditText.text.toString().trim()
        val description = binding.eventDescriptionEditText.text.toString().trim()
        val date = binding.eventDateEditText.text.toString().trim()
        val location = binding.eventLocationEditText.text.toString().trim()
        val price = binding.eventPriceEditText.text.toString().trim()
        val imageUrl = binding.eventImageUrlEditText.text.toString().trim()

        // Validate input fields
        if (title.isEmpty()) {
            Toast.makeText(this, "Event name is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Event description is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Event date is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "Event location is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (price.isEmpty()) {
            Toast.makeText(this, "Ticket price is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (imageUrl.isEmpty()) {
            Toast.makeText(this, "Event image URL is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate price input to ensure it's a valid number
        val priceValue = try {
            price.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        // Create an Event object with the validated data
        val eventId = database.push().key ?: return
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val eventDate = LocalDate.parse(date, dateFormatter)

        val event = Event(
            eventId = eventId,
            title = title,
            description = description,
            startDate = eventDate.toString(),
            location = location,
            imageUrl = imageUrl,
            ticketPrice = priceValue,
            participantLimit = 100,
            duration = 2,
            category = "music"
        )

        // Save the event to the Firebase Realtime Database
        database.child(eventId).setValue(event).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after saving
            } else {
                Toast.makeText(this, "Failed to save event: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
