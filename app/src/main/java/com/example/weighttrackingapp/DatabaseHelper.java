package com.example.weighttrackingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Central SQLite helper for user accounts, goal weights, and daily weight entries.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE = "phone";

    public static final String TABLE_GOALS = "goal_weights";
    public static final String COLUMN_GOAL_ID = "goal_id";
    public static final String COLUMN_GOAL_USER_ID = "user_id";
    public static final String COLUMN_GOAL_WEIGHT = "goal_weight";

    public static final String TABLE_WEIGHTS = "daily_weights";
    public static final String COLUMN_WEIGHT_ID = "weight_id";
    public static final String COLUMN_WEIGHT_USER_ID = "user_id";
    public static final String COLUMN_WEIGHT_DATE = "entry_date";
    public static final String COLUMN_WEIGHT_VALUE = "weight_value";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_PHONE + " TEXT)";

        String createGoalTable = "CREATE TABLE " + TABLE_GOALS + " ("
                + COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_GOAL_USER_ID + " INTEGER UNIQUE NOT NULL, "
                + COLUMN_GOAL_WEIGHT + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_GOAL_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USER_ID + "))";

        String createWeightTable = "CREATE TABLE " + TABLE_WEIGHTS + " ("
                + COLUMN_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_WEIGHT_USER_ID + " INTEGER NOT NULL, "
                + COLUMN_WEIGHT_DATE + " TEXT NOT NULL, "
                + COLUMN_WEIGHT_VALUE + " REAL NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_WEIGHT_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USER_ID + "))";

        db.execSQL(createUsersTable);
        db.execSQL(createGoalTable);
        db.execSQL(createWeightTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.trim());
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + " = ?",
                new String[]{username.trim()},
                null,
                null,
                null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public int authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username.trim(), password},
                null,
                null,
                null
        );

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }

    public boolean saveGoalWeight(int userId, double goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_USER_ID, userId);
        values.put(COLUMN_GOAL_WEIGHT, goalWeight);
        long result = db.replace(TABLE_GOALS, null, values);
        return result != -1;
    }

    public double getGoalWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_GOALS,
                new String[]{COLUMN_GOAL_WEIGHT},
                COLUMN_GOAL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        double goalWeight = -1;
        if (cursor.moveToFirst()) {
            goalWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_WEIGHT));
        }
        cursor.close();
        return goalWeight;
    }

    public long addWeightEntry(int userId, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT_USER_ID, userId);
        values.put(COLUMN_WEIGHT_DATE, date.trim());
        values.put(COLUMN_WEIGHT_VALUE, weight);
        return db.insert(TABLE_WEIGHTS, null, values);
    }

    public boolean updateWeightEntry(int weightId, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT_DATE, date.trim());
        values.put(COLUMN_WEIGHT_VALUE, weight);
        int rowsUpdated = db.update(
                TABLE_WEIGHTS,
                values,
                COLUMN_WEIGHT_ID + " = ?",
                new String[]{String.valueOf(weightId)}
        );
        return rowsUpdated > 0;
    }

    public boolean deleteWeightEntry(int weightId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(
                TABLE_WEIGHTS,
                COLUMN_WEIGHT_ID + " = ?",
                new String[]{String.valueOf(weightId)}
        );
        return rowsDeleted > 0;
    }

    public List<WeightEntry> getAllWeightEntriesForUser(int userId) {
        List<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_WEIGHTS,
                null,
                COLUMN_WEIGHT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COLUMN_WEIGHT_DATE + " DESC, " + COLUMN_WEIGHT_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_DATE));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_VALUE));
                entries.add(new WeightEntry(id, date, weight));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return entries;
    }

    public boolean updateUserPhone(int userId, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHONE, phone.trim());
        int rowsUpdated = db.update(
                TABLE_USERS,
                values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
        return rowsUpdated > 0;
    }

    public String getUserPhone(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_PHONE},
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        String phone = "";
        if (cursor.moveToFirst()) {
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));
        }
        cursor.close();
        return phone == null ? "" : phone;
    }
}
