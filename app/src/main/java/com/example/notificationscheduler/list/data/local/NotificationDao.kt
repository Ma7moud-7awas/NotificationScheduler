package com.example.notificationscheduler.list.data.local

import androidx.room.*
import com.example.notificationscheduler.core.data.model.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Transaction
    suspend fun updateNotifications(notifications: List<Notification>) {
        deleteAllNotifications()
        insertNotifications(notifications)
    }
}
