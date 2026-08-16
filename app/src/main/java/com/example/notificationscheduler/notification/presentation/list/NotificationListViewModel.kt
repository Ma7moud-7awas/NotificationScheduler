package com.example.notificationscheduler.notification.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notificationscheduler.notification.domain.model.Notification
import com.example.notificationscheduler.core.presentation.model.UiState
import com.example.notificationscheduler.notification.alarm.NotificationAlarmScheduler
import com.example.notificationscheduler.notification.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val notificationRepo: NotificationRepository,
    private val alarmScheduler: NotificationAlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Notification>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Notification>>> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepo.getNotifications().collectLatest { notifications ->
                if (notifications.isNotEmpty())
                    _uiState.update { UiState.Success(notifications) }
            }
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            updateEmptyState(UiState.Loading)

            try {
                notificationRepo.refreshNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
                updateEmptyState(UiState.Error(e.message, e))
            }
        }
    }

    private fun updateEmptyState(newState: UiState<List<Notification>>) {
        val noData = _uiState.value.let { state ->
            state !is UiState.Success || state.data.isEmpty()
        }

        if (noData) {
            _uiState.update { newState }
        }
    }

    fun cancelAllNotifications() {
        viewModelScope.launch {
            notificationRepo
                .getAllScheduledNotifications()
                .forEach {
                    alarmScheduler.cancel(it.id)
                    notificationRepo.updateScheduledState(it.id, false)
                }
        }
    }
}