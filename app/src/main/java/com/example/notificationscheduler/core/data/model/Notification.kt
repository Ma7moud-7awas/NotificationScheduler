package com.example.notificationscheduler.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import java.io.Serializable

@Xml(name = "notification")
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey
    @PropertyElement(name = "id")
    val id: Int = 0,
    @PropertyElement(name = "title")
    val title: String = "",
    @PropertyElement(name = "timeInSeconds")
    val timeInSeconds: Long = 0,
) : Serializable
