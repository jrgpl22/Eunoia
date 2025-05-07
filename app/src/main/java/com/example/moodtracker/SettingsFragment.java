package com.example.moodtracker;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {
    private TextView accName;
    private TextView editUsernameText;
    private Button logoutButton, deleteDataButton, changePasswordButton;
    private Button notificationButton, accountButton, helpButton;
    private ImageView editProfileButton;
    private ShapeableImageView profileImgButton;
    private MoodDatabaseHelper dbHelper;
    private AccountDatabaseHelper accountDbHelper;
    private String username;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        dbHelper = new MoodDatabaseHelper(requireContext());
        accountDbHelper = new AccountDatabaseHelper(requireContext());

        accName = view.findViewById(R.id.ACC_NAME);
        editUsernameText = view.findViewById(R.id.editusername);
        logoutButton = view.findViewById(R.id.logout);
        deleteDataButton = view.findViewById(R.id.delete_data);
        notificationButton = view.findViewById(R.id.notifation);
        changePasswordButton = view.findViewById(R.id.changePasswordBtn);
        accountButton = view.findViewById(R.id.accountButton);
        helpButton = view.findViewById(R.id.helpButton);
        editProfileButton = view.findViewById(R.id.editbtn);
        profileImgButton = view.findViewById(R.id.profileimg);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE);
        username = prefs.getString("loggedInUser", "");
        accName.setText(username);

        // Load the profile picture using the new manager
        ProfilePictureManager.loadProfilePicture(requireContext(), username, profileImgButton);

        // Launcher for selecting a new image (only used by edit button)
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Use the ProfilePictureManager to save the profile picture
                            if (ProfilePictureManager.saveProfilePicture(requireContext(), username, uri)) {
                                // Reload the profile picture
                                ProfilePictureManager.loadProfilePicture(requireContext(), username, profileImgButton);
                                Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to save profile picture", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error updating profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Button actions
        logoutButton.setOnClickListener(v -> showLogoutDialog());
        deleteDataButton.setOnClickListener(v -> showDeleteDataDialog());
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationSettingsActivity.class);
            startActivity(intent);
        });
        accountButton.setOnClickListener(v -> openAboutPage());
        helpButton.setOnClickListener(v -> openHelpPage());
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePassword.class);
            startActivity(intent);
        });

        // Edit username action
        editUsernameText.setOnClickListener(v -> showChangeUsernameDialog());

        // Edit profile photo using edit button
        editProfileButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // View profile image when image is clicked
        profileImgButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ImgPreviewActivity.class);

            // Check if the user has a custom profile picture
            if (ProfilePictureManager.hasCustomProfilePicture(requireContext(), username)) {
                Uri imageUri = ProfilePictureManager.getProfilePictureUri(requireContext(), username);
                if (imageUri != null) {
                    intent.putExtra("imageUri", imageUri.toString());
                    intent.putExtra("isDefaultImage", false);
                } else {
                    intent.putExtra("isDefaultImage", true);
                }
            } else {
                // Show default image in preview activity
                intent.putExtra("isDefaultImage", true);
            }

            startActivity(intent);
        });

        return view;
    }

    private void setupButtonEffects() {
        // List all your buttons
        Button[] buttons = new Button[] {
                logoutButton,
                deleteDataButton,
                notificationButton,
                changePasswordButton,
                accountButton,
                helpButton
        };

        // Apply effects to each button
        for (Button button : buttons) {
            applyButtonEffects(button);
        }

        // Apply effects to other clickable views
        applyClickEffects(editUsernameText);
        applyClickEffects(editProfileButton);
        applyClickEffects(profileImgButton);
    }

    private void applyButtonEffects(final Button button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // When button is pressed
                        animateButtonPress(button);
                        return false;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // When button is released
                        animateButtonRelease(button);
                        return false;
                }
                return false;
            }
        });
    }

    private void applyClickEffects(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // When view is pressed
                        animateViewPress(view);
                        return false;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // When view is released
                        animateViewRelease(view);
                        return false;
                }
                return false;
            }
        });
    }

    private void animateButtonPress(Button button) {
        // Scale down slightly
        button.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(100)
                .start();

        // Add shadow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setElevation(button, 8f);
        }
    }

    private void animateButtonRelease(Button button) {
        // Scale back to normal
        button.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(100)
                .start();

        // Remove shadow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setElevation(button, 0f);
        }
    }

    private void animateViewPress(View view) {
        // Apply slight darkening effect
        view.animate()
                .alpha(0.7f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .start();
    }

    private void animateViewRelease(View view) {
        // Return to normal
        view.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(100)
                .start();
    }

// Call setupButtonEffects() at the end of your onCreateView method
// Add this line right before the "return view;" statement:
// setupButtonEffects();

    private void showChangeUsernameDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_username, null);

        EditText newUsernameInput = dialogView.findViewById(R.id.new_username);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUsername = newUsernameInput.getText().toString().trim();
                    if (newUsername.isEmpty()) {
                        Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newUsername.length() < 6) {
                        Toast.makeText(requireContext(), "Username must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newUsername.equals(username)) {
                        Toast.makeText(requireContext(), "New username is the same as current username", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (accountDbHelper.isUsernameExists(newUsername)) {
                        Toast.makeText(requireContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update the username in the accounts database
                    if (updateUsername(username, newUsername)) {
                        // Update the UI with the new username
                        accName.setText(newUsername);

                        // Update the SharedPreferences with the new username
                        SharedPreferences prefs = requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("loggedInUser", newUsername);
                        editor.apply();

                        // Update the class variable
                        String oldUsername = username;
                        username = newUsername;

                        // Transfer mood data and profile picture to new username
                        transferUserData(oldUsername, newUsername);

                        Toast.makeText(requireContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean updateUsername(String oldUsername, String newUsername) {
        // Update username in the AccountDatabaseHelper
        return accountDbHelper.updateUsername(oldUsername, newUsername);
    }

    private void transferUserData(String oldUsername, String newUsername) {
        // 1. Transfer mood data to the new username
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE mood SET username = ? WHERE username = ?",
                new String[]{newUsername, oldUsername});

        // 2. Transfer profile picture if it exists
        if (ProfilePictureManager.hasCustomProfilePicture(requireContext(), oldUsername)) {
            Uri oldImageUri = ProfilePictureManager.getProfilePictureUri(requireContext(), oldUsername);
            if (oldImageUri != null) {
                try {
                    // Save the image with the new username
                    ProfilePictureManager.saveProfilePicture(requireContext(), newUsername, oldImageUri);

                    // Reload the profile picture
                    ProfilePictureManager.loadProfilePicture(requireContext(), newUsername, profileImgButton);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Error transferring profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // 3. Transfer notification settings if needed (assuming they're username-specific)
        SharedPreferences prefs = requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(oldUsername + "_notifications_enabled", false);
        int notificationHour = prefs.getInt(oldUsername + "_notification_hour", 20);
        int notificationMinute = prefs.getInt(oldUsername + "_notification_minute", 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(newUsername + "_notifications_enabled", notificationsEnabled);
        editor.putInt(newUsername + "_notification_hour", notificationHour);
        editor.putInt(newUsername + "_notification_minute", notificationMinute);

        // 4. Remove old username preferences
        editor.remove(oldUsername + "_notifications_enabled");
        editor.remove(oldUsername + "_notification_hour");
        editor.remove(oldUsername + "_notification_minute");
        editor.apply();

        // 5. Update notifications
        updateNotificationsForNewUsername(oldUsername, newUsername);
    }

    private void updateNotificationsForNewUsername(String oldUsername, String newUsername) {
        // Cancel notifications for the old username
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.setAction("MOOD_REMINDER_ALARM");
        intent.putExtra("username", oldUsername);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                oldUsername.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);

        // Recreate notifications for the new username if they were enabled
        SharedPreferences prefs = requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean(newUsername + "_notifications_enabled", false);

        if (notificationsEnabled) {
            int hour = prefs.getInt(newUsername + "_notification_hour", 20);
            int minute = prefs.getInt(newUsername + "_notification_minute", 0);

            // This assumes there's a static method in some notification helper class
            // If not, you'd need to implement the logic to reschedule notifications
            // NotificationHelper.scheduleNotification(requireContext(), newUsername, hour, minute);

            // Example implementation if no helper class exists:
            // Intent newIntent = new Intent(requireContext(), AlarmReceiver.class);
            // newIntent.setAction("MOOD_REMINDER_ALARM");
            // newIntent.putExtra("username", newUsername);
            // PendingIntent newPendingIntent = PendingIntent.getBroadcast(
            //         requireContext(),
            //         newUsername.hashCode(),
            //         newIntent,
            //         PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            // );
            // Calendar calendar = Calendar.getInstance();
            // calendar.set(Calendar.HOUR_OF_DAY, hour);
            // calendar.set(Calendar.MINUTE, minute);
            // calendar.set(Calendar.SECOND, 0);
            // if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            //     calendar.add(Calendar.DAY_OF_YEAR, 1);
            // }
            // alarmManager.setRepeating(
            //         AlarmManager.RTC_WAKEUP,
            //         calendar.getTimeInMillis(),
            //         AlarmManager.INTERVAL_DAY,
            //         newPendingIntent
            // );
        }
    }

    private void openAboutPage() {
        startActivity(new Intent(requireContext(), AboutActivity.class));
    }

    private void openHelpPage() {
        startActivity(new Intent(requireContext(), HelpActivity.class));
    }

    private void showDeleteDataDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete All Data")
                .setMessage("Are you sure you want to delete all your mood data?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("mood", "username = ?", new String[]{username});
                    db.close();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Success")
                            .setMessage("All mood data has been deleted.")
                            .setPositiveButton("OK", null)
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    cancelUserNotifications();
                    SharedPreferences prefs = requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE);
                    prefs.edit().remove("loggedInUser").apply();
                    Intent intent = new Intent(getActivity(), Entrance.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelUserNotifications() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.setAction("MOOD_REMINDER_ALARM");
        intent.putExtra("username", username);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                username.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (accountDbHelper != null) {
            accountDbHelper.close();
        }
    }
}