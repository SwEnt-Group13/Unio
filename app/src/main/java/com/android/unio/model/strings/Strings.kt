package com.android.unio.model.strings

object FirestorePathsStrings {
  const val ASSOCIATION_PATH = "associations"
  const val USER_PATH = "users"
  const val EVENT_PATH = "events"
  const val EVENT_USER_PICTURES_PATH = "eventUserPictures"
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
}

object TextLengthSamples {
  // 25 characters
  const val SMALL = "A wonderful serenity has."

  // 90 characters
  const val MEDIUM =
      "A wonderful serenity has taken possession of my entire soul, like these sweet mornings of."

  // 280 characters
  const val LARGE =
      "A wonderful serenity has taken possession of my entire soul, like these sweet mornings of spring which " +
          "I enjoy with my whole heart. I am alone, and feel the charm of existence in this spot, " +
          "which was created for the bliss of souls like mine. I am so happy, my dear friend, so abso"
}
