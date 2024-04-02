package com.example.project;

/*
Name: DataActivity.java
Version: 1.0
Author: Alex Ho
Date: 2024-04-02
Description: Defines the activity for a logged-in user to view a pie chart data visualization of
their personal inventory table. Utilizes MPAndroidChart - https://weeklycoding.com/mpandroidchart/
 */

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;
import java.util.List;

public class DataActivity extends AppCompatActivity {

    // Declarations for elements for Activity to use
    private PieChart pieChart;
    private Button buttonBack;
    private InventoryDatabaseHelper dbHelper;

    // Creation process
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use activity_data.xml for layout view
        setContentView(R.layout.activity_data);

        // Initialize database helper
        dbHelper = new InventoryDatabaseHelper(this);

        // Initialize pie chart with pieChart element in xml
        pieChart = findViewById(R.id.pieChart);

        // Initialize back button with buttonBackData element in xml
        buttonBack = findViewById(R.id.buttonBackData);

        // Set click listener for register button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes activity and exits back to main activity
            }
        });

        // Retrieve userId and inventory table name from intent
        Intent intent = getIntent();

        // Grab the inventoryTable string from intent passed from main activity
        String inventoryTable = intent.getStringExtra("inventoryTable");

        // Load data from inventory table into the pie chart
        loadDataIntoPieChart(inventoryTable);
    }

    /**
     * Loads data from the user's inventory table
     * Sets all properties for the pie chart itself
     * @param inventoryTable String title used to look up items
     */
    private void loadDataIntoPieChart(String inventoryTable) {
        List<PieEntry> entries = new ArrayList<>(); // initialize an entries list for the pie chart

        // Retrieve item names and quantities from the inventory table
        List<String> itemNames = dbHelper.getItemNames(inventoryTable);

        // Loop through the item names to look up their quantities in the inventory table
        for (String itemName : itemNames) {
            int quantity = dbHelper.getItemQuantity(itemName, inventoryTable);
            entries.add(new PieEntry(quantity, itemName)); // Add the name and quantity to the entries list
        }

        // Create a dataset from the entries
        PieDataSet dataSet = new PieDataSet(entries, "Inventory");
        int[] colors = grabColors(); // Use grabColors to use the colors stored in ColorTemplate provided by MPAndroidChart
        dataSet.setColors(colors); // Set colors for the slices
        dataSet.setValueTextSize(18f); // Sets size of text for values on slices

        // Set Description
        Description description = new Description(); // Declare a new description object provided by MPAndroidChart
        description.setText("Total Inventory"); // Set the text of the Description label
        description.setTextAlign(Paint.Align.RIGHT); // Align the text to the Right
        description.setTextSize(18f); // Set the text size to 18f

        // Create a PieData object from the dataset
        PieData data = new PieData(dataSet);

        // Set the data and description to the pie chart
        pieChart.setData(data);
        pieChart.setDescription(description);

        // Refresh chart
        pieChart.invalidate();
        // Alter values in legend
        Legend legend = pieChart.getLegend();
        legend.setTextSize(18f); // Set text size of the legend
        legend.setEnabled(true); // Enable the legend
        legend.setWordWrapEnabled(true); // Enable wrapping of multiple legend entries
        legend.setMaxSizePercent(0.50f); // Set the max size for wrapping purposes
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT); // Set the alignment to the right
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Set the orientation to horizontal for wrapping
        legend.setDrawInside(false); // Prevent legend from drawing in chart
    }

    /**
     * Creates a concatenated array of the colors provided in the ColorTemplate provided by MPAndroidChart
     * @return The concatenated array of colors
     */
    private int[] grabColors() {
        int length = ColorTemplate.COLORFUL_COLORS.length + ColorTemplate.PASTEL_COLORS.length + ColorTemplate.MATERIAL_COLORS.length;
        int[] concatArray = new int[length];
        // Copy colors from beginning
        for (int i = 0; i < ColorTemplate.COLORFUL_COLORS.length; i++) {
            concatArray[i] = ColorTemplate.COLORFUL_COLORS[i];
        }

        // Copy colors from the end of the last array
        for (int i = 0; i < ColorTemplate.PASTEL_COLORS.length; i++) {
            concatArray[ColorTemplate.COLORFUL_COLORS.length + i] = ColorTemplate.PASTEL_COLORS[i];
        }

        // Copy colors from the end of the last array
        for (int i = 0; i < ColorTemplate.MATERIAL_COLORS.length; i++) {
            concatArray[ColorTemplate.PASTEL_COLORS.length + i] = ColorTemplate.MATERIAL_COLORS[i];
        }
        return concatArray;
    }
}