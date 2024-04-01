package com.example.project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private InventoryDatabaseHelper dbHelper;
    private ImageButton buttonAddItem;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        dbHelper = new InventoryDatabaseHelper(this);

        // Initialize button
        buttonAddItem = findViewById(R.id.button4);

        // Get userId from Intent
        userId = getIntent().getLongExtra("userId", -1); // -1 is default value if userId not found


        // Set click listener for the Add Item button
        buttonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display a dialog to enter item details
                showAddItemDialog();
            }
        });

        populatePanelsFromDatabase();
    }

    // Method to show dialog for adding a new item
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item");

        // Inflate the dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(view);

        // Get references to EditText fields in the dialog layout
        final EditText editTextName = view.findViewById(R.id.editText_name);
        final EditText editTextQuantity = view.findViewById(R.id.editText_quantity);

        // Set positive button (Add Item)
        builder.setPositiveButton("Add Item", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retrieve input values
                String name = editTextName.getText().toString().trim();
                String quantityStr = editTextQuantity.getText().toString().trim();

                // Validate input
                if (name.isEmpty() || quantityStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert quantity to integer
                int quantity = Integer.parseInt(quantityStr);

                // Insert item into database
                long result = dbHelper.insertItem(name, quantity);

                // Check if insertion was successful
                if (result != -1) {
                    Toast.makeText(MainActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    // Optionally update the grid layout with the new item
                    populatePanelsFromDatabase();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set negative button (Cancel)
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private void populatePanelsFromDatabase() {
        int position=0;
        // Get a reference to the GridLayout where panels will be added
        GridLayout gridLayoutPanels = findViewById(R.id.gridLayout_panels);

        // Clear existing panels before adding new ones
        gridLayoutPanels.removeAllViews();

        // Get a readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define the columns to be retrieved
        String[] projection = {
                InventoryDatabaseHelper.InventoryTable.COL_ITEM,
                InventoryDatabaseHelper.InventoryTable.COL_QTY
        };

        // Query the database to retrieve all items
        Cursor cursor = db.query(
                InventoryDatabaseHelper.InventoryTable.TABLE,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        // Iterate over the cursor to populate panels
        while (cursor.moveToNext()) {
            // Retrieve item name and quantity from the cursor
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.InventoryTable.COL_ITEM));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.InventoryTable.COL_QTY));

            // Create a new panel for each item
            addPanelToGridLayout(gridLayoutPanels, itemName, quantity, position);
            position++;
        }

        // Close the cursor and database
        cursor.close();
        dbHelper.close();
    }

    private void addPanelToGridLayout(GridLayout gridLayout, String itemName, int quantity, int position) {
        // Inflate the panel layout
        View panelView = LayoutInflater.from(this).inflate(R.layout.panel_layout, gridLayout, false);

        // Set item name and quantity in the panel
        TextView textViewName = panelView.findViewById(R.id.textView_item);
        TextView textViewQuantity = panelView.findViewById(R.id.textView_quantity);
        textViewName.setText(itemName);
        textViewQuantity.setText(String.valueOf(quantity));

        // Set background color based on position
        int colorResId;
        switch (position % 3) {
            case 0:
                colorResId = R.color.green;
                break;
            case 1:
                colorResId = R.color.blue;
                break;
            case 2:
            default:
                colorResId = R.color.red;
                break;
        }
        panelView.setBackgroundColor(ContextCompat.getColor(this, colorResId));

        // Add the panel to the GridLayout
        gridLayout.addView(panelView);

        // Add edit button
        ImageButton editButton = panelView.findViewById(R.id.edit_button);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show edit options dialog
                showEditOptionsDialog(itemName, quantity);
            }
        });
    }

    private void showEditOptionsDialog(final String itemName, final int quantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Options");

        // Set options in the dialog
        String[] options = {"Edit Name", "Edit Quantity", "Delete"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Edit Name
                        showEditNameDialog(itemName, quantity);
                        break;
                    case 1:
                        // Edit Quantity
                        showEditQuantityDialog(itemName, quantity);
                        break;
                    case 2:
                        // Delete Item
                        deleteItem(itemName);
                        break;
                }
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private void showEditNameDialog(final String itemName, final int quantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                // Update name in the database
                dbHelper.updateItemName(itemName, newName);
                populatePanelsFromDatabase();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private void showEditQuantityDialog(final String itemName, final int quantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Quantity");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(quantity)); // Pre-fill with current quantity
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int newQuantity = Integer.parseInt(input.getText().toString().trim());
                // Update quantity in the database
                dbHelper.updateItemQuantity(itemName, newQuantity);

                if (newQuantity == 0) {
                    sendSMSNotification(itemName);
                }

                populatePanelsFromDatabase();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private void deleteItem(final String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this item?");

        // Set up the buttons
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete item from the database
                dbHelper.deleteItem(itemName);
                populatePanelsFromDatabase();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private void sendSMSNotification(String itemName) {
        // Check if both permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                        == PackageManager.PERMISSION_GRANTED) {
            String message = "Item \"" + itemName + "\" has reached zero quantity.";

            // Retrieve the device's phone number
            // This can be updated to use numbers for any users by adding a field for phone numbers in UserTable
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNumber = telephonyManager.getLine1Number();

            // Check if the phone number is available
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // Send SMS notification
                Log.i("SMS", "sendSMSNotification: Sending message");
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            } else {
                // Phone number not available or permission not granted
                Toast.makeText(this, "Unable to retrieve phone number.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Not all permissions granted .", Toast.LENGTH_SHORT).show();
        }
    }
}