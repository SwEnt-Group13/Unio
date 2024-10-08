package com.android.unio.model.events

data class Event(
    val id: String,
    val title: String,
    val catchy_description: String,
    val description: String,
    val date: String, // Consider using Date type with proper formatting
    val location: String,
    val main_type: String,
    val picture: String
)
