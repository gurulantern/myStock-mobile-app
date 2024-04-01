package com.example.project;

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
    private EditText editTextEmail, editTextPassword;
    private TextView textViewInvalidLogin;
    private Button buttonSubmit, buttonRegister;
    private InventoryDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            // Login successful, navigate to the next activity
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            // Navigate to the next activity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // Finish the LoginActivity so that pressing back button won't come back to it
        } else {
            // Login failed, display error message
            textViewInvalidLogin.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateUser(String email, String password) {
        // Perform database query to validate user credentials
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }
}