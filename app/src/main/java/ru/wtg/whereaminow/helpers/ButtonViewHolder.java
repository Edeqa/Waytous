package ru.wtg.whereaminow.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import ru.wtg.whereaminow.R;

import static ru.wtg.whereaminow.helpers.MyUser.ASSIGN_TO_CAMERA;
import static ru.wtg.whereaminow.helpers.MyUser.CHANGE_NAME;
import static ru.wtg.whereaminow.helpers.MyUser.REFUSE_FROM_CAMERA;

/**
 * Created by tujger on 11/18/16.
 */
public class ButtonViewHolder implements ViewHolder<ButtonViewHolder.ButtonView> {
    public static final String TYPE = "button";
    private final Activity context;

    private ButtonView buttonView;
    private LinearLayout layout;

    public ButtonViewHolder(Activity context) {
        this.context = context;
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public ButtonView createView(MyUser myUser) {
        if (myUser == null) return null;

        buttonView = this.new ButtonView(myUser);

        return buttonView;
    }

    public ButtonViewHolder setLayout(LinearLayout layout) {
        this.layout = layout;
        layout.setVisibility(View.INVISIBLE);
        return this;
    }

    public void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    public class ButtonView implements AbstractView {
        private Button button;
        private MyUser myUser;

        public ButtonView(MyUser myUser){
            this.myUser = myUser;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            button = (Button) inflater.inflate(R.layout.view_user_button, null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 1, 0);
            button.setLayoutParams(params);


            button.setOnClickListener(onClickListener);
            button.setOnLongClickListener(onLongClickListener);
            context.registerForContextMenu(button);

            int index = myUser.getNumber();
            if(index == State.getInstance().getUsers().getMyNumber()){
                index = 0;
            } else if(index == 0 && index != State.getInstance().getUsers().getMyNumber()){
                index = 1;
            }
            if(index >= layout.getChildCount()) {
                layout.addView(button);
            } else {
                layout.addView(button, index);
            }

            if(myUser.isSelected()) button.setTypeface(Typeface.DEFAULT_BOLD);
            button.setText((myUser.getNumber()==0 ? "*" : "")+myUser.getName());

            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Drawable drawable = Utils.renderDrawable(context, R.drawable.semi_transparent_background, myUser.getColor(), size, size);

            button.setBackgroundDrawable(drawable);
            button.setTag(myUser.getNumber());

        }

        @Override
        public void remove() {
            layout.removeView(button);
            button = null;
        }

        @Override
        public boolean dependsOnLocation(){
            return false;
        }

        @Override
        public void onChangeLocation(Location location) {
        }

        @Override
        public void setNumber(int number) {
            button.setTag(number);
        }

        @Override
        public int getNumber() {
            return myUser.getNumber();
        }

        @Override
        public void onEvent(int event, Object object) {
            switch(event){
                case CHANGE_NAME:
                    button.setText((myUser.getNumber()==0 ? "*" : "")+myUser.getName());
                    break;
                case ASSIGN_TO_CAMERA:
                    button.setTypeface(Typeface.DEFAULT_BOLD);
                    break;
                case REFUSE_FROM_CAMERA:
                    button.setTypeface(Typeface.DEFAULT);
                    break;
            }
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.setSelected(false);
                        myUser.fire(REFUSE_FROM_CAMERA);
                    }
                });
                myUser.setSelected(true);
                myUser.fire(ASSIGN_TO_CAMERA, 0);
            }
        };

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                System.out.println("onLongClickListener:"+view.getTag());
                context.openContextMenu(view);
                return true;
            }
        };
    }

}
