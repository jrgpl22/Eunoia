package com.example.moodtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MoodDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MoodTracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MOOD = "mood";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_EMOTION = "emotion";
    private static final String COLUMN_NOTES = "notes";

    private static final String CREATE_TABLE_MOOD =
            "CREATE TABLE " + TABLE_MOOD + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USERNAME + " TEXT,"
                    + COLUMN_DATE + " TEXT,"
                    + COLUMN_EMOTION + " TEXT,"
                    + COLUMN_NOTES + " TEXT,"
                    + "UNIQUE(" + COLUMN_DATE + ", " + COLUMN_USERNAME + ")"
                    + ")";

    public MoodDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MOOD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOOD);
        onCreate(db);
    }

    public String getNotesByDate(String date, String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String notes = "";

        Cursor cursor = db.query(TABLE_MOOD,
                new String[]{COLUMN_NOTES},
                COLUMN_DATE + " = ? AND " + COLUMN_USERNAME + " = ?",
                new String[]{date, username},
                null, null, null);

        if (cursor.moveToFirst()) {
            notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES));
        }
        cursor.close();
        db.close();
        return notes;
    }

    public void insertOrUpdateMood(String date, String emotion, String notes, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_EMOTION, emotion);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_USERNAME, username); // Add username to the ContentValues
        db.insertWithOnConflict(TABLE_MOOD, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteMood(String date, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MOOD,
                COLUMN_DATE + " = ? AND " + COLUMN_USERNAME + " = ?",
                new String[]{date, username});
        db.close();
    }

    public List<MoodEntry> getAllMoods(String username) {
        List<MoodEntry> moodList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MOOD,
                null,
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, COLUMN_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String emotion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMOTION));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES));
                moodList.add(new MoodEntry(date, emotion, notes));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return moodList;
    }

    public String getEmotionByDate(String date, String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String emotion = "";

        Cursor cursor = db.query(TABLE_MOOD,
                new String[]{COLUMN_EMOTION},
                COLUMN_DATE + " = ? AND " + COLUMN_USERNAME + " = ?",
                new String[]{date, username},
                null, null, null);

        if (cursor.moveToFirst()) {
            emotion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMOTION));
        }
        cursor.close();
        db.close();
        return emotion;
    }

    public static class MoodEntry {
        public String date;
        public String emotion;
        public String notes;

        public MoodEntry(String date, String emotion, String notes) {
            this.date = date;
            this.emotion = emotion;
            this.notes = notes;
        }
    }
}