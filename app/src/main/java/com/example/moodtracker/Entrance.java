package com.example.moodtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class Entrance extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login state immediately
        sharedPreferences = getSharedPreferences("MoodTracker", MODE_PRIVATE);
        if (!sharedPreferences.getString("loggedInUser", "").isEmpty()) {
            // User is logged in, go directly to MainActivity
            startActivity(new Intent(Entrance.this, MainActivity.class));
            finish();
            return;
        }

        // If not logged in, show the splash screen
        setContentView(R.layout.activity_main3);

        Button loginbck3 = findViewById(R.id.loginbck3);
        Button register3 = findViewById(R.id.register3);
        ImageView gifImageView = findViewById(R.id.puppy);

        Glide.with(this)
                .asGif()
                .load(R.drawable.puppy2)
                .into(gifImageView);

        loginbck3.setOnClickListener(view -> {
            startActivity(new Intent(Entrance.this, Login.class));
        });

        register3.setOnClickListener(view -> {
            startActivity(new Intent(Entrance.this, Register.class));
        });
    }
}
