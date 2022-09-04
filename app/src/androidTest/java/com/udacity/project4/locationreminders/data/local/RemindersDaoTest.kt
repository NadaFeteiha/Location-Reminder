package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initializeDataBase() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN: the user insert  location and title..etc.
        val locationReminder = ReminderDTO("test place x", "family place", "cairo", 30.0, 31.0)
        database.reminderDao().saveReminder((locationReminder))

        // WHEN: the app get location by id from the database to show it to the user.
        val loaded = database.reminderDao().getReminderById(locationReminder.id)

        // THEN - the data that we got it from database should be same as expected.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(locationReminder.id))
        assertThat(loaded.title, `is`(locationReminder.title))
        assertThat(loaded.description, `is`(locationReminder.description))
        assertThat(loaded.location, `is`(locationReminder.location))
        assertThat(loaded.latitude, `is`(locationReminder.latitude))
        assertThat(loaded.longitude, `is`(locationReminder.longitude))
    }

    @After
    fun closeDb() = database.close()

}