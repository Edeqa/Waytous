package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.ACCEPTED;
import static ru.wtg.whereaminow.State.ADJUST_ZOOM;
import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.ERROR;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.STOPPED;
import static ru.wtg.whereaminow.State.STOP_TRACKING;
import static ru.wtg.whereaminow.State.UNSELECT_USER;

/**
 * Created 11/18/16.
 */
public class ButtonViewHolder extends AbstractViewHolder<ButtonViewHolder.ButtonView> {
    private static final String TYPE = "button";
    private final Activity context;

    private LinearLayout layout;

    public ButtonViewHolder(Activity context) {
        this.context = context;
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public String[] getOwnEvents() {
        return new String[0];
    }

    @Override
    public ButtonView create(MyUser myUser) {
        if (myUser == null) return null;
        return this.new ButtonView(myUser);
    }

    public ButtonViewHolder setLayout(LinearLayout layout) {
        this.layout = layout;
        if(State.getInstance().tracking()){
            show();
        } else {
            hide();
        }
        return this;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case ACCEPTED:
                show();
                break;
            case STOP_TRACKING:
            case STOPPED:
            case ERROR:
                hide();
                break;
        }
        return true;
    }

    public void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    class ButtonView extends AbstractView {
        private Button button;
        private MyUser myUser;
        private volatile boolean clicked;

        ButtonView(MyUser myUser){
            this.myUser = myUser;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            button = (Button) inflater.inflate(R.layout.view_user_button, null);

            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 1, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(onClickListener);
            button.setOnLongClickListener(onLongClickListener);
            context.registerForContextMenu(button);

            int index = myUser.getProperties().getNumber();
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

//            button.setTextColor(Color.LTGRAY);
//            button.getBackground().setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(Color.CYAN)));

            if(myUser.getProperties().isSelected()) button.setTypeface(Typeface.DEFAULT_BOLD);
            button.setText((myUser.getProperties().getNumber()==0 ? "*" : "")+myUser.getProperties().getName());

//            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Drawable drawable = Utils.renderDrawable(context, R.drawable.semi_transparent_background, myUser.getProperties().getColor(), size, size);

            button.setBackgroundDrawable(drawable);
            button.setTag(myUser.getProperties().getNumber());

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

//        public void setNumber(int number) {
//            button.setTag(number);
//        }
//
//        public int getNumber() {
//            return myUser.getProperties().getNumber();
//        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SELECT_USER:
                    button.setTypeface(Typeface.DEFAULT_BOLD);
                    break;
                case UNSELECT_USER:
                    button.setTypeface(Typeface.DEFAULT);
                    break;
                case CHANGE_NAME:
                    String name = myUser.getProperties().getName();
                    if(object == null){
                        if(myUser == State.getInstance().getMe()){
                            name = "Me";
                        } else if (myUser.getProperties().getNumber() == 0) {
                            name = "Leader";
                        } else {
                            name = "Friend "+myUser.getProperties().getNumber();
                        }
                    }
                    button.setText((myUser.getProperties().getNumber()==0 ? "*" : "")+name);

            }
            return true;
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clicked) {
                    myUser.fire(ADJUST_ZOOM);
                    clicked = false;
                } else {
                    State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser user) {
                            if(user != myUser) {
                                user.fire(UNSELECT_USER);
                            }
                        }
                    });
                    myUser.fire(SELECT_USER, 0);
                    clicked = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clicked = false;
                        }
                    }, 500);
                }
            }
        };

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                context.openContextMenu(view);
                return true;
            }
        };
    }

}
