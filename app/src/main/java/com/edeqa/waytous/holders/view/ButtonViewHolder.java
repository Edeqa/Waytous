package com.edeqa.waytous.holders.view;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;
import com.edeqa.waytous.helpers.Utils;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import static com.edeqa.waytous.Constants.USER_DISMISSED;
import static com.edeqa.waytous.Constants.USER_JOINED;
import static com.edeqa.waytous.Constants.USER_NUMBER;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.DROPPED_TO_USER;
import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;
import static com.edeqa.waytous.helpers.Events.UNSELECT_USER;
import static com.edeqa.waytous.holders.view.CameraViewHolder.CAMERA_UPDATED;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.CREATE_SETTINGS;

/**
 * Created 11/18/16.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ButtonViewHolder extends AbstractViewHolder<ButtonViewHolder.ButtonView> {

    public static final String TYPE = "buttonView"; //NON-NLS

    private static final String PREFERENCE_SHOW_LABELS_IN_CONTEXT_MENU = "show_labels_ic_context_menu"; //NON-NLS

    private Handler handlerHideMenu;
    private Runnable runnableHideMenu;
    private LinearLayout layout;
    private HorizontalScrollView scrollLayout;
    private FlexboxLayout menuLayout;
    private boolean showLabelsInContextMenu = true;

    public ButtonViewHolder(MainActivity context) {
        super(context);

        setScrollLayout((HorizontalScrollView) context.findViewById(R.id.sv_users));
        setLayout((LinearLayout) context.findViewById(R.id.layout_users));
        setMenuLayout((FlexboxLayout) context.findViewById(R.id.layout_context_menu));

        showLabelsInContextMenu = State.getInstance().getBooleanPreference(PREFERENCE_SHOW_LABELS_IN_CONTEXT_MENU, true);

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
                updateBackgroundColors(null);
                hide();
                break;
            case USER_JOINED:
            case USER_DISMISSED:
                if(State.getInstance().getUsers().getCountActiveTotal() > 1 || !State.getInstance().tracking_disabled()) {
                    show();
                } else if(State.getInstance().tracking_disabled()) {
                    hide();
                }
                break;
            case CAMERA_UPDATED:
                if(State.getInstance().getUsers().getCountActiveTotal() > 1) {
                    show();
                }
                break;
            case CREATE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;
                item.add(new SettingItem.Group(SettingsViewHolder.PREFERENCES_GENERAL).setTitle(R.string.general).setPriority(100));
                item.add(new SettingItem.Checkbox(PREFERENCE_SHOW_LABELS_IN_CONTEXT_MENU).setValue(showLabelsInContextMenu).setTitle(R.string.show_labels_in_context_menu).setGroupId(SettingsViewHolder.PREFERENCES_GENERAL).setCallback(new Runnable1<Boolean>() {
                    @Override
                    public void call(Boolean arg) {
                        showLabelsInContextMenu = arg;
                    }
                }));
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
        //noinspection HardCodedStringLiteral
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

        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                context.openContextMenu(view);

                String show = myUser.getProperties().getDescription();
                if(show == null || show.length() == 0) {
                    show = myUser.getProperties().getDisplayName();
                }
                Toast.makeText(context, show, Toast.LENGTH_SHORT).show();

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
                    myUser.fire(CameraViewHolder.CAMERA_ZOOM);
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
            super(ButtonViewHolder.this.context, myUser);

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

            for(int i = 0; i < layout.getChildCount(); i++) {
                int number = Integer.valueOf(""+ layout.getChildAt(i).getTag());
                if(number != State.getInstance().getUsers().getMyNumber() && index < number) {
                    index = i;
                    break;
                }
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
        public List<String> events() {
            return new ArrayList<>();
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

                    updateBackgroundColors(myUser);

                    int left = scrollLayout.getScrollX();
                    int right = scrollLayout.getWidth() + left;
                    if(State.getInstance().getUsers().getCountSelectedTotal() == 1) {
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
                    title.setText(String.format("%s%s", myUser.getProperties().getNumber() == 0 ? "*" : "", myUser.getProperties().getDisplayName())); //NON-NLS
                    break;
                /*case MAKE_ACTIVE:
                case MAKE_INACTIVE:
                    if(layout.getChildCount()>1) {
                        show();
                    } else if(State.getInstance().tracking_disabled()) {
                        stop();
                    }
                    break;*/
            }
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            //noinspection EmptyCatchBlock
            try {
                title.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));
                title.setTypeface(null, (myUser.getProperties().isSelected()) ? Typeface.BOLD : Typeface.NORMAL);
            } catch(Exception e) {

            }
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

                        ((ImageView) button.findViewById(R.id.iv_button_image)).setImageDrawable(item.getIcon());

                        if(showLabelsInContextMenu) {
                            ((TextView) button.findViewById(R.id.tv_button_title)).setText(item.getTitle());// .setVisibility(View.GONE);
                            ((TextView) button.findViewById(R.id.tv_button_title)).setTextSize(8);
                        } else {
                            button.findViewById(R.id.tv_button_title).setVisibility(View.GONE);
                        }
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i(getType(),"Perform menu item { id=" + item.getItemId() +", title="+item.getTitle() + ", username=" + myUser.getProperties().getDisplayName() + "}"); //NON-NLS
                                popup.getMenu().performIdentifierAction(item.getItemId(),item.getGroupId());
                                handlerHideMenu.removeCallbacks(runnableHideMenu);
                                menuLayout.setVisibility(View.GONE);
                            }
                        });
                        button.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
//                                Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
                                menuLayout.setVisibility(View.GONE);

//                                PopupMenu popupMenu = new PopupMenu(context, null);
//                                popupMenu
                                popup.show();

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

    @SuppressWarnings("ConstantConditions")
    private void updateBackgroundColors(MyUser user) {
        if(State.getInstance().tracking_active() && user != null && State.getInstance().getUsers().getCountSelectedTotal() == 1) {
            if (Build.VERSION.SDK_INT >= 21) {
                Window window = context.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor((user.getProperties().getColor() & 0x00FFFFFF) | (0x20 << 24));
            }
            context.getSupportActionBar().setBackgroundDrawable(new ColorDrawable((user.getProperties().getColor() & 0x00FFFFFF) | (0x40 << 24)));
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                Window window = context.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            context.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
        }

    }
}
