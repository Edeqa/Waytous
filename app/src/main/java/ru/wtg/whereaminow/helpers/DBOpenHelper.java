package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created 12/21/2016.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "database";

    private static DBOpenHelper instance;

    public static DBOpenHelper getInstance(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        if(instance == null) {
            instance = new DBOpenHelper(context, factory, version);
        }
        return instance;
    }

    private DBOpenHelper(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
