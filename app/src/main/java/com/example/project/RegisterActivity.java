package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_SMS = 0;
    private static final int PERMISSION_REQUEST_READ_PHONE_NUMBERS = 456;
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private InventoryDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database helper
        dbHelper = new InventoryDatabaseHelper(this);

        // Initialize UI components
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Set click listener for register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle register button click
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate input fields
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password and confirm password match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert user into the database
        long result = dbHelper.insertUser(name, email, password);
        if (result != -1) {
            // Registration successful
            requestPermissions();
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
        } else {
            // Registration failed
            Log.e("RegisterActivity", "Registration failed");
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SMS);
        } else {
            // SMS permission already granted
            Toast.makeText(this, "SMS Permission already granted", Toast.LENGTH_SHORT).show();
            // Check if READ_PHONE_NUMBERS permission is not granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
                // Request READ_PHONE_NUMBERS permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS}, PERMISSION_REQUEST_READ_PHONE_NUMBERS);
            } else {
                // READ_PHONE_NUMBERS permission already granted
                Toast.makeText(this, "Phone Number Permission already granted", Toast.LENGTH_SHORT).show();
                // Both permissions granted, start the MainActivity
                startMainActivity();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // SMS permission granted
                Toast.makeText(this, "SMS Permission granted", Toast.LENGTH_SHORT).show();
                // Check if READ_PHONE_NUMBERS permission is needed
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request READ_PHONE_NUMBERS permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_PHONE_NUMBERS},
                            PERMISSION_REQUEST_READ_PHONE_NUMBERS);
                } else {
                    // Both permissions granted, start the MainActivity
                    startMainActivity();
                }
            } else {
                // SMS permission denied
                Toast.makeText(this, "SMS Permission denied", Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        } else if (requestCode == PERMISSION_REQUEST_READ_PHONE_NUMBERS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_NUMBERS permission granted
                Toast.makeText(this, "Phone Number Permission granted", Toast.LENGTH_SHORT).show();
                // Both permissions granted, start the MainActivity
                startMainActivity();
            } else {
                // READ_PHONE_NUMBERS permission denied
                Toast.makeText(this, "Phone Number Permission denied", Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        }
    }


    private void startMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}