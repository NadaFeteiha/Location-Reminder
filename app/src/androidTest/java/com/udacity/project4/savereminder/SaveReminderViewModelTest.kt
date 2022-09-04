package com.udacity.project4.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.stopKoin
import org.robolectric.annotation.Config


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    private fun getReminder() = ReminderDataItem(
        title = "title",
        description = "description",
        location = "location",
        latitude = 23.80,
        longitude = 45.36
    )


    @Test
    fun saveReminder() = mainCoroutineRule.runBlockingTest {
        //GIVEN: user enter a place to remind it.
        val reminder = getReminder()
        //WHEN: user click to save it.
        saveReminderViewModel.saveReminder(reminder)
        //THEN: the user see toast that place is saved.
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun saveReminder_When_TitleEmpty() = mainCoroutineRule.runBlockingTest {
        //GIVEN: user enter a place to remind it without title.
        val reminder = getReminder()
        reminder.title = ""
        //WHEN: user click to save it.
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //THEN: place will not save not valid.
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

    @Test
    fun saveReminder_When_LocationEmpty() = mainCoroutineRule.runBlockingTest {
        val reminder = getReminder()
        reminder.location = ""

        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

    @Test
    fun saveReminder_When_LocationEmpty_TitleEmpty() = mainCoroutineRule.runBlockingTest {
        val reminder = getReminder()
        reminder.title = ""
        reminder.location = ""

        saveReminderViewModel.validateAndSaveReminder(reminder)
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

    @After
    fun stopDown() {
        stopKoin()
    }
}