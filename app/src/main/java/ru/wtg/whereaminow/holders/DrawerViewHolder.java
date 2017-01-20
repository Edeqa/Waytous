package ru.wtg.whereaminow.holders;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.SettingsActivity;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.R.id.map;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_DRAWER;
import static ru.wtg.whereaminow.State.EVENTS.PREPARE_DRAWER;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_NORMAL;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_SATELLITE;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_TERRAIN;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_TRAFFIC;

/**
 * Created 11/27/16.
 */
public class DrawerViewHolder extends AbstractViewHolder {

    public static final String TYPE = "drawer";

    private final MainActivity context;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private GoogleMap map;
    SimpleCallback onNavigationDrawerCallback = new SimpleCallback<Integer>() {
        @Override
        public void call(Integer id) {
            switch(id) {
                case R.id.nav_settings:
                    context.startActivity(new Intent(context, SettingsActivity.class));
                    break;
                case R.id.nav_traffic:
                    State.getInstance().fire(REQUEST_MODE_TRAFFIC);
                    break;
                case R.id.nav_satellite:
                    if (map != null && map.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
                        State.getInstance().fire(REQUEST_MODE_SATELLITE);
                    } else {
                        State.getInstance().fire(REQUEST_MODE_NORMAL);
                    }
                    break;
                case R.id.nav_terrain:
                    if (map != null && map.getMapType() != GoogleMap.MAP_TYPE_TERRAIN)
                        State.getInstance().fire(REQUEST_MODE_TERRAIN);
                    else
                        State.getInstance().fire(REQUEST_MODE_NORMAL);
                    break;
            }
        }
    };

    public DrawerViewHolder(MainActivity context){
        this.context = context;

        setViewAndToolbar(context.findViewById(R.id.drawer_layout),(Toolbar) context.findViewById(R.id.toolbar));
        setCallback(onNavigationDrawerCallback);
        setMap(context.getMap());
    }

    public void setViewAndToolbar(View view, final Toolbar toolbar) {
        drawer = (DrawerLayout) view;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                context, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (!isDrawerOpen()) {
                        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_custom);
                        menuItem.setVisible(false);
                        State.getInstance().fire(PREPARE_DRAWER, menuItem);
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
                MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_custom);
                menuItem.setVisible(false);
                State.getInstance().fire(CREATE_DRAWER, menuItem);

                break;
        }
        return true;
    }

    public void setCallback(final SimpleCallback<Integer> callback) {

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

    public void setMap(GoogleMap map) {
        this.map = map;
    }
}
