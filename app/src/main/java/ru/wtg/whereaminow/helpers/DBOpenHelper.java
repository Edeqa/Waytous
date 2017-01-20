package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created 12/21/2016.
 */
@SuppressWarnings("WeakerAccess")
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "database";

    private static DBOpenHelper instance;

    private DBOpenHelper(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, version);
    }

    public static DBOpenHelper getInstance(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        if(instance == null) {
            instance = new DBOpenHelper(context, factory, version);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
