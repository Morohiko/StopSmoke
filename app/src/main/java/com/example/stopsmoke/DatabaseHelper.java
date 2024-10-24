package com.example.stopsmoke;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Helper class to manage SQLite database operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StopSmoke.db";
    private static final int DATABASE_VERSION = 1;

    // Table and columns
    private static final String TABLE_LOGS = "cigarette_logs";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_LOGS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIMESTAMP + " INTEGER"
                + ")";
        db.execSQL(CREATE_LOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now, simply drop and recreate the table on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        onCreate(db);
    }

    /**
     * Inserts a new cigarette log into the database.
     *
     * @param log The CigaretteLog object to insert.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insertLog(CigaretteLog log) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, log.getTimestamp());

        long result = db.insert(TABLE_LOGS, null, values);
        db.close();
        return result;
    }

    /**
     * Retrieves logs between specified timestamps.
     *
     * @param startTimestamp Start time in milliseconds.
     * @param endTimestamp   End time in milliseconds.
     * @return A list of CigaretteLog objects.
     */
    public List<CigaretteLog> getLogsBetween(long startTimestamp, long endTimestamp) {
        List<CigaretteLog> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOGS,
                new String[]{COLUMN_ID, COLUMN_TIMESTAMP},
                COLUMN_TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{String.valueOf(startTimestamp), String.valueOf(endTimestamp)},
                null, null, COLUMN_TIMESTAMP + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                CigaretteLog log = new CigaretteLog();
                log.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                log.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                logs.add(log);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return logs;
    }

    /**
     * Counts the number of cigarettes smoked on a specific date.
     *
     * @param date The date to count cigarettes for.
     * @return The count of cigarettes smoked on that date.
     */
    public int countCigarettesOn(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Set to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        // Set to end of day
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long endOfDay = calendar.getTimeInMillis() - 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LOGS
                        + " WHERE " + COLUMN_TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)});

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        db.close();
        return count;
    }

    /**
     * Deletes all cigarette logs from the database.
     *
     * @return True if deletion was successful, false otherwise.
     */
    public boolean deleteAllLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_LOGS, null, null);
        db.close();
        return deletedRows >= 0; // Returns true even if 0 rows were deleted
    }
}
