package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.SELECT_SINGLE_USER;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.State.UNSELECT_USER;
import static ru.wtg.whereaminow.holders.CameraViewHolder.CAMERA_ZOOM;

/**
 * Created 11/18/16.
 */
public class ButtonViewHolder extends AbstractViewHolder<ButtonViewHolder.ButtonView> {
    private static final String TYPE = "button";
    private final Activity context;
    private Handler handlerHideMenu;
    private Runnable runnableHideMenu;

    private LinearLayout layout;
    private FlexboxLayout menuLayout;

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
        if(State.getInstance().tracking_active()){
            show();
        } else {
            hide();
        }
        return this;
    }

    public ButtonViewHolder setMenuLayout(final FlexboxLayout menuLayout) {
        this.menuLayout = menuLayout;
        handlerHideMenu = new Handler();
        runnableHideMenu = new Runnable() {
            @Override
            public void run() {
                menuLayout.setVisibility(View.GONE);
            }
        };

        return this;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case TRACKING_ACTIVE:
                show();
                break;
            case TRACKING_STOP:
            case TRACKING_DISABLED:
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
                    myUser.fire(CAMERA_ZOOM);
                    clicked = false;
                } else {
                    myUser.fire(SELECT_SINGLE_USER);
                    clicked = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clicked = false;
                        }
                    }, 500);

                    openContextMenu(view);
                }
            }
        };

        private void openContextMenu(View view) {

            final PopupMenu popup = new PopupMenu(context, view);
            context.getMenuInflater().inflate(R.menu.user_menu, popup.getMenu());
            myUser.fire(CREATE_CONTEXT_MENU, popup.getMenu());
            handlerHideMenu.removeCallbacks(runnableHideMenu);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    menuLayout.removeAllViews();

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    for(int i=0; i<popup.getMenu().size(); i++) {
                        final MenuItem item = popup.getMenu().getItem(i);
                        if(!item.isVisible()) continue;

                        LinearLayout button = (LinearLayout) inflater.inflate(R.layout.view_user_button, null);

//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(1, 1, 1, 1);
                        button.setLayoutParams(params);

//                                button.setOnClickListener(onClickListener);
//                                button.setOnLongClickListener(onLongClickListener);
//                                context.registerForContextMenu(button);
                        ((ImageView)button.findViewById(R.id.iv_button_person)).setImageDrawable(item.getIcon());
                        button.findViewById(R.id.tv_button_username).setVisibility(View.GONE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("PERFORM:"+item.getItemId()+":"+item.getGroupId()+":"+item.getTitle()+":"+myUser.getProperties().getDisplayName());
                                popup.getMenu().performIdentifierAction(item.getItemId(),item.getGroupId());
                                handlerHideMenu.removeCallbacks(runnableHideMenu);
                                menuLayout.setVisibility(View.GONE);
                            }
                        });
                        menuLayout.addView(button);
                    }
                    menuLayout.setVisibility(View.VISIBLE);

                    handlerHideMenu.postDelayed(runnableHideMenu, 3000);
                }
            }, 0);

        }

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                context.openContextMenu(view);

                openContextMenu(view);

//                PopupMenu popup = new PopupMenu(context, view);
//                context.getMenuInflater().inflate(R.menu.user_menu, popup.getMenu());
//
//                myUser.fire(CREATE_CONTEXT_MENU, popup.getMenu());
//                popup.show();


                return true;
            }
        };

     }

    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(TRACKING_ACTIVE).setId("button_intro").setView(layout).setTitle("Top buttons").setDescription("Here are the buttons of group members. Touch any button to switch to this member or long touch for context menu."));

        return rules;
    }


}
