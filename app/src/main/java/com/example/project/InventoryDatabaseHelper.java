package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "iventory.db";
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
        public static final String COL_NAME = "name";
        public static final String COL_EMAIL = "email";
        public static final String COL_PASSWORD = "password";
    }

    // When the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + InventoryTable.TABLE + " (" +
                InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryTable.COL_ITEM + " TEXT, " +
                InventoryTable.COL_QTY + " INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + UserTable.TABLE + " (" +
                UserTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COL_NAME + " TEXT, " +
                UserTable.COL_EMAIL + " TEXT UNIQUE, " +
                UserTable.COL_PASSWORD + " TEXT)");
    }

    // When the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + InventoryTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE);
        // Create tables again
        onCreate(db);
    }

    // Method to insert a user into the User database
    public long insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("users", null, values);
        db.close();
        return newRowId;
    }

    // Method to insert an item into the Inventory database
    public long insertItem(String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("item", itemName);
        values.put("qty", quantity);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("inventory", null, values);
        db.close(); // Close the database connection
        return newRowId;
    }

    // Method to delete an item from the Inventory database
    public void deleteItem(String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("inventory", "item=?", new String[]{String.valueOf(itemName)});
        db.close();
    }

    // Method to update the quantity of an item in the Inventory database
    public void updateItemQuantity(String itemName, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("qty", newQuantity);
        db.update("inventory", values, "item=?", new String[]{String.valueOf(itemName)});
        db.close();
    }

    public void updateItemName(String oldName, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("item", newName);
        db.update("inventory", values, "item = ?", new String[]{oldName});
        db.close();
    }

}
