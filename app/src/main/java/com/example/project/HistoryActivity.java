package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private InventoryDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItemList;
    private Button buttonBack;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        buttonBack = findViewById(R.id.buttonBack);

        // Set click listener for register button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize search EditText
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterHistoryItems(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


    }

    private List<HistoryItem> queryHistoryItems(long userId, String inventoryTable) {
        return dbHelper.queryHistoryItems(userId, inventoryTable);
    }

    private void filterHistoryItems(String query) {
        List<HistoryItem> filteredList = new ArrayList<>();
        for (HistoryItem item : historyItemList) {
            if (item.getCurrentName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.setItems(filteredList);
    }

    // This method is just for demonstration, you should replace it with your actual data loading logic
    private List<HistoryItem> loadHistoryItems() {
        List<HistoryItem> items = new ArrayList<>();
        // Load your history items from wherever you get them
        return items;
    }
}