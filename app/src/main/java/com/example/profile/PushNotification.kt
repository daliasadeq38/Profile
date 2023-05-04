package com.example.profile

import com.example.profile.NotificationData

data class PushNotification(
    val data: NotificationData,
    val to: String
)