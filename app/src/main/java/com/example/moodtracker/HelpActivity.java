package com.example.moodtracker;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class HelpActivity extends AppCompatActivity {

    private ImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        gifImageView = findViewById(R.id.gifImageView);
        gifImageView.setVisibility(View.GONE);
        // Load GIF if needed (using Glide)
        Glide.with(this)
                .asGif()
                .load(R.drawable.puppy2) // Replace with your GIF resource
                .into(gifImageView);
    }
}