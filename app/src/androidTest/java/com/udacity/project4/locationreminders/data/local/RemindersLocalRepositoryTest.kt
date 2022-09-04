package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
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


    @After
    fun closeDataBase() {
        database.close()
    }

    @Test
    fun getReminders() = runBlockingTest {
        val reminderList = listOf(
            setFakeData(1),
            setFakeData(2)
        )

        reminderList.forEach {
            remindersLocalRepository.saveReminder(it)
        }
        val result = remindersLocalRepository.getReminders()
        var loaded: List<ReminderDTO> = listOf()
        if (result is Result.Success<*>) {
            val dataList = ArrayList<ReminderDTO>()
            dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                ReminderDTO(
                    reminder.title,
                    reminder.description,
                    reminder.location,
                    reminder.latitude,
                    reminder.longitude,
                    reminder.id
                )
            })
            loaded = dataList
        }
        assertThat(loaded, `is`(reminderList))
    }


    @Test
    fun deleteAllReminders() = runBlockingTest {
        val reminderFake = setFakeData(1)
        remindersLocalRepository.saveReminder(reminderFake)
        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()
        assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }
}