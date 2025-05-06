package com.example.moodtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class Login extends AppCompatActivity {
    private EditText username, password;
    private TextView error;
    private AccountDatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MoodTracker", MODE_PRIVATE);

        // Check if user is already logged in
        if (isLoggedIn()) {
            startMainActivity();
            finish();
            return;
        }

        dbHelper = new AccountDatabaseHelper(this);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        error = findViewById(R.id.error);
        Button loginBtn = findViewById(R.id.loginbutton);
        Button registerBtn = findViewById(R.id.register);
        ImageView gifImageView = findViewById(R.id.puppy2);

        // Load GIF using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.puppy2)
                .into(gifImageView);

        loginBtn.setOnClickListener(view -> handleLogin());

        registerBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
    }

    private boolean isLoggedIn() {
        return !sharedPreferences.getString("loggedInUser", "").isEmpty();
    }

    private void handleLogin() {
        String inputUsername = username.getText().toString();
        String inputPassword = password.getText().toString();

        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
            error.setText("Please fill in all fields");
            return;
        }

        if (dbHelper.isValidUser(inputUsername, inputPassword)) {
            // Save login state
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("loggedInUser", inputUsername);
            editor.apply();

            startMainActivity();
            finish();
        } else {
            error.setText("Invalid username or password");
            username.setText("");
            password.setText("");
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}