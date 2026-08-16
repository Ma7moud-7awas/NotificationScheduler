package com.example.notificationscheduler.notification.data.remote

import com.example.notificationscheduler.notification.domain.model.Notification
import javax.inject.Inject

class NotificationRemoteDataSource @Inject constructor(
    private val notificationService: NotificationService
) {

    suspend fun fetchNotifications(): List<Notification> {
        return notificationService.fetchNotifications().notifications
    }
}
