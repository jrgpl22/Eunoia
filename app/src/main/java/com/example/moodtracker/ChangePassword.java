package com.example.moodtracker;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;

public class ChangePassword extends AppCompatActivity {
    private EditText oldPassword, newPassword, confirmPassword;
    private ImageView toggleOldPass, toggleNewPass, toggleConfirmPass;
    private Button changePasswordButton;
    private boolean isOldPassVisible = false, isNewPassVisible = false, isConfirmPassVisible = false;
    private AccountDatabaseHelper dbHelper;

    // Match these with your other activities
    private static final String PREFS_NAME = "MoodTracker";
    private static final String KEY_USERNAME = "loggedInUser";
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize database helper
        dbHelper = new AccountDatabaseHelper(this);

        // Get current logged-in username using the same keys as other activities
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString(KEY_USERNAME, "");

        if (currentUsername.isEmpty()) {
            Toast.makeText(this, "No user is currently logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.newPassword2);
        toggleOldPass = findViewById(R.id.toggleOldPass);
        toggleNewPass = findViewById(R.id.toggleNewPass);
        toggleConfirmPass = findViewById(R.id.toggleConfirmPass);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        // Set Click Listeners for Password Visibility Toggle
        toggleOldPass.setOnClickListener(view -> togglePasswordVisibility(oldPassword, toggleOldPass, isOldPassVisible = !isOldPassVisible));
        toggleNewPass.setOnClickListener(view -> togglePasswordVisibility(newPassword, toggleNewPass, isNewPassVisible = !isNewPassVisible));
        toggleConfirmPass.setOnClickListener(view -> togglePasswordVisibility(confirmPassword, toggleConfirmPass, isConfirmPassVisible = !isConfirmPassVisible));

        // Handle Change Password Button Click
        changePasswordButton.setOnClickListener(view -> {
            String oldPass = oldPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            // Validate all fields are filled
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(ChangePassword.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if old password is correct
            if (!dbHelper.isValidUser(currentUsername, oldPass)) {
                Toast.makeText(ChangePassword.this, "Current password is incorrect!", Toast.LENGTH_SHORT).show();
                oldPassword.setText("");
                return;
            }

            // Validate password length
            if (newPass.length() < MIN_PASSWORD_LENGTH) {
                Toast.makeText(ChangePassword.this, "New password must be at least 8 characters long!", Toast.LENGTH_SHORT).show();
                newPassword.setText("");
                confirmPassword.setText("");
                return;
            }

            // Validate passwords match
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(ChangePassword.this, "New passwords do not match!", Toast.LENGTH_SHORT).show();
                newPassword.setText("");
                confirmPassword.setText("");
                return;
            }

            // Update password in database
            if (dbHelper.updatePassword(currentUsername, newPass)) {
                Toast.makeText(ChangePassword.this, "Password successfully changed!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ChangePassword.this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_eye_open);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_eye_closed);
        }
    }
}