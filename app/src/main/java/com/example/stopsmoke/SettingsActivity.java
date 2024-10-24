package com.example.stopsmoke;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextInitialCigs;
    private EditText editTextReductionRate;
    private Button buttonSaveSettings;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        editTextInitialCigs = findViewById(R.id.editTextInitialCigs);
        editTextReductionRate = findViewById(R.id.editTextReductionRate);
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("StopSmokePrefs", MODE_PRIVATE);

        // Load existing settings
        loadSettings();

        // Set click listener
        buttonSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        int initialCigs = prefs.getInt("allowedCigarettes", 20);
        int reductionRate = prefs.getInt("reductionRate", 1);

        editTextInitialCigs.setText(String.valueOf(initialCigs));
        editTextReductionRate.setText(String.valueOf(reductionRate));
    }

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
}
