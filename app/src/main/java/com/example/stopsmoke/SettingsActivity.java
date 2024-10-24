package com.example.stopsmoke;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

/**
 * SettingsActivity allows users to configure app settings,
 * including clearing the app's cache and smoking history.
 */
public class SettingsActivity extends AppCompatActivity {

    private EditText editTextInitialCigs;
    private EditText editTextReductionRate;

    private SharedPreferences prefs;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        editTextInitialCigs = findViewById(R.id.editTextInitialCigs);
        editTextReductionRate = findViewById(R.id.editTextReductionRate);
        Button buttonSaveSettings = findViewById(R.id.buttonSaveSettings);
        Button buttonClearCacheHistory = findViewById(R.id.buttonClearCacheHistory);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("StopSmokePrefs", MODE_PRIVATE);

        // Initialize DatabaseHelper
        db = new DatabaseHelper(this);

        // Load existing settings
        loadSettings();

        // Set click listeners
        buttonSaveSettings.setOnClickListener(v -> saveSettings());

        buttonClearCacheHistory.setOnClickListener(v -> showClearCacheHistoryConfirmationDialog());
    }

    /**
     * Loads existing settings into the input fields.
     */
    private void loadSettings() {
        int initialCigs = prefs.getInt("allowedCigarettes", 20);
        int reductionRate = prefs.getInt("reductionRate", 1);

        editTextInitialCigs.setText(String.valueOf(initialCigs));
        editTextReductionRate.setText(String.valueOf(reductionRate));
    }

    /**
     * Saves the user settings from the input fields.
     */
    private void saveSettings() {
        String initialCigsStr = editTextInitialCigs.getText().toString();
        String reductionRateStr = editTextReductionRate.getText().toString();

        if (initialCigsStr.isEmpty() || reductionRateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer initialCigs = null;
        Integer reductionRate = null;

        try {
            initialCigs = Integer.parseInt(initialCigsStr);
            reductionRate = Integer.parseInt(reductionRateStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (initialCigs <= 0 || reductionRate <= 0) {
            Toast.makeText(this, "Values must be positive integers.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("allowedCigarettes", initialCigs);
        editor.putInt("reductionRate", reductionRate);
        editor.apply();

        Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show();

        // Optionally, reset smokedToday
        editor.putInt("smokedToday", 0);
        editor.apply();

        // Finish activity
        finish();
    }

    /**
     * Displays a confirmation dialog before clearing cache and history.
     */
    private void showClearCacheHistoryConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear Cache and History")
                .setMessage("Are you sure you want to clear the app's cache and remove all smoking history? This action cannot be undone.")
                .setPositiveButton("Yes, Clear", (dialog, which) -> clearCacheAndHistory())
                .setNegativeButton("No, Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Clears the app's cache, deletes all cigarette logs, and stops any running timers.
     */
    private void clearCacheAndHistory() {
        boolean cacheCleared = clearCache();
        boolean historyCleared = clearSmokingHistory();

        if (cacheCleared && historyCleared) {
            Toast.makeText(this, "Cache and Smoking History cleared successfully.", Toast.LENGTH_SHORT).show();

            // Reset timer-related SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("nextCigaretteTime");
            editor.remove("lastCigaretteTime");
            editor.putInt("smokedToday", 0);
            editor.apply();

            // Notify MainActivity to stop any running timers
            // This can be achieved by sending a broadcast or using SharedPreferences listener
            // Here, we'll use a simple approach by restarting MainActivity if it's running
            // Alternatively, you can implement a more robust event-based communication

            Intent intent = new Intent("com.example.stopsmoke.ACTION_CLEAR_CACHE_HISTORY");
            sendBroadcast(intent);
        } else {
            Toast.makeText(this, "Failed to clear Cache and/or Smoking History.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clears the app's cache directory.
     *
     * @return True if cache was cleared successfully, false otherwise.
     */
    private boolean clearCache() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                return deleteDir(cacheDir);
            }
            return true; // If cacheDir is null or not a directory, consider cache as cleared
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes all cigarette logs from the database.
     *
     * @return True if history was cleared successfully, false otherwise.
     */
    private boolean clearSmokingHistory() {
        try {
            return db.deleteAllLogs();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param dir The directory to delete.
     * @return True if deletion was successful, false otherwise.
     */
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        // The directory is now empty or it's a file, so delete it
        return dir.delete();
    }
}
