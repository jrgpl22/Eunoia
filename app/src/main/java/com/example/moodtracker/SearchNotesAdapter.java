package com.example.moodtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.regex.Pattern;

public class SearchNotesAdapter extends RecyclerView.Adapter<SearchNotesAdapter.SearchViewHolder> {

    private List<MoodDatabaseHelper.MoodEntry> moodEntries;
    private String searchQuery;
    private Context context;

    public SearchNotesAdapter(List<MoodDatabaseHelper.MoodEntry> moodEntries, String searchQuery) {
        this.moodEntries = moodEntries;
        this.searchQuery = searchQuery;
    }

    public void updateData(List<MoodDatabaseHelper.MoodEntry> newEntries, String newQuery) {
        this.moodEntries = newEntries;
        this.searchQuery = newQuery;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_note, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        MoodDatabaseHelper.MoodEntry entry = moodEntries.get(position);

        // Apply highlighting to date if it matches search
        if (!searchQuery.isEmpty() && entry.date.toLowerCase().contains(searchQuery.toLowerCase())) {
            String pattern = "(?i)(" + Pattern.quote(searchQuery) + ")";
            String highlighted = entry.date.replaceAll(pattern, "<font color='#FF4081'>$1</font>");
            holder.noteDate.setText(Html.fromHtml(highlighted, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.noteDate.setText(entry.date);
        }

        // Get and format emotion text
        String emotionText = getTextForEmotion(entry.emotion);

        // Apply highlighting to emotion if it matches search
        if (!searchQuery.isEmpty() &&
                (emotionText.toLowerCase().contains(searchQuery.toLowerCase()) ||
                        entry.emotion.toLowerCase().contains(searchQuery.toLowerCase()))) {
            String pattern = "(?i)(" + Pattern.quote(searchQuery) + ")";
            String highlighted = emotionText.replaceAll(pattern, "<font color='#FF4081'>$1</font>");
            holder.noteEmotion.setText(Html.fromHtml(highlighted, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.noteEmotion.setText(emotionText);
        }

        // Apply highlighting to notes
        if (!searchQuery.isEmpty() && entry.notes.toLowerCase().contains(searchQuery.toLowerCase())) {
            String pattern = "(?i)(" + Pattern.quote(searchQuery) + ")";
            String highlighted = entry.notes.replaceAll(pattern, "<font color='#FF4081'>$1</font>");
            holder.noteText.setText(Html.fromHtml(highlighted, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.noteText.setText(entry.notes);
        }

        // Set emotion icon
        switch (entry.emotion.toLowerCase()) {
            case "happy":
                holder.emotionIcon.setImageResource(R.drawable.joyful);
                break;
            case "sad":
                holder.emotionIcon.setImageResource(R.drawable.sadness);
                break;
            case "angry":
                holder.emotionIcon.setImageResource(R.drawable.anger);
                break;
            case "calm":
                holder.emotionIcon.setImageResource(R.drawable.embarressed);
                break;
            case "neutral":
                holder.emotionIcon.setImageResource(R.drawable.bored);
                break;
            case "fear":
                holder.emotionIcon.setImageResource(R.drawable.fear);
                break;
            default:
                holder.emotionIcon.setImageResource(R.drawable.joyful);
                break;
        }

        // Add click listener to open EmotionInputted activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmotionInputted.class);
            intent.putExtra("selectedDate", entry.date);
            intent.putExtra("emotion", entry.emotion);
            intent.putExtra("text", getTextForEmotion(entry.emotion));
            intent.putExtra("notes", entry.notes);

            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, 1);
            } else {
                context.startActivity(intent);
            }
        });
    }

    private String getTextForEmotion(String emotion) {
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
                return "No emotion recorded.";
        }
    }

    @Override
    public int getItemCount() {
        return moodEntries.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView noteDate, noteEmotion, noteText;
        ImageView emotionIcon;

        SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            noteDate = itemView.findViewById(R.id.noteDate);
            noteEmotion = itemView.findViewById(R.id.noteEmotion);
            noteText = itemView.findViewById(R.id.noteText);
            emotionIcon = itemView.findViewById(R.id.emotionIcon);
        }
    }
}