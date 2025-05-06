package com.example.moodtracker;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

public class NotificationSettingsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationSettings";
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private Switch notificationSwitch;
    private Button timePickerButton;
    private ImageButton backButton;
    private SharedPreferences prefs;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Get current username from shared preferences
        SharedPreferences loginPrefs = getSharedPreferences("MoodTracker", MODE_PRIVATE);
        currentUsername = loginPrefs.getString("loggedInUser", null);

        // If no user is logged in, finish the activity
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Please log in to access notification settings", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Use username-specific preferences
        prefs = getSharedPreferences("MoodTracker_" + currentUsername, MODE_PRIVATE);

        notificationSwitch = findViewById(R.id.notificationSwitch);
        timePickerButton = findViewById(R.id.timePickerButton);
        backButton = findViewById(R.id.backButton);

        // Check and request notification permission for Android 13+
        checkNotificationPermission();

        // Set initial states based on user preferences
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
        notificationSwitch.setChecked(notificationsEnabled);

        // Load saved time for current user
        int savedHour = prefs.getInt("reminder_hour", 20);
        int savedMinute = prefs.getInt("reminder_minute", 0);
        updateTimeButtonText(savedHour, savedMinute);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !areNotificationsEnabled()) {
                notificationSwitch.setChecked(false);
                openNotificationSettings();
            } else {
                prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
                int hour = prefs.getInt("reminder_hour", 20);
                int minute = prefs.getInt("reminder_minute", 0);
                scheduleNotification(hour, minute);
            }
        });

        timePickerButton.setOnClickListener(v -> showTimePickerDialog());
        backButton.setOnClickListener(v -> finish());
    }



    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                notificationSwitch.setChecked(false);
            }
        }
    }

    private boolean areNotificationsEnabled() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        return notificationManager.areNotificationsEnabled();
    }

    private void openNotificationSettings() {
        Toast.makeText(this, "Please enable notifications for the app",
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    private void showTimePickerDialog() {
        int currentHour = prefs.getInt("reminder_hour", 20);
        int currentMinute = prefs.getInt("reminder_minute", 0);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // Save selected time
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("reminder_hour", hourOfDay);
                    editor.putInt("reminder_minute", minute);
                    editor.apply();

                    updateTimeButtonText(hourOfDay, minute);

                    // Schedule the notification
                    scheduleNotification(hourOfDay, minute);

                    // Show confirmation toast
                    Toast.makeText(this, "Reminder set for " + timePickerButton.getText(),
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Time set to: " + hourOfDay + ":" + minute);
                },
                currentHour,
                currentMinute,
                false
        );
        timePickerDialog.show();
    }

    private void scheduleNotification(int hour, int minute) {
        if (currentUsername == null) return;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("MOOD_REMINDER_ALARM");
        intent.putExtra("username", currentUsername);

        // Create unique request code based on username
        int requestCode = currentUsername.hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel any existing alarms for this user
        alarmManager.cancel(pendingIntent);

        // Only schedule if notifications are enabled
        if (notificationSwitch.isChecked()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "Alarm scheduled for user " + currentUsername + " at: " + calendar.getTime().toString());
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to schedule alarm for user " + currentUsername, e);
            }
        }
    }
    private boolean checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return false;
            }
        }
        return true;
    }

    private void updateTimeButtonText(int hour, int minute) {
        String timeString = String.format("%02d:%02d %s",
                hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour),
                minute,
                hour >= 12 ? "PM" : "AM");
        timePickerButton.setText(timeString);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Use the saved preference instead of system notification state
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
        notificationSwitch.setChecked(notificationsEnabled);
    }
}
