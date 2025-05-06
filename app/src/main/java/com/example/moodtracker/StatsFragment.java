package com.example.moodtracker;

import static android.content.Context.MODE_PRIVATE;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsFragment extends Fragment {
    private PieChart pieChart;
    private TextView monthYearText, legendTextView;
    private ImageButton prevButton, nextButton;
    private MoodDatabaseHelper dbHelper;
    private YearMonth currentYearMonth;
    private final DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private View rootView;

    private static final String KEY_CURRENT_YEAR = "current_year";
    private static final String KEY_CURRENT_MONTH = "current_month";
    private static final String KEY_EMPTY_STATE = "empty_state";
    private boolean isEmptyState = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            int year = savedInstanceState.getInt(KEY_CURRENT_YEAR, YearMonth.now().getYear());
            int month = savedInstanceState.getInt(KEY_CURRENT_MONTH, YearMonth.now().getMonthValue());
            currentYearMonth = YearMonth.of(year, month);
            isEmptyState = savedInstanceState.getBoolean(KEY_EMPTY_STATE, false);
        } else {
            currentYearMonth = YearMonth.now();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        dbHelper = new MoodDatabaseHelper(requireContext());

        pieChart = rootView.findViewById(R.id.pieChart);
        monthYearText = rootView.findViewById(R.id.monthYearText);
        prevButton = rootView.findViewById(R.id.prevButton);
        nextButton = rootView.findViewById(R.id.nextButton);
        legendTextView = rootView.findViewById(R.id.legendTextView);

        prevButton.setOnClickListener(v -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateChart();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setStatsCurrentYearMonth(currentYearMonth);
            }
        });

        nextButton.setOnClickListener(v -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateChart();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setStatsCurrentYearMonth(currentYearMonth);
            }
        });

        monthYearText.setOnClickListener(v -> {
            currentYearMonth = YearMonth.now();
            updateChart();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setStatsCurrentYearMonth(currentYearMonth);
            }
        });

        setupPieChart();

        if (getActivity() instanceof MainActivity) {
            YearMonth savedYearMonth = ((MainActivity) getActivity()).getStatsCurrentYearMonth();
            if (savedYearMonth != null) {
                currentYearMonth = savedYearMonth;
            }
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_YEAR, currentYearMonth.getYear());
        outState.putInt(KEY_CURRENT_MONTH, currentYearMonth.getMonthValue());
        outState.putBoolean(KEY_EMPTY_STATE, isEmptyState);
    }

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(40f);
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setEntryLabelTextSize(0f);
        pieChart.setEntryLabelColor(Color.TRANSPARENT);
        pieChart.getLegend().setEnabled(false);

        // Add these lines to handle the empty state text
        pieChart.setNoDataText("");  // Remove the "No chart data available" text
        pieChart.setNoDataTextColor(Color.TRANSPARENT);
    }

    private void updateChart() {
        monthYearText.setText(currentYearMonth.format(monthYearFormatter));

        if (currentYearMonth.equals(YearMonth.now())) {
            nextButton.setEnabled(false);
            nextButton.setVisibility(View.INVISIBLE);
        } else {
            nextButton.setEnabled(true);
            nextButton.setVisibility(View.VISIBLE);
        }

        Map<String, Float> emotionPercentages = getEmotionPercentagesForMonth();

        // Check if there are any entries for the month
        if (emotionPercentages.isEmpty()) {
            showEmptyState();
        } else {
            // Create entries list before deciding whether to show chart
            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            SpannableStringBuilder legendText = new SpannableStringBuilder();
            int emotionsPerLine = 0;

            if (emotionPercentages.containsKey("happy")) {
                entries.add(new PieEntry(emotionPercentages.get("happy"), ""));
                colors.add(Color.parseColor("#ffde59"));
                legendText.append(createLegendItem("Happy", R.drawable.joyful));
                emotionsPerLine++;
                if (emotionsPerLine % 2 == 0) legendText.append("\n");
            }
            if (emotionPercentages.containsKey("calm")) {
                entries.add(new PieEntry(emotionPercentages.get("calm"), ""));
                colors.add(Color.parseColor("#feaae9"));
                legendText.append(createLegendItem("Shy", R.drawable.embarressed));
                emotionsPerLine++;
                if (emotionsPerLine % 2 == 0) legendText.append("\n");
            }
            if (emotionPercentages.containsKey("neutral")) {
                entries.add(new PieEntry(emotionPercentages.get("neutral"), ""));
                colors.add(Color.parseColor("#d0b49f"));
                legendText.append(createLegendItem("Bored", R.drawable.bored));
                emotionsPerLine++;
                if (emotionsPerLine % 2 == 0) legendText.append("\n");
            }
            if (emotionPercentages.containsKey("sad")) {
                entries.add(new PieEntry(emotionPercentages.get("sad"), ""));
                colors.add(Color.parseColor("#70c1ef"));
                legendText.append(createLegendItem("Sad", R.drawable.sadness));
                emotionsPerLine++;
                if (emotionsPerLine % 2 == 0) legendText.append("\n");
            }
            if (emotionPercentages.containsKey("angry")) {
                entries.add(new PieEntry(emotionPercentages.get("angry"), ""));
                colors.add(Color.parseColor("#da5457"));
                legendText.append(createLegendItem("Angry", R.drawable.anger));
                emotionsPerLine++;
                if (emotionsPerLine % 2 == 0) legendText.append("\n");
            }
            if (emotionPercentages.containsKey("fear")) {
                entries.add(new PieEntry(emotionPercentages.get("fear"), ""));
                colors.add(Color.parseColor("#b571e1"));
                legendText.append(createLegendItem("Fear", R.drawable.fear));
                emotionsPerLine++;
                if (emotionsPerLine % 2 == 0) legendText.append("\n");
            }

            // Check if after filtering we still have entries
            if (entries.isEmpty()) {
                showEmptyState();
            } else {
                showChartState(entries, colors, legendText);
            }
        }
    }

    // Helper method to show the empty state
    private void showEmptyState() {
        isEmptyState = true;

        // Important: Set background on rootView directly
        rootView.setBackgroundResource(R.drawable.statsempty);

        // Hide the chart and legend
        pieChart.setVisibility(View.GONE);
        legendTextView.setVisibility(View.GONE);

        // Clear any existing chart data to prevent "No chart data available" message
        pieChart.clear();
        pieChart.setNoDataText("");  // Set empty text for no data message
        pieChart.invalidate();
    }

    // Helper method to show the chart state
    private void showChartState(List<PieEntry> entries, List<Integer> colors, SpannableStringBuilder legendText) {
        isEmptyState = false;

        // Important: Set background on rootView directly
        rootView.setBackgroundResource(R.drawable.notes_layout);

        // Show the chart and legend
        pieChart.setVisibility(View.VISIBLE);
        legendTextView.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.BLACK);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%d%%", Math.round(value));
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();

        legendTextView.setText(legendText);
    }

    private SpannableString createLegendItem(String text, int drawableId) {
        SpannableString legendItem = new SpannableString(" " + text + "    ");

        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableId);
        if (drawable != null) {
            int iconSize = (int) (legendTextView.getTextSize() * 2.5);
            drawable.setBounds(0, 0, iconSize, iconSize);
            ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
            legendItem.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        return legendItem;
    }

    private Map<String, Float> getEmotionPercentagesForMonth() {
        Map<String, Integer> emotionCounts = new HashMap<>();
        String username = requireActivity().getSharedPreferences("MoodTracker", MODE_PRIVATE).getString("loggedInUser", "");
        List<MoodDatabaseHelper.MoodEntry> allMoods = dbHelper.getAllMoods(username);
        int totalCount = 0;

        // First, get the counts for the current month
        for (MoodDatabaseHelper.MoodEntry mood : allMoods) {
            LocalDate moodDate = LocalDate.parse(mood.date, dbDateFormatter);
            if (moodDate.getYear() == currentYearMonth.getYear() &&
                    moodDate.getMonth() == currentYearMonth.getMonth()) {
                emotionCounts.put(mood.emotion,
                        emotionCounts.getOrDefault(mood.emotion, 0) + 1);
                totalCount++;
            }
        }

        // Convert counts to percentages
        Map<String, Float> emotionPercentages = new HashMap<>();
        if (totalCount > 0) {
            for (Map.Entry<String, Integer> entry : emotionCounts.entrySet()) {
                float percentage = (entry.getValue() * 100f) / totalCount;
                emotionPercentages.put(entry.getKey(), percentage);
            }
        }

        return emotionPercentages;
    }
}