package com.example.notificationscheduler.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notificationscheduler.notification.domain.model.Notification
import com.example.notificationscheduler.notification.data.local.NotificationDao

@Database(entities = [Notification::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
