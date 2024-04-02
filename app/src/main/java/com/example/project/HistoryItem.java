package com.example.project;

/*
Name: HistoryItem.java
Version: 1.0
Author: Alex Ho
Date: 2024-04-02
Description: Defines the History Item model for the adapter to use in the RecyclerView.
 */

import android.util.Log;

public class HistoryItem {
    private String action;
    private String timestamp;
    private String userName;
    private int quantity;
    private String oldName;
    private String newName;

    // Constructor
    public HistoryItem(String action, String timestamp, String userName, int quantity, String oldName, String newName) {
        Log.d("Setting User Name", userName);
        this.action = action;
        this.timestamp = timestamp;
        this.userName = userName;
        this.quantity = quantity;
        this.oldName = oldName;
        this.newName = newName;
    }

    /**
     * Switch statement checks the action field and returns a string that matches the action.
     * @return String for the history item displayed int history recycler view
     */
    public String getActionString() {
        switch (action) {
            case "rename":
                action = "Renamed '" + oldName + "' to '" + newName + ".'" ;
                break;
            case "add":
                action = "Added " + newName + " to database.";
                break;
            case "delete":
                action = "Deleted " + oldName + " from database.";
                break;
            case "change_quantity":
                if (quantity <= 0) {
                    action = "Changed quantity of " + newName + ": " + String.valueOf(quantity);
                } else {
                    action = "Changed quantity of " + newName + ": +" + String.valueOf(quantity);
                }
                break;
            default:
                action = "Unknown action";
                break;
        }
        return action;
    }

    /**
     * Getter for the timestamp
     * @return timestamp string
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the userName
     * @return userName string
     */
    public String getUserName() {
        return userName;
    }
}
