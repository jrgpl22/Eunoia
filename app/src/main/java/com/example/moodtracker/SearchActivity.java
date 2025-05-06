package com.example.moodtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private RecyclerView searchRecyclerView;
    private SearchNotesAdapter searchAdapter;
    private MoodDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Initialize views
        searchInput = findViewById(R.id.searchInput);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database helper
        dbHelper = new MoodDatabaseHelper(this);

        // Initialize adapter with empty list
        searchAdapter = new SearchNotesAdapter(new ArrayList<>(), "");
        searchRecyclerView.setAdapter(searchAdapter);

        // Set up search input listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            searchAdapter.updateData(new ArrayList<>(), "");
            return;
        }
        String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        List<MoodDatabaseHelper.MoodEntry> allEntries = dbHelper.getAllMoods(username);
        List<MoodDatabaseHelper.MoodEntry> searchResults = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();

        for (MoodDatabaseHelper.MoodEntry entry : allEntries) {
            // Search in notes
            boolean foundInNotes = entry.notes.toLowerCase().contains(lowercaseQuery);

            // Search in date
            boolean foundInDate = entry.date.toLowerCase().contains(lowercaseQuery);

            // Search in emotion
            boolean foundInEmotion = entry.emotion.toLowerCase().contains(lowercaseQuery);

            // Search in formatted emotion text
            String formattedEmotion = getFormattedEmotion(entry.emotion);
            boolean foundInFormattedEmotion = formattedEmotion.toLowerCase().contains(lowercaseQuery);

            if (foundInNotes || foundInDate || foundInEmotion || foundInFormattedEmotion) {
                searchResults.add(entry);
            }
        }

        searchAdapter.updateData(searchResults, query);
    }

    private String getFormattedEmotion(String emotion) {
        switch (emotion.toLowerCase()) {
            case "calm":
                return "Shy";
            case "happy":
                return "Happy";
            case "sad":
                return "Sad";
            case "angry":
                return "Angry";
            case "neutral":
                return "Bored";
            case "fear":
                return "Fear";
            default:
                return "No emotion recorded";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh search results when returning to activity
        performSearch(searchInput.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // Refresh search results regardless of result code
            // This ensures updates after both edits and deletions
            performSearch(searchInput.getText().toString());
        }
    }
}