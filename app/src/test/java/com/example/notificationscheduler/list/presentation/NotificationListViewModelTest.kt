package com.example.notificationscheduler.list.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.notificationscheduler.notification.domain.model.Notification
import com.example.notificationscheduler.core.presentation.model.UiState
import com.example.notificationscheduler.notification.alarm.NotificationAlarmScheduler
import com.example.notificationscheduler.notification.domain.repository.NotificationRepository
import com.example.notificationscheduler.notification.presentation.list.NotificationListViewModel
import com.example.notificationscheduler.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val notificationRepo: NotificationRepository = mockk()
    private val alarmScheduler: NotificationAlarmScheduler = mockk()
    private lateinit var viewModel: NotificationListViewModel

    @Test
    fun `init observes notifications and fetches them`() = runTest {
        // Given
        val notifications = listOf(Notification(id = 1, title = "Test 1"))
        coEvery { notificationRepo.getNotifications() } returns flowOf(notifications)
        coEvery { notificationRepo.refreshNotifications() } returns Unit

        // When
        viewModel = NotificationListViewModel(notificationRepo, alarmScheduler)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(notifications, (state as UiState.Success).data)
        }
        verify { notificationRepo.getNotifications() }
        coVerify { notificationRepo.refreshNotifications() }
    }

    @Test
    fun `fetchNotifications success does not change state if data already present`() = runTest {
        // Given
        val notifications = listOf(Notification(id = 1, title = "Test 1"))
        coEvery { notificationRepo.getNotifications() } returns flowOf(notifications)
        coEvery { notificationRepo.refreshNotifications() } returns Unit
        
        viewModel = NotificationListViewModel(notificationRepo, alarmScheduler)

        // When
        viewModel.refreshNotifications()

        // Then
        assertTrue(viewModel.uiState.value is UiState.Success)
        coVerify(exactly = 2) { notificationRepo.refreshNotifications() } // Once in init, once in call
    }

    @Test
    fun `fetchNotifications error updates state to Error when empty`() = runTest {
        // Given
        val errorMessage = "Network Error"
        val notificationsFlow = MutableStateFlow<List<Notification>>(emptyList())
        coEvery { notificationRepo.getNotifications() } returns notificationsFlow
        coEvery { notificationRepo.refreshNotifications() } throws Exception(errorMessage)

        // When
        viewModel = NotificationListViewModel(notificationRepo, alarmScheduler)

        // Then
        viewModel.uiState.test {
            // Account for any intermediate states (Success(empty), Loading) and find the Error state
            var foundError = false
            while (true) {
                val item = awaitItem()
                if (item is UiState.Error) {
                    assertEquals(errorMessage, item.message)
                    foundError = true
                    break
                }
            }
            assertTrue("Expected Error state but reached end of flow", foundError)
        }
    }

    @Test
    fun `cancelAllNotifications calls scheduler and repository`() = runTest {
        // Given
        val scheduled = listOf(Notification(id = 1, isScheduled = true))
        coEvery { notificationRepo.getNotifications() } returns flowOf(emptyList())
        coEvery { notificationRepo.refreshNotifications() } returns Unit
        coEvery { notificationRepo.getAllScheduledNotifications() } returns scheduled
        every { alarmScheduler.cancel(any()) } returns true
        coEvery { notificationRepo.updateScheduledState(any(), any()) } returns Unit
        
        viewModel = NotificationListViewModel(notificationRepo, alarmScheduler)

        // When
        viewModel.cancelAllNotifications()

        // Then
        coVerify { notificationRepo.getAllScheduledNotifications() }
        verify { alarmScheduler.cancel(1) }
        coVerify { notificationRepo.updateScheduledState(1, false) }
    }
}
