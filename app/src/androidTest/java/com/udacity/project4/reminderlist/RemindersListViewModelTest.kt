package com.udacity.project4.reminderlist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.junit.Before
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P)
class RemindersListViewModelTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initViewModel() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(
            getApplicationContext(),
            fakeDataSource
        )
    }

    private fun getReminder() = ReminderDTO(
        title = "title",
        description = "description",
        location = "location",
        latitude = 23.80,
        longitude = 45.36
    )


    @Test
    fun loadRemindersWhenRemindersAreUnavailable_returnError() = runBlockingTest {
        fakeDataSource.setShouldReturnError(true)
        reminderListViewModel.loadReminders()
        assertThat(
            reminderListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Reminders not found")
        )
    }

    @Test
    fun loadReminders_ReturnNoData() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun shouldReturnError() {
        //Given: error in fake data
        fakeDataSource.setShouldReturnError(true)
        //WHEN: try to load data
        reminderListViewModel.loadReminders()
        //THEN: error will appear
        assertThat(
            reminderListViewModel
                .showSnackBar
                .getOrAwaitValue(),
            `is`("Reminders not found")
        )
    }

    @Test
    fun check_loading() = mainCoroutineRule.runBlockingTest {
        val result = getReminder()
        fakeDataSource.saveReminder(result)
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()

        val loadingBeforeCoroutine = reminderListViewModel.showLoading.getOrAwaitValue()
        mainCoroutineRule.resumeDispatcher()

        val loadingAfterCoroutine = reminderListViewModel.showLoading.getOrAwaitValue()
        var viewModelReminder: ReminderDTO? = null

        reminderListViewModel.remindersList.getOrAwaitValue()[0].apply {
            viewModelReminder = ReminderDTO(title, description, location, latitude, longitude, id)
        }

        //Then
        assertThat(loadingBeforeCoroutine, `is`(true))
        assertThat(loadingAfterCoroutine, `is`(false))
        assertThat(viewModelReminder, `is`(result))
    }
}