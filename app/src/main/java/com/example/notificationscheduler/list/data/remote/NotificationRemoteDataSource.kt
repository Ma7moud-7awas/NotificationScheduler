package com.example.notificationscheduler.list.data.remote

import com.example.notificationscheduler.core.data.model.Notification
import javax.inject.Inject

class NotificationRemoteDataSource @Inject constructor(
    private val notificationService: NotificationService
) {

    suspend fun fetchNotifications(): List<Notification> {
        return notificationService.fetchNotifications().notifications
    }
}
