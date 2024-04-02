package com.example.project;

import android.util.Log;

public class HistoryItem {
    private String action;
    private String timestamp;
    private String userName;
    private int quantity;
    private String oldName;
    private String newName;

    // You can add more fields as needed

    public HistoryItem(String action, String timestamp, String userName, int quantity, String oldName, String newName) {
        Log.d("Setting User Name", userName);
        this.action = action;
        this.timestamp = timestamp;
        this.userName = userName;
        this.quantity = quantity;
        this.oldName = oldName;
        this.newName = newName;
    }

    public String getCurrentName() {
        return oldName;
    }

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

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserName() {
        Log.d("History Getting User Name", userName);
        return userName;
    }
}
