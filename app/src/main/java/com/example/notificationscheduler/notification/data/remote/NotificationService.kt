package com.example.notificationscheduler.notification.data.remote

import com.example.notificationscheduler.notification.data.remote.model.NotificationResponse
import retrofit2.http.GET

interface NotificationService {

    @GET("localalerts.php")
    suspend fun fetchNotifications(): NotificationResponse
}