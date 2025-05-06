package com.example.moodtracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EmotionConfirmation extends AppCompatActivity {

    private String selectedEmotion;
    private String selectedDate;
    private String selectedTime;
    private MoodDatabaseHelper dbHelper;
    private EditText notesInput;
    private ScrollView scrollView;
    private float startY;
    private static final float DRAG_THRESHOLD = 10;
    private boolean isDragging = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbHelper = new MoodDatabaseHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_confirmation);

        notesInput = findViewById(R.id.notesInput);
        scrollView = findViewById(R.id.scrollView);

        // Improved ScrollView touch handling
        scrollView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    isDragging = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float deltaY = Math.abs(event.getY() - startY);
                    if (deltaY > DRAG_THRESHOLD) {
                        isDragging = true;
                        notesInput.clearFocus();
                        // Allow parent to handle the scroll
                        scrollView.requestDisallowInterceptTouchEvent(false);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isDragging = false;
                    break;
            }
            // Don't consume the event - let it propagate to handle scrolling
            return false;
        });

        // Improved EditText touch handling
        notesInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // If we're not dragging, allow text editing
                if (!isDragging) {
                    notesInput.requestFocus();
                    return false;
                }
            }
            // If we're dragging, don't handle the touch event
            return isDragging;
        });

        // Enable vertical scrolling for EditText
        notesInput.setVerticalScrollBarEnabled(true);


        selectedEmotion = getIntent().getStringExtra("selectedEmotion");
        selectedDate = getIntent().getStringExtra("selectedDate");
        selectedTime = getIntent().getStringExtra("selectedTime");

        ImageView emotionCF = findViewById(R.id.emotionCF);
        TextView textCF = findViewById(R.id.textCF);
        TextView dateCnfrm = findViewById(R.id.dateCNFRM);

        String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        String existingNotes = dbHelper.getNotesByDate(selectedDate, username);
        if (existingNotes != null && !existingNotes.isEmpty()) {
            notesInput.setText(existingNotes);
        }

        // Set selected date in the UI
        dateCnfrm.setText(selectedDate);

        // Set emotion display
        switch (selectedEmotion) {
            case "happy":
                emotionCF.setImageResource(R.drawable.joyful);
                textCF.setText("Happy");
                break;
            case "calm":
                emotionCF.setImageResource(R.drawable.embarressed);
                textCF.setText("Shy");
                break;
            case "neutral":
                emotionCF.setImageResource(R.drawable.bored);
                textCF.setText("Bored");
                break;
            case "sad":
                emotionCF.setImageResource(R.drawable.sadness);
                textCF.setText("Sad");
                break;
            case "angry":
                emotionCF.setImageResource(R.drawable.anger);
                textCF.setText("Angry");
                break;
            case "fear":
                emotionCF.setImageResource(R.drawable.fear);
                textCF.setText("Fear");
                break;
        }

        Button saveButton = findViewById(R.id.save);
        saveButton.setOnClickListener(v -> saveEmotion());

        ImageView closeButton = findViewById(R.id.x);
        closeButton.setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void saveEmotion() {
        try {
            if (selectedDate == null || selectedEmotion == null) {
                Log.e("EmotionConfirmation", "Missing data: Date or Emotion is null");
                return;
            }

            String notes = notesInput != null ? notesInput.getText().toString().trim() : "";
            Log.d("EmotionConfirmation", "Saving notes: " + notes);

            // Save to database
            String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
            dbHelper.insertOrUpdateMood(selectedDate, selectedEmotion, notes, username);

            // Send broadcast to update calendar
            Intent updateIntent = new Intent("com.example.moodtracker.UPDATE_CALENDAR");
            sendBroadcast(updateIntent);

            // Add notes to result intent
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDate", selectedDate);
            resultIntent.putExtra("selectedEmotion", selectedEmotion);
            resultIntent.putExtra("notes", notes);
            resultIntent.putExtra("updateToCurrentMonth", getIntent().getBooleanExtra("updateToCurrentMonth", false));
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            Log.e("EmotionConfirmation", "Error saving emotion: " + e.getMessage());
            Toast.makeText(this, "Error saving emotion", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Confirmation")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Instead of starting a new activity, just finish this one
                    // and set result to let the caller know user cancelled
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedDate", selectedDate);
                    setResult(RESULT_CANCELED, resultIntent);
                    finish();
                    // Add animation for smooth transition
                    overridePendingTransition(R.anim.no_animation, R.anim.slide_down);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}
