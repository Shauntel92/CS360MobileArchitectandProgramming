package com.example.weighttrackingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Requests SMS permission only when the user interacts with notification settings.
 */
public class SmsPermissionActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private TextView tvPermissionStatus;
    private EditText etPhoneNumber;

    private final ActivityResultLauncher<String> requestSmsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                updatePermissionStatus();
                if (isGranted) {
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "SMS permission denied. The app will still work without text alerts.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permissions);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        Button btnGrantSmsPermission = findViewById(R.id.btnGrantSmsPermission);
        Button btnSavePhone = findViewById(R.id.btnSavePhone);
        Button btnBackToWeights = findViewById(R.id.btnBackToWeights);

        int userId = sessionManager.getUserId();
        etPhoneNumber.setText(databaseHelper.getUserPhone(userId));
        updatePermissionStatus();

        btnGrantSmsPermission.setOnClickListener(v -> requestSmsPermission());
        btnSavePhone.setOnClickListener(v -> savePhoneNumber());
        btnBackToWeights.setOnClickListener(v -> finish());
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SMS permission is already granted", Toast.LENGTH_SHORT).show();
        } else {
            requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }

    private void savePhoneNumber() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter a phone number first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean saved = databaseHelper.updateUserPhone(sessionManager.getUserId(), phoneNumber);
        if (saved) {
            Toast.makeText(this, "Phone number saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to save phone number", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePermissionStatus() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        tvPermissionStatus.setText(granted
                ? "Permission status: Granted"
                : "Permission status: Not granted");
    }
}
