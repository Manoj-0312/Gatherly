package com.example.eventmanagementapp.model

data class User_model(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNum: String = "",
    val favoriteEvents: List<String> = emptyList(), // List of event IDs
    val tickets: List<String> = emptyList(), // List of ticket IDs
    val registrationDate: Long = System.currentTimeMillis() // Automatically record signup timestamp
)
