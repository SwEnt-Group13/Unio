package com.android.unio.model.events

import androidx.compose.ui.graphics.Color

enum class EventType(val color: Color) {
    FESTIVAL(Color(0xFF6200EE)),    // purple
    APERITIF(Color(0xFF03DAC5)),    // teal
    NIGHT_PARTY(Color(0xFFFF5722)), // deep orange
    JAM(Color(0xFFFFEB3B)),         // yellow
    NETWORKING(Color(0xFF009688)),  // cyan
    SPORT(Color(0xFF8BC34A)),       // light green
    TRIP(Color(0xFFE91E63)),        // pink
    OTHER(Color.Gray);              // default color

    companion object {

        fun getColorForEventType(eventType: String): Color {
            return values().find { it.name.equals(eventType, ignoreCase = true) }?.color ?: Color.Gray
        }
    }
}


// val color = EventType.getColorForEventType("festival")
