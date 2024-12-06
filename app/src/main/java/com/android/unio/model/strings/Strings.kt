package com.android.unio.model.strings

object FirestorePathsStrings {
  const val ASSOCIATION_PATH = "associations"
  const val USER_PATH = "users"
  const val EVENT_PATH = "events"
}

object StoragePathsStrings {
  const val USER_IMAGES = "images/users/"
  const val ASSOCIATION_IMAGES = "images/associations"
}

object MapStrings {
  const val EVENT_ALREADY_OCCURED = "Event has already occurred"
}

object FormatStrings {
  const val DAY_MONTH_FORMAT = "dd/MM"
  const val HOUR_MINUTE_FORMAT = "HH:mm"
  const val DAY_MONTH_YEAR_FORMAT = "dd/MM/yy"
}

object NotificationStrings {
  const val EVENT_REMINDER_CHANNEL_ID = "EventReminder"
  const val NOTIFICATION_SCHEDULER_TYPE_CANCEL = "cancel"
  const val NOTIFICATION_SCHEDULER_TYPE_CREATE = "create"
  const val NOTIFICATION_SCHEDULER_TYPE = "type"

  const val EVENT_BROADCAST_CHANNEL_ID = "EventBroadcast"
  const val EVENT_BROADCAST_NOTIFICATION_ID = 1
}
