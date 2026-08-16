package com.example.notificationscheduler.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notificationscheduler.core.data.model.Notification
import com.example.notificationscheduler.list.data.local.NotificationDao

@Database(entities = [Notification::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
