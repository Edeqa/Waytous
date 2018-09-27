package com.edeqa.waytous.abstracts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;

import com.edeqa.helpers.interfaces.Consumer;

/**
 * Created 8/10/2017.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
abstract public class AbstractSavedItemsAdapter<T extends AbstractSavedItem> extends RecyclerView.Adapter implements LoaderManager.LoaderCallbacks<Cursor> {

    protected final Context context;
    protected Consumer<T> onItemClickListener;
    protected Consumer<T> onItemTouchListener;

    private final RecyclerView list;
    private Cursor cursor;
    private Consumer<Cursor> onCursorReloadListener;
    private View emptyView;



    public AbstractSavedItemsAdapter(final Context context, final RecyclerView list) {
        super();
        this.context = context;
        this.list = list;

        list.setAdapter(this);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        list.setItemAnimator(itemAnimator);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);

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
        if (getItemCount() > 0) {
            if(emptyView != null) emptyView.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        } else {
            if(emptyView != null) emptyView.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Cursor cursor = this.getItem(position);
        this.onBindViewHolder(holder, cursor);
    }

    public abstract void onBindViewHolder(final RecyclerView.ViewHolder holder, final Cursor cursor);

    public void setOnRightSwipeListener(Consumer<Integer> callback) {
        enableSwipe(ItemTouchHelper.RIGHT, callback);
    }

    public void setOnLeftSwipeListener(Consumer<Integer> callback) {
        enableSwipe(ItemTouchHelper.LEFT, callback);
    }

    public void setOnCursorReloadListener(Consumer<Cursor> callback) {
        this.onCursorReloadListener = callback;
    }

    public String getString(String fieldName) {
        return cursor.getString(cursor.getColumnIndex(fieldName + "_"));
    }

    public Integer getInt(String fieldName) {
        return cursor.getInt(cursor.getColumnIndex(fieldName + "_"));
    }

    public Long getLong(String fieldName) {
        return cursor.getLong(cursor.getColumnIndex(fieldName + "_"));
    }

    private void enableSwipe(final int direction, final Consumer<Integer> callback) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, direction) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = list.getChildLayoutPosition(viewHolder.itemView);
                callback.accept(position);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    return;
                }
                if (direction == ItemTouchHelper.RIGHT && dX > 0) {
                    itemView.setAlpha(1 - dX / (itemView.getWidth() / 2));
                } else //noinspection StatementWithEmptyBody
                    if (direction == ItemTouchHelper.LEFT && dX < 0) {
//                        itemView.setAlpha(1+dX / (itemView.getWidth()));
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(list);
    }

    public void setOnItemClickListener(Consumer<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemTouchListener(Consumer<T> onItemTouchListener) {
        this.onItemTouchListener = onItemTouchListener;
    }

    abstract public Loader<Cursor> onCreateLoader(int id, Bundle args);

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        swapCursor(cursor);
        if (onCursorReloadListener != null) {
            onCursorReloadListener.accept(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        swapCursor(null);
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }
}
