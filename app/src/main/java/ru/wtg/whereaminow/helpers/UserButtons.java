package ru.wtg.whereaminow.helpers;

import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import ru.wtg.whereaminow.R;

/**
 * Created by tujger on 10/13/16.
 */

public class UserButtons {
    private Button me;
    private Button user;
    private LinearLayout layout;
    private LinearLayout layoutOther;
    private ArrayList<Button> users;
    private View leftSibling;
    private View rightSibling;

    public UserButtons(){
        users = new ArrayList<>();
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
        user = (Button) layout.findViewById(R.id.button_user);

        layoutOther.removeAllViews();
        layoutOther.setVisibility(View.GONE);

        me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("me.setOnClickListener");
            }
        });
        me.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                System.out.println("me.setOnLongClickListener");
                return false;
            }
        });

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
}
