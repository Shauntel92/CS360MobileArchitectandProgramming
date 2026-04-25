package com.example.weighttrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WeightLogActivity extends AppCompatActivity {

    Button btnGoToSms;
    RecyclerView recyclerWeights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_log);

        btnGoToSms = findViewById(R.id.btnGoToSms);
        recyclerWeights = findViewById(R.id.recyclerWeights);

        recyclerWeights.setLayoutManager(new LinearLayoutManager(this));

        btnGoToSms.setOnClickListener(v -> {
            Intent intent = new Intent(WeightLogActivity.this, SmsPermissionActivity.class);
            startActivity(intent);
        });
    }
}