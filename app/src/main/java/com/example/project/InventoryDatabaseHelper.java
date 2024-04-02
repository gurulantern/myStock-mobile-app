package com.example.project;

/*
Name: InventoryDatabaseHelper.java
Version: 2.0
Author: Alex Ho
Date: 2024-04-02
Description: Defines the DB helper to manage the User Table, User Inventory Mapping Table, Inventory
Tables, and History Tables.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 2;

    // Constructor
    public InventoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Strings for the InventoryTable fields
     */
    public static final class InventoryTable {
        public static final String TABLE = "inventory";
        public static final String COL_ID = "_id";
        public static final String COL_ITEM = "item";
        public static final String COL_QTY = "qty";
    }

    /**
     * Strings for the UserTable fields
     */
    public static final class UserTable {
        public static final String TABLE = "users";
        public static final String COL_ID = "_id";
        public static final String COL_USER_ID = "user";
        public static final String COL_NAME = "name";
        public static final String COL_EMAIL = "email";
        public static final String COL_PASSWORD = "password";
    }

    /**
     * Strings for the UserInventoryMapTable fields
     */
    public static final class UserInventoryMapTable {
        public static final String TABLE = "user_inventory_map";
        public static final String COL_ID = "_id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_INVENTORY_TABLE_NAME = "inventory_table_name";
    }

    /**
     * Strings for the HistoryTable fields
     */
    public static final class HistoryTable {
        public static final String TABLE = "history";
        public static final String COL_ID = "_id";

        public static final String COL_USER_ID = "user_id";
        public static final String COL_ACTIONS = "actions";
        public static final String COL_TIMESTAMP = "timestamp";
        public static final String COL_OLD_NAME = "old_name";
        public static final String COL_CURRENT_NAME = "current_name";
        public static final String COL_QTY = "qty";

    }

    // Creation process for the database. Only initializes the User and UserInventoryMap as
    // Histories and Inventories will now be initialized when a user registers
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + UserTable.TABLE + " (" +
                UserTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COL_USER_ID + " LONG, " +
                UserTable.COL_NAME + " TEXT, " +
                UserTable.COL_EMAIL + " TEXT UNIQUE, " +
                UserTable.COL_PASSWORD + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + UserInventoryMapTable.TABLE + " (" +
                UserInventoryMapTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserInventoryMapTable.COL_USER_ID + " LONG, " +
                UserInventoryMapTable.COL_INVENTORY_TABLE_NAME + " TEXT, " +
                "FOREIGN KEY(" + UserInventoryMapTable.COL_USER_ID + ") REFERENCES " + UserTable.TABLE + "(" + UserTable.COL_ID + "))");
    }

    // Upgrade Process when a database is updated.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + InventoryTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + UserInventoryMapTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + HistoryTable.TABLE);
        // Create tables again
        onCreate(db);
    }

    /**
     * NEW function to register a user and create Inventory and History tables for user.
     * Inserts user information into the global UserTable and UserInventoryTable.
     * @param name User's name entered at registration
     * @param email User's email entered at registration
     * @param password User's passwrod entered at registration
     * @return the generated userId
     */
    public long registerUserDB(String name, String email, String password) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            if (db == null) {
                System.err.println("Failed to get writable database");
                return -1; // Indicate failure
            }
            // Generate a unique user id
            long userId = generateUniqueUserId(name);

            // Create a unique InventoryTable for the user
            String inventoryTableName = "inventory_" + name; // Unique name
            createInventoryTable(db, inventoryTableName);

            // Create a unique HistoryTable for the user
            String historyTableName = "history_" + inventoryTableName; // Unique name
            createHistoryTable(db, historyTableName);

            // Insert the user into UserTable
            ContentValues userValues = new ContentValues();
            userValues.put(UserTable.COL_USER_ID, userId);
            userValues.put(UserTable.COL_NAME, name);
            userValues.put(UserTable.COL_EMAIL, email);
            userValues.put(UserTable.COL_PASSWORD, password);
            db.insert(UserTable.TABLE, null, userValues);

            // Insert a mapping between the user and their inventory table
            ContentValues mapValues = new ContentValues();
            mapValues.put(UserInventoryMapTable.COL_USER_ID, userId);
            mapValues.put(UserInventoryMapTable.COL_INVENTORY_TABLE_NAME, inventoryTableName);
            db.insert(UserInventoryMapTable.TABLE, null, mapValues);

            // Return userId
            return userId;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    /**
     * NEW Creates an Inventory table using the strings from InventoryTable.
     * @param db this instance of database
     * @param tableName "inventory_[user's name]" Created at registration
     */
    private void createInventoryTable(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryTable.COL_ITEM + " TEXT, " +
                InventoryTable.COL_QTY + " INTEGER)");
    }

    /**
     * NEW Creates a History Table using strings from HistoryTable
     * References UserTable with userId
     * @param db this instance of the database
     * @param tableName "history_[inventoryTable name]" Created at registration
     */
    private void createHistoryTable(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                HistoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HistoryTable.COL_USER_ID + " LONG, " +
                HistoryTable.COL_ACTIONS + " TEXT, " +
                HistoryTable.COL_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                HistoryTable.COL_OLD_NAME + " STRING, " +
                HistoryTable.COL_CURRENT_NAME + " STRING, " +
                HistoryTable.COL_QTY + " INTEGER, " +
                "FOREIGN KEY(" + HistoryTable.COL_USER_ID + ") REFERENCES " + UserTable.TABLE + "(" + UserTable.COL_ID + "))");
    }

    /**
     * NEW Function to query history table for all history items
     * @param userId userId for query
     * @param inventoryTable inventory table name for query
     * @return
     */
    public List<HistoryItem> queryHistoryItems(long userId, String inventoryTable) {
        List<HistoryItem> historyItemList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Initialize strings so if nothing is found, errors will display
        String action = "Error getting action";
        String timestamp = "Error getting timestamp";
        int quantity = 0;
        String oldName = "Error getting old name";
        String newName = "Error getting new name";

        // Query the database for history items based on userId and inventoryTable
        String query = "SELECT * FROM " + "history_" + inventoryTable +
                " WHERE " + HistoryTable.COL_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        // Iterate over the cursor to retrieve history items
        while (cursor.moveToNext()) {
            // Throw exception if -1
            int columnIndex = cursor.getColumnIndexOrThrow(HistoryTable.COL_ACTIONS);
            action = cursor.getString(columnIndex); // set actions

            columnIndex = cursor.getColumnIndexOrThrow(HistoryTable.COL_TIMESTAMP);
            timestamp = cursor.getString(columnIndex); // set timestamps

            columnIndex = cursor.getColumnIndexOrThrow(HistoryTable.COL_QTY);
            quantity = cursor.getInt(columnIndex); // set

            columnIndex = cursor.getColumnIndexOrThrow(HistoryTable.COL_OLD_NAME);
            oldName = cursor.getString(columnIndex); // set old name

            columnIndex = cursor.getColumnIndexOrThrow(HistoryTable.COL_CURRENT_NAME);
            newName = cursor.getString(columnIndex); // set new name

            // Query the UserTable to get the username
            String userName = getUserName(userId);

            // Create a new HistoryItem and add it to the list
            HistoryItem historyItem = new HistoryItem(action, timestamp, userName, quantity, oldName, newName);
            historyItemList.add(historyItem);
        }

        // Close the cursor and database
        cursor.close();
        db.close();

        return historyItemList;
    }

    /**
     * NEW Getter for inventory table name using userId
     * @param userId long number userId
     * @return String of user's name
     */
    public String getInventoryTableName(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String inventoryTableName = null;

        try (Cursor cursor = db.query(
                UserInventoryMapTable.TABLE,
                new String[]{UserInventoryMapTable.COL_INVENTORY_TABLE_NAME},
                UserInventoryMapTable.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, // Use userId in template
                null,
                null,
                null)) {

            if (cursor.moveToFirst()) {
                // Throw exception if index if -1
                int columnIndex = cursor.getColumnIndexOrThrow(UserInventoryMapTable.COL_INVENTORY_TABLE_NAME);
                inventoryTableName = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inventoryTableName;
    }

    /**
     * NEW Getter for current timestamp. Mainly for setting timestamp in History insert
     * @return simple date format "yyyy-MM-dd HH:mm:ss"
     */
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * NEW Getter for the item names in the inventory table
     * Used for Pie Chart
     * @param inventoryTableName inventory table name
     * @return list of names of items in the inventory
     */
    public List<String> getItemNames(String inventoryTableName) {
        // initialize array for item names
        List<String> itemNames = new ArrayList<>();

        // Initialize item name holder for iterate
        String itemName = "";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Query the inventory table to retrieve item names
            cursor = db.query(
                    inventoryTableName,
                    new String[]{InventoryTable.COL_ITEM},
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // Iterate over the cursor to retrieve item names
            while (cursor.moveToNext()) {

                // Throw exception if -1
                int columnIndex = cursor.getColumnIndexOrThrow(InventoryTable.COL_ITEM);
                itemName = cursor.getString(columnIndex);

                // Add item name to the item list
                itemNames.add(itemName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return itemNames;
    }

    /**
     * NEW Getter for item quantity
     * @param itemName name of item to search
     * @param inventoryTableName name of inventory table
     * @return quantity
     */
    public int getItemQuantity(String itemName, String inventoryTableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int quantity = 0; // Default value if item not found

        try (Cursor cursor = db.query(
                inventoryTableName,
                new String[]{InventoryTable.COL_QTY},
                InventoryTable.COL_ITEM + " = ?",
                new String[]{itemName},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                // Throw exception if index is -1
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryTable.COL_QTY));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quantity;
    }


    /**
     * NEW Getter for user id
     * @param email user's email
     * @param password user's password
     * @return long of user id
     */
    public long getUserId(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1; // Initialize with default value indicating no user found

        try (Cursor cursor = db.rawQuery(
                "SELECT " + UserTable.COL_USER_ID +
                        " FROM " + UserTable.TABLE +
                        " WHERE " + UserTable.COL_EMAIL + " = ? AND " + UserTable.COL_PASSWORD + " = ?",
                new String[]{email, password})) {

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(UserTable.COL_USER_ID);
                userId = cursor.getLong(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userId;
    }

    /**
     * NEW Getter for user name
     * @param userId user's id
     * @return string of user's name
     */
    private String getUserName(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userName = "";

        try (Cursor cursor = db.rawQuery(
                "SELECT " + UserTable.COL_NAME +
                        " FROM " + UserTable.TABLE +
                        " WHERE " + UserTable.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)})) {

            if (cursor.moveToFirst()) {
                // Throw exception if index is -1
                int columnIndex = cursor.getColumnIndexOrThrow(UserTable.COL_NAME);
                userName = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userName;
    }

    /**
     * UPDATE Inserts item into user's inventory table
     * No longer one shared table
     * @param inventoryTableName inventory table name
     * @param itemName name of item
     * @param quantity quantity of item
     * @return row id of insertion
     */
    public long insertItem(String inventoryTableName, String itemName, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            // values to insert
            values.put(InventoryTable.COL_ITEM, itemName);
            values.put(InventoryTable.COL_QTY, quantity);
            return db.insert(inventoryTableName, null, values);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * UPDATE Deletes item from user's inventory table
     * @param inventoryTableName name of user's inventory table
     * @param itemName name of item to be deleted
     */
    public void deleteItem(String inventoryTableName, String itemName) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            // Delete command
            db.delete(inventoryTableName, InventoryTable.COL_ITEM + "=?", new String[]{itemName});
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * UPDATE Updates the quantity attached to an item
     * @param inventoryTableName name of user's inventory table
     * @param itemName name of item to be updated
     * @param newQuantity updated quantity
     */
    public void updateItemQuantity(String inventoryTableName, String itemName, int newQuantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InventoryTable.COL_QTY, newQuantity);
            // Update call
            db.update(inventoryTableName, values, InventoryTable.COL_ITEM + "=?", new String[]{itemName});
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * UPDATE Updates item name in user's inventory table
     *
     * @param inventoryTableName name of user's inventory table
     * @param oldName old name of item
     * @param newName new name of item
     */
    public void updateItemName(String inventoryTableName, String oldName, String newName) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InventoryTable.COL_ITEM, newName);
            db.update(inventoryTableName, values, InventoryTable.COL_ITEM + "=?", new String[]{oldName});
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * NEW Rename action insertion to history table
     * Sets quantity to 0 as it is irrelevant
     * @param historyTableName name of history table
     * @param userId id of user for looking up name
     * @param oldName old name of item
     * @param currentName new name of item
     * @return row id of insertion
     */
    public long insertRenameAction(String historyTableName, long userId, String oldName, String currentName) {
        return insertAction(historyTableName, userId, "rename",0, oldName, currentName);
    }

    /**
     * NEW Delete action insertion to history table.
     * Sets quantity to 0 as it is irrelevant
     * @param historyTableName name of history table
     * @param userId id of user
     * @param itemName name of deleted item
     * @return row id of insertion
     */
    public long insertDeleteAction(String historyTableName, long userId, String itemName) {
        return insertAction(historyTableName, userId, "delete", 0, itemName, itemName);
    }

    /**
     * NEW Add action insertion to history table
     * Sets oldName to "N/A" as it is irrelevant
     * @param historyTableName name of history table
     * @param userId id of user
     * @param itemName name of added item
     * @param quantity quantity of item when added
     * @return row id of insertion
     */
    public long insertAddAction(String historyTableName, long userId, String itemName, int quantity) {
        return insertAction(historyTableName, userId, "add", quantity, "N/A", itemName);
    }

    /**
     * NEW Change Quantity action insertion to history table
     * @param historyTableName name of history table
     * @param userId id of user
     * @param itemName name of item changed
     * @param quantity quantity change (- or +)
     * @return row id of insertion
     */
    public long insertChangeQuantityAction(String historyTableName, long userId, String itemName, int quantity) {
        return insertAction(historyTableName, userId, "change_quantity", quantity, "N/A", itemName);
    }

    /**
     * Basic insert action used in specific insertions above
     * @param historyTableName name of history table
     * @param userId id of user
     * @param actionType string of action type
     * @param quantity changed quantity
     * @param oldName old name for rename
     * @param currentName current name of item
     * @return row id of insertion
     */
    private long insertAction(String historyTableName, long userId, String actionType, int quantity, String oldName, String currentName) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(HistoryTable.COL_USER_ID, userId);
            values.put(HistoryTable.COL_ACTIONS, actionType);
            values.put(HistoryTable.COL_QTY, quantity);
            values.put(HistoryTable.COL_TIMESTAMP, getCurrentTimestamp());
            values.put(HistoryTable.COL_OLD_NAME, oldName);
            values.put(HistoryTable.COL_CURRENT_NAME, currentName);
            return db.insert(historyTableName, null, values);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Generates a unique user id using user's name and current time in milliseconds
     * Hashes the combined string
     * @param name user's name entered at registration
     * @return unique user id
     */
    private long generateUniqueUserId(String name) {
        // Generate a unique id using a combination of user's name and current time
        String combinedString = name + System.currentTimeMillis();
        // You can further process the combinedString if needed, like hashing
        return combinedString.hashCode(); // Using hashCode() as a simple example
    }
}
