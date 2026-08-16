package com.example.notificationscheduler.list.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import app.cash.turbine.test
import com.example.notificationscheduler.core.model.Notification
import com.example.notificationscheduler.core.model.UiState
import com.example.notificationscheduler.details.presentaion.NotificationDetailsViewModel
import com.example.notificationscheduler.list.data.remote.RemoteDataSource
import com.example.notificationscheduler.rules.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val remoteDataSource: RemoteDataSource = mockk()
    private val workManager: WorkManager = mockk()
    private lateinit var viewModel: NotificationViewModel

    @Test
    fun `fetchNotifications success updates uiState to Success`() = runTest {
        // Given
        val notifications = listOf(
            Notification(id = 1, title = "Test 1", timeInSeconds = 10),
            Notification(id = 2, title = "Test 2", timeInSeconds = 20)
        )
        coEvery { remoteDataSource.fetchNotifications() } returns notifications

        // When
        viewModel = NotificationViewModel(remoteDataSource, workManager)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(notifications, (state as UiState.Success).data)
        }
    }

    @Test
    fun `fetchNotifications failure updates uiState to Error`() = runTest {
        // Given
        val errorMessage = "Network Error"
        coEvery { remoteDataSource.fetchNotifications() } throws Exception(errorMessage)

        // When
        viewModel = NotificationViewModel(remoteDataSource, workManager)

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(errorMessage, (state as UiState.Error).message)
        }
    }

    @Test
    fun `cancelAllNotifications calls workManager cancelAllWorkByTag`() {
        // Given
        coEvery { remoteDataSource.fetchNotifications() } returns emptyList()
        viewModel = NotificationViewModel(remoteDataSource, workManager)
        every { workManager.cancelAllWorkByTag(any()) } returns mockk()

        // When
        viewModel.cancelAllNotifications()

        // Then
        verify { workManager.cancelAllWorkByTag(NotificationDetailsViewModel.NOTIFICATION_WORK_TAG) }
    }
}
