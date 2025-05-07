package com.example.moodtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AccountDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AccountDB.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TAG = "AccountDatabaseHelper";

    private static final String CREATE_TABLE_ACCOUNTS =
            "CREATE TABLE " + TABLE_ACCOUNTS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USERNAME + " TEXT UNIQUE,"
                    + COLUMN_PASSWORD + " TEXT"
                    + ")";

    public AccountDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ACCOUNTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        try {
            db.insertOrThrow(TABLE_ACCOUNTS, null, values);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    public boolean isValidUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCOUNTS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCOUNTS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, newPassword);

            // Debug - log the values
            Log.d(TAG, "Updating password for user: " + username);

            // Update the password for the specified username
            int rowsAffected = db.update(
                    TABLE_ACCOUNTS,
                    values,
                    COLUMN_USERNAME + "=?",
                    new String[]{username}
            );

            Log.d(TAG, "Rows affected: " + rowsAffected);

            // Return true if at least one row was updated
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Updates a user's username in the database
     * @param oldUsername Current username
     * @param newUsername New username to set
     * @return true if username was updated successfully
     */
    public boolean updateUsername(String oldUsername, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // First, get the user's password
            Cursor cursor = db.query(
                    TABLE_ACCOUNTS,
                    new String[]{COLUMN_PASSWORD},
                    COLUMN_USERNAME + "=?",
                    new String[]{oldUsername},
                    null, null, null
            );

            if (!cursor.moveToFirst()) {
                cursor.close();
                return false;
            }

            String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            cursor.close();

            // Start a transaction to ensure atomicity
            db.beginTransaction();
            try {
                // Delete the old username entry
                int rowsDeleted = db.delete(
                        TABLE_ACCOUNTS,
                        COLUMN_USERNAME + "=?",
                        new String[]{oldUsername}
                );

                if (rowsDeleted == 0) {
                    return false;
                }

                // Insert the new username with the same password
                ContentValues values = new ContentValues();
                values.put(COLUMN_USERNAME, newUsername);
                values.put(COLUMN_PASSWORD, password);

                long newRowId = db.insert(TABLE_ACCOUNTS, null, values);
                if (newRowId == -1) {
                    return false;
                }

                // If we made it here, mark the transaction as successful
                db.setTransactionSuccessful();
                return true;
            } finally {
                // End the transaction
                db.endTransaction();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating username: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
}