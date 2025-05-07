package com.example.moodtracker;

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
    private Button logoutButton, deleteDataButton, changePasswordButton;
    private Button notificationButton, accountButton, helpButton;
    private ImageView editProfileButton;
    private ShapeableImageView profileImgButton;
    private MoodDatabaseHelper dbHelper;
    private String username;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        dbHelper = new MoodDatabaseHelper(requireContext());

        accName = view.findViewById(R.id.ACC_NAME);
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
    }
}