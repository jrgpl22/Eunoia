package com.example.moodtracker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String username = intent.getStringExtra("username");
        if (username == null) return;

        // Check if user is still logged in
        SharedPreferences loginPrefs = context.getSharedPreferences("MoodTracker", Context.MODE_PRIVATE);
        String currentUser = loginPrefs.getString("loggedInUser", null);

        if (currentUser == null || currentUser.isEmpty() || !currentUser.equals(username)) {
            Log.d(TAG, "Skipping notification - user not logged in or different user");
            return;
        }

        String channelId = "mood_reminder_channel_" + username;
        createNotificationChannel(context, channelId, username);
        showNotification(context, channelId, username);
        rescheduleAlarmForNextDay(context, username);
    }

    private void createNotificationChannel(Context context, String channelId, String username) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Mood Reminder for " + username,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Daily mood tracking reminders for " + username);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Context context, String channelId, String username) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, Loading.class);
        intent.putExtra("username", username);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                username.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String[] messages = {
                "Hey " + username + ", time to track your mood! 😊",
                "How are you feeling today, " + username + "? 🤔",
                "Don't forget to log your mood, " + username + "! 📅",
                "Your emotions matter, " + username + ". Let's track them! ❤️",
                "Another day, another mood to capture, " + username + "! 🎭"
        };
        String notificationText = messages[(int) (Math.random() * messages.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("Mood Tracker Reminder")
                .setContentText(notificationText)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(username.hashCode(), builder.build());
    }

    private void rescheduleAlarmForNextDay(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences("MoodTracker_" + username, Context.MODE_PRIVATE);
        int hour = prefs.getInt("reminder_hour", 20);
        int minute = prefs.getInt("reminder_minute", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("MOOD_REMINDER_ALARM");
        intent.putExtra("username", username);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                username.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to reschedule alarm for user " + username, e);
        }
    }
}