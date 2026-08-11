package com.example.notificationscheduler.list.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.notificationscheduler.list.data.remote.RemoteDataSource
import com.example.notificationscheduler.core.model.Notification
import com.example.notificationscheduler.core.model.UiState
import com.example.notificationscheduler.details.presentaion.NotificationDetailsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Notification>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Notification>>> = _uiState.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val notifications = remoteDataSource.fetchNotifications()
                _uiState.value = UiState.Success(notifications)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message, e)
            }
        }
    }

    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(NotificationDetailsViewModel.NOTIFICATION_WORK_TAG)
    }
}