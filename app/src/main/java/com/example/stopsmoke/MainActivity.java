package com.example.stopsmoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView textViewAllowed;
    private TextView textViewSmoked;
    private Button buttonLogCigarette;
    private Button buttonSettings;
    private Button buttonViewProgress;

    private SharedPreferences prefs;
    private int allowedCigarettes = 20;
    private int smokedToday = 0;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        textViewAllowed = findViewById(R.id.textViewAllowed);
        textViewSmoked = findViewById(R.id.textViewSmoked);
        buttonLogCigarette = findViewById(R.id.buttonLogCigarette);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonViewProgress = findViewById(R.id.buttonViewProgress);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("StopSmokePrefs", MODE_PRIVATE);
        allowedCigarettes = prefs.getInt("allowedCigarettes", 20);
        smokedToday = prefs.getInt("smokedToday", 0);

        // Initialize DatabaseHelper
        db = new DatabaseHelper(this);

        // Update UI
        updateUI();

        // Schedule daily notification
        scheduleDailyNotification();

        // Set click listeners
        buttonLogCigarette.setOnClickListener(v -> logCigarette());

        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        buttonViewProgress.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
            startActivity(intent);
        });
    }

    private void loadSettings() {
        allowedCigarettes = prefs.getInt("allowedCigarettes", 20);
        smokedToday = prefs.getInt("smokedToday", 0);
    }

    private void updateUI() {
        textViewAllowed.setText("Allowed Today: " + allowedCigarettes);
        textViewSmoked.setText("Smoked Today: " + smokedToday);
    }

    private void logCigarette() {
        if (smokedToday < allowedCigarettes) {
            smokedToday++;
            textViewSmoked.setText("Smoked Today: " + smokedToday);

            // Insert log into database
            CigaretteLog log = new CigaretteLog(System.currentTimeMillis());
            db.insertLog(log);

            Toast.makeText(this, "Cigarette logged.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You've reached your daily limit!", Toast.LENGTH_SHORT).show();
        }

        // Save smokedToday to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("smokedToday", smokedToday);
        editor.apply();
    }

    private void scheduleDailyNotification() {
        PeriodicWorkRequest notificationWork =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "StopSmokeNotification",
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload settings in case they were changed
        loadSettings();
        updateUI();
    }
}
