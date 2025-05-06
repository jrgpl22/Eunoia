package com.example.moodtracker;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class AboutActivity extends AppCompatActivity {

    private ImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        gifImageView = findViewById(R.id.gifImageView);

        // Load GIF if needed (using Glide)
        Glide.with(this)
                .asGif()
                .load(R.drawable.puppy2) // Replace with your GIF resource
                .into(gifImageView);
    }
}