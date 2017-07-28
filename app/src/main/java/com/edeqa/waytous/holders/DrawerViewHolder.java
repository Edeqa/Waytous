package com.edeqa.waytous.holders;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.interfaces.Runnable1;
import com.edeqa.waytous.interfaces.Runnable2;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;


import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.PREPARE_DRAWER;
import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_CONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_NORMAL;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_SATELLITE;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_TERRAIN;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_TRAFFIC;

/**
 * Created 11/27/16.
 */
public class DrawerViewHolder extends AbstractViewHolder {

    public static final String TYPE = "drawer";

    private ActionBar actionBar;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageButton ibPrimary;

    private static int[] ids = new int[]{
            R.id.drawer_section_primary,
            R.id.drawer_section_communication,
            R.id.drawer_section_share,
            R.id.drawer_section_navigation,
            R.id.drawer_section_views,
            R.id.drawer_section_miscellaneous,
            R.id.drawer_section_last
    };

    public DrawerViewHolder(MainActivity context){
        super(context);



        setViewAndToolbar(context.findViewById(R.id.drawer_layout), (Toolbar) context.findViewById(R.id.toolbar));
        setCallback(onNavigationDrawerCallback);

        if(context.getSupportActionBar() != null) {
            actionBar = context.getSupportActionBar();
        }
    }

    public void setViewAndToolbar(View view, final Toolbar toolbar) {
        drawer = (DrawerLayout) view;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                context, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (!isDrawerOpen()) {
                        navigationView.getMenu().findItem(R.id.nav_satellite).setChecked(context.getMap().getMapType() == GoogleMap.MAP_TYPE_SATELLITE);
                        navigationView.getMenu().findItem(R.id.nav_terrain).setChecked(context.getMap().getMapType() == GoogleMap.MAP_TYPE_TERRAIN);
                        navigationView.getMenu().findItem(R.id.nav_traffic).setChecked(context.getMap().isTrafficEnabled());

                        for(int id:ids) {
                            navigationView.getMenu().findItem(id).setVisible(false);
                        }
                        State.getInstance().fire(PREPARE_DRAWER, navigationView);
                    }
                    drawer.invalidate();
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) drawer.findViewById(R.id.nav_view);

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
            case ACTIVITY_RESUME:
                for(int id:ids) {
                    navigationView.getMenu().findItem(id).setVisible(false);
                }
                State.getInstance().fire(CREATE_DRAWER, navigationView);

                ibPrimary = (ImageButton) navigationView.findViewById(R.id.ibPrim);
                ibPrimary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        State.getInstance().getUsers().forMe(new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser user) {
                                closeDrawer();
                                user.fire(SELECT_SINGLE_USER);
                            }
                        });
                    }
                });
                break;
            case TRACKING_ACTIVE:
            case TRACKING_DISABLED:
                if(actionBar != null) {
                    actionBar.setTitle(context.getString(R.string.app_name));
                }
                break;
            case TRACKING_CONNECTING:
            case TRACKING_RECONNECTING:
                if(actionBar != null) {
                    actionBar.setTitle(R.string.connecting);
                }
                break;
        }
        return true;
    }

    public void setCallback(final Runnable1<Integer> callback) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                callback.call(item.getItemId());
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    public boolean isDrawerOpen() {
        return drawer != null && drawer.isDrawerOpen(GravityCompat.START);
    }

    public void closeDrawer() {
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("drawer_intro").setLinkTo(IntroRule.LINK_TO_DRAWER_BUTTON).setTitle("Drawer").setDescription("Open left drawer to access main preferences."));
        return rules;
    }

    private Runnable1 onNavigationDrawerCallback = new Runnable1<Integer>() {
        @Override
        public void call(Integer id) {
            switch(id) {
//                case R.id.nav_settings:
//                    context.startActivity(new Intent(context, SettingsActivity.class));
//                    break;
                case R.id.nav_traffic:
                    State.getInstance().fire(REQUEST_MODE_TRAFFIC);
                    break;
                case R.id.nav_satellite:
                    if (context.getMap() != null && context.getMap().getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
                        State.getInstance().fire(REQUEST_MODE_SATELLITE);
                    } else {
                        State.getInstance().fire(REQUEST_MODE_NORMAL);
                    }
                    break;
                case R.id.nav_terrain:
                    if (context.getMap() != null && context.getMap().getMapType() != GoogleMap.MAP_TYPE_TERRAIN)
                        State.getInstance().fire(REQUEST_MODE_TERRAIN);
                    else
                        State.getInstance().fire(REQUEST_MODE_NORMAL);
                    break;
            }
        }
    };

}
