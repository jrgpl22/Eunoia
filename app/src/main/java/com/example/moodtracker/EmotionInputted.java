package com.example.moodtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

public class EmotionInputted extends AppCompatActivity {

    private TextView dateCNFRM3, textCF3;
    private ImageView emotionCF3, back2, dotsMenu;
    private MoodDatabaseHelper dbHelper;
    private String selectedDate;
    private TextView notesDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_inputted);

        dbHelper = new MoodDatabaseHelper(this);

        dateCNFRM3 = findViewById(R.id.dateCNFRM3);
        textCF3 = findViewById(R.id.textCF3);
        emotionCF3 = findViewById(R.id.emotionCF3);
        back2 = findViewById(R.id.back2);
        dotsMenu = findViewById(R.id.dots);
        notesDisplay = findViewById(R.id.notesDisplay);

        selectedDate = getIntent().getStringExtra("selectedDate");
        String emotion = getIntent().getStringExtra("emotion");
        String text = getIntent().getStringExtra("text");


        if (selectedDate == null || emotion == null || text == null) {
            Toast.makeText(this, "Error: Missing data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        String notes = dbHelper.getNotesByDate(selectedDate, username);
        Log.d("EmotionInputted", "Retrieved notes for date " + selectedDate + ": " + notes);

        if (notes != null && !notes.isEmpty()) {
            notesDisplay.setText(notes);
            Log.d("EmotionInputted", "Setting notes to display: " + notes);
        } else {
            notesDisplay.setText("No notes added");
            Log.d("EmotionInputted", "No notes found for date: " + selectedDate);

        // Rest of your onCreate code...
    }

        dateCNFRM3.setText(selectedDate);
        textCF3.setText(text);

        switch (emotion) {
            case "calm":
                emotionCF3.setImageResource(R.drawable.embarressed);
                break;
            case "happy":
                emotionCF3.setImageResource(R.drawable.joyful);
                break;
            case "sad":
                emotionCF3.setImageResource(R.drawable.sadness);
                break;
            case "angry":
                emotionCF3.setImageResource(R.drawable.anger);
                break;
            case "neutral":
                emotionCF3.setImageResource(R.drawable.bored);
                break;
            case "fear":
                emotionCF3.setImageResource(R.drawable.fear);
                break;
            default:
                emotionCF3.setImageResource(R.drawable.calm);
                break;
        }

        back2.setOnClickListener(v -> finish());

        dotsMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(EmotionInputted.this, dotsMenu);
            getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.editOption) {
                    Intent intent = new Intent(EmotionInputted.this, EmotionEdit.class);
                    intent.putExtra("selectedDate", selectedDate);
                    intent.putExtra("emotion", emotion);
                    intent.putExtra("text", text);
                    startActivityForResult(intent, 1);
                    return true;
                } else if (itemId == R.id.deleteOption) {
                    showDeleteConfirmationDialog();
                    return true;
                } else if (itemId == R.id.cancelOption) {
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        });
    }
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this emotion?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteEmotion(selectedDate);
                    // Send a broadcast to update the calendar view
                    Intent intent = new Intent("com.example.moodtracker.UPDATE_CALENDAR");
                    sendBroadcast(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteEmotion(String date) {
        String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        dbHelper.deleteMood(selectedDate, username);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}