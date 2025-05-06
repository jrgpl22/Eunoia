package com.example.moodtracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EmotionEdit extends AppCompatActivity {

    private TextView dateCNFRM2, textCF2;
    private ImageView emotionCF2, x2;
    private EditText notesInput2;
    private ScrollView scrollView2;
    private MoodDatabaseHelper dbHelper;
    private String selectedDate;
    private String selectedEmotion;
    private float startY;
    private static final float DRAG_THRESHOLD = 10;
    private boolean isDragging = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emotion_edit);
        dbHelper = new MoodDatabaseHelper(this);
        dateCNFRM2 = findViewById(R.id.dateCNFRM2);
        textCF2 = findViewById(R.id.textCF2);
        emotionCF2 = findViewById(R.id.emotionCF2);
        x2 = findViewById(R.id.x2);
        notesInput2 = findViewById(R.id.notesInput2);

        scrollView2 = findViewById(R.id.scrollView2);

        scrollView2.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    isDragging = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float deltaY = Math.abs(event.getY() - startY);
                    if (deltaY > DRAG_THRESHOLD) {
                        isDragging = true;
                        notesInput2.clearFocus();
                        // Allow parent to handle the scroll
                        scrollView2.requestDisallowInterceptTouchEvent(false);
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
        notesInput2.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // If we're not dragging, allow text editing
                if (!isDragging) {
                    notesInput2.requestFocus();
                    return false;
                }
            }
            // If we're dragging, don't handle the touch event
            return isDragging;
        });

        // Enable vertical scrolling for EditText
        notesInput2.setVerticalScrollBarEnabled(true);

        Button saveButton = findViewById(R.id.save2);

        selectedDate = getIntent().getStringExtra("selectedDate");
        selectedEmotion = getIntent().getStringExtra("emotion");
        String text = getIntent().getStringExtra("text");

        if (selectedDate == null || selectedEmotion == null || text == null) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        String existingNotes = dbHelper.getNotesByDate(selectedDate, username);
        if (existingNotes != null && !existingNotes.isEmpty()) {
            notesInput2.setText(existingNotes);
        }

        dateCNFRM2.setText(selectedDate);
        textCF2.setText(text);

        updateEmotionView(selectedEmotion);

        emotionCF2.setOnClickListener(v -> {
            Intent intent = new Intent(EmotionEdit.this, EditInput.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("emotion", selectedEmotion);
            intent.putExtra("text", text);
            startActivityForResult(intent, 1);
        });

        saveButton.setOnClickListener(v -> {
            String notes = notesInput2.getText().toString().trim();
            dbHelper.insertOrUpdateMood(selectedDate, selectedEmotion, notes, username);

            // Create result intent with all necessary data
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDate", selectedDate);
            resultIntent.putExtra("selectedEmotion", selectedEmotion);
            resultIntent.putExtra("notes", notes);

            // Send broadcast to update calendar
            Intent updateIntent = new Intent("com.example.moodtracker.UPDATE_CALENDAR");
            sendBroadcast(updateIntent);

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        x2.setOnClickListener(v -> showExitConfirmationDialog());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            selectedEmotion = data.getStringExtra("selectedEmotion");
            updateEmotionView(selectedEmotion);
        }
    }

    private void updateEmotionView(String emotion) {
        switch (emotion) {
            case "calm":
                emotionCF2.setImageResource(R.drawable.embarressed);
                textCF2.setText("Shy");
                break;
            case "happy":
                emotionCF2.setImageResource(R.drawable.joyful);
                textCF2.setText("Happy");
                break;
            case "sad":
                emotionCF2.setImageResource(R.drawable.sadness);
                textCF2.setText("Sad");
                break;
            case "angry":
                emotionCF2.setImageResource(R.drawable.anger);
                textCF2.setText("Angry");
                break;
            case "neutral":
                emotionCF2.setImageResource(R.drawable.bored);
                textCF2.setText("Bored");
                break;
            case "fear":
                emotionCF2.setImageResource(R.drawable.fear);
                textCF2.setText("Fear");
                break;
            default:
                emotionCF2.setImageResource(R.drawable.calm);
                textCF2.setText("No emotion recorded.");
                break;
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? Any unsaved changes will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> finish())
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
