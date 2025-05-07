package com.example.moodtracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.InputStream;

public class ImgPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_preview);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        ImageView previewImg = findViewById(R.id.previewImg);
        TextView errorText = findViewById(R.id.errorText);

        // Check if we should display the default image
        boolean isDefaultImage = getIntent().getBooleanExtra("isDefaultImage", false);
        String imageUriString = getIntent().getStringExtra("imageUri");

        if (isDefaultImage) {
            // Show default profile image
            try {
                previewImg.setImageResource(R.drawable.profile);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading default profile image", Toast.LENGTH_SHORT).show();
                errorText.setVisibility(View.VISIBLE);
                errorText.setText("Could not load image");
            }
        } else if (imageUriString != null && !imageUriString.isEmpty()) {
            // Show custom profile image
            try {
                Uri imageUri = Uri.parse(imageUriString);

                // Check if the image is a file:// URI (from our internal storage)
                if (imageUri.getScheme().equals("file")) {
                    File imageFile = new File(imageUri.getPath());
                    if (imageFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        if (bitmap != null) {
                            previewImg.setImageBitmap(bitmap);
                        } else {
                            throw new Exception("Failed to decode bitmap");
                        }
                    } else {
                        throw new Exception("Image file does not exist");
                    }
                } else {
                    // For content:// URIs
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            previewImg.setImageBitmap(bitmap);
                            inputStream.close();
                        } else {
                            throw new Exception("Failed to decode bitmap");
                        }
                    } else {
                        throw new Exception("Failed to open input stream");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                // Fall back to default image if custom image fails
                try {
                    previewImg.setImageResource(R.drawable.profile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Could not load image");
                }
            }
        } else {
            // No valid image URI was provided, show default image
            try {
                previewImg.setImageResource(R.drawable.profile);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading default image", Toast.LENGTH_SHORT).show();
                errorText.setVisibility(View.VISIBLE);
                errorText.setText("Could not load image");
            }
        }

        // Clicking anywhere closes the preview
        previewImg.setOnClickListener(v -> finish());

        // Also add click listener to the whole layout
        findViewById(R.id.previewLayout).setOnClickListener(v -> finish());
    }
}