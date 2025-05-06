package com.example.moodtracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class Register extends AppCompatActivity {
    Button loginbck, registerbtn;
    TextView error2;
    EditText username2, password2;
    ImageView gifImageView;
    private AccountDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new AccountDatabaseHelper(this);

        loginbck = findViewById(R.id.loginbck);
        registerbtn = findViewById(R.id.registerbtn);
        username2 = findViewById(R.id.username2);
        password2 = findViewById(R.id.password2);
        error2 = findViewById(R.id.error2);
        gifImageView = findViewById(R.id.puppy3);

        // Load GIF using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.puppy2)
                .into(gifImageView);

        loginbck.setOnClickListener(view -> {
            Intent in = new Intent(Register.this, Login.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
            finish();
        });

        registerbtn.setOnClickListener(view -> {
            String username = username2.getText().toString();
            String password = password2.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                error2.setText("Insert Value");
            } else if (username.length() < 8 || password.length() < 8) {
                error2.setText("Minimum 8 characters is required");
                username2.setText("");
                password2.setText("");
            } else if (dbHelper.isUsernameExists(username)) {
                error2.setText("Username Already Exists");
                username2.setText("");
                password2.setText("");
            } else {
                if (dbHelper.addUser(username, password)) {
                    username2.setText("");
                    password2.setText("");
                    error2.setText("Account Created");
                } else {
                    error2.setText("Failed to create account");
                }
            }
        });
    }
}