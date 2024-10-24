package com.example.stopsmoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewProgressDescription;
    private DatabaseHelper db;
    private CigaretteLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewLogs);
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

    private void updateProgress() {
        SharedPreferences prefs = getSharedPreferences("StopSmokePrefs", MODE_PRIVATE);
        int initialCigs = prefs.getInt("allowedCigarettes", 20);
        int reductionRate = prefs.getInt("reductionRate", 1);

        // Calculate total allowed cigarettes over 4 weeks with reduction
        int totalAllowed = 0;
        for (int i = 0; i < 4; i++) {
            totalAllowed += Math.max(initialCigs - (reductionRate * i), 0);
        }

        // Calculate total smoked cigarettes
        List<CigaretteLog> logs = db.getLogsBetween(getStartOfWeek(4), System.currentTimeMillis());
        int totalSmoked = logs.size();

        // Update ProgressBar
        progressBar.setMax(totalAllowed);
        progressBar.setProgress(totalSmoked);

        // Update Description
        textViewProgressDescription.setText("Smoked " + totalSmoked + " out of " + totalAllowed + " cigarettes");
    }

    private long getStartOfWeek(int weeksAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
