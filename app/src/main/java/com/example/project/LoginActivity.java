package com.example.project;

/*
Name: LoginActivity.java
Version: 2.0
Author: Alex Ho
Date: 2024-04-02
Description: Defines the Login activity to validate a user's email and password.
Passes intent with userId for table lookup in main activity
 */

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {
    // Declarations of elements for activity
    private EditText editTextEmail, editTextPassword;
    private TextView textViewInvalidLogin;
    private Button buttonSubmit, buttonRegister;
    private InventoryDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use activity_login.xml for view
        setContentView(R.layout.activity_login);

        // Initialize database helper
        dbHelper = new InventoryDatabaseHelper(this);

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewInvalidLogin = findViewById(R.id.textViewInvalidLogin);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Set click listener for submit button
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle submit button click
                login();
            }
        });

        // Set click listener for register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Login function validates if fields are empty
     * Validates inputs and gets userId for intent
     */
    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user credentials in the database
        if (validateUser(email, password)) {
            long userId = dbHelper.getUserId(email, password);
            // Login successful, navigate to the next activity
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            // Navigate to the next activity
            startMainActivity(userId);
            finish(); // Finish the LoginActivity so that pressing back button won't come back to it
        } else {
            // Login failed, display error message
            textViewInvalidLogin.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Function to query database and validate the user input
     * @param email user input email
     * @param password user input password
     * @return True if valid, False if invalid
     */
    private boolean validateUser(String email, String password) {
        // Perform database query to validate user credentials
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    /**
     * Starts main activity passing userId so inventory table can be found
     * @param userId id of user
     */
    private void startMainActivity(long userId) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userId", userId); // Pass userId to MainActivity
        startActivity(intent); // start main activity
        finish(); // Finish the LoginActivity so that pressing back button won't come back to it
    }
}