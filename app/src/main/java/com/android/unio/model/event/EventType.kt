package com.android.unio.model.event

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.android.unio.R

enum class EventType(val color: Color, val textResId: Int) {

    FESTIVAL(Color(0xFF6200EE), R.string.event_type_festival), // purple
    APERITIF(Color(0xFF03DAC5), R.string.event_type_aperitif), // teal
    NIGHT_PARTY(Color(0xFFFF5722), R.string.event_type_night_party), // deep orange
    JAM(Color(0xFFFFEB3B), R.string.event_type_jam), // yellow
    NETWORKING(Color(0xFF009688), R.string.event_type_networking), // cyan
    SPORT(Color(0xFF8BC34A), R.string.event_type_sport), // light green
    TRIP(Color(0xFFE91E63), R.string.event_type_trip), // pink
    OTHER(Color.Gray, R.string.event_type_other); // default color

  }


