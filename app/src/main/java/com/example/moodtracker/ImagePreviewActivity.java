package com.example.moodtracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ImagePreviewActivity extends AppCompatActivity {

    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);

        // Get the image URI from intent and display it
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("imageUri")) {
            Uri imageUri = Uri.parse(intent.getStringExtra("imageUri"));
            fullScreenImageView.setImageURI(imageUri);
        }
    }
}
