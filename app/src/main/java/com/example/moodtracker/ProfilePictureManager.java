package com.example.moodtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class to manage profile picture operations
 */
public class ProfilePictureManager {
    private static final String TAG = "ProfilePictureManager";
    private static final String PREFS_NAME = "MoodTracker";
    private static final String PROFILE_PIC_DIR = "profile_pictures";

    /**
     * Saves a profile picture from a selected Uri
     * @param context Application context
     * @param username Current username
     * @param sourceUri Uri of the selected image
     * @return true if save was successful
     */
    public static boolean saveProfilePicture(Context context, String username, Uri sourceUri) {
        if (context == null || username == null || username.isEmpty() || sourceUri == null) {
            return false;
        }

        try {
            // Create directory for profile pictures if it doesn't exist
            File profileDir = new File(context.getFilesDir(), PROFILE_PIC_DIR);
            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }

            // Create a file to save the image
            File destFile = new File(profileDir, username + "_profile.jpg");

            // Copy from URI to internal storage
            try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(destFile)) {

                if (in == null) {
                    return false;
                }

                // Copy the file
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            // Save the path in SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(username + "_profile_image_path", destFile.getAbsolutePath()).apply();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving profile picture", e);
            Toast.makeText(context, "Error saving profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Loads a profile picture into the provided ImageView
     * @param context Application context
     * @param username Current username
     * @param imageView ImageView to load the picture into
     * @return true if loading was successful
     */
    public static boolean loadProfilePicture(Context context, String username, ImageView imageView) {
        if (context == null || username == null || username.isEmpty() || imageView == null) {
            return false;
        }

        // Get the saved path from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String imagePath = prefs.getString(username + "_profile_image_path", "");

        if (!imagePath.isEmpty()) {
            try {
                // Load image from file
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile picture", e);
            }
        }

        // If we couldn't load the image, set the default
        try {
            imageView.setImageResource(R.drawable.profile);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error loading default profile image", e);
            return false;
        }
    }

    /**
     * Gets the profile picture URI for displaying in full-screen preview
     * @param context Application context
     * @param username Current username
     * @return Uri to the profile picture or null if not found
     */
    public static Uri getProfilePictureUri(Context context, String username) {
        if (context == null || username == null || username.isEmpty()) {
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String imagePath = prefs.getString(username + "_profile_image_path", "");

        if (!imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return Uri.fromFile(imageFile);
            }
        }

        return null;
    }

    /**
     * Checks if a user has a custom profile picture
     * @param context Application context
     * @param username Current username
     * @return true if the user has a custom profile picture
     */
    public static boolean hasCustomProfilePicture(Context context, String username) {
        if (context == null || username == null || username.isEmpty()) {
            return false;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String imagePath = prefs.getString(username + "_profile_image_path", "");

        if (!imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            return imageFile.exists();
        }

        return false;
    }
}