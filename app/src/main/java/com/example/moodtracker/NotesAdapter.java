package com.example.moodtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<MoodDatabaseHelper.MoodEntry> moodEntries;
    private Context context;

    public NotesAdapter(List<MoodDatabaseHelper.MoodEntry> moodEntries) {
        this.moodEntries = moodEntries;
    }

    public void updateData(List<MoodDatabaseHelper.MoodEntry> newEntries) {
        this.moodEntries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();  // Store context
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        MoodDatabaseHelper.MoodEntry entry = moodEntries.get(position);

        // Format emotion text with the first letter capitalized
        String formattedEmotion = entry.emotion.substring(0, 1).toUpperCase() + entry.emotion.substring(1).toLowerCase();
        holder.noteEmotion.setText(getTextForEmotion(entry.emotion));

        // Add space between date and emotion text
        holder.noteDate.setText(entry.date); // Adds spacing
        holder.noteText.setText(entry.notes);

        // Set emotion icon using case-insensitive matching
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EmotionInputted.class);
            intent.putExtra("selectedDate", entry.date);
            intent.putExtra("emotion", entry.emotion); // Keeping original case for logic
            intent.putExtra("text", getTextForEmotion(entry.emotion));
            intent.putExtra("notes", entry.notes);

            // If you need to handle the result, start with startActivityForResult
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
        return moodEntries != null ? moodEntries.size() : 0;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteDate, noteEmotion, noteText;
        ImageView emotionIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteDate = itemView.findViewById(R.id.noteDate);
            noteEmotion = itemView.findViewById(R.id.noteEmotion);
            noteText = itemView.findViewById(R.id.noteText);
            emotionIcon = itemView.findViewById(R.id.emotionIcon);
        }
    }
}
