package com.android.unio.model.event

import androidx.compose.ui.graphics.Color
import com.android.unio.model.strings.EventTypeStrings
import com.android.unio.ui.theme.eventTypeAperitif
import com.android.unio.ui.theme.eventTypeFestival
import com.android.unio.ui.theme.eventTypeJam
import com.android.unio.ui.theme.eventTypeNetworking
import com.android.unio.ui.theme.eventTypeNightParty
import com.android.unio.ui.theme.eventTypeOther
import com.android.unio.ui.theme.eventTypeSport
import com.android.unio.ui.theme.eventTypeTrip

enum class EventType(val color: Color, val text: String) {
  FESTIVAL(eventTypeFestival, EventTypeStrings.FESTIVAL),
  APERITIF(eventTypeAperitif, EventTypeStrings.APERITIF),
  NIGHT_PARTY(eventTypeNightParty, EventTypeStrings.NIGHT_PARTY),
  JAM(eventTypeJam, EventTypeStrings.JAM),
  NETWORKING(eventTypeNetworking, EventTypeStrings.NETWORKING),
  SPORT(eventTypeSport, EventTypeStrings.SPORT),
  TRIP(eventTypeTrip, EventTypeStrings.TRIP),
  OTHER(eventTypeOther, EventTypeStrings.OTHER);  // default

  companion object {
    fun getColorForEventType(eventType: String): Color {
      return values().find { it.name.equals(eventType, ignoreCase = true) }?.color ?: eventTypeOther
    }
  }
}
