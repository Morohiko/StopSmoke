package com.example.stopsmoke;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("StopSmokePrefs", Context.MODE_PRIVATE);
            long nextCigTime = prefs.getLong("nextCigaretteTime", 0);
            long currentTime = System.currentTimeMillis();

            if (nextCigTime > currentTime) {
                long delay = nextCigTime - currentTime;

                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TimerWorker.class)
                        .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .build();

                WorkManager.getInstance(context).enqueueUniqueWork("timerWork", ExistingWorkPolicy.REPLACE, workRequest);
            }
        }
    }
}
