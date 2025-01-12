package com.example.eventmanagementapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Ticket(
    val id: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val eventDate: String = "",         // Changed from LocalDateTime to String
    val eventLocation: String = "",
    val price: Double = 0.0,
    val status: String = "Active",
    val generatedTime: String = "",     // Changed from LocalTime to String
    val generatedDate: String = ""      // Changed from LocalDate to String
)
