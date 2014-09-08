package com.randiw.synccontact.model.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class Repository {

    protected SQLiteDatabase database;
    protected ContentResolver resolver;

    public Repository(Context context) {
        resolver = context.getContentResolver();
    }

    public static String createTableIfNotExists(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName;
    }

    public static String primary(String str) {
        String primary = "(" + str;
        return primary;
    }

    public static String primaryAutoID(String field_name) {
        String primaryAutoID = field_name + " INTEGER PRIMARY KEY AUTOINCREMENT";
        return primary(primaryAutoID);
    }

    public static String field(String str) {
        String field = ", " + str;
        return field;
    }

    public static String fieldVarchar(String str) {
        String fieldVarchar = str + " VARCHAR";
        return field(fieldVarchar);
    }

    public static String fieldInteger(String field_name) {
        String fieldInteger = field_name + " INTEGER";
        return field(fieldInteger);
    }

    public static String close() {
        return ");";
    }
}