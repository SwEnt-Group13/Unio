package com.android.unio.model.events

import androidx.compose.ui.graphics.Color


enum class EventType(val color: Color, val text: String) {
    FESTIVAL(Color(0xFF6200EE), "festival"),    // purple
    APERITIF(Color(0xFF03DAC5), "aperitif"),    // teal
    NIGHT_PARTY(Color(0xFFFF5722), "night party"), // deep orange
    JAM(Color(0xFFFFEB3B), "jam"),              // yellow
    NETWORKING(Color(0xFF009688), "networking"), // cyan
    SPORT(Color(0xFF8BC34A), "sport"),          // light green
    TRIP(Color(0xFFE91E63), "trip"),            // pink
    OTHER(Color.Gray, "other");                 // default color

    companion object {
        // function to get color for a given event type as string
        fun getColorForEventType(eventType: String): Color {
            return values().find { it.name.equals(eventType, ignoreCase = true) }?.color ?: Color.Gray
        }
    }
}
