package com.example.notificationscheduler.core.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import java.io.Serializable

@Xml(name = "notification")
data class Notification(
    @PropertyElement(name = "id")
    val id: Int = 0,
    @PropertyElement(name = "title")
    val title: String = "",
    @PropertyElement(name = "timeInSeconds")
    val timeInSeconds: Long = 0,
) : Serializable
