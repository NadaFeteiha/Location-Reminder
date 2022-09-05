package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase


    @Before
    fun initRepository() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    private fun setFakeData(number: Int) = ReminderDTO(
        "title$number",
        "description",
        "location",
        24.45,
        53.67
    )

    @Test
    fun getReminders() = mainCoroutineRule.runBlockingTest {
       //GIVEN: we have multiple reminder
        val reminderList = listOf(
            setFakeData(1),
            setFakeData(2)
        )
        reminderList.forEach {
            remindersLocalRepository.saveReminder(it)
        }

        //WHEN: want to get this reminders
        val result = remindersLocalRepository.getReminders()
        result as Result.Success
        //THEN: we should get the same data.
        assertThat(result.data.size, `is`(2))
    }


    @Test
    fun deleteAllReminders() = mainCoroutineRule.runBlockingTest {
        //GIVEN: a reminder saved in Database
        val reminderFake = setFakeData(1)
        remindersLocalRepository.saveReminder(reminderFake)
        //WHEN: delete all the reminders
        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()
        //THEN: the delete will work correctly and size of it will be Zero.
        result as Result.Success
        assertThat(result.data.size, `is`(0))
    }

    @Test
    fun getReminder_returnsError() = mainCoroutineRule.runBlockingTest {
        //GIVEN: delete all reminders
        remindersLocalRepository.deleteAllReminders()
        //WHEN: try to get reminders.
        val result = remindersLocalRepository.getReminder(setFakeData(1).id)
        result as Result.Error
        //THEN: got error result massed
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @After
    fun closeDataBase() {
        database.close()
    }
}