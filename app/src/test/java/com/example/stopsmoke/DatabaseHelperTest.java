package com.example.stopsmoke;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DatabaseHelperTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); // Reset the database before each test
    }

    @After
    public void tearDown() {
        dbHelper.close();
    }

    @Test
    public void insertLog_ShouldInsertSuccessfully() {
        CigaretteLog log = new CigaretteLog(System.currentTimeMillis());
        long rowId = dbHelper.insertLog(log);
        assertTrue("Insert should return a valid row ID", rowId != -1);
    }

    @Test
    public void getLogsBetween_ShouldReturnCorrectLogs() throws InterruptedException {
        long currentTime = System.currentTimeMillis();

        // Insert logs at different times
        CigaretteLog log1 = new CigaretteLog(currentTime - 10000); // 10 seconds ago
        CigaretteLog log2 = new CigaretteLog(currentTime);
        CigaretteLog log3 = new CigaretteLog(currentTime + 10000); // 10 seconds later

        dbHelper.insertLog(log1);
        dbHelper.insertLog(log2);
        dbHelper.insertLog(log3);

        // Retrieve logs between currentTime - 5000 and currentTime + 5000
        List<CigaretteLog> logs = dbHelper.getLogsBetween(currentTime - 5000, currentTime + 5000);
        assertEquals("Should retrieve 1 log", 1, logs.size());
        assertEquals("Retrieved log should match log2", log2.getTimestamp(), logs.get(0).getTimestamp());
    }

    @Test
    public void countCigarettesOn_ShouldReturnCorrectCount() {
        // Today's date
        Date today = new Date();
        long startOfDay = getStartOfDay(today);
        long endOfDay = getEndOfDay(today);

        // Insert 3 logs today
        dbHelper.insertLog(new CigaretteLog(startOfDay + 1000)); // 00:00:01
        dbHelper.insertLog(new CigaretteLog(startOfDay + 2000)); // 00:00:02
        dbHelper.insertLog(new CigaretteLog(startOfDay + 3000)); // 00:00:03

        // Insert 2 logs yesterday
        Date yesterday = new Date(startOfDay - 86400000L); // 1 day in milliseconds
        dbHelper.insertLog(new CigaretteLog(yesterday.getTime() + 1000));
        dbHelper.insertLog(new CigaretteLog(yesterday.getTime() + 2000));

        int countToday = dbHelper.countCigarettesOn(today);
        assertEquals("Should count 3 cigarettes today", 3, countToday);

        int countYesterday = dbHelper.countCigarettesOn(yesterday);
        assertEquals("Should count 2 cigarettes yesterday", 2, countYesterday);
    }

    @Test
    public void deleteAllLogs_ShouldDeleteSuccessfully() {
        CigaretteLog log1 = new CigaretteLog(System.currentTimeMillis());
        CigaretteLog log2 = new CigaretteLog(System.currentTimeMillis());

        dbHelper.insertLog(log1);
        dbHelper.insertLog(log2);

        boolean result = dbHelper.deleteAllLogs();
        assertTrue("Delete all logs should return true", result);

        List<CigaretteLog> logs = dbHelper.getLogsBetween(0, System.currentTimeMillis());
        assertTrue("Logs should be empty after deletion", logs.isEmpty());
    }

    /**
     * Helper method to get the start of the day in milliseconds.
     */
    private long getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Set to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Helper method to get the end of the day in milliseconds.
     */
    private long getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Set to end of day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}
