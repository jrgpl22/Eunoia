package com.example.moodtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity implements CalendarFragment.OnFabIconChangeListener {

    private FloatingActionButton fab;
    private MoodDatabaseHelper dbHelper;
    private boolean shouldSwitchToCalendar = false;
    private boolean shouldReturnToCurrentMonth = false;  // Add this field
    private LocalDate savedCalendarDate;
    private YearMonth statsCurrentYearMonth;

    private YearMonth notesCurrentYearMonth;

    public void setNotesCurrentYearMonth(YearMonth yearMonth) {
        this.notesCurrentYearMonth = yearMonth;
    }

    public YearMonth getNotesCurrentYearMonth() {
        return notesCurrentYearMonth;
    }
    public void setStatsCurrentYearMonth(YearMonth yearMonth) {
        this.statsCurrentYearMonth = yearMonth;
    }

    public YearMonth getStatsCurrentYearMonth() {
        return statsCurrentYearMonth;
    }
    public LocalDate getSavedCalendarDate() {
        return savedCalendarDate;
    }

    public void setSavedCalendarDate(LocalDate date) {
        this.savedCalendarDate = date;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new MoodDatabaseHelper(this);
        savedCalendarDate = LocalDate.now();

        if (savedInstanceState == null) {
            CalendarFragment calendarFragment = new CalendarFragment();
            Bundle args = new Bundle();
            args.putSerializable("selectedDate", savedCalendarDate);
            calendarFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, calendarFragment)
                    .commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.calendar) {
                    CalendarFragment calendarFragment = new CalendarFragment();
                    Bundle args = new Bundle();
                    // If we should return to current month, use current date
                    if (shouldReturnToCurrentMonth) {
                        savedCalendarDate = LocalDate.now();
                        shouldReturnToCurrentMonth = false;
                    }
                    args.putSerializable("selectedDate", savedCalendarDate);
                    calendarFragment.setArguments(args);
                    selectedFragment = calendarFragment;
                } else if (itemId == R.id.notes) {
                    selectedFragment = new NotesFragment();
                } else if (itemId == R.id.stats) {
                    selectedFragment = new StatsFragment();
                } else if (itemId == R.id.settings) {
                    selectedFragment = new SettingsFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldReturnToCurrentMonth = true;  // Set flag to return to current month
                savedCalendarDate = LocalDate.now(); // Reset saved date to current
                handleFabClick();
            }
        });

        updateFabIcon();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldSwitchToCalendar) {
            shouldSwitchToCalendar = false;
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.calendar);
            }
        }
    }

    @Override
    public void updateFabIcon() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        String formattedDate = currentDate.format(formatter);
        String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        String emotion = dbHelper.getEmotionByDate(formattedDate, username);

        if (emotion != null && !emotion.isEmpty()) {
            fab.setImageResource(R.drawable.edit);
        } else {
            fab.setImageResource(R.drawable.add);
        }
    }

    private void handleFabClick() {
        shouldSwitchToCalendar = true;
        shouldReturnToCurrentMonth = true;  // Set flag to return to current month

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        String formattedDate = currentDate.format(formatter);
        String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        String emotion = dbHelper.getEmotionByDate(formattedDate, username);
        String notes = dbHelper.getNotesByDate(formattedDate, username);

        Intent intent;
        if (emotion != null && !emotion.isEmpty()) {
            intent = new Intent(MainActivity.this, EmotionInputted.class);
            intent.putExtra("selectedDate", formattedDate);
            intent.putExtra("emotion", emotion);
            intent.putExtra("text", getTextForEmotion(emotion));
            intent.putExtra("notes", notes);
        } else {
            intent = new Intent(MainActivity.this, EmotionsInput.class);
            intent.putExtra("selectedDate", formattedDate);
        }

        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.slide_up, R.anim.no_animation);
    }

    private String getTextForEmotion(String emotion) {
        switch (emotion) {
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
                return "No emotion recorded.";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                String selectedDate = data.getStringExtra("selectedDate");
                String selectedEmotion = data.getStringExtra("selectedEmotion");
                String notes = data.getStringExtra("notes");
                boolean updateToCurrentMonth = data.getBooleanExtra("updateToCurrentMonth", false);

                if (selectedDate != null && selectedEmotion != null) {
                    String username = getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
                    dbHelper.insertOrUpdateMood(selectedDate, selectedEmotion, notes, username);
                }

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof CalendarFragment) {
                    ((CalendarFragment) currentFragment).updateEmotion(selectedDate, selectedEmotion);
                    if (updateToCurrentMonth || shouldReturnToCurrentMonth) {
                        ((CalendarFragment) currentFragment).setToCurrentMonth();
                        shouldReturnToCurrentMonth = false;
                    } else {
                        ((CalendarFragment) currentFragment).setMonthView();
                    }
                }

                updateFabIcon();
            }
        }
    }
}