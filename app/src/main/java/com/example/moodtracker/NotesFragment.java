package com.example.moodtracker;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotesFragment extends Fragment {

    private RecyclerView notesRecyclerView;
    private MoodDatabaseHelper dbHelper;
    private NotesAdapter adapter;
    private View rootView;
    private TextView monthYearText;
    private ImageButton prevButton, nextButton;
    private YearMonth currentYearMonth;
    private final DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    private static final String KEY_CURRENT_YEAR = "notes_current_year";
    private static final String KEY_CURRENT_MONTH = "notes_current_month";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            int year = savedInstanceState.getInt(KEY_CURRENT_YEAR, YearMonth.now().getYear());
            int month = savedInstanceState.getInt(KEY_CURRENT_MONTH, YearMonth.now().getMonthValue());
            currentYearMonth = YearMonth.of(year, month);
        } else {
            currentYearMonth = YearMonth.now();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        dbHelper = new MoodDatabaseHelper(getContext());

        // Initialize views
        setupViews();

        // Initialize adapter
        adapter = new NotesAdapter(Collections.emptyList());
        notesRecyclerView.setAdapter(adapter);

        // Check if there's a saved state in MainActivity
        if (getActivity() instanceof MainActivity) {
            YearMonth savedYearMonth = ((MainActivity) getActivity()).getNotesCurrentYearMonth();
            if (savedYearMonth != null) {
                currentYearMonth = savedYearMonth;
            }
        }

        // Set up month navigation
        setupMonthNavigation();

        // Load initial notes
        loadNotes();

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter("com.example.moodtracker.UPDATE_CALENDAR");
        getContext().registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        return rootView;
    }

    private void setupViews() {
        notesRecyclerView = rootView.findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        monthYearText = rootView.findViewById(R.id.monthYearText);
        prevButton = rootView.findViewById(R.id.prevButton);
        nextButton = rootView.findViewById(R.id.nextButton);
        CardView searchBarContainer = rootView.findViewById(R.id.searchBarContainer);
        searchBarContainer.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
    }


    private void setupMonthNavigation() {
        updateMonthYearText();

        prevButton.setOnClickListener(v -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateMonthYearText();
            loadNotes();
            saveCurrentMonth();
        });

        nextButton.setOnClickListener(v -> {
            if (!currentYearMonth.equals(YearMonth.now())) {
                currentYearMonth = currentYearMonth.plusMonths(1);
                updateMonthYearText();
                loadNotes();
                saveCurrentMonth();
            }
        });

        monthYearText.setOnClickListener(v -> {
            currentYearMonth = YearMonth.now();
            updateMonthYearText();
            loadNotes();
            saveCurrentMonth();
        });
    }

    private void saveCurrentMonth() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNotesCurrentYearMonth(currentYearMonth);
        }
    }

    private void updateMonthYearText() {
        monthYearText.setText(currentYearMonth.format(monthYearFormatter));

        // Disable next button if we're in the current month
        if (currentYearMonth.equals(YearMonth.now())) {
            nextButton.setEnabled(false);
            nextButton.setVisibility(View.INVISIBLE);
        } else {
            nextButton.setEnabled(true);
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNotes();
        }
    };

    private void loadNotes() {
        String username = requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        List<MoodDatabaseHelper.MoodEntry> allMoodEntries = dbHelper.getAllMoods(username);
        List<MoodDatabaseHelper.MoodEntry> monthlyEntries = new ArrayList<>();

        // Filter entries for current month
        for (MoodDatabaseHelper.MoodEntry entry : allMoodEntries) {
            LocalDate entryDate = LocalDate.parse(entry.date, dbDateFormatter);
            if (entryDate.getYear() == currentYearMonth.getYear() &&
                    entryDate.getMonth() == currentYearMonth.getMonth()) {
                monthlyEntries.add(entry);
            }
        }

        if (monthlyEntries.isEmpty()) {
            rootView.setBackgroundResource(R.drawable.notesempty);
            notesRecyclerView.setVisibility(View.GONE);
        } else {
            rootView.setBackgroundResource(R.drawable.notes_layout);

            notesRecyclerView.setVisibility(View.VISIBLE);

            // Sort by date (newest to oldest)
            Collections.sort(monthlyEntries, (entry1, entry2) -> {
                LocalDate date1 = LocalDate.parse(entry1.date, dbDateFormatter);
                LocalDate date2 = LocalDate.parse(entry2.date, dbDateFormatter);
                return date2.compareTo(date1);
            });

            adapter.updateData(monthlyEntries);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_YEAR, currentYearMonth.getYear());
        outState.putInt(KEY_CURRENT_MONTH, currentYearMonth.getMonthValue());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getContext() != null) {
            getContext().unregisterReceiver(updateReceiver);
        }
    }
}