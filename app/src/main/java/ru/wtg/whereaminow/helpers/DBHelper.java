package ru.wtg.whereaminow.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created 12/20/2016.
 */

public class DBHelper<T extends AbstractSavedItem> {

    private static final String DB_NAME = "mydb";
    private static final int DB_VERSION = 1;

    public static final String COLUMN_ID = "_id";
//        public static final String COLUMN_IMG = "img";
//        public static final String COLUMN_TXT = "txt";

    private final T.Fields fields;
    private final Context context;

    private DBOpenHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DBHelper(Context context, T.Fields fields){
        this.context = context;
        this.fields = fields;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBOpenHelper(context, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        if(!isTableExists(fields.itemType)) {
            mDB.execSQL(fields.getCreateString());
        }
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDB.query(fields.itemType, null, null, null, null, null, null);
    }

    public int getCount() {
        return getAllData().getCount();
//        return DatabaseUtils.queryNumEntries(mDB, fields.itemType);
    }

    public void clear() {
        mDB.delete(fields.itemType, null, null);
    }

    // добавить запись в DB_TABLE
    public void save(T item) {
        ContentValues cv = new ContentValues();

        System.out.println("SAVEVALUE:"+item);
        for(Map.Entry<String,T.Fields.FieldOptions> x: fields.fields.entrySet()){
            try {
                Field field = item.getClass().getDeclaredField(x.getValue().name);
                field.setAccessible(true);
                Object value = field.get(item);
                if(value != null) {
                    if(x.getValue().serialize) {
                        cv.put(x.getValue().name+"_", Utils.serializeToString((Serializable) Class.forName(x.getValue().sourceType).cast(value)));
                    } else if (x.getValue().sourceType.equals("boolean")) {
                        cv.put(x.getValue().name+"_", Boolean.class.cast(value));
                    } else if (x.getValue().sourceType.equals("int")) {
                        cv.put(x.getValue().name+"_", Integer.class.cast(value));
                    } else if (x.getValue().sourceType.equals("long")) {
                        cv.put(x.getValue().name+"_", Long.class.cast(value));
                    } else if (x.getValue().sourceType.equals("float")) {
                        cv.put(x.getValue().name+"_", Float.class.cast(value));
                    } else if (x.getValue().sourceType.equals("double")) {
                        cv.put(x.getValue().name+"_", Double.class.cast(value));
                    } else if (x.getValue().sourceType.equals("java.lang.String")) {
                        cv.put(x.getValue().name+"_", String.class.cast(value));
                    }
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("CONTENTVALUE:"+cv);
        if(item.getNumber() > 0){
            mDB.update(fields.itemType, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getNumber())});
        } else {
            long a = mDB.insert(fields.itemType, null, cv);
            item.setNumber(a);
            System.out.println("INSERTED:"+a);
        }

    }

    // удалить запись из DB_TABLE
    public void delete(long id) {
        mDB.delete(fields.itemType, COLUMN_ID + " = " + id, null);
    }

    public void deleteByPosition(int position) {
        Cursor cursor = getAllData();
        cursor.move(position+1);
        delete(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
    }

    public void delete(T item) {
        delete(item.getNumber());
    }

    // класс по созданию и управлению БД
    private class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {

/*
                ContentValues cv = new ContentValues();
                for (int i = 1; i < 5; i++) {
                    cv.put(COLUMN_TXT, "sometext " + i);
//                    cv.put(COLUMN_IMG, R.drawable.ic_launcher);
                    db.insert(DB_TABLE, null, cv);
                }
*/
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }

    public boolean isTableExists(String tableName) {
        boolean isExist = false;
        Cursor cursor = mDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                isExist = true;
            }
            cursor.close();
        }
        return isExist;
    }

}
