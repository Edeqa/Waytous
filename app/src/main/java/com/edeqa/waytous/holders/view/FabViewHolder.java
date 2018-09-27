package com.edeqa.waytous.holders.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.ShareSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.PREPARE_FAB;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_CONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_ERROR;
import static com.edeqa.waytous.helpers.Events.TRACKING_EXPIRED;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;

/**
 * Created 11/27/16.
 */
@SuppressWarnings("WeakerAccess")
public class FabViewHolder extends AbstractViewHolder {

    public static final String PREPARE_SHARE_BUTTONS = "prepare_share_buttons"; //NON-NLS

    private LinearLayoutCompat fabButtons;
    private FloatingActionButton fab;

    private boolean isFabMenuOpen = false;
    private LayoutInflater inflater;

    public FabViewHolder(MainActivity context){
        super(context);

        setView(context.findViewById(R.id.fab_layout));
    }

    public void setView(View view) {
        fabButtons = view.findViewById(R.id.fab_buttons);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        close(false);

        fab = view.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_gps_off_white_24dp);
        fab.setOnClickListener(onInitialClickListener);

        fabButtons.removeAllViews();

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
            case ACTIVITY_RESUME:
                if(State.getInstance().tracking_active()) {
                    fab.setImageResource(R.drawable.ic_add_white_24dp);
                } else if(State.getInstance().tracking_connecting() || State.getInstance().tracking_reconnecting()) {
                    fab.setImageResource(R.drawable.ic_clear_white_24dp);
                } else {
                    fab.setImageResource(R.drawable.ic_navigation_twinks_white_24dp);
                }
                fab.setOnClickListener(onMainClickListener);
                break;
            case TRACKING_CONNECTING:
            case TRACKING_RECONNECTING:
                fab.setImageResource(R.drawable.ic_clear_white_24dp);
                break;
            case TRACKING_ACTIVE:
                fab.setImageResource(R.drawable.ic_add_white_24dp);
                break;
            case TRACKING_DISABLED:
            case TRACKING_ERROR:
            case TRACKING_EXPIRED:
                fab.setImageResource(R.drawable.ic_navigation_twinks_white_24dp);
                break;
        }
        switch(event){
            case PREPARE_FAB:
            case InfoViewHolder.SHOW_INFO:
                break;
            default:
                close(true);
        }
        return true;
    }

    private void open(boolean animation){
        isFabMenuOpen = true;
        fabButtons.setVisibility(View.VISIBLE);
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
        fabButtons.setVisibility(View.GONE);
    }

    public View add(int stringResource, int drawableResource) {
        LinearLayoutCompat button = (LinearLayoutCompat) inflater.inflate(R.layout.view_fab_button, null);

        ((TextView)button.findViewById(R.id.tv_fab_title)).setText(context.getString(stringResource));
        ((FloatingActionButton)button.findViewById(R.id.iv_fab_button)).setImageResource(drawableResource);

        LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(lps);
        button.setId(stringResource);

        fabButtons.addView(button);
        return button;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("fab_nav_button").setView(fab).setTitle("Quick button").setDescription("Click to create new group when button has this picture."));
        rules.add(new IntroRule().setEvent(TRACKING_ACTIVE).setId("fab_plus_button").setView(fab).setTitle("Quick button").setDescription("Now click + to open menu of quick group actions."));
        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("fab_share_link").setViewId(R.string.share_link).setTitle("Here you can").setDescription("Click to share this group to your friends using your usual tools."));
        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("fab_fit_to_screen").setViewId(R.string.fit_to_screen).setTitle("Here you can").setDescription("Click to zoom and fit all members to the screen."));
        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("fab_stop_tracking").setViewId(R.string.exit_group).setTitle("Here you can").setDescription("Cancel tracking and leave the group."));

        return rules;
    }

    private final OnClickListener onInitialClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            State.getInstance().setGpsAccessRequested(false);
            context.onResume();
        }
    };

    private final OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            close(true);
            switch (view.getId()) {
                case R.string.exit_group:
                    State.getInstance().fire(TRACKING_STOP);
                    break;
                case R.string.share_link:
                    openShareDialog(State.getInstance().getTracking().getTrackingUri());
                    break;
            }
        }
    };

    private final OnClickListener onMainClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isFabMenuOpen){
                close(true);
            } else {
                if(State.getInstance().tracking_active()){
                    fabButtons.removeAllViews();
                    add(R.string.share_link, R.drawable.ic_share_black_24dp).setOnClickListener(onClickListener);
                    State.getInstance().fire(PREPARE_FAB, FabViewHolder.this);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            add(R.string.exit_group, R.drawable.ic_clear_black_24dp).setOnClickListener(onClickListener);
                            open(true);
                        }
                    });
                } else if(State.getInstance().tracking_connecting() || State.getInstance().tracking_reconnecting()) {
                    State.getInstance().fire(TRACKING_STOP);
                } else {
                    State.getInstance().fire(TRACKING_NEW);
                }
            }
        }
    };

    @SuppressWarnings("HardCodedStringLiteral")
    private void openShareDialog(final String link) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(context.getString(R.string.share_link));

        @SuppressLint("InflateParams") final View content = context.getLayoutInflater().inflate(R.layout.dialog_share_link, null);

        LinearLayout layoutButtons = content.findViewById(R.id.layoutShareButtons);

        Map<String,Object> m = new HashMap<>();
        m.put("layout", layoutButtons);
        m.put("dialog", dialog);

        ((TextView)content.findViewById(R.id.tvLink)).setText(link);

        State.getInstance().fire(PREPARE_SHARE_BUTTONS, m);

        content.findViewById(R.id.bShareSelect).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new ShareSender(context).sendLink(link);
            }
        });
        content.findViewById(R.id.bShareByMail).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, "");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Waytous at " + link);
                        intent.putExtra(Intent.EXTRA_TEXT, link);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

                        context.startActivity(Intent.createChooser(intent, "Share link to mail"));
                    }
                }).start();
            }
        });
        content.findViewById(R.id.bCancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setView(content);
        dialog.show();
    }


}
