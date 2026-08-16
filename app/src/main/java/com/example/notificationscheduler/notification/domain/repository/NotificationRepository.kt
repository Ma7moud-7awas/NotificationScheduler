package com.example.notificationscheduler.notification.domain.repository

import com.example.notificationscheduler.notification.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    fun getNotifications(): Flow<List<Notification>>

    fun getNotificationById(id: Int): Flow<Notification?>

    suspend fun getAllScheduledNotifications(): List<Notification>

    suspend fun refreshNotifications()

    suspend fun updateScheduledState(id: Int, isScheduled: Boolean)
}