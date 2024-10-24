package com.example.stopsmoke;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TimerWorker extends Worker {

    private static final String CHANNEL_ID = "timer_channel";

    public TimerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        sendTimerCompletedNotification();
        return Result.success();
    }

    /**
     * Sends a notification indicating that the timer has completed.
     */
    private void sendTimerCompletedNotification() {
        Context context = getApplicationContext();

        // Create a notification channel if necessary (for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for when you can smoke your next cigarette.");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ensure this icon exists
                .setContentTitle("StopSmoke")
                .setContentText("You can smoke your next cigarette now.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());
    }
}
