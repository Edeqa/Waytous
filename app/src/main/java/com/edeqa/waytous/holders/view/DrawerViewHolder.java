package com.edeqa.waytous.holders.view;

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
import com.edeqa.waytous.interfaces.Runnable2;

import java.util.ArrayList;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.BACK_PRESSED;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.PREPARE_DRAWER;
import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_CONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;

/**
 * Created 11/27/16.
 */
public class DrawerViewHolder extends AbstractViewHolder {

    public static final String TYPE = DrawerViewHolder.class.getSimpleName();

    private ActionBar actionBar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageButton ibPrimary;
    private ItemsHolder itemsHolder;

    private static int[] ids = new int[]{
            R.id.drawer_section_primary,
            R.id.drawer_section_communication,
            R.id.drawer_section_share,
            R.id.drawer_section_navigation,
            R.id.drawer_section_views,
            R.id.drawer_section_map,
            R.id.drawer_section_miscellaneous,
            R.id.drawer_section_last
    };

    public DrawerViewHolder(MainActivity context){
        super(context);

        setViewAndToolbar(context.findViewById(R.id.drawer_layout), (Toolbar) context.findViewById(R.id.toolbar));

        itemsHolder = new ItemsHolder();

        if(context.getSupportActionBar() != null) {
            actionBar = context.getSupportActionBar();
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void setViewAndToolbar(View view, final Toolbar toolbar) {
        drawer = (DrawerLayout) view;

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                context, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (!isDrawerOpen()) {
                        State.getInstance().fire(PREPARE_DRAWER, itemsHolder);
                    }
                    drawer.invalidate();
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) drawer.findViewById(R.id.nav_view);

        for(int i = 0; i < ids.length; i++) {
            if(navigationView.getMenu().findItem(ids[i]) == null) {
                MenuItem item = navigationView.getMenu().add(ids[i], ids[i], i * 100, null);
                navigationView.getMenu().setGroupEnabled(ids[i], true);
                item.setVisible(false);
            }
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

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
                State.getInstance().fire(CREATE_DRAWER, itemsHolder);

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
            case BACK_PRESSED:
                if (isDrawerOpen()) {
                    closeDrawer();
                    return false;
                }
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
            case SELECT_SINGLE_USER:
                System.out.println("DRAWER:"+object);
                break;
        }
        return true;
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
        //noinspection HardCodedStringLiteral
        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("drawer_intro").setLinkTo(IntroRule.LINK_TO_DRAWER_BUTTON).setTitle("Drawer").setDescription("Open left drawer to access main preferences."));
        return rules;
    }

    @SuppressWarnings("WeakerAccess")
    public class ItemsHolder {
        public MenuItem add(int groupId, int itemId, int titleResId, int iconResId) {
            return add(groupId, itemId, context.getString(titleResId), iconResId);
        }
        public MenuItem add(int groupId, int itemId, String title, int iconResId) {
            int order = 0;

            MenuItem item = findItem(itemId);
            if(item == null) {
                if (groupId > 0) {
                    for (int i = 0; i < navigationView.getMenu().size(); i++) {
                        if (navigationView.getMenu().getItem(i).getGroupId() == groupId) {
                            order = navigationView.getMenu().getItem(i).getOrder();
                            break;
                        }
                    }
                }
                item = navigationView.getMenu().add(groupId, itemId, order, title);
                item.setIcon(iconResId);
            }
            return item;
        }

        public MenuItem findItem(int itemId) {
            return navigationView.getMenu().findItem(itemId);
        }
    }
}
