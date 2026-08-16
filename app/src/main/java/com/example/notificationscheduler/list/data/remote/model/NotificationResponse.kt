package com.example.notificationscheduler.list.data.remote.model

import com.example.notificationscheduler.core.data.model.Notification
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "xml")
data class NotificationResponse(

    @Path("notifications")
    @Element(name = "notification")
    val notifications: List<Notification> = emptyList()
)