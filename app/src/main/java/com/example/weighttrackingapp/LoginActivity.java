package com.example.weighttrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Lets existing users log in and first-time users create an account.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // If a user is already logged in, send them directly to the weight log.
        if (sessionManager.isLoggedIn()) {
            openWeightLog();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(v -> loginUser());
        btnCreateAccount.setOnClickListener(v -> createAccount());
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateCredentials(username, password)) {
            return;
        }

        int userId = databaseHelper.authenticateUser(username, password);
        if (userId != -1) {
            sessionManager.saveLogin(userId, username);
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            openWeightLog();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void createAccount() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateCredentials(username, password)) {
            return;
        }

        if (databaseHelper.usernameExists(username)) {
            Toast.makeText(this, "Username already exists. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean created = databaseHelper.createUser(username, password);
        if (created) {
            int userId = databaseHelper.authenticateUser(username, password);
            sessionManager.saveLogin(userId, username);
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
            openWeightLog();
        } else {
            Toast.makeText(this, "Unable to create account", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateCredentials(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter both a username and password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void openWeightLog() {
        Intent intent = new Intent(LoginActivity.this, WeightLogActivity.class);
        startActivity(intent);
        finish();
    }
}
