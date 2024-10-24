package com.example.stopsmoke;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import java.util.Date;

public class MainActivityTest {

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); // Reset the database before each test

        prefs = context.getSharedPreferences("StopSmokePrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void logCigarette_ShouldUpdateUIAndDatabase() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Perform click on "Log Cigarette" button
        onView(withId(R.id.buttonLogCigarette)).perform(ViewActions.click());

        // Check that "Smoked Today" count is updated to 1
        onView(withId(R.id.textViewSmoked))
                .check(matches(withText("Smoked Today: 1")));

        // Verify that a log is inserted in the database
        int count = dbHelper.countCigarettesOn(new Date());
        assertEquals("Database should have 1 log", 1, count);

        // Verify that the timer is started and the "Log Cigarette" button is disabled
        onView(withId(R.id.textViewNextCigTimer)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonLogCigarette)).check(matches(not(isEnabled())));
    }

    @Test
    public void clearCacheAndHistory_ShouldResetUIAndDatabase() {
        // Insert a log
        dbHelper.insertLog(new CigaretteLog(System.currentTimeMillis()));
        prefs.edit().putInt("smokedToday", 1).apply();

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Navigate to Settings
        onView(withId(R.id.buttonSettings)).perform(ViewActions.click());

        // Perform click on "Clear Cache and History" button
        onView(withId(R.id.buttonClearCacheHistory)).perform(ViewActions.click());

        // Confirm the dialog
        onView(withText("Yes, Clear")).perform(ViewActions.click());

        // Check that a toast message is displayed (Toast testing requires additional setup)

        // Navigate back to MainActivity
        pressBack();

        // Check that "Smoked Today" count is reset to 0
        onView(withId(R.id.textViewSmoked))
                .check(matches(withText("Smoked Today: 0")));

        // Verify that the database has no logs
        int count = dbHelper.countCigarettesOn(new Date());
        assertEquals("Database should have 0 logs", 0, count);

        // Check that the timer is not displayed and "Log Cigarette" button is enabled
        onView(withId(R.id.textViewNextCigTimer)).check(matches(not(isDisplayed())));
        onView(withId(R.id.buttonLogCigarette)).check(matches(isEnabled()));
    }
}
