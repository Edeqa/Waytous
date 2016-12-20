package ru.wtg.whereaminow.holders;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.OvershootInterpolator;

import java.util.HashMap;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;

import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.PREPARE_FAB;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.TRACKING_NEW;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.holders.MessagesHolder.NEW_MESSAGE;

/**
 * Created 11/27/16.
 */
public class FabViewHolder extends AbstractViewHolder {

    public static final String TYPE = "fab_layout";

    private final MainActivity context;
    private LinearLayoutCompat fab_layout;
    private LinearLayoutCompat fab_buttons;
    private FloatingActionButton fab;

    private HashMap<Integer,View> buttons;

    private boolean isFabMenuOpen = false;

    public FabViewHolder(MainActivity context){
        this.context = context;
    }

    public FabViewHolder setView(View view) {
        fab_layout = (LinearLayoutCompat) view;
        fab_buttons = (LinearLayoutCompat) view.findViewById(R.id.fab_buttons);

        hide(false);
        close(false);

//        fab_layout.findViewById(R.id.fab_main_button).getMenuIconView().setButtonView(R.drawable.ic_gps_off_white_24dp);
        fab = (FloatingActionButton) fab_layout.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_gps_off_white_24dp);
        fab.setOnClickListener(onInitialClickListener);

        buttons = new HashMap<>();
        for(int i = 0; i < fab_buttons.getChildCount(); i++) {
            View child = fab_buttons.getChildAt(i);
            buttons.put(child.getId(), child);
            child.setOnClickListener(onClickListener);
        }
        fab_buttons.removeAllViews();

        show(true);

//        fab_layout.setClosedOnTouchOutside(true);
//        fab_layout.setOnMenuButtonClickListener(onInitialClickListener);

        return this;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case TRACKING_NEW:
                State.getInstance().setToken(null);
                Intent intentService = new Intent(context, WhereAmINowService.class);
                intentService.putExtra("mode", "start");
                context.startService(intentService);
                break;
            case TRACKING_STOP:
//                fab_layout.close(true);
                break;
            case ACTIVITY_RESUME:
                hide(true);
                fab.setImageResource(R.drawable.ic_add_white_24dp);
                show(true);
                fab.setOnClickListener(onMainClickListener);
                break;
            case TOKEN_CREATED:
                new InviteSender(context).send("https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken());
                break;
        }
        return true;
    }

    public View addMenuButton(int buttonId){
        if(fab_buttons != null) {
            fab_buttons.addView(buttons.get(buttonId),0);
            return buttons.get(buttonId);
        }
        return null;
    }

    private void hide(boolean animation){
        fab_layout.setVisibility(View.GONE);
    }

    private void show(boolean animation){
        fab_layout.setVisibility(View.VISIBLE);
    }

    private void open(boolean animation){
        isFabMenuOpen = true;
        fab_buttons.setVisibility(View.VISIBLE);
        if(animation) {
            ViewCompat.animate(fab)
                    .rotation(45F)
                    .withLayer()
                    .setDuration(300L)
                    .setInterpolator(new OvershootInterpolator(10.0F))
                    .start();
        }
    }

    void close(boolean animation) {
        isFabMenuOpen = false;
        if(animation) {
            ViewCompat.animate(fab)
                    .rotation(0F)
                    .withLayer()
                    .setDuration(300L)
                    .setInterpolator(new OvershootInterpolator(10.0F))
                    .start();
        }
        fab_buttons.setVisibility(View.GONE);
    }

    public View addMenuButtonAt(int buttonId, int index){
        if(fab_buttons != null && buttons.containsKey(buttonId)) {
            fab_buttons.addView(buttons.get(buttonId), index);
            return buttons.get(buttonId);
        }
        return null;
    }

    private OnClickListener onInitialClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            State.getInstance().setGpsAccessRequested(false);
            context.onResume();
        }
    };

    private OnClickListener onMainClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("FABMENUOPEN:"+isFabMenuOpen);
            if(isFabMenuOpen){
                close(true);
            } else {
                if(State.getInstance().tracking()){
                    fab_buttons.removeAllViews();
                    addMenuButton(R.id.fab_stop_tracking);
                    addMenuButton(R.id.fab_new_message);
                    if(State.getInstance().getUsers().getCountActive() > 1 && State.getInstance().getUsers().getCountSelected() < State.getInstance().getUsers().getCountActive()) {
                        addMenuButton(R.id.fab_fit_to_screen);
                    }
                    addMenuButton(R.id.fab_send_link);
                    State.getInstance().fire(PREPARE_FAB, FabViewHolder.this);
                    open(true);
                } else {
                    State.getInstance().fire(TRACKING_NEW);
                }

            }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            close(true);
            switch (view.getId()) {
                case R.id.fab_stop_tracking:
                case R.id.fab_cancel_tracking:
                    State.getInstance().getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.removeViews();
                        }
                    });
                    State.getInstance().fire(TRACKING_STOP);
                    Intent intentService = new Intent(context, WhereAmINowService.class);
                    intentService.putExtra("mode", "stop");
                    context.startService(intentService);
                    break;
                case R.id.fab_send_link:
                    new InviteSender(context).send("https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken());
                    break;
                case R.id.fab_new_message:
                    State.getInstance().fire(NEW_MESSAGE);
                    break;
                case R.id.fab_fit_to_screen:
                    State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            if(myUser.getProperties().isActive()) {
                                myUser.fire(SELECT_USER, 0);
                            }
                        }
                    });
                    break;
            }

            /*if (fab_layout.isOpened()) {
                fab_layout.toggle(true);
            } else {
                if(State.getInstance().tracking()){
                    fab_layout.removeAllMenuButtons();
                    fab_layout.addMenuButton((FloatingActionButton) buttons.get(R.id.fab_send_link));
                    if(State.getInstance().getUsers().getCountActive() > 1 && State.getInstance().getUsers().getCountSelected() < State.getInstance().getUsers().getCountActive()) {
                        fab_layout.addMenuButton((FloatingActionButton) buttons.get(R.id.fab_fit_to_screen));
                    }
                    fab_layout.addMenuButton((FloatingActionButton) buttons.get(R.id.fab_new_message));
                    fab_layout.addMenuButton((FloatingActionButton) buttons.get(R.id.fab_stop_tracking));
                    State.getInstance().fire(PREPARE_FAB, FabViewHolder.this);
                    fab_layout.toggle(true);
                } else {
                    State.getInstance().fire(TRACKING_NEW);
                }
            }*/

        }
    };

}
