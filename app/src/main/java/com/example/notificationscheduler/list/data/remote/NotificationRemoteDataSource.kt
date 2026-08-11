package com.example.notificationscheduler.list.data.remote

import com.example.notificationscheduler.core.model.Notification
import javax.inject.Inject

interface RemoteDataSource {
    suspend fun fetchNotifications(): List<Notification>
}

class RemoteDataSourceImpl @Inject constructor(
    private val notificationService: NotificationService
) : RemoteDataSource {

    override suspend fun fetchNotifications(): List<Notification> {
        return try {
            notificationService.fetchNotifications().notifications
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
