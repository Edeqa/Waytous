package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminowserver.helpers.MyUser;

import static android.R.attr.x;

/**
 * Created by tujger on 10/13/16.
 */

public class UserButtons {
    private final Context context;
    public Button me;
    private LinearLayout layout;
    private LinearLayout layoutOther;
    private HashMap<Integer,Button> users;
    private View leftSibling;
    private View rightSibling;

    public UserButtons(Context context){
        this.context = context;
        users = new HashMap<>();
    }

    public void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    public void setLayout(LinearLayout layout) {
        this.layout = layout;

        layoutOther = (LinearLayout) layout.findViewById(R.id.layout_other_users);

        me = (Button) layout.findViewById(R.id.button_me);

        layoutOther.removeAllViews();
//        layoutOther.setVisibility(View.GONE);

        me.setOnClickListener(onClickListener);
        me.setOnLongClickListener(onLongClickListener);

    }

    public void setLeftSibling(View leftSibling) {
        this.leftSibling = leftSibling;
        if(leftSibling != null){

//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams();
//            int margin = positionWidth/5;
//            zoomParams.setMargins(margin, 0, 0, margin);
//            zoomParams.addRule(RelativeLayout.BELOW, myLocationButton.getId());
//            zoomParams.addRule(RelativeLayout.ALIGN_LEFT, myLocationButton.getId());
//            layout.setLayoutParams(zoomParams);

        }
    }

    public void setRightSibling(View rightSibling) {
        this.rightSibling = rightSibling;
    }

    public void setMyId(MyMarker marker){
        me.setTag(marker.getMarker().getId());
    }

    public void add(int number, MyMarker marker){
        Button user;
        if(users.containsKey(number)){
        System.out.println("BUTTONEXISTS");
            user = users.get(number);
        } else {
        System.out.println("BUTTONADD");
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            user = (Button) inflater.inflate(R.layout.view_user_button, null);


//            LayoutParams params = (LayoutParams) user.getLayoutParams();
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 1, 0);
            user.setLayoutParams(params);

            user.setTag(marker.getMarker().getId());

            user.setOnClickListener(onClickListener);
            user.setOnLongClickListener(onLongClickListener);

            if(number == 0) {
                user.setText("Leader");
                layoutOther.addView(user,0);
            } else {
                user.setText("Friend"+number);
                layoutOther.addView(user);
            }


            Drawable drawable;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                drawable = context.getResources().getDrawable(R.drawable.semi_transparent_background,context.getTheme());
            } else {
                drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(R.drawable.semi_transparent_background);

            }
            drawable.setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(marker.getColor())));
            Canvas canvas = new Canvas();

            Bitmap bitmap = Bitmap.createBitmap(me.getWidth(), me.getHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, me.getWidth(), me.getHeight());
            drawable.draw(canvas);

            user.setBackgroundDrawable(drawable);


            users.put(number,user);
        }
    }

    public void remove(int number) {
        if(users.containsKey(number)){
        System.out.println("BUTTONREMOVE");
            Button user = users.get(number);
            layoutOther.removeView(user);
            users.remove(number);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("onClickListener:"+view.getTag());
        }
    };

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            System.out.println("onLongClickListener:"+view.getTag());
            return false;
        }
    };


    public void removeUnused(MyUsers users) {
        System.out.println("SYNC");
        for(Map.Entry<Integer,Button> button: this.users.entrySet()){
            boolean has = false;
            String buttonTag = (String) button.getValue().getTag();
            for(Map.Entry<Integer,MyMarker> marker: users.getUsers().entrySet()){
                try {
                    String markerTag = marker.getValue().getMarker().getId();
                    if (markerTag.equals(buttonTag)) {
                        has = true;
                        break;
                    }
                }catch(Exception e){
                }
            }
            if(!has){
                remove(button.getKey());
            }
        }
    }
}
