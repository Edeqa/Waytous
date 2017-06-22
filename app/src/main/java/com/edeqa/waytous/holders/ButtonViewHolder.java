package com.edeqa.waytous.holders;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Runnable2;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;


import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.DROPPED_TO_USER;
import static com.edeqa.waytous.helpers.Events.MAKE_ACTIVE;
import static com.edeqa.waytous.helpers.Events.MAKE_INACTIVE;
import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;
import static com.edeqa.waytous.helpers.Events.UNSELECT_USER;
import static com.edeqa.waytous.holders.CameraViewHolder.CAMERA_ZOOM;
import static com.edeqa.waytousserver.helpers.Constants.USER_NUMBER;

/**
 * Created 11/18/16.
 */
public class ButtonViewHolder extends AbstractViewHolder<ButtonViewHolder.ButtonView> {
    private static final String TYPE = "Button";
    private Handler handlerHideMenu;
    private Runnable runnableHideMenu;
    private LinearLayout layout;
    private HorizontalScrollView scrollLayout;
    private FlexboxLayout menuLayout;

    public ButtonViewHolder(MainActivity context) {
        super(context);

        setScrollLayout((HorizontalScrollView) context.findViewById(R.id.sv_users));
        setLayout((LinearLayout) context.findViewById(R.id.layout_users));
        setMenuLayout((FlexboxLayout) context.findViewById(R.id.layout_context_menu));
    }

    @Override
    public String getType(){
        return TYPE;
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

    @Override
    public ButtonView create(MyUser myUser) {
        if (myUser == null) return null;
        return new ButtonView(myUser);
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

    private ButtonViewHolder setMenuLayout(final FlexboxLayout menuLayout) {
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

    private void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    @Override
    public ArrayList<IntroRule> getIntro() {
        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(TRACKING_ACTIVE).setId("button_intro").setView(layout).setTitle("Top buttons").setDescription("Here are the buttons of group members. Touch any button to switch to this member or long touch for context menu."));

        return rules;
    }

    public void setScrollLayout(HorizontalScrollView scrollLayout) {
        this.scrollLayout = scrollLayout;
    }

    public HorizontalScrollView getScrollLayout() {
        return scrollLayout;
    }

    class ButtonView extends AbstractView {
        private LinearLayout button;
        private TextView title;
        private MyUser myUser;

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                context.openContextMenu(view);

                openContextMenu(view);

                ClipData data = ClipData.newPlainText(USER_NUMBER, ""+myUser.getProperties().getNumber());
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
                view.startDrag(data, shadow, null, 0);
//                PopupMenu popup = new PopupMenu(context, view);
//                context.getMenuInflater().inflate(R.menu.user_menu, popup.getMenu());
//
//                myUser.fire(CREATE_CONTEXT_MENU, popup.getMenu());
//                popup.showSnack();
                return true;
            }
        };
        View.OnDragListener onDropListener = new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                final int action = dragEvent.getAction();
                switch(action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        break;
                    case DragEvent.ACTION_DROP:
                        ClipData data = dragEvent.getClipData();
                        int number = Integer.valueOf(String.valueOf(data.getItemAt(0).getText()));
                        State.getInstance().getUsers().forUser(number, new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser user) {
                                    user.fire(DROPPED_TO_USER, myUser);
                            }
                        });
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                    default:
                        break;
                }
                return true;
            }
        };
        private volatile boolean clicked;
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

        public ButtonView(MyUser myUser){

            this.myUser = myUser;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int buttonView = myUser.getProperties().getImageResource();
            if(buttonView < 1) {
                buttonView = R.layout.view_user_button;
            }

            button = (LinearLayout) inflater.inflate(buttonView, null);

            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 1, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(onClickListener);
            button.setOnLongClickListener(onLongClickListener);
            button.setOnDragListener(onDropListener);
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

            title = ((TextView) button.findViewById(R.id.tv_button_title));
            String titleText = (myUser.getProperties().getNumber()==0 ? "*" : "") + myUser.getProperties().getDisplayName();

            title.setText(titleText);
            if(myUser.getLocation() == null) {
                title.setTextColor(Color.DKGRAY);
                title.setTypeface(null, Typeface.ITALIC);
            }
            if(myUser.getProperties().isSelected()) title.setTypeface(null, Typeface.BOLD);

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
            return true;
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SELECT_USER:
                    title.setTypeface(null, (myUser.getLocation() == null) ? Typeface.BOLD_ITALIC : Typeface.BOLD);
                    if(layout.getChildCount()>1) {
                        show();
                    } else if(State.getInstance().tracking_disabled()) {
                        hide();
                    }

                    int left = scrollLayout.getScrollX();
                    int right = scrollLayout.getWidth() + left;
                    if(State.getInstance().getUsers().getCountAllSelected() == 1) {
                        if(button.getRight() < left) {
                            scrollLayout.smoothScrollTo(button.getLeft() - scrollLayout.getWidth()/2, 0);
                        } else if(button.getLeft() > right) {
                            scrollLayout.smoothScrollTo(button.getRight() - scrollLayout.getWidth()/2, 0);
                        }
                    }
                    break;
                case UNSELECT_USER:
                    title.setTypeface(null, (myUser.getLocation() == null) ? Typeface.ITALIC : Typeface.NORMAL);
                    break;
                case CHANGE_NAME:
                    title.setText((myUser.getProperties().getNumber()==0 ? "*" : "") + myUser.getProperties().getDisplayName());
                    break;
                case MAKE_ACTIVE:
                case MAKE_INACTIVE:
                    if(layout.getChildCount()>1) {
                        show();
                    } else if(State.getInstance().tracking_disabled()) {
                        hide();
                    }
                    break;
            }
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            title.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));
            title.setTypeface(null, (myUser.getProperties().isSelected()) ? Typeface.BOLD: Typeface.NORMAL);
        }

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

                        ((ImageView)button.findViewById(R.id.iv_button_image)).setImageDrawable(item.getIcon());
                        button.findViewById(R.id.tv_button_title).setVisibility(View.GONE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i(TYPE,"Perform menu item { id=" + item.getItemId() +", title="+item.getTitle() + ", username=" + myUser.getProperties().getDisplayName() + "}");
                                popup.getMenu().performIdentifierAction(item.getItemId(),item.getGroupId());
                                handlerHideMenu.removeCallbacks(runnableHideMenu);
                                menuLayout.setVisibility(View.GONE);
                            }
                        });
                        button.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
                                return true;
                            }
                        });
                        menuLayout.addView(button);
                    }
                    menuLayout.setVisibility(View.VISIBLE);

                    handlerHideMenu.postDelayed(runnableHideMenu, 3000);
                }
            }, 0);
        }

    }
}
