package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.interfaces.SimpleCallback;
import ru.wtg.whereaminow.interfaces.TypedCallback;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created 12/9/16.
 */

abstract public class AbstractSavedItem implements Serializable {

    static final long serialVersionUID = -6395904747332820032L;

    private transient static String LAST = "last";
    transient private String itemType;
    transient private Context context;
    transient private SharedPreferences sharedPreferences;

    transient private int number;
    static transient private Map<String,Integer> count = new HashMap<String, Integer>();
    static transient private Map<String,TypedCallback<Boolean,Integer, AbstractSavedItem>> restrs = new HashMap<>();

    protected AbstractSavedItem(Context context, String itemType){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(itemType, MODE_PRIVATE);
        this.itemType = itemType;

        number = sharedPreferences.getInt(LAST, 0) + 1;

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(LAST, number).apply();

    }

    private void setProperties(Context context, SharedPreferences sharedPreferences, int number){
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.number = number;
    }

    /*public int getNumber(){
        return number;
    }*/

    public class Restrictions {

    }

    public void save(SimpleCallback<AbstractSavedItem> onSaveCallback) {
        sharedPreferences.edit().putString("item_" + number, Utils.serializeToString(this)).apply();

        reCount(context, itemType);

//        System.out.println("SAVED:"+number+":"+this);
        if(onSaveCallback != null) {
            onSaveCallback.call(this);
        }
    }

    public void delete(SimpleCallback<AbstractSavedItem> onDeleteCallback){
        sharedPreferences.edit().remove("item_" + number).apply();

        reCount(context, itemType);

        if(onDeleteCallback != null) {
            onDeleteCallback.call(this);
        }
    }

    public static int getCount(Context context, String itemType) {

        if(count.containsKey(itemType)) return count.get(itemType);
        return reCount(context, itemType);

    }

    public static void setRestrictions(Context context, String itemType, TypedCallback restrictions) {
        if(restrictions == null && restrs.containsKey(itemType)) {
            restrs.remove(itemType);
            reCount(context, itemType);
        } else {
            restrs.put(itemType, restrictions);
            reCount(context, itemType);
        }
    }

    private static int reCount(Context context, String itemType){
        final AtomicInteger counter = new AtomicInteger();

        filter(context, itemType, new TypedCallback<Boolean, Integer, AbstractSavedItem>() {
            @Override
            public Boolean call(Integer number, AbstractSavedItem arg) {
                counter.getAndIncrement();
                return true;
            }
        });
        int cnt = counter.get();

        count.put(itemType, counter.get());
        System.out.println("COUBNTFOR:"+itemType+":"+cnt);
        return cnt;
    }

    private static void filter(Context context, String itemType, TypedCallback<Boolean,Integer,AbstractSavedItem> callback) {
        int last = getSharedPreferences(context, itemType).getInt(LAST, 0);
        TypedCallback<Boolean, Integer, AbstractSavedItem> restr = null;
        if(restrs.containsKey(itemType)) {
            restr = restrs.get(itemType);
        }
        for(int i = 1; i<=last; i++){
            String saved = getSharedPreferences(context, itemType).getString("item_" + i, null);
            if(saved != null) {
                AbstractSavedItem item = (AbstractSavedItem) Utils.deserializeFromString(saved);
                if(restr != null) {
                    if(restr.call(i, item)) {
                        if(!callback.call(i, item)) break;
                    }
                } else {
                    if(!callback.call(i, item)) break;
                }
            }
        }
    }

    private static SharedPreferences getSharedPreferences(Context context, String itemType){
        return context.getSharedPreferences(itemType, MODE_PRIVATE);
    }

    public static AbstractSavedItem getItemByPosition(final Context context, final String itemType, final int position) {
        final AbstractSavedItem[] item = new AbstractSavedItem[1];
        /*int count = 0;
        int last = getSharedPreferences(context, itemType).getInt(LAST, 0);
        for(int i = 1; i<=last; i++){
            String saved = getSharedPreferences(context, itemType).getString("item_" + i, null);
            if(count == position && saved != null) {
                item = (AbstractSavedItem) Utils.deserializeFromString(saved);
                if(item != null) {
                    item.number = i;
                    item.itemType = itemType;
                    item.setProperties(context, getSharedPreferences(context, itemType), i);
                }
            }
            if(saved != null) count ++;
        }
        return item;*/





        final AtomicInteger counter = new AtomicInteger();
//        final AtomicReference<AbstractSavedItem> saved = new AtomicReference<>();

        filter(context, itemType, new TypedCallback<Boolean,Integer,AbstractSavedItem>() {
            @Override
            public Boolean call(Integer number, AbstractSavedItem arg) {
                if(counter.get() == position) {
                    item[0] = arg;
                    item[0].number = number;
                    item[0].itemType = itemType;
                    item[0].setProperties(context, getSharedPreferences(context, itemType), number);
                    return false;
                }
                counter.getAndIncrement();
                return true;
            }
        });

//        int cnt = counter.get();

//        count.put(itemType, counter.get());
//        System.out.println("COUBNTFOR:"+itemType+":"+cnt);
        return item[0];
    }

    public static AbstractSavedItem getItemByNumber(Context context, String itemType, int number) {
        AbstractSavedItem item = null;
        String saved = getSharedPreferences(context, itemType).getString("item_" + number, null);
        if(saved != null) {
            item = (AbstractSavedItem) Utils.deserializeFromString(saved);
            if(item != null) {
                item.number = number;
                item.itemType = itemType;
                item.setProperties(context, getSharedPreferences(context, itemType), number);
            }
        }
        return item;
    }

    public static void clear(Context context, String itemType){
        getSharedPreferences(context, itemType).edit().clear().apply();
    }

    public int getNumber(){
        return number;
    }



    abstract static public class AbstractSavedItemsAdapter<T extends AbstractSavedItem> extends RecyclerView.Adapter{

        protected final Context context;
        protected final String itemType;
        private final LinearLayoutManager layoutManager;
        private final RecyclerView list;
        protected SimpleCallback<T> onItemClickListener;
        protected SimpleCallback<T> onItemTouchListener;

        public AbstractSavedItemsAdapter(final Context context, final String itemType, final RecyclerView list){
            this.context = context;
            this.itemType = itemType;
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

        public void setOnRightSwipeListener(SimpleCallback<Integer> callback) {
            enableSwipe(ItemTouchHelper.RIGHT, callback);
        }

        public void setOnLeftSwipeListener(SimpleCallback<Integer> callback) {
            enableSwipe(ItemTouchHelper.LEFT, callback);
        }


        private void enableSwipe(final int direction, final SimpleCallback<Integer> callback){
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, direction) {
                Drawable background;
                Drawable xMark;
                boolean initiated;

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    int position = list.getChildLayoutPosition(viewHolder.itemView);
                    callback.call(position);
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    }, 500);*/
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    View itemView = viewHolder.itemView;

                    // not sure why, but this method get's called for viewholder that are already swiped away
                    if (viewHolder.getAdapterPosition() == -1) {
                        return;
                    }

                    if(direction == ItemTouchHelper.RIGHT && dX > 0) {
                        if (!initiated) {
                            background = new ColorDrawable(Color.TRANSPARENT);
                            xMark = ContextCompat.getDrawable(context, R.drawable.ic_clear_black);
                            initiated = true;
                        }
                        background.setBounds(itemView.getLeft() - (int) dX, itemView.getTop(), itemView.getLeft(), itemView.getBottom());
                        background.draw(c);

                        int itemHeight = itemView.getBottom() - itemView.getTop();
                        int intrinsicWidth = xMark.getIntrinsicWidth();
                        int intrinsicHeight = xMark.getIntrinsicWidth();
                        int leftMargin = context.getResources().getDimensionPixelOffset(R.dimen.material_button_width_minimum) / 2;

                        if (dX < itemView.getWidth() / 2) {
                            xMark.setColorFilter(Color.argb(Math.round(255 - 255 * (dX / (itemView.getWidth() / 2))), 255, 255, 255),
                                    PorterDuff.Mode.SRC_ATOP);
                        }

                        int right = itemView.getLeft() + intrinsicWidth + leftMargin - intrinsicWidth/2;
                        int left = itemView.getLeft() + leftMargin - intrinsicWidth/2;
                        int top = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                        int bottom = top + intrinsicHeight;
                        xMark.setBounds(left, top, right, bottom);

                        xMark.draw(c);
                    } else if(direction == ItemTouchHelper.LEFT && dX < 0) {
//                        dX = Math.abs(dX);
                        if (!initiated) {
                            background = new ColorDrawable(Color.TRANSPARENT);
                            xMark = ContextCompat.getDrawable(context, R.drawable.ic_clear_black);
                            initiated = true;
                        }
                        background.setBounds(itemView.getRight() - (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        background.draw(c);

/*
                        int itemHeight = itemView.getBottom() - itemView.getTop();
                        int intrinsicWidth = xMark.getIntrinsicWidth();
                        int intrinsicHeight = xMark.getIntrinsicWidth();
                        int rightMargin = context.getResources().getDimensionPixelOffset(R.dimen.material_button_width_minimum) / 2;
*/

                        /*if (dX < itemView.getWidth() / 2) {
                            xMark.setColorFilter(Color.argb(Math.round(255 - 255 * (dX / (itemView.getWidth() / 2))), 255, 255, 255),
                                    PorterDuff.Mode.SRC_ATOP);
                        }

                        int right = itemView.getRight() + intrinsicWidth - rightMargin - intrinsicWidth/2;
                        int left = itemView.getRight() - rightMargin - intrinsicWidth/2;
                        int top = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                        int bottom = top + intrinsicHeight;
                        xMark.setBounds(left, top, right, bottom);

                        xMark.draw(c);*/
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(list);
        }

        abstract public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

        abstract public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position);

        @Override
        public int getItemCount() {
            return getCount(context, itemType);
        }

        public void setOnItemClickListener(SimpleCallback<T> onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }
        public void setOnItemTouchListener(SimpleCallback<T> onItemTouchListener) {
            this.onItemTouchListener = onItemTouchListener;
        }
    }

}