package com.example.eventmanagementapp.model

import java.time.LocalTime

data class Booking(
    val bookingId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val ticketStatus: String = "upcoming",
    val ticketCount: Int = 1,
    val bookingDate: LocalTime?=null // Timestamp
)
