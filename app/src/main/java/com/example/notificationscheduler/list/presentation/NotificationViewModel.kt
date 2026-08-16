package com.example.notificationscheduler.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.notificationscheduler.core.data.model.Notification
import com.example.notificationscheduler.core.presentation.model.UiState
import com.example.notificationscheduler.details.presentaion.NotificationDetailsViewModel
import com.example.notificationscheduler.list.domain.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepo: NotificationRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Notification>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Notification>>> = _uiState.asStateFlow()

    init {
        observeNotifications()
        fetchNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepo.getNotifications().collectLatest { notifications ->
                if (notifications.isNotEmpty()) {
                    _uiState.update { UiState.Success(notifications) }
                }
            }
        }
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            if (_uiState.value !is UiState.Success) {
                _uiState.value = UiState.Loading
            }
            try {
                notificationRepo.refreshNotifications()
            } catch (e: Exception) {
                if (_uiState.value !is UiState.Success) {
                    _uiState.value = UiState.Error(e.message, e)
                }
            }
        }
    }

    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(NotificationDetailsViewModel.NOTIFICATION_WORK_TAG)
    }
}