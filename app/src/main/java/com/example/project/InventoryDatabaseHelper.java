package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final class InventoryTable {
        public static final String TABLE = "inventory";
        public static final String COL_ID = "_id";
        public static final String COL_ITEM = "item";
        public static final String COL_QTY = "qty";
    }

    public static final class UserTable {
        public static final String TABLE = "users";
        public static final String COL_ID = "_id";
        public static final String COL_USER_ID = "user";
        public static final String COL_NAME = "name";
        public static final String COL_EMAIL = "email";
        public static final String COL_PASSWORD = "password";
    }

    public static final class UserInventoryMapTable {
        public static final String TABLE = "user_inventory_map";
        public static final String COL_ID = "_id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_INVENTORY_TABLE_NAME = "inventory_table_name";
    }


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

    // When the database is created for the first time
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

    // When the database needs to be upgraded
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

    // NEW: Register a user into DB and create unique tables
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

            return userId;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    // NEW: Create unique InventoryTable for unique user
    private void createInventoryTable(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryTable.COL_ITEM + " TEXT, " +
                InventoryTable.COL_QTY + " INTEGER)");
    }

    // NEW: Create unique HistoryTable for unique user
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

    public List<HistoryItem> queryHistoryItems(long userId, String inventoryTable) {
        List<HistoryItem> historyItemList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
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

            int columnIndex = cursor.getColumnIndex(HistoryTable.COL_ACTIONS);
            if (columnIndex != -1) {
                action = cursor.getString(columnIndex);
            }
            columnIndex = cursor.getColumnIndex(HistoryTable.COL_TIMESTAMP);
            if (columnIndex != -1) {
                timestamp = cursor.getString(columnIndex);
            }
            columnIndex = cursor.getColumnIndex(HistoryTable.COL_QTY);
            if (columnIndex != -1) {
                quantity = cursor.getInt(columnIndex);
            }
            columnIndex = cursor.getColumnIndex(HistoryTable.COL_OLD_NAME);
            if (columnIndex != -1) {
                oldName = cursor.getString(columnIndex);
            }
            columnIndex = cursor.getColumnIndex(HistoryTable.COL_CURRENT_NAME);
            if (columnIndex != -1) {
                newName = cursor.getString(columnIndex);
            }

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

    public String getInventoryTableName(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String inventoryTableName = null;

        try (Cursor cursor = db.query(
                UserInventoryMapTable.TABLE,
                new String[]{UserInventoryMapTable.COL_INVENTORY_TABLE_NAME},
                UserInventoryMapTable.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null)) {

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(UserInventoryMapTable.COL_INVENTORY_TABLE_NAME);
                if (columnIndex != -1) {
                    inventoryTableName = cursor.getString(columnIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inventoryTableName;
    }

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
                quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryTable.COL_QTY));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quantity;
    }

    public long getUserId(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        long userId = -1; // Initialize with default value indicating no user found

        try (Cursor cursor = db.rawQuery(
                "SELECT " + UserTable.COL_USER_ID +
                        " FROM " + UserTable.TABLE +
                        " WHERE " + UserTable.COL_EMAIL + " = ? AND " + UserTable.COL_PASSWORD + " = ?",
                new String[]{email, password})) {

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(UserTable.COL_USER_ID);
                if (columnIndex != -1) {
                    userId = cursor.getLong(columnIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userId;
    }

    // Method to retrieve the username based on userId
    private String getUserName(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userName = "";

        try (Cursor cursor = db.rawQuery(
                "SELECT " + UserTable.COL_NAME +
                        " FROM " + UserTable.TABLE +
                        " WHERE " + UserTable.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)})) {

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(UserTable.COL_NAME);
                if (columnIndex != -1) {
                    userName = cursor.getString(columnIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("Getting User Name", userName);
        return userName;
    }

    public long insertItem(String inventoryTableName, String itemName, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InventoryTable.COL_ITEM, itemName);
            values.put(InventoryTable.COL_QTY, quantity);
            return db.insert(inventoryTableName, null, values);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void deleteItem(String inventoryTableName, String itemName) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.delete(inventoryTableName, InventoryTable.COL_ITEM + "=?", new String[]{itemName});
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void updateItemQuantity(String inventoryTableName, String itemName, int newQuantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InventoryTable.COL_QTY, newQuantity);
            db.update(inventoryTableName, values, InventoryTable.COL_ITEM + "=?", new String[]{itemName});
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

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

    public long insertRenameAction(String historyTableName, long userId, String oldName, String currentName) {
        return insertAction(historyTableName, userId, "rename",0, oldName, currentName);
    }

    public long insertDeleteAction(String historyTableName, long userId, String itemName) {
        return insertAction(historyTableName, userId, "delete", 0, itemName, "N/A");
    }

    public long insertAddAction(String historyTableName, long userId, String itemName, int quantity) {
        return insertAction(historyTableName, userId, "add", quantity, "N/A", itemName);
    }

    public long insertChangeQuantityAction(String historyTableName, long userId, String itemName, int quantity) {
        return insertAction(historyTableName, userId, "change_quantity", quantity, "N/A", itemName);
    }

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

    // Helper method to get current timestamp
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    private long generateUniqueUserId(String name) {
        // Generate a unique id using a combination of user's name and current time
        String combinedString = name + System.currentTimeMillis();
        // You can further process the combinedString if needed, like hashing
        return combinedString.hashCode(); // Using hashCode() as a simple example
    }
}
