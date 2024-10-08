package com.android.unio.model.events

import androidx.compose.ui.graphics.Color

// class for event types and their corresponding colors
data class EventTypeColor(val type: String, val color: Color)

// list of event types with their associated colors
val eventTypeColors =
    listOf(
        EventTypeColor("festival", Color(0xFF6200EE)), // purple
        EventTypeColor("apéro", Color(0xFF03DAC5)), // teal
        EventTypeColor("soirée", Color(0xFFFF5722)), // deep orange
        EventTypeColor("jam", Color(0xFFFFEB3B)), // yellow
        EventTypeColor("networking", Color(0xFF009688)), // cyan
        EventTypeColor("sport", Color(0xFF8BC34A)), // light green
        EventTypeColor("trip", Color(0xFFE91E63)), // pink
        EventTypeColor("Other", Color.Gray) // default color
        )

// get color for a given event type
fun getColorForEventType(eventType: String): Color {
  return eventTypeColors.find { it.type == eventType }?.color ?: Color.Gray
}
