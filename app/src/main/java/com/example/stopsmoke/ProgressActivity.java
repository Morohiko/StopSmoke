package com.example.stopsmoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textViewProgressDescription;
    private DatabaseHelper db;
    private CigaretteLogAdapter adapter;

    private static final String PREFS_NAME = "StopSmokePrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // Initialize views
        RecyclerView recyclerView = findViewById(R.id.recyclerViewLogs);
        progressBar = findViewById(R.id.progressBar);
        textViewProgressDescription = findViewById(R.id.textViewProgressDescription);

        // Initialize DatabaseHelper
        db = new DatabaseHelper(this);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CigaretteLogAdapter();
        recyclerView.setAdapter(adapter);

        // Load data
        loadLogs();
    }

    /**
     * Loads cigarette logs from the past 4 weeks and updates the UI.
     */
    private void loadLogs() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -4); // Past 4 weeks
        long startTimestamp = calendar.getTimeInMillis();
        long endTimestamp = System.currentTimeMillis();

        List<CigaretteLog> logs = db.getLogsBetween(startTimestamp, endTimestamp);
        adapter.setLogs(logs);

        // Update ProgressBar and Description
        updateProgress();
    }

    /**
     * Updates the ProgressBar and its descriptive TextView based on user settings and logs.
     */
    @SuppressLint("SetTextI18n")
    private void updateProgress() {
        // Retrieve user settings from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int initialCigs = prefs.getInt("allowedCigarettes", 20);
        int reductionRate = prefs.getInt("reductionRate", 1);

        // Calculate total allowed cigarettes over 4 weeks with reduction
        int totalAllowed = 0;
        for (int i = 0; i < 4; i++) {
            totalAllowed += Math.max(initialCigs - (reductionRate * i), 0);
        }

        // Calculate total smoked cigarettes in the past 4 weeks
        List<CigaretteLog> logs = db.getLogsBetween(getStartOfWeek(), System.currentTimeMillis());
        int totalSmoked = logs.size();

        // Update ProgressBar
        progressBar.setMax(totalAllowed);
        progressBar.setProgress(totalSmoked);

        // Update Description
        textViewProgressDescription.setText("Smoked " + totalSmoked + " out of " + totalAllowed + " cigarettes in the last 4 weeks.");
    }

    /**
     * Calculates the start timestamp of the week, a specified number of weeks ago.
     *
     * @return Timestamp in milliseconds representing the start of the specified week.
     */
    private long getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -4);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
