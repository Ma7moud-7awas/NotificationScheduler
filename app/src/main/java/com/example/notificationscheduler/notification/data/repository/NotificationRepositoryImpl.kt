package com.example.notificationscheduler.notification.data.repository

import com.example.notificationscheduler.notification.domain.model.Notification
import com.example.notificationscheduler.notification.data.local.NotificationDao
import com.example.notificationscheduler.notification.data.remote.NotificationRemoteDataSource
import com.example.notificationscheduler.notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val localDS: NotificationDao,
    private val remoteDS: NotificationRemoteDataSource
) : NotificationRepository {

    override fun getNotifications(): Flow<List<Notification>> {
        return localDS.getAllNotifications()
    }

    override fun getNotificationById(id: Int): Flow<Notification?> {
        return localDS.getNotificationById(id)
    }

    override suspend fun getAllScheduledNotifications(): List<Notification> {
        return localDS.getAllScheduledNotifications()
    }

    override suspend fun refreshNotifications() {
        val remoteNotifications = remoteDS.fetchNotifications()

        val scheduledIds = localDS.getAllScheduledNotifications().map { it.id }.toSet()
        val updatedNotifications = remoteNotifications.map {
            it.copy(isScheduled = scheduledIds.contains(it.id))
        }

        localDS.updateNotifications(updatedNotifications)
    }

    override suspend fun updateScheduledState(id: Int, isScheduled: Boolean) {
        localDS.updateScheduledState(id, isScheduled)
    }
}
