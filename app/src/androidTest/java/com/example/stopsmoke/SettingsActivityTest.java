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
import static org.junit.Assert.*;

import java.util.Date;

public class SettingsActivityTest {

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
    public void saveSettings_ShouldPersistValues() {
        ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(SettingsActivity.class);

        // Input allowed cigarettes
        onView(withId(R.id.editTextInitialCigs))
                .perform(ViewActions.replaceText("15"), ViewActions.closeSoftKeyboard());

        // Input reduction rate
        onView(withId(R.id.editTextReductionRate))
                .perform(ViewActions.replaceText("2"), ViewActions.closeSoftKeyboard());

        // Click on "Save Settings"
        onView(withId(R.id.buttonSaveSettings)).perform(ViewActions.click());

        // Verify that settings are saved in SharedPreferences
        int allowedCigs = prefs.getInt("allowedCigarettes", 0);
        int reductionRate = prefs.getInt("reductionRate", 0);

        assertEquals("Allowed cigarettes should be 15", 15, allowedCigs);
        assertEquals("Reduction rate should be 2", 2, reductionRate);
    }

    @Test
    public void clearCacheAndHistory_ShouldResetPreferencesAndDatabase() {
        // Insert a log
        dbHelper.insertLog(new CigaretteLog(System.currentTimeMillis()));
        prefs.edit().putInt("smokedToday", 1).apply();
        prefs.edit().putLong("nextCigaretteTime", System.currentTimeMillis() + 60000L).apply(); // Example value
        prefs.edit().putLong("lastCigaretteTime", System.currentTimeMillis()).apply();

        // Launch SettingsActivity
        ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(SettingsActivity.class);

        // Click on "Clear Cache and History" button
        onView(withId(R.id.buttonClearCacheHistory)).perform(ViewActions.click());

        // Confirm the dialog by clicking "Yes, Clear"
        onView(withText("Yes, Clear")).perform(ViewActions.click());

        // Verify that SharedPreferences are reset
        int smokedToday = prefs.getInt("smokedToday", -1);
        long nextCigTime = prefs.getLong("nextCigaretteTime", -1);
        long lastCigTime = prefs.getLong("lastCigaretteTime", -1);

        assertEquals("smokedToday should be 0", 0, smokedToday);
        assertEquals("nextCigaretteTime should be -1 (key removed)", -1L, nextCigTime);
        assertEquals("lastCigaretteTime should be -1 (key removed)", -1L, lastCigTime);

        // Alternatively, check that the keys do not exist
        assertFalse("nextCigaretteTime key should be removed", prefs.contains("nextCigaretteTime"));
        assertFalse("lastCigaretteTime key should be removed", prefs.contains("lastCigaretteTime"));

        // Verify that the database has no logs
        int count = dbHelper.countCigarettesOn(new Date());
        assertEquals("Database should have 0 logs", 0, count);
    }
}
