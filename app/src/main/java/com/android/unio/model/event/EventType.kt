package com.android.unio.model.event

import androidx.compose.ui.graphics.Color
import com.android.unio.ui.theme.eventTypeAperitif
import com.android.unio.ui.theme.eventTypeFestival
import com.android.unio.ui.theme.eventTypeJam
import com.android.unio.ui.theme.eventTypeNetworking
import com.android.unio.ui.theme.eventTypeNightParty
import com.android.unio.ui.theme.eventTypeOther
import com.android.unio.ui.theme.eventTypeSport
import com.android.unio.ui.theme.eventTypeTrip

enum class EventType(val color: Color, val text: String) {
  FESTIVAL(eventTypeFestival, "festival"),
  APERITIF(eventTypeAperitif, "aperitif"),
  NIGHT_PARTY(eventTypeNightParty, "night party"),
  JAM(eventTypeJam, "jam"),
  NETWORKING(eventTypeNetworking, "networking"),
  SPORT(eventTypeSport, "sport"),
  TRIP(eventTypeTrip, "trip"),
  OTHER(eventTypeOther, "other"); // Default color

  companion object {
    // Function to get color for a given event type as string
    fun getColorForEventType(eventType: String): Color {
      return values().find { it.name.equals(eventType, ignoreCase = true) }?.color ?: eventTypeOther
    }
  }
}
