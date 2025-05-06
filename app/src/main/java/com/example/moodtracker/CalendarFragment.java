package com.example.moodtracker;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;
    private Button prevMonthButton, nextMonthButton;
    private MoodDatabaseHelper dbHelper;
    private OnFabIconChangeListener fabIconChangeListener;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getSerializable("selectedDate") != null) {
            selectedDate = (LocalDate) getArguments().getSerializable("selectedDate");
        } else {
            selectedDate = LocalDate.now();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbHelper = new MoodDatabaseHelper(getContext());
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        initWidgets(view);
        setMonthView();

        prevMonthButton.setOnClickListener(v -> {
            previousMonthAction();
            saveCurrentDate();
        });

        nextMonthButton.setOnClickListener(v -> {
            nextMonthAction();
            saveCurrentDate();
        });

        monthYearText.setOnClickListener(v -> {
            selectedDate = LocalDate.now();
            setMonthView();
            saveCurrentDate();
        });

        IntentFilter filter = new IntentFilter("com.example.moodtracker.UPDATE_CALENDAR");
        getContext().registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        return view;
    }

    private void saveCurrentDate() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setSavedCalendarDate(selectedDate);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(updateReceiver);
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setMonthView();
        }
    };

    private void initWidgets(View view) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
        prevMonthButton = view.findViewById(R.id.prevMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);
    }

    public void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, selectedDate, getContext(), dbHelper);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);

        calendarRecyclerView.scheduleLayoutAnimation();
        calendarAdapter.notifyDataSetChanged();

        if (fabIconChangeListener != null) {
            fabIconChangeListener.updateFabIcon();
        }
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int adjustedDayOfWeek = (dayOfWeek == 7) ? 0 : dayOfWeek;

        for (int i = 1; i <= 42; i++) {
            if (i <= adjustedDayOfWeek || i > daysInMonth + adjustedDayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - adjustedDayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
        saveCurrentDate();
    }

    public void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
        saveCurrentDate();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            String selectedDateText = dayText + " " + monthYearFromDate(selectedDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            LocalDate date = LocalDate.parse(selectedDateText, formatter);
            LocalDate currentDate = LocalDate.now();

            if (!date.isAfter(currentDate)) {
                String username =  requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
                String emotionForDate = dbHelper.getEmotionByDate(selectedDateText, username);

                Intent intent;
                if (emotionForDate != null && !emotionForDate.isEmpty()) {
                    String textForEmotion = getTextForEmotion(emotionForDate);
                    intent = new Intent(getActivity(), EmotionInputted.class);
                    intent.putExtra("selectedDate", selectedDateText);
                    intent.putExtra("emotion", emotionForDate);
                    intent.putExtra("text", textForEmotion);
                } else {
                    intent = new Intent(getActivity(), EmotionsInput.class);
                    intent.putExtra("selectedDate", selectedDateText);
                }
                startActivityForResult(intent, 1);
            } else {
                Toast.makeText(getContext(), "Cannot select a future date", Toast.LENGTH_SHORT).show();
            }
        }
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

    public void updateEmotion(String date, String emotion) {
        String username =  requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        String existingNotes = dbHelper.getNotesByDate(date, username);
        dbHelper.insertOrUpdateMood(date, emotion, existingNotes, username);
        setMonthView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            setMonthView();

            if (fabIconChangeListener != null) {
                fabIconChangeListener.updateFabIcon();
            }
        }
    }

    public void setToCurrentMonth() {
        selectedDate = LocalDate.now();
        setMonthView();
        saveCurrentDate();

        if (fabIconChangeListener != null) {
            fabIconChangeListener.updateFabIcon();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFabIconChangeListener) {
            fabIconChangeListener = (OnFabIconChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFabIconChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fabIconChangeListener = null;
    }

    public interface OnFabIconChangeListener {
        void updateFabIcon();
    }
}