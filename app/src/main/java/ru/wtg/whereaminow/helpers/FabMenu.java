package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import ru.wtg.whereaminow.R;

/**
 * Created 7/15/16.
 */
public class FabMenu extends FloatingActionMenu {
    private boolean initialized = false;
    public FloatingActionButton startTrackingAndSendLink;
    public FloatingActionButton sendLink;
    public FloatingActionButton stopTracking;
    public FloatingActionButton cancelTracking;
    public FloatingActionButton splitScreen;
    public FloatingActionButton switchToFriend;
    public FloatingActionButton switchToMe;
    public FloatingActionButton fitToScreen;
    public FloatingActionButton navigate;
    public FloatingActionButton newMessage;

    public FabMenu(Context context) {
        super(context);
    }

    public FabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initAndSetOnClickListener(View.OnClickListener onClickListener) {

        if (!initialized) {
            startTrackingAndSendLink = (FloatingActionButton) findViewById(R.id.fab_start_tracking_and_send_link);
            sendLink = (FloatingActionButton) findViewById(R.id.fab_send_link);
            stopTracking = (FloatingActionButton) findViewById(R.id.fab_stop_tracking);
            cancelTracking = (FloatingActionButton) findViewById(R.id.fab_cancel_tracking);
            switchToFriend = (FloatingActionButton) findViewById(R.id.fab_switch_to_friend);
            switchToMe = (FloatingActionButton) findViewById(R.id.fab_switch_to_me);
            navigate = (FloatingActionButton) findViewById(R.id.fab_navigate);
            fitToScreen = (FloatingActionButton) findViewById(R.id.fab_fit_to_screen);
            newMessage = (FloatingActionButton) findViewById(R.id.fab_new_message);
            splitScreen = (FloatingActionButton) findViewById(R.id.fab_split_screen);
        }

        removeAllMenuButtons();
        setClosedOnTouchOutside(true);

        startTrackingAndSendLink.setOnClickListener(onClickListener);
        sendLink.setOnClickListener(onClickListener);
        stopTracking.setOnClickListener(onClickListener);
        cancelTracking.setOnClickListener(onClickListener);
        splitScreen.setOnClickListener(onClickListener);
        switchToFriend.setOnClickListener(onClickListener);
        switchToMe.setOnClickListener(onClickListener);
        fitToScreen.setOnClickListener(onClickListener);
        navigate.setOnClickListener(onClickListener);
        newMessage.setOnClickListener(onClickListener);

        setOnMenuButtonClickListener(onClickListener);
        toggleMenuButton(false);
        setVisibility(View.VISIBLE);
        toggleMenuButton(true);

        setInitialized(true);
    }

    public void setGpsOff() {
        getMenuIconView().setImageDrawable(getResources().getDrawable(R.drawable.ic_gps_off_white_24dp));
    }

    public void setPlus() {
        getMenuIconView().setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp));
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

}
