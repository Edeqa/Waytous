package ru.wtg.whereaminow.holders;

import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.FabMenu;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.NEW_MESSAGE;
import static ru.wtg.whereaminow.State.NEW_TRACKING;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.SEND_LINK;
import static ru.wtg.whereaminow.State.STOP_TRACKING;

/**
 * Created 11/27/16.
 */
public class FabViewHolder extends AbstractViewHolder {
    private static FabViewHolder instance;
    private FabMenu fab;
    private SimpleCallback<String> onFabClick;


    @Override
    public String getType() {
        return "fab";
    }

    @Override
    public String[] getOwnEvents() {
        return new String[0];
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

    public FabViewHolder setFab(final FabMenu fab) {
        this.fab = fab;

//        fab.showMenu(true);
        fab.toggleMenuButton(false);
        fab.setVisibility(View.VISIBLE);
        fab.toggleMenuButton(true);

        fab.setOnClickListener(onClickListener);
        fab.removeAllMenuButtons();
        /*fab.initAndSetOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hideMenu(true);
                context.checkPermissions(REQUEST_PERMISSION_LOCATION,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        });*/
        return this;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case STOP_TRACKING:
                fab.close(true);
                break;
        }
        return true;
    }

    public FabViewHolder setOnFabClick(SimpleCallback<String> onFabClick) {
        this.onFabClick = onFabClick;
        return this;
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab_stop_tracking:
                case R.id.fab_cancel_tracking:
                    State.getInstance().fire(STOP_TRACKING);
                    onFabClick.call(STOP_TRACKING);
                    break;
                case R.id.fab_send_link:
                    onFabClick.call(SEND_LINK);
                    break;
                case R.id.fab_new_message:
                    onFabClick.call(NEW_MESSAGE);
                    break;
                case R.id.fab_fit_to_screen:
                    State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.fire(SELECT_USER, 0);
                        }
                    });
                    break;
            }

            if (fab.isOpened()) {
                fab.removeAllMenuButtons();
                fab.toggle(true);
            } else {
                if(State.getInstance().tracking()){
                    fab.addMenuButton(fab.sendLink);
                    if(State.getInstance().getUsers().getCountActive() > 1 && State.getInstance().getUsers().getCountSelected() < State.getInstance().getUsers().getCountActive()) {
                        fab.addMenuButton(fab.fitToScreen);
                    }
                    fab.addMenuButton(fab.newMessage);
                    fab.addMenuButton(fab.stopTracking);
                    fab.toggle(true);
                } else {
                    /*if (state.rejected()) {
                        checkPermissions(REQUEST_PERMISSION_LOCATION,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                    } else if (state.disconnected()) {
                        System.out.println("fab.onClick:start");

                    }*/

                    State.getInstance().fire(NEW_TRACKING);
                    onFabClick.call(NEW_TRACKING);
                }
            }

        }
    };

}
