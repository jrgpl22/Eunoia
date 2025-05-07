package com.example.moodtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmotionsInput extends AppCompatActivity {

    private String selectedDate;
    private String currentEmotion;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("EmotionConfirmation", "Starting onCreate");
        setContentView(R.layout.activity_emotions_input);

        selectedDate = getIntent().getStringExtra("selectedDate");

        TextView dateInput = findViewById(R.id.dateinput);
        if (dateInput != null) {
            dateInput.setText(selectedDate);
        }
        TextView questionTextView = findViewById(R.id.textView);
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        LocalDate selectedLocalDate = LocalDate.parse(selectedDate, formatter);

        if (selectedLocalDate.equals(currentDate)) {
            questionTextView.setText("How are you feeling today?");
        } else {
            questionTextView.setText("How are you feeling\nthat day?");


        }
        SharedPreferences sharedPreferences = getSharedPreferences("MoodTracker", MODE_PRIVATE);
        currentEmotion = sharedPreferences.getString(selectedDate, "");

        // Emotion buttons
        ImageView happy = findViewById(R.id.happy);
        ImageView calm = findViewById(R.id.calm);
        ImageView neutral = findViewById(R.id.neutral);
        ImageView sad = findViewById(R.id.sad);
        ImageView angry = findViewById(R.id.angry);
        ImageView fear = findViewById(R.id.fear);

        View.OnClickListener emotionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emotion = "";
                if (v.getId() == R.id.happy) {
                    emotion = "happy";
                } else if (v.getId() == R.id.calm) {
                    emotion = "calm";
                } else if (v.getId() == R.id.neutral) {
                    emotion = "neutral";
                } else if (v.getId() == R.id.sad) {
                    emotion = "sad";
                } else if (v.getId() == R.id.angry) {
                    emotion = "angry";
                } else if (v.getId() == R.id.fear) {
                    emotion = "fear";
                }
                handleEmotionSelection(emotion);
            }
        };

        happy.setOnClickListener(emotionClickListener);
        calm.setOnClickListener(emotionClickListener);
        neutral.setOnClickListener(emotionClickListener);
        sad.setOnClickListener(emotionClickListener);
        angry.setOnClickListener(emotionClickListener);
        fear.setOnClickListener(emotionClickListener);

        // Back button
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finishWithAnimation());
    }

    private void handleEmotionSelection(String emotion) {
        try {
            Log.d("EmotionsInput", "Starting handleEmotionSelection with emotion: " + emotion);
            Log.d("EmotionsInput", "Selected date: " + selectedDate);

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            String selectedTime = now.format(timeFormatter);

            Intent intent = new Intent(EmotionsInput.this, EmotionConfirmation.class);
            intent.putExtra("selectedEmotion", emotion);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("selectedTime", selectedTime);

            Log.d("EmotionsInput", "Starting EmotionConfirmation activity");
            startActivityForResult(intent, 2);
        } catch (Exception e) {
            Log.e("EmotionsInput", "Error in handleEmotionSelection: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error selecting emotion", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Log.d("EmotionsInput", "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == 2) {
                if (resultCode == RESULT_OK && data != null) {
                    String selectedDate = data.getStringExtra("selectedDate");
                    String selectedEmotion = data.getStringExtra("selectedEmotion");
                    String notes = data.getStringExtra("notes");

                    Log.d("EmotionsInput", "Result data - date: " + selectedDate + ", emotion: " + selectedEmotion);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedDate", selectedDate);
                    resultIntent.putExtra("selectedEmotion", selectedEmotion);
                    resultIntent.putExtra("notes", notes);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Log.d("EmotionsInput", "Activity result not OK or data is null");
                }
            }
        } catch (Exception e) {
            Log.e("EmotionsInput", "Error in onActivityResult: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Emotion")
                .setMessage("Are you sure you want to delete the emotion for this day?")
                .setPositiveButton("Yes", (dialog, id) -> deleteEmotion())
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    private void deleteEmotion() {
        SharedPreferences sharedPreferences = getSharedPreferences("MoodTracker", MODE_PRIVATE);
        sharedPreferences.edit().remove(selectedDate).apply();
        setResult(RESULT_OK);
        finish();
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
