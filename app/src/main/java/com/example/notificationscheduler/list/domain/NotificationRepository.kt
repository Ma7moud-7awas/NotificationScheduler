package com.example.notificationscheduler.list.domain

import com.example.notificationscheduler.core.data.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    fun getNotifications(): Flow<List<Notification>>

    suspend fun refreshNotifications()
}
