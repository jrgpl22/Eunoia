package com.example.moodtracker;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class Loading extends AppCompatActivity {

    private ImageView joy, sad, anger, bored, fear, shy;
    private Handler handler = new Handler();
    private int animationDelay = 5; // Faster delay for a smooth domino effect
    private int animationDuration = 400; // Slightly fast but still smooth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Load GIF into ImageView
        ImageView gifImageView = findViewById(R.id.puppyf);
        Glide.with(this)
                .asGif()
                .load(R.drawable.puppyf)
                .into(gifImageView);

        // Initialize emojis
        joy = findViewById(R.id.joy);
        sad = findViewById(R.id.sad);
        anger = findViewById(R.id.anger);
        shy = findViewById(R.id.shy);
        bored = findViewById(R.id.bored);
        fear = findViewById(R.id.fear);

        // Start domino wave animation
        startDominoWave();

        // Move to next activity after 5 seconds
        handler.postDelayed(() -> {
            // Check if the user is logged in
            SharedPreferences prefs = getSharedPreferences("MoodTracker", MODE_PRIVATE);
            String loggedInUser = prefs.getString("loggedInUser", "");

            Intent intent;
            if (!loggedInUser.isEmpty()) {
                // User is logged in, go to MainActivity
                intent = new Intent(Loading.this, MainActivity.class);
            } else {
                // User is not logged in, go to login/register page
                intent = new Intent(Loading.this, Entrance.class);
            }

            // Clear the back stack and start the new activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close the Loading activity
        }, 5000); // 5 seconds delay
    }

    private void startDominoWave() {
        handler.postDelayed(() -> animateEmoji(joy, () ->
                animateEmoji(sad, () ->
                        animateEmoji(anger, () ->
                                animateEmoji(shy, () ->
                                        animateEmoji(bored, () ->
                                                animateEmoji(fear, this::startDominoWave) // Loop back to Joy
                                        )
                                )
                        )
                )
        ), animationDelay);
    }

    private void animateEmoji(ImageView emoji, Runnable nextAnimation) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(emoji, "translationY", 0, -50, 0);
        animator.setDuration(animationDuration);
        animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                handler.postDelayed(nextAnimation, animationDelay);
            }
        });
        animator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Prevent memory leaks
    }
}