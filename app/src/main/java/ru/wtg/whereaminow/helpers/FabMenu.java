package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import ru.wtg.whereaminow.R;

/**
 * Created by tujger on 7/15/16.
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
    public FloatingActionButton showUs;
    public FloatingActionButton navigate;
    public FloatingActionButton messages;

    public FabMenu(Context context) {
        super(context);
    }

    public FabMenu(Context context,AttributeSet attrs) {
        super(context,attrs);
    }

    public void initAndSetOnClickListener(View.OnClickListener onClickListener) {

        startTrackingAndSendLink = (FloatingActionButton) findViewById(R.id.fab_start_tracking_and_send_link);
        sendLink = (FloatingActionButton) findViewById(R.id.fab_send_link);
        stopTracking = (FloatingActionButton) findViewById(R.id.fab_stop_tracking);
        cancelTracking = (FloatingActionButton) findViewById(R.id.fab_cancel_tracking);
        switchToFriend = (FloatingActionButton) findViewById(R.id.fab_switch_to_friend);
        switchToMe = (FloatingActionButton) findViewById(R.id.fab_switch_to_me);
        navigate = (FloatingActionButton) findViewById(R.id.fab_navigate);
        showUs = (FloatingActionButton) findViewById(R.id.fab_show_us);
        messages = (FloatingActionButton) findViewById(R.id.fab_messages);
        splitScreen = (FloatingActionButton) findViewById(R.id.fab_split_screen);

        removeAllMenuButtons();
        setClosedOnTouchOutside(true);

        startTrackingAndSendLink.setOnClickListener(onClickListener);
        sendLink.setOnClickListener(onClickListener);
        stopTracking.setOnClickListener(onClickListener);
        cancelTracking.setOnClickListener(onClickListener);
        splitScreen.setOnClickListener(onClickListener);
        switchToFriend.setOnClickListener(onClickListener);
        switchToMe.setOnClickListener(onClickListener);
        showUs.setOnClickListener(onClickListener);
        navigate.setOnClickListener(onClickListener);
        messages.setOnClickListener(onClickListener);

        setOnMenuButtonClickListener(onClickListener);
        toggleMenuButton(false);
        setVisibility(View.VISIBLE);
        toggleMenuButton(true);

        setInitialized(true);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

}
