package com.android.unio.model.events

/**
 * Represents an event with detailed information.
 *
 * @property id The unique identifier for the event.
 * @property title The title of the event.
 * @property catchy_description A short, attention-grabbing description of the event.
 * @property description A detailed description of the event.
 * @property date The date of the event in string format. Consider using a proper Date type for
 *   better handling of date values.
 * @property location The location where the event will take place.
 * @property main_type The main category or type of the event (e.g., conference, concert, etc.).
 * @property picture A URL or path to the event's main picture or image.
 */
data class Event(
    val id: String,
    val title: String,
    val catchy_description: String,
    val description: String,
    val date: String, // Consider using Date type with proper formatting
    val location: String,
    val mainType: EventType,
    val picture: String
)
