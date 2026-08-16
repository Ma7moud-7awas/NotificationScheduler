package com.example.notificationscheduler.list.domain

import com.example.notificationscheduler.core.data.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    fun getNotifications(): Flow<List<Notification>>

    fun getNotificationById(id: Int): Flow<Notification?>

    suspend fun getAllScheduledNotifications(): List<Notification>

    suspend fun refreshNotifications()

    suspend fun updateScheduledState(id: Int, isScheduled: Boolean)
}
