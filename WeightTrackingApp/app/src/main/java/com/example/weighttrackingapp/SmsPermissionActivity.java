package com.example.weighttrackingapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SmsPermissionActivity extends AppCompatActivity {

    Button btnBackToWeights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permissions);

        btnBackToWeights = findViewById(R.id.btnBackToWeights);

        btnBackToWeights.setOnClickListener(v -> finish());
    }
}