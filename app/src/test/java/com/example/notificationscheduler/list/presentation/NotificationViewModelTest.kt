package com.example.notificationscheduler.list.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import app.cash.turbine.test
import com.example.notificationscheduler.core.data.model.Notification
import com.example.notificationscheduler.core.presentation.model.UiState
import com.example.notificationscheduler.details.presentaion.NotificationDetailsViewModel
import com.example.notificationscheduler.list.domain.NotificationRepository
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
class NotificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val notificationRepo: NotificationRepository = mockk()
    private val workManager: WorkManager = mockk()
    private lateinit var viewModel: NotificationViewModel

    @Test
    fun `init observes notifications and fetches them`() = runTest {
        // Given
        val notifications = listOf(Notification(id = 1, title = "Test 1"))
        coEvery { notificationRepo.getNotifications() } returns flowOf(notifications)
        coEvery { notificationRepo.refreshNotifications() } returns Unit

        // When
        viewModel = NotificationViewModel(notificationRepo, workManager)

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
        
        viewModel = NotificationViewModel(notificationRepo, workManager)

        // When
        viewModel.fetchNotifications()

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
        viewModel = NotificationViewModel(notificationRepo, workManager)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(errorMessage, (state as UiState.Error).message)
        }
    }

    @Test
    fun `cancelAllNotifications calls workManager cancelAllWorkByTag`() = runTest {
        // Given
        coEvery { notificationRepo.getNotifications() } returns flowOf(emptyList())
        coEvery { notificationRepo.refreshNotifications() } returns Unit
        every { workManager.cancelAllWorkByTag(any()) } returns mockk()
        
        viewModel = NotificationViewModel(notificationRepo, workManager)

        // When
        viewModel.cancelAllNotifications()

        // Then
        verify { workManager.cancelAllWorkByTag(NotificationDetailsViewModel.NOTIFICATION_WORK_TAG) }
    }
}
