package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ru.wtg.whereaminow.R;

/**
 * Created by tujger on 10/13/16.
 */

public class UserButtons {
    private Context context;
    private LinearLayout layout;
    private int myNumber;

    public UserButtons(Context context){
        this.context = context;
    }

    public void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    public void setLayout(LinearLayout layout) {
        this.layout = layout;
        layout.setVisibility(View.INVISIBLE);
    }

    public Button add(int number, MyUser marker, String text){
        Button user;

        View view = layout.findViewById(number);
        if(view != null){
            user = (Button) view;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            user = (Button) inflater.inflate(R.layout.view_user_button, null);

//            LayoutParams params = (LayoutParams) user.getLayoutParams();
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 1, 0);
            user.setLayoutParams(params);

            user.setId(number);
            user.setTag(marker.getId());

            user.setOnClickListener(onClickListener);
            user.setOnLongClickListener(onLongClickListener);

            user.setText(text);
            layout.addView(user);

            System.out.println("SIZE:"+context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size));
            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Drawable drawable = Utils.renderDrawable(context,R.drawable.semi_transparent_background,marker.getColor(),size,size);
//            Drawable drawable = Utils.renderDrawable(context,R.drawable.semi_transparent_background,marker.getColor(),me.getWidth(),me.getHeight());

            user.setBackgroundDrawable(drawable);

        }
        return user;
    }

    public void remove(int number) {
        View view = layout.findViewById(number);
        if(view != null){
            layout.removeView(view);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("onClickListener:"+view.getTag());

            State state = State.getInstance();
            MyUser user = findUserByButton(state.getUsers(), (Button) view);
            onClickCallback.call(user);

        }
    };

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            System.out.println("onLongClickListener:"+view.getTag());
            return false;
        }
    };

    public UserButtons synchronizeWith(MyUsers users) {
        MyUser user;
        String tag;
        ArrayList<Integer> exists = new ArrayList<>();

        int count = layout.getChildCount();
        for(int i = count-1;i>=0;i--){
            View button = layout.getChildAt(i);
            int id = layout.getChildAt(i).getId();
            tag = null;
            if(layout.getChildAt(i).getTag() != null)
                tag = layout.getChildAt(i).getTag().toString();

            if(!users.getUsers().containsKey(id)) {
                layout.removeViewAt(i);
            } else if(users.getUsers().get(id).getMarker() == null) {
                layout.removeViewAt(i);
            } else if(tag == null || !tag.equals(users.getUsers().get(id).getId())) {
                layout.removeViewAt(i);
            } else {
                exists.add(id);
            }
        }

        for(Map.Entry<Integer,MyUser> entry: users.getUsers().entrySet()){
            if(!exists.contains(entry.getKey())){
                user = entry.getValue();
                add(entry.getKey(),user,"Friend "+entry.getKey());

//                if(entry.getKey() == users.getMyNumber()){
//                    add(entry.getKey(),user,"Me");
//                } else if(entry.getKey() == 0){
//                    add(entry.getKey(),user,"Leader");
//                } else {
//                    add(entry.getKey(),user,"Friend "+entry.getKey());
//                }
            }
        }
        if(layout.getChildCount()>0) layout.setVisibility(View.VISIBLE);
        else layout.setVisibility(View.GONE);

        return this;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setOnClickCallback(Callback onClickCallback) {
        this.onClickCallback = onClickCallback;
    }

    public void setMyNumber(int myNumber) {
        this.myNumber = myNumber;
    }

    public interface Callback {
        void call(MyUser marker);
    }

    private Callback onClickCallback;

    private MyUser findUserByButton(MyUsers users,Button button){
        String buttonTag = button.getTag().toString();

        for(Map.Entry<Integer,MyUser> entry: users.getUsers().entrySet()){
            try {
                String markerTag = entry.getValue().getId();
                if (markerTag.equals(buttonTag)) {
                    return entry.getValue();
                }
            }catch(Exception e){
            }
        }
        return null;
    }

}
