package com.example.eventmanagementapp.model

import java.time.LocalDate
import java.time.LocalTime
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Event(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val mapUrl: String = "",
    val startDate: String? = null, //2025-01-25
    val startTime: String?=null, // 24 hrs 17:30
    val duration: Int = 0, // Provide a default value
    val createdBy: String = "",
    val imageUrl:String = "",
    val category: String = "",
    val ticketPrice: Double = 0.0,
    val participantLimit:Int = 100,
    val status: String = "active",
    val bookedUsers: List<String> = emptyList()
): Parcelable