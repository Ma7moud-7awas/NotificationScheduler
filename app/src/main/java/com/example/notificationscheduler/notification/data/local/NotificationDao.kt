package com.example.notificationscheduler.notification.data.local

import androidx.room.*
import com.example.notificationscheduler.notification.domain.model.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    fun getNotificationById(id: Int): Flow<Notification?>

    @Query("SELECT * FROM notifications WHERE isScheduled = 1")
    suspend fun getAllScheduledNotifications(): List<Notification>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("UPDATE notifications SET isScheduled = :isScheduled WHERE id = :id")
    suspend fun updateScheduledState(id: Int, isScheduled: Boolean)

    @Transaction
    suspend fun updateNotifications(notifications: List<Notification>) {
        deleteAllNotifications()
        insertNotifications(notifications)
    }
}
