package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.CONNECTION_ERROR;
import static ru.wtg.whereaminow.State.SELECT_SINGLE_USER;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.TRACKING_ACCEPTED;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.State.TRACKING_STOPPED;
import static ru.wtg.whereaminow.State.UNSELECT_USER;
import static ru.wtg.whereaminow.holders.CameraViewHolder.ADJUST_ZOOM;

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
            case TRACKING_ACCEPTED:
                show();
                break;
            case TRACKING_STOP:
            case TRACKING_STOPPED:
            case CONNECTION_ERROR:
                hide();
                break;
        }
        return true;
    }

    private void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    class ButtonView extends AbstractView {
        private LinearLayout button;
        private TextView title;
        private MyUser myUser;
        private volatile boolean clicked;

        ButtonView(MyUser myUser){
            this.myUser = myUser;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int buttonView = myUser.getProperties().getImageResource();
            if(buttonView <1) {
                buttonView = R.layout.view_user_button;
            }

            button = (LinearLayout) inflater.inflate(buttonView, null);

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

            title = ((TextView) button.findViewById(R.id.tv_button_username));
            if(myUser.getProperties().isSelected()) title.setTypeface(Typeface.DEFAULT_BOLD);
            String titleText = (myUser.getProperties().getNumber()==0 ? "*" : "") + myUser.getProperties().getDisplayName();

            title.setText(titleText);

//            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Drawable drawable = Utils.renderDrawable(context, R.drawable.semi_transparent_background, myUser.getProperties().getColor(), size, size);

            button.setBackground(drawable);
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

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SELECT_USER:
                    title.setTypeface(Typeface.DEFAULT_BOLD);
                    break;
                case UNSELECT_USER:
                    title.setTypeface(Typeface.DEFAULT);
                    break;
                case CHANGE_NAME:
                    title.setText(myUser.getProperties().getDisplayName());
                    break;
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
                    myUser.fire(SELECT_SINGLE_USER);
/*
                    State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser user) {
                            if(user != myUser) {
                                user.fire(UNSELECT_USER, 0);
                            }
                        }
                    });
*/
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
