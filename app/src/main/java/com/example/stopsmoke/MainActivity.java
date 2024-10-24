package com.example.stopsmoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // SharedPreferences keys
    private static final String PREFS_NAME = "StopSmokePrefs";
    private static final String KEY_LAST_CIG_TIME = "lastCigaretteTime";
    private static final String KEY_NEXT_CIG_TIME = "nextCigaretteTime";
    private static final String KEY_ALLOWED_CIGS = "allowedCigarettes";

    // Default values
    private static final int DEFAULT_ALLOWED_CIGS = 20;

    // Notification channel ID
    private static final String CHANNEL_ID = "timer_channel";

    // UI Components
    private TextView textViewAllowed;
    private TextView textViewSmoked;
    private TextView textViewNextCigTimer;
    private Button buttonLogCigarette;
    private Button buttonSettings;
    private Button buttonViewProgress;

    // SharedPreferences and DatabaseHelper
    private SharedPreferences prefs;
    private DatabaseHelper db;

    // Timer variables
    private CountDownTimer countDownTimer;
    private long nextCigaretteTimeMillis;
    private int smokedToday = 0;

    // BroadcastReceiver to handle cache and history clearance
    private final BroadcastReceiver clearCacheHistoryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.stopsmoke.ACTION_CLEAR_CACHE_HISTORY".equals(intent.getAction())) {
                stopTimer();
                updateUI();
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        textViewAllowed = findViewById(R.id.textViewAllowed);
        textViewSmoked = findViewById(R.id.textViewSmoked);
        textViewNextCigTimer = findViewById(R.id.textViewNextCigTimer);
        buttonLogCigarette = findViewById(R.id.buttonLogCigarette);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonViewProgress = findViewById(R.id.buttonViewProgress);

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize DatabaseHelper
        db = new DatabaseHelper(this);

        // Update UI
        updateUI();

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

        // Register BroadcastReceiver for cache and history clearance
        IntentFilter filter = new IntentFilter("com.example.stopsmoke.ACTION_CLEAR_CACHE_HISTORY");
        registerReceiver(clearCacheHistoryReceiver, filter);

        // Check if a timer is already running
        checkExistingTimer();

        // Create Notification Channel
        createNotificationChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        checkExistingTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        unregisterReceiver(clearCacheHistoryReceiver);
    }

    /**
     * Checks if there's an existing timer and starts it if necessary.
     */
    private void checkExistingTimer() {
        nextCigaretteTimeMillis = prefs.getLong(KEY_NEXT_CIG_TIME, 0);
        long currentTimeMillis = System.currentTimeMillis();

        if (nextCigaretteTimeMillis > currentTimeMillis) {
            long millisUntilNextCig = nextCigaretteTimeMillis - currentTimeMillis;
            startTimer(millisUntilNextCig);
        } else {
            // Timer has expired or not set
            textViewNextCigTimer.setVisibility(View.GONE);
            buttonLogCigarette.setEnabled(true);
        }
    }

    /**
     * Logs a cigarette and starts the timer based on allowed cigarettes per day.
     */
    private void logCigarette() {
        long currentTimeMillis = System.currentTimeMillis();

        // Insert log into database
        CigaretteLog log = new CigaretteLog(currentTimeMillis);
        long result = db.insertLog(log);

        if (result != -1) {
            // Successfully logged
            Toast.makeText(this, "Cigarette logged.", Toast.LENGTH_SHORT).show();

            // Update smoked count
            smokedToday = db.countCigarettesOn(new Date());
            textViewSmoked.setText("Smoked Today: " + smokedToday);

            // Update SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_LAST_CIG_TIME, currentTimeMillis);

            // Calculate next allowed time based on allowed cigs per day
            int allowedCigsPerDay = prefs.getInt(KEY_ALLOWED_CIGS, DEFAULT_ALLOWED_CIGS);
            long intervalMillis = calculateIntervalMillis(allowedCigsPerDay);

            nextCigaretteTimeMillis = currentTimeMillis + intervalMillis;
            editor.putLong(KEY_NEXT_CIG_TIME, nextCigaretteTimeMillis);
            editor.apply();

            // Start the timer
            startTimer(intervalMillis);
        } else {
            // Failed to log
            Toast.makeText(this, "Failed to log cigarette.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Calculates the interval between cigarettes based on allowed cigs per day.
     *
     * @param allowedCigsPerDay Number of allowed cigarettes per day.
     * @return Interval in milliseconds.
     */
    private long calculateIntervalMillis(int allowedCigsPerDay) {
        // Total minutes in a day: 1440
        int totalMinutes = 1440;
        if (allowedCigsPerDay <= 0) {
            allowedCigsPerDay = DEFAULT_ALLOWED_CIGS; // Prevent division by zero
        }
        int intervalMinutes = totalMinutes / allowedCigsPerDay;
        return intervalMinutes * 60 * 1000L; // Convert to milliseconds
    }

    /**
     * Starts the countdown timer.
     *
     * @param millisInFuture Duration in milliseconds for the timer.
     */
    private void startTimer(long millisInFuture) {
        // Make the timer TextView visible
        textViewNextCigTimer.setVisibility(View.VISIBLE);

        // Disable the log cigarette button
        buttonLogCigarette.setEnabled(false);

        // Initialize and start the CountDownTimer
        countDownTimer = new CountDownTimer(millisInFuture, 1000) { // Tick every second

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                textViewNextCigTimer.setText("Next cigarette available in: " + formatMillis(millisUntilFinished));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                textViewNextCigTimer.setText("You can smoke now!");
                buttonLogCigarette.setEnabled(true);
                // Optionally hide the timer after a short delay
                textViewNextCigTimer.postDelayed(() -> textViewNextCigTimer.setVisibility(View.GONE), 5000);

                // Send a notification
                sendTimerCompletedNotification();
            }
        }.start();
    }

    /**
     * Stops the running timer, hides the timer TextView, and enables the log button.
     */
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        textViewNextCigTimer.setVisibility(View.GONE);
        buttonLogCigarette.setEnabled(true);
    }

    /**
     * Formats milliseconds into HH:mm format.
     *
     * @param millis Milliseconds to format.
     * @return Formatted time string.
     */
    private String formatMillis(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    /**
     * Sends a notification when the timer completes.
     */
    private void sendTimerCompletedNotification() {
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ensure this icon exists
                .setContentTitle("StopSmoke")
                .setContentText("You can smoke your next cigarette now.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1001, builder.build());
    }

    /**
     * Updates the UI elements based on current data.
     */
    private void updateUI() {
        // Retrieve allowed cigarettes from SharedPreferences
        int allowedCigarettes = prefs.getInt(KEY_ALLOWED_CIGS, DEFAULT_ALLOWED_CIGS);
        textViewAllowed.setText("Allowed Today: " + allowedCigarettes);

        // Retrieve smoked today count
        smokedToday = db.countCigarettesOn(new Date());
        textViewSmoked.setText("Smoked Today: " + smokedToday);
    }

    /**
     * Creates a notification channel for Android O and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Timer Notifications";
            String description = "Notifications for when you can smoke your next cigarette.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
