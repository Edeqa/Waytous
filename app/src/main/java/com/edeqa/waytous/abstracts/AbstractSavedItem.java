package com.edeqa.waytous.abstracts;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.helpers.DBHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Created 12/9/16.
 */

@SuppressWarnings("ALL")
abstract public class AbstractSavedItem<T extends AbstractSavedItem> implements Serializable {

    static final long serialVersionUID = -6395904747332820032L;

    static transient private Map<String,Integer> count = new HashMap<>();
    static transient private Map<String,DBHelper> dbHelpers = new HashMap<>();

    private transient static String LAST = "last";
    protected transient Context context;
    transient private String itemType;
    transient private long number;

    protected AbstractSavedItem() {
    }

    @SuppressWarnings("WeakerAccess")
    protected AbstractSavedItem(Context context, String itemType){
        this.context = context;
        this.itemType = itemType;

        number = 0;//sharedPreferences.getInt(LAST, 0) + 1;
    }

    @SuppressWarnings("WeakerAccess")
    protected static void init(Context context, Class<?> item, String itemType) {
//        Fields fields = new Fields(itemType, item);
        DBHelper<? extends AbstractSavedItem> dbHelper = new DBHelper<>(context, itemType, item);
//        System.out.println("========================================== "+dbHelper.fields.getCreateString());
        dbHelpers.put(itemType,new DBHelper(context, itemType, item));
    }

    @SuppressWarnings("WeakerAccess")
    protected static DBHelper getDb(String itemType) {
        return dbHelpers.get(itemType);
    }

    public static int getCount(String itemType) {
        return dbHelpers.get(itemType).getCount();
    }

    public static AbstractSavedItem getItemByPosition(String itemType, int position) {
        Cursor cursor = dbHelpers.get(itemType).getByPosition(position);
//        cursor.moveToFirst();
        return getSingleItemByCursor(itemType, cursor);
    }

    public static AbstractSavedItem getItemByNumber(String itemType, long number) {
        Cursor cursor = dbHelpers.get(itemType).getById(number);
        cursor.moveToFirst();
        return getSingleItemByCursor(itemType, cursor);
    }

    public static AbstractSavedItem getItemByCursor(String itemType, Cursor cursor) {
        return dbHelpers.get(itemType).load(cursor);
    }

    public static AbstractSavedItem getItemByFieldValue(String itemType, String field, String value) {
        Cursor cursor = dbHelpers.get(itemType).getByFieldValue(field, value);
        cursor.moveToFirst();
        return getSingleItemByCursor(itemType, cursor);
    }

    public static AbstractSavedItem getItemByFieldValue(String itemType, String field, Number value) {
        Cursor cursor = dbHelpers.get(itemType).getByFieldValue(field, value);
        cursor.moveToFirst();
        return getSingleItemByCursor(itemType, cursor);
    }

    private static AbstractSavedItem getSingleItemByCursor(String itemType, Cursor cursor) {
        AbstractSavedItem item = dbHelpers.get(itemType).load(cursor);
        cursor.close();
        return item;
    }

    public static void clear(String itemType){
        dbHelpers.get(itemType).clear();
    }

    public void save(Runnable1<T> onSaveCallback) {
        dbHelpers.get(itemType).save(this);
        if(onSaveCallback != null) {
            onSaveCallback.call((T) this);
        }
    }

    public void delete(Runnable1<AbstractSavedItem<? extends AbstractSavedItem>> onDeleteCallback){
        dbHelpers.get(itemType).deleteByItem(this);
        if(onDeleteCallback != null) {
            onDeleteCallback.call(this);
        }
    }

    public long getNumber(){
        return number;
    }

    public void setNumber(long number){
        this.number = number;
    }

    public static class SavedItemCursorLoader extends CursorLoader {

        private final String itemType;

        public SavedItemCursorLoader(Context context, String itemType) {
            super(context);
            this.itemType = itemType;
        }

        @Override
        public Cursor loadInBackground() {
            return dbHelpers.get(itemType).getAll();
        }
    }
}