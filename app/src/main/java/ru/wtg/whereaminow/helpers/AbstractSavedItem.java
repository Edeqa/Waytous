package ru.wtg.whereaminow.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

/**
 * Created 12/9/16.
 */

abstract public class AbstractSavedItem<T extends AbstractSavedItem> implements Serializable {

    static final long serialVersionUID = -6395904747332820032L;

    static transient private Map<String,Integer> count = new HashMap<String, Integer>();
    static transient private Map<String,Fields> dbOptions = new HashMap<String, Fields>();

    private transient static String LAST = "last";
    transient private String itemType;
    transient private Context context;

    transient private long number;

    protected AbstractSavedItem(Context context, String itemType){
        this.context = context;
//        sharedPreferences = context.getSharedPreferences(itemType, MODE_PRIVATE);
        this.itemType = itemType;

        number = 0;//sharedPreferences.getInt(LAST, 0) + 1;

//        SharedPreferences.Editor edit = sharedPreferences.edit();
//        edit.putLong(LAST, number).apply();

    }

    protected static void init(Context context, Class<?> item, String itemType) {
        Fields fields = new Fields(itemType, item);
        fields.db = new DBHelper(context, fields);
        System.out.println("========================================== "+fields.getCreateString());

        dbOptions.put(itemType,fields);
        fields.db.open();
    }

    public void save(SimpleCallback<T> onSaveCallback) {

        dbOptions.get(itemType).db.saveRec((T) this);

//        System.out.println("SAVED:"+number+":"+this);
        if(onSaveCallback != null) {
            onSaveCallback.call((T) this);
        }
    }

    public void delete(SimpleCallback<AbstractSavedItem> onDeleteCallback){


        if(onDeleteCallback != null) {
            onDeleteCallback.call(this);
        }
    }

    public static int getCount(Context context, String itemType) {

        return 1;

    }


    public static AbstractSavedItem getItemByPosition(Context context, String itemType, int position) {
        AbstractSavedItem item = null;
        return item;
    }

    public static AbstractSavedItem getItemByNumber(Context context, String itemType, long number) {
        AbstractSavedItem item = null;
        return item;
    }

    public static void clear(Context context, String itemType){
    }

    public long getNumber(){
        return number;
    }

    public void setNumber(long number){
        this.number = number;
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

    protected static class Fields {
        final String itemType;
        TreeMap<String,FieldOptions> fields = new TreeMap<>();
        DBHelper db;

        class FieldOptions {
            String name;
            String type;
            String sourceType;
            boolean serialize;
            public String toString(){
                return "{"+name+", "+type+", "+sourceType+", "+ serialize +"}";
            }
        }

        public Fields(String itemType, Class item) {
            this.itemType = itemType;
//            for (Field field : item.getSuperclass().getDeclaredFields()) {
//                processField(field);
//            }
            for (Field field : item.getDeclaredFields()) {
                processField(field);
            }
            System.out.println("FIELDS::::"+fields);
        }

        private void processField(Field field) {
            if(!Modifier.isStatic(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers()))
            {
                FieldOptions o = new FieldOptions();
                o.name = field.getName();

                if(field.getType().isAssignableFrom(String.class)){
                    o.type = "text";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(Boolean.TYPE)){
                    o.type = "integer";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(Integer.TYPE)){
                    o.type = "integer";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Long.class) || field.getType().isAssignableFrom(Long.TYPE)){
                    o.type = "integer";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(Float.TYPE)){
                    o.type = "real";
                    o.serialize = false;
                } else if(field.getType().isAssignableFrom(Double.class) || field.getType().isAssignableFrom(Double.TYPE)){
                    o.type = "real";
                    o.serialize = false;
                } else {
                    o.type = "blob";
                    o.serialize = Serializable.class.isAssignableFrom(field.getType());
                    if(!Serializable.class.isAssignableFrom(field.getType())){
                        try {
                            throw new Exception("Not serialize value: "+field.getName()+", type: "+field.getType().getCanonicalName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                o.sourceType = field.getType().getCanonicalName();
                fields.put(field.getName(),o);
            }

            /*System.out.println(":::"+field.getName()+":transient-"+ Modifier.isTransient(field.getModifiers())
                    +":static-"+ Modifier.isStatic(field.getModifiers())
                    +":final-"+ Modifier.isFinal(field.getModifiers())
                    +":serialize-"+ (Serializable.class.isAssignableFrom(field.getType()))
                    +":"+field.getType().getSimpleName());*/
        }
        public String getCreateString() {
            String res = "create table " + itemType + "(_id integer primary key autoincrement, ";
            for(Map.Entry<String,FieldOptions> x: fields.entrySet()){
                res += x.getValue().name + "_ " + x.getValue().type;
                if(!x.getKey().equals(fields.lastKey())) res += ", ";
            }
            res += ")";
            return res;
        }

    }

}