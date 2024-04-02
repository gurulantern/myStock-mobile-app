package com.example.project;

/*
Name: HistoryActivity.java
Version: 1.0
Author: Alex Ho
Date: 2024-04-02
Description: Defines the activity for the user to see a history of the actions taken upon their
inventory table.
 */

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    // Declarations for elements for Activity to use
    private InventoryDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItemList;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use activity_history.xml for layout view
        setContentView(R.layout.activity_history);

        // Initialize database helper
        dbHelper = new InventoryDatabaseHelper(this);

        // Retrieve userId and inventoryTable from the intent
        Intent intent = getIntent();
        long userId = intent.getLongExtra("userId", -1);
        String inventoryTable = intent.getStringExtra("inventoryTable");

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyItemList = queryHistoryItems(userId, inventoryTable);
        adapter = new HistoryAdapter(historyItemList);
        recyclerView.setAdapter(adapter);

        // Initialize back button
        buttonBack = findViewById(R.id.buttonBackHistory);

        // Set click listener for register button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Call dbHelper to query for all history items from the associated history table
     * @param userId userId for query,
     * @param inventoryTable inventoryTable name for query
     * @return List of the history items for rendering
     */
    private List<HistoryItem> queryHistoryItems(long userId, String inventoryTable) {
        return dbHelper.queryHistoryItems(userId, inventoryTable);
    }
}