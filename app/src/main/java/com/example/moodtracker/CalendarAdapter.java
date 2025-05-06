package com.example.moodtracker;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;

    private final LocalDate selectedDate;
    private final LocalDate currentDate;
    private final Context context;
    private final MoodDatabaseHelper dbHelper;
    private final int cellHeight;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener,
                           LocalDate selectedDate, Context context, MoodDatabaseHelper dbHelper) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.selectedDate = selectedDate;
        this.currentDate = LocalDate.now();
        this.context = context;
        this.dbHelper = dbHelper;
        // Calculate cell height once during initialization
        this.cellHeight = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.12);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);

        // Set fixed height for all cells
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = cellHeight;
        view.setLayoutParams(layoutParams);

        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.dayOfMonth.setText(dayText);

        // Reset visibility of views
        holder.circle.setVisibility(View.GONE);
        holder.icon.setVisibility(View.GONE);
        holder.dayOfMonth.setVisibility(View.VISIBLE);

        if (!dayText.isEmpty()) {
            // Show circle for all valid dates
            holder.circle.setVisibility(View.VISIBLE);

            // Get emotion for the date
            String dateKey = dayText + " " + monthYearFromDate(selectedDate);
            String username = context.getSharedPreferences("MoodTracker", Context.MODE_PRIVATE).getString("loggedInUser", "");
            String emotion = dbHelper.getEmotionByDate(dateKey, username);

            // Check if this is the current day
            boolean isCurrentDayCell = isCurrentDay(dayText);

            // Handle emotion icon
            if (emotion != null && !emotion.isEmpty()) {
                holder.icon.setVisibility(View.VISIBLE);
                setEmotionIcon(holder, emotion);
            }
            // Handle current day marker
            else if (isCurrentDayCell) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageResource(R.drawable.current);
            }
            // For dates without emotion input (except the current date)
            else {
                // Only show noneinput for dates that are not in the future
                LocalDate cellDate = getDateFromDayText(dayText);
                if (cellDate != null && !cellDate.isAfter(LocalDate.now())) {
                    holder.icon.setVisibility(View.VISIBLE);
                    holder.icon.setImageResource(R.drawable.noneinput);
                }
            }

            // Ensure the day number remains visible
            holder.dayOfMonth.setVisibility(View.VISIBLE);
        } else {
            // Clear empty cells
            holder.dayOfMonth.setVisibility(View.INVISIBLE);
            holder.circle.setVisibility(View.GONE);
            holder.icon.setVisibility(View.GONE);
        }
    }

    // Helper method to convert day text to actual date
    private LocalDate getDateFromDayText(String dayText) {
        try {
            int day = Integer.parseInt(dayText);
            return selectedDate.withDayOfMonth(day);
        } catch (NumberFormatException | java.time.DateTimeException e) {
            return null;
        }
    }

    private void setEmotionIcon(@NonNull CalendarViewHolder holder, String emotion) {
        int resourceId;
        switch (emotion) {
            case "happy":
                resourceId = R.drawable.joyful;
                break;
            case "calm":
                resourceId = R.drawable.embarressed;
                break;
            case "neutral":
                resourceId = R.drawable.bored;
                break;
            case "sad":
                resourceId = R.drawable.sadness;
                break;
            case "angry":
                resourceId = R.drawable.anger;
                break;
            case "fear":
                resourceId = R.drawable.fear;
                break;
            default:
                return;
        }
        holder.icon.setImageResource(resourceId);
    }

    private boolean isCurrentDay(String dayText) {
        return dayText.equals(String.valueOf(currentDate.getDayOfMonth())) &&
                selectedDate.getMonth() == currentDate.getMonth() &&
                selectedDate.getYear() == currentDate.getYear();
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}