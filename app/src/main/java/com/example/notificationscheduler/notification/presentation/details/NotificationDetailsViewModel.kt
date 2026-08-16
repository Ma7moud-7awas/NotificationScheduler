package com.example.notificationscheduler.notification.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notificationscheduler.notification.domain.model.Notification
import com.example.notificationscheduler.notification.alarm.NotificationAlarmScheduler
import com.example.notificationscheduler.notification.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationDetailsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val alarmScheduler: NotificationAlarmScheduler
) : ViewModel() {

    fun getNotificationById(id: Int): Flow<Notification?> {
        return repository.getNotificationById(id)
    }

    fun scheduleNotification(notification: Notification) {
        viewModelScope.launch {
            repository.updateScheduledState(notification.id, true)
            alarmScheduler.schedule(notification.copy(isScheduled = true))
        }
    }

    fun cancelNotification(notificationId: Int) {
        viewModelScope.launch {
            if (alarmScheduler.cancel(notificationId))
                repository.updateScheduledState(notificationId, false)
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return alarmScheduler.canScheduleExactAlarms()
    }
}
