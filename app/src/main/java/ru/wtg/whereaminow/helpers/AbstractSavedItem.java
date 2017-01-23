package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ru.wtg.whereaminow.interfaces.SimpleCallback;

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
        return getSingleItemByCursor(itemType, cursor);
    }

    public static AbstractSavedItem getItemByNumber(String itemType, long number) {
        Cursor cursor = dbHelpers.get(itemType).getById(number);
        return getSingleItemByCursor(itemType, cursor);
    }

    public static AbstractSavedItem getItemByCursor(String itemType, Cursor cursor) {
        return dbHelpers.get(itemType).load(cursor);
    }

    public static AbstractSavedItem getItemByFieldValue(String itemType, String field, String value) {
        Cursor cursor = dbHelpers.get(itemType).getByFieldValue(field, value);
        return getSingleItemByCursor(itemType, cursor);
    }

    public static AbstractSavedItem getItemByFieldValue(String itemType, String field, Number value) {
        Cursor cursor = dbHelpers.get(itemType).getByFieldValue(field, value);
        return getSingleItemByCursor(itemType, cursor);
    }

    private static AbstractSavedItem getSingleItemByCursor(String itemType, Cursor cursor) {
        cursor.moveToFirst();
        AbstractSavedItem item = dbHelpers.get(itemType).load(cursor);
        cursor.close();
        return item;
    }

    public static void clear(String itemType){
        dbHelpers.get(itemType).clear();
    }

    public void save(SimpleCallback<T> onSaveCallback) {
        dbHelpers.get(itemType).save(this);
        if(onSaveCallback != null) {
            onSaveCallback.call((T) this);
        }
    }

    public void delete(SimpleCallback<AbstractSavedItem<? extends AbstractSavedItem>> onDeleteCallback){
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

    abstract static public class AbstractSavedItemsAdapter<T extends AbstractSavedItem> extends RecyclerView.Adapter implements LoaderManager.LoaderCallbacks<Cursor> {

        protected final Context context;
        private final LinearLayoutManager layoutManager;
        private final RecyclerView list;
        protected Cursor cursor;
        protected SimpleCallback<T> onItemClickListener;
        protected SimpleCallback<T> onItemTouchListener;
        protected SimpleCallback<Cursor> onCursorReloadListener;

        public AbstractSavedItemsAdapter(final Context context, final RecyclerView list){
            super();
            this.context = context;
            this.list = list;

            list.setAdapter(this);

            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
            list.setItemAnimator(itemAnimator);

            layoutManager = new LinearLayoutManager(context);

            list.setLayoutManager(layoutManager);

            DividerItemDecoration divider = new DividerItemDecoration(list.getContext(), ((LinearLayoutManager) list.getLayoutManager()).getOrientation());
            list.addItemDecoration(divider);

            list.setItemAnimator(new DefaultItemAnimator());
        }

        abstract public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

        @Override
        public int getItemCount() {
            return cursor != null ? cursor.getCount() : 0;
        }

        public Cursor getItem(final int position) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.moveToPosition(position);
            }
            return cursor;
        }

        public void swapCursor(final Cursor cursor) {
            this.cursor = cursor;
            this.notifyDataSetChanged();
        }

        @Override
        public final void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final Cursor cursor = this.getItem(position);
            this.onBindViewHolder(holder, cursor);
        }

        public abstract void onBindViewHolder(final RecyclerView.ViewHolder holder, final Cursor cursor);

        public void setOnRightSwipeListener(SimpleCallback<Integer> callback) {
            enableSwipe(ItemTouchHelper.RIGHT, callback);
        }

        public void setOnLeftSwipeListener(SimpleCallback<Integer> callback) {
            enableSwipe(ItemTouchHelper.LEFT, callback);
        }

        public void setOnCursorReloadListener(SimpleCallback<Cursor> callback) {
            this.onCursorReloadListener = callback;
        }

        public String getString(String fieldName){
            return cursor.getString(cursor.getColumnIndex(fieldName + "_"));
        }

        public Integer getInt(String fieldName){
            return cursor.getInt(cursor.getColumnIndex(fieldName + "_"));
        }

        public Long getLong(String fieldName){
            return cursor.getLong(cursor.getColumnIndex(fieldName + "_"));
        }

        private void enableSwipe(final int direction, final SimpleCallback<Integer> callback){
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, direction) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    int position = list.getChildLayoutPosition(viewHolder.itemView);
                    callback.call(position);
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    View itemView = viewHolder.itemView;

                    // not sure why, but this method get's called for viewholder that are already swiped away
                    if (viewHolder.getAdapterPosition() == -1) {
                        return;
                    }
                    if(direction == ItemTouchHelper.RIGHT && dX > 0) {
                        itemView.setAlpha(1-dX / (itemView.getWidth()/2));
                    } else if(direction == ItemTouchHelper.LEFT && dX < 0) {
//                        itemView.setAlpha(1+dX / (itemView.getWidth()));
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(list);
        }

        public void setOnItemClickListener(SimpleCallback<T> onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void setOnItemTouchListener(SimpleCallback<T> onItemTouchListener) {
            this.onItemTouchListener = onItemTouchListener;
        }

        abstract public Loader<Cursor> onCreateLoader(int id, Bundle args);

        @Override
        public void onLoadFinished(Loader loader, Cursor cursor) {
            swapCursor(cursor);
            if(onCursorReloadListener != null) {
                onCursorReloadListener.call(cursor);
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {
            swapCursor(null);
        }

    }

    static class SavedItemCursorLoader extends CursorLoader {

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