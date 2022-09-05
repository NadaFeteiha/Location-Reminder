package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.koin.core.context.startKoin
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@LargeTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var appContext: Application
    lateinit var repository: ReminderDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            modules(listOf(myModule))
        }

        repository = get().get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun show_WhenNoDataInList() = runBlockingTest {
        //GIVEN: repository is delete all.
        runBlocking {
            repository.deleteAllReminders()
        }

        //WHEN: user first time open or after delete all data.
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView))
            //THEN: should match with empty screen .
            .check(ViewAssertions.matches(isDisplayed()))
    }

    private fun getReminder() = ReminderDTO(
        title = "title",
        description = "description",
        location = "location",
        latitude = 23.80,
        longitude = 45.36
    )

    @Test
    fun show_When_listHasData() = runBlockingTest {
        //GIVEN: there is data in the list.
        val item = getReminder()
        runBlocking {
            repository.saveReminder(item)
        }
        //WHEN: the user open the screen .
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //THEN: the user will see the data.
        onView(withId(R.id.title)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.description)).check(matches(isDisplayed()))
        onView(withId(R.id.location)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))

        onView(withId(R.id.title)).check(matches(withText(item.title.toString())))
        onView(withId(R.id.description)).check(matches(withText(item.description.toString())))
        onView(withId(R.id.location)).check(matches(withText(item.location.toString())))
    }

    @Test
    fun navigateToSaveReminderFragment() = runBlockingTest {
        // GIVEN: the user in reminderList fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN: the user click  on the button add
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN:  should open Save Reminder fragment to the user
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

}