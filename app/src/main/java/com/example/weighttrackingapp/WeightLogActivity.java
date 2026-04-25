package com.example.weighttrackingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * Displays the logged-in user's weight history and supports full CRUD operations.
 */
public class WeightLogActivity extends AppCompatActivity implements WeightAdapter.OnWeightActionListener {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private WeightAdapter weightAdapter;

    private EditText etDate;
    private EditText etDailyWeight;
    private EditText etGoalWeight;
    private TextView tvWelcome;
    private TextView tvCurrentGoal;

    private int currentUserId;
    private int selectedWeightId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_log);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();

        if (currentUserId == -1) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        tvCurrentGoal = findViewById(R.id.tvCurrentGoal);
        etDate = findViewById(R.id.etDate);
        etDailyWeight = findViewById(R.id.etDailyWeight);
        etGoalWeight = findViewById(R.id.etGoalWeight);
        Button btnAddWeight = findViewById(R.id.btnAddWeight);
        Button btnUpdateWeight = findViewById(R.id.btnUpdateWeight);
        Button btnSaveGoal = findViewById(R.id.btnSaveGoal);
        Button btnGoToSms = findViewById(R.id.btnGoToSms);
        Button btnLogout = findViewById(R.id.btnLogout);
        RecyclerView recyclerWeights = findViewById(R.id.recyclerWeights);

        tvWelcome.setText(String.format(Locale.US, "Welcome, %s", sessionManager.getUsername()));

        weightAdapter = new WeightAdapter(this);
        recyclerWeights.setLayoutManager(new LinearLayoutManager(this));
        recyclerWeights.setAdapter(weightAdapter);

        btnAddWeight.setOnClickListener(v -> addWeight());
        btnUpdateWeight.setOnClickListener(v -> updateWeight());
        btnSaveGoal.setOnClickListener(v -> saveGoalWeight());
        btnGoToSms.setOnClickListener(v -> startActivity(new Intent(this, SmsPermissionActivity.class)));
        btnLogout.setOnClickListener(v -> logout());

        refreshGoalDisplay();
        loadWeights();
    }

    private void addWeight() {
        String date = etDate.getText().toString().trim();
        String weightText = etDailyWeight.getText().toString().trim();

        if (!validateWeightInput(date, weightText)) {
            return;
        }

        double weightValue = Double.parseDouble(weightText);
        long result = databaseHelper.addWeightEntry(currentUserId, date, weightValue);
        if (result != -1) {
            Toast.makeText(this, "Weight entry added", Toast.LENGTH_SHORT).show();
            clearEntryFields();
            loadWeights();
            sendGoalReachedSmsIfNeeded(weightValue);
        } else {
            Toast.makeText(this, "Unable to add weight entry", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWeight() {
        if (selectedWeightId == -1) {
            Toast.makeText(this, "Tap a row first to select it for editing", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = etDate.getText().toString().trim();
        String weightText = etDailyWeight.getText().toString().trim();

        if (!validateWeightInput(date, weightText)) {
            return;
        }

        double weightValue = Double.parseDouble(weightText);
        boolean updated = databaseHelper.updateWeightEntry(selectedWeightId, date, weightValue);
        if (updated) {
            Toast.makeText(this, "Weight entry updated", Toast.LENGTH_SHORT).show();
            clearEntryFields();
            loadWeights();
            sendGoalReachedSmsIfNeeded(weightValue);
        } else {
            Toast.makeText(this, "Unable to update weight entry", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGoalWeight() {
        String goalText = etGoalWeight.getText().toString().trim();
        if (TextUtils.isEmpty(goalText)) {
            Toast.makeText(this, "Enter a goal weight", Toast.LENGTH_SHORT).show();
            return;
        }

        double goalWeight = Double.parseDouble(goalText);
        boolean saved = databaseHelper.saveGoalWeight(currentUserId, goalWeight);
        if (saved) {
            Toast.makeText(this, "Goal weight saved", Toast.LENGTH_SHORT).show();
            etGoalWeight.setText("");
            refreshGoalDisplay();
        } else {
            Toast.makeText(this, "Unable to save goal weight", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateWeightInput(String date, String weightText) {
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(weightText)) {
            Toast.makeText(this, "Enter both a date and a weight", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadWeights() {
        List<WeightEntry> entries = databaseHelper.getAllWeightEntriesForUser(currentUserId);
        weightAdapter.setWeightEntries(entries);
    }

    private void refreshGoalDisplay() {
        double goalWeight = databaseHelper.getGoalWeight(currentUserId);
        if (goalWeight > 0) {
            tvCurrentGoal.setText(String.format(Locale.US, "Current goal: %.1f lbs", goalWeight));
        } else {
            tvCurrentGoal.setText("Current goal: Not set");
        }
    }

    private void clearEntryFields() {
        selectedWeightId = -1;
        etDate.setText("");
        etDailyWeight.setText("");
    }

    private void sendGoalReachedSmsIfNeeded(double currentWeight) {
        double goalWeight = databaseHelper.getGoalWeight(currentUserId);
        if (goalWeight <= 0 || currentWeight > goalWeight) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Goal reached. SMS permission is not enabled.", Toast.LENGTH_LONG).show();
            return;
        }

        String phone = databaseHelper.getUserPhone(currentUserId);
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Goal reached. Add a phone number on the notification screen.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = String.format(Locale.US,
                    "Congratulations! You reached your goal weight. Current weight: %.1f lbs. Goal: %.1f lbs.",
                    currentWeight,
                    goalWeight);
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(this, "Goal reached! SMS alert sent.", Toast.LENGTH_LONG).show();
        } catch (Exception exception) {
            Toast.makeText(this, "Unable to send SMS on this device/emulator.", Toast.LENGTH_LONG).show();
        }
    }

    private void logout() {
        sessionManager.clearSession();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onEdit(WeightEntry entry) {
        selectedWeightId = entry.getId();
        etDate.setText(entry.getDate());
        etDailyWeight.setText(String.valueOf(entry.getWeight()));
        Toast.makeText(this, "Entry loaded. Edit the values and tap Update Selected.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(WeightEntry entry) {
        boolean deleted = databaseHelper.deleteWeightEntry(entry.getId());
        if (deleted) {
            Toast.makeText(this, "Weight entry deleted", Toast.LENGTH_SHORT).show();
            if (selectedWeightId == entry.getId()) {
                clearEntryFields();
            }
            loadWeights();
        } else {
            Toast.makeText(this, "Unable to delete entry", Toast.LENGTH_SHORT).show();
        }
    }
}
