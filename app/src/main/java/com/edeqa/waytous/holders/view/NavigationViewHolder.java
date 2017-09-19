package com.edeqa.waytous.holders.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.NavigationHelper;
import com.edeqa.waytous.helpers.NavigationStarter;
import com.edeqa.waytous.helpers.SettingItem;
import com.edeqa.waytous.helpers.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.id.list;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;
import static com.edeqa.waytous.holders.view.CameraViewHolder.CAMERA_UPDATED;
import static com.edeqa.waytous.holders.view.SensorsViewHolder.REQUEST_MODE_DAY;
import static com.edeqa.waytous.holders.view.SensorsViewHolder.REQUEST_MODE_NIGHT;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.CREATE_SETTINGS;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.PREPARE_SETTINGS;


/**
 * Created 12/29/16.
 */

@SuppressWarnings({"HardCodedStringLiteral", "WeakerAccess"})
public class NavigationViewHolder extends AbstractViewHolder<NavigationViewHolder.NavigationView> implements Serializable {

    static final long serialVersionUID = -6395904747332820058L;

    public static final String TYPE = "navigationView";

    @SuppressWarnings("WeakerAccess")
    public static final String SHOW_NAVIGATION = "show_navigation";
    public static final String HIDE_NAVIGATION = "hide_navigation";

//    private static final String NAVIGATION_MODE_DRIVING = "driving";
//    private static final String NAVIGATION_MODE_WALKING = "walking";
//    private static final String NAVIGATION_MODE_BICYCLING = "bicycling";

    private static final String PREFERENCE_MODE = "navigation_mode";
    private static final String PREFERENCE_OPTIONS = "navigation_type";

    private static final String PREFERENCE_AVOID_HIGHWAYS = "navigation_avoid_highways";
    private static final String PREFERENCE_AVOID_TOLLS = "navigation_avoid_tolls";
    private static final String PREFERENCE_AVOID_FERRIES = "navigation_avoid_ferries";

    private static final int REBUILD_TRACK_IF_LOCATION_CHANGED_IN_METERS = 10;
    private static final int HIDE_TRACK_IF_DISTANCE_LESS_THAN = 10;
    private static final int SHOW_TRACK_IF_DISTANCE_BIGGER_THAN = 20;


    transient private GoogleMap map;

    private View buttonsView;

    private NavigationHelper.Mode mode = NavigationHelper.Mode.DRIVING;
    private int iconNavigationStyle;
    private Handler handlerHideButtons;

    public NavigationViewHolder(MainActivity context) {
        super(context);
        this.map = context.getMap();
        handlerHideButtons = new Handler();

        try {
            mode = NavigationHelper.Mode.valueOf(State.getInstance().getStringPreference(PREFERENCE_MODE, NavigationHelper.Mode.DRIVING.toString()));
        } catch(Exception e) {
            mode = NavigationHelper.Mode.DRIVING;
        }

//        NavigationViewHolder m = (NavigationViewHolder) State.getInstance().getPropertiesHolder().loadFor(TYPE);
//        if(m != null) {
//            mode = m.mode;
//        }

        iconNavigationStyle = R.style.iconNavigationMarkerTextDay;
        setButtonsView(context.findViewById(R.id.layout_navigation_mode));
    }

    @Override
    public NavigationView create(MyUser myUser) {
        if (myUser == null) return null;
        return new NavigationView(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.hide_navigations, Menu.NONE, R.string.hide_navigations).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                NavigationView view = ((NavigationView) myUser.getView(TYPE));
                                if(view != null && view.showNavigation) {
                                    myUser.fire(HIDE_NAVIGATION);
                                }
                            }
                        });
                        return false;
                    }
                });
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                final MenuItem menuItemHideNavigations = optionsMenu.findItem(R.string.hide_navigations);
                menuItemHideNavigations.setVisible(false);
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        NavigationView view = ((NavigationView) myUser.getView(TYPE));
                        if(view != null && view.showNavigation) {
                            menuItemHideNavigations.setVisible(true);
                        }
                    }
                });
                break;
            case SHOW_NAVIGATION:
                buttonsView.setVisibility(View.VISIBLE);
                handlerHideButtons.removeCallbacks(hideButtons);
                handlerHideButtons.postDelayed(hideButtons, 5000);
                break;
            case CAMERA_UPDATED:
                State.getInstance().getUsers().forAllUsersExceptMe(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        if(myUser!= null && myUser.getView(TYPE) != null && ((NavigationView)myUser.getView(TYPE)).track != null) {
                            buttonsView.setVisibility(View.VISIBLE);
                            handlerHideButtons.removeCallbacks(hideButtons);
                            handlerHideButtons.postDelayed(hideButtons, 5000);
                            NavigationView view = (NavigationView) myUser.getView(TYPE);
                            Utils.updateMarkerPosition(map, view.marker, view.points);
                        }
                    }
                });
                break;
            case REQUEST_MODE_NIGHT:
                iconNavigationStyle = R.style.iconNavigationMarkerTextNight;
                break;
            case REQUEST_MODE_DAY:
                iconNavigationStyle = R.style.iconNavigationMarkerTextDay;
                break;
            case CREATE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;

                //noinspection unchecked
                item.add(new SettingItem.Page(TYPE).setTitle(R.string.navigation)
                        .add(new SettingItem.Group(SettingsViewHolder.PREFERENCES_GENERAL).setTitle(R.string.general).setGroupId(TYPE))
                        .add(new SettingItem.List(PREFERENCE_MODE)
                                .add(NavigationHelper.Mode.DRIVING.toString(), R.string.driving)
                                .add(NavigationHelper.Mode.WALKING.toString(), R.string.walking)
                                .add(NavigationHelper.Mode.BICYCLING.toString(), R.string.bicycling)
                                .setValue(mode.toString())
                                .setTitle(R.string.mode).setGroupId(SettingsViewHolder.PREFERENCES_GENERAL)
                                .setMessage(context.getString(R.string.select_your_preferred_navigation_mode))
                                .setCallback(new Runnable1<String>() {
                                    @Override
                                    public void call(String arg) {
                                        View v = new View(context);
                                        switch (NavigationHelper.Mode.valueOf(arg)) {
                                            case WALKING:
                                                v.setId(R.id.ib_navigation_walking);
                                                break;
                                            case BICYCLING:
                                                v.setId(R.id.ib_navigation_bicycling);
                                                break;
                                            default:
                                                v.setId(R.id.ib_navigation_driving);
                                        }
                                        onClickListener.onClick(v);
                                    }
                                }))
                        .add(new SettingItem.Group(PREFERENCE_OPTIONS).setTitle(R.string.options).setGroupId(TYPE))
                        .add(new SettingItem.Checkbox(PREFERENCE_AVOID_HIGHWAYS).setTitle(R.string.avoid_highways).setGroupId(PREFERENCE_OPTIONS))
                        .add(new SettingItem.Checkbox(PREFERENCE_AVOID_TOLLS).setTitle(R.string.avoid_tolls).setGroupId(PREFERENCE_OPTIONS))
                        .add(new SettingItem.Checkbox(PREFERENCE_AVOID_FERRIES).setTitle(R.string.avoid_ferries).setGroupId(PREFERENCE_OPTIONS)));
                break;
            case PREPARE_SETTINGS:
                SettingItem settings = (SettingItem) object;
                settings.update(PREFERENCE_MODE, mode.toString().toLowerCase());
                settings.update(PREFERENCE_AVOID_HIGHWAYS, State.getInstance().getBooleanPreference(PREFERENCE_AVOID_HIGHWAYS, false));
                settings.update(PREFERENCE_AVOID_TOLLS, State.getInstance().getBooleanPreference(PREFERENCE_AVOID_TOLLS, false));
                settings.update(PREFERENCE_AVOID_FERRIES, State.getInstance().getBooleanPreference(PREFERENCE_AVOID_FERRIES, false));
                break;
        }
        return true;
    }

    private NavigationViewHolder setButtonsView(View buttonsView) {
        this.buttonsView = buttonsView;

        buttonsView.findViewById(R.id.ib_navigation_driving).setOnClickListener(onClickListener);
        buttonsView.findViewById(R.id.ib_navigation_driving).setOnLongClickListener(onLongClickListener);
        buttonsView.findViewById(R.id.ib_navigation_walking).setOnClickListener(onClickListener);
        buttonsView.findViewById(R.id.ib_navigation_walking).setOnLongClickListener(onLongClickListener);
        buttonsView.findViewById(R.id.ib_navigation_bicycling).setOnClickListener(onClickListener);
        buttonsView.findViewById(R.id.ib_navigation_bicycling).setOnLongClickListener(onLongClickListener);

        switch (mode) {
            case DRIVING:
                buttonsView.findViewById(R.id.ib_navigation_driving).performClick();
                break;
            case WALKING:
                buttonsView.findViewById(R.id.ib_navigation_walking).performClick();
                break;
            case BICYCLING:
                buttonsView.findViewById(R.id.ib_navigation_bicycling).performClick();
                break;
        }
        return this;
    }

    private void updateAll() {
        State.getInstance().getUsers().forAllUsersExceptMe(new Runnable2<Integer, MyUser>() {
            @Override
            public void call(Integer number, MyUser myUser) {
                NavigationView view = (NavigationView) myUser.getView(TYPE);
                if(view != null && view.showNavigation){
                    view.navigationHelper.setMode(mode);
                    view.navigationHelper.updatePath(true);
                }
            }
        });
    }

    private void options(final int type) {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();

        @SuppressLint("InflateParams") final View content = context.getLayoutInflater().inflate(R.layout.dialog_navigation_options_driving, null);

        final CheckBox cbAvoidHighways = (CheckBox) content.findViewById(R.id.cb_navigation_avoid_highways);
        final CheckBox cbAvoidTolls = (CheckBox) content.findViewById(R.id.cb_navigation_avoid_tolls);
        final CheckBox cbAvoidFerries = (CheckBox) content.findViewById(R.id.cb_navigation_avoid_ferries);

        if(type != R.id.ib_navigation_driving) {
            cbAvoidHighways.setVisibility(View.GONE);
            cbAvoidTolls.setVisibility(View.GONE);
        }

        cbAvoidHighways.setChecked(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_HIGHWAYS, false));
        cbAvoidTolls.setChecked(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_TOLLS, false));
        cbAvoidFerries.setChecked(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_FERRIES, false));

        dialog.setTitle(context.getString(R.string.options));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                State.getInstance().setPreference(PREFERENCE_AVOID_HIGHWAYS, cbAvoidHighways.isChecked());
                State.getInstance().setPreference(PREFERENCE_AVOID_TOLLS, cbAvoidTolls.isChecked());
                State.getInstance().setPreference(PREFERENCE_AVOID_FERRIES, cbAvoidFerries.isChecked());

                buttonsView.findViewById(type).performClick();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
            }
        });
        dialog.setView(content);
        dialog.show();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    class NavigationView extends AbstractView {

        volatile private Polyline track;
        private Polyline trackCenter;
        private boolean showNavigation = false;
        private Marker marker;
        private IconGenerator iconFactory;
        private List<LatLng> points;
        private NavigationHelper navigationHelper;
        private String distance;
        private String duration;

        NavigationView(final MyUser myUser){
            super(NavigationViewHolder.this.context, myUser);

            navigationHelper = new NavigationHelper();
            navigationHelper.setOnStart(new Runnable() {
                @Override
                public void run() {
                    try {
                        final int color = (myUser.getProperties().getColor() & 0x00FFFFFF) | 0xAA000000;
                        iconFactory = new IconGenerator(State.getInstance());
                        iconFactory.setColor(color);

                        iconFactory.setTextAppearance(iconNavigationStyle);

                        float density = State.getInstance().getResources().getDisplayMetrics().density;
                        track = map.addPolyline(new PolylineOptions().width((int) (20 * density)).color(color).geodesic(true).zIndex(100f));
                        trackCenter = map.addPolyline(new PolylineOptions().width((int) (6 * density)).color(Color.WHITE).geodesic(true).zIndex(101f));

                        MarkerOptions markerOptions = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("")))
                                .position(Utils.latLng(myUser.getLocation()))
//                                .position(SphericalUtil.interpolate(Utils.latLng(State.getInstance().getMe().getLocation()), Utils.latLng(myUser.getLocation()), .5))
                                .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV())
                                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(context.getString(R.string.setting_up))))
                                .visible(false);

                        marker = map.addMarker(markerOptions);
                        Bundle b = new Bundle();
                        b.putString(MarkerViewHolder.TYPE, TYPE);
                        b.putInt(RESPONSE_NUMBER, myUser.getProperties().getNumber());

                        buttonsView.setVisibility(View.VISIBLE);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            navigationHelper.setOnRequest(new Runnable1<String>() {
                @Override
                public void call(String req) {
                    Utils.log(NavigationView.this, "User:", myUser.getProperties().getNumber()+"|"+myUser.getProperties().getDisplayName(), "Request:",req);
                }
            });
            navigationHelper.setOnUpdate(new Runnable2<NavigationHelper.Type, Object>() {
                @Override
                public void call(NavigationHelper.Type type, Object object) {
                    switch (type) {
                        case DISTANCE:
                            distance = object.toString();
                            break;
                        case DURATION:
                            duration = object.toString();
                            break;
                        case POINTS:
                            break;
                        case POINTS_BEFORE:
                            break;
                        case POINTS_AFTER:
                            points = (List<LatLng>) object;
                            track.setPoints(points);
                            trackCenter.setPoints(points);
                            if(!track.isVisible()) track.setVisible(true);
                            break;
                        case UPDATED:
                            Utils.updateMarkerPosition(map, marker, points);
                            String text = distance + "\n" + duration;

                            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                            if (!bounds.contains(Utils.latLng(State.getInstance().getMe().getLocation())) || !bounds.contains(Utils.latLng(myUser.getLocation()))) {
                                text += "\n" + myUser.getProperties().getDisplayName();
                            }
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text)));
                            marker.setVisible(true);
                            break;
                    }
                }

            });
            navigationHelper.setOnStop(new Runnable() {
                @Override
                public void run() {
                    track.remove();
                    track = null;
                    trackCenter.remove();
                    trackCenter = null;
                    marker.remove();
                    marker = null;
                    buttonsView.setVisibility(View.INVISIBLE);
                    State.getInstance().getUsers().forAllUsersExceptMe(new Runnable2<Integer, MyUser>() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            if(myUser!= null && myUser.getView(TYPE) != null && ((NavigationView)myUser.getView(TYPE)).track != null) {
                                buttonsView.setVisibility(View.VISIBLE);
                                handlerHideButtons.removeCallbacks(hideButtons);
                                handlerHideButtons.postDelayed(hideButtons, 5000);
                            }
                        }
                    });

                }
            });
            navigationHelper.setRunner(runner);
            navigationHelper.setAvoidHighways(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_HIGHWAYS, false));
            navigationHelper.setAvoidTolls(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_TOLLS, false));
            navigationHelper.setAvoidFerries(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_FERRIES, false));
            navigationHelper.setMode(mode);

            Boolean props = (Boolean) myUser.getProperties().loadFor(TYPE);
            showNavigation = !(props == null || !props);
            if(showNavigation){
                myUser.fire(SHOW_NAVIGATION);
            }
        }

        @Override
        public String getType() {
            return NavigationViewHolder.this.getType();
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
            if(myUser == State.getInstance().getMe()) {
                State.getInstance().getUsers().forAllUsersExceptMe(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        NavigationView view = (NavigationView) myUser.getView(TYPE);

                        if(view != null && view.showNavigation) {
                            view.navigationHelper.updateStartLocation(location);
//                            view.update();
                        }
                    }
                });
            } else if (showNavigation && location != null) {
                navigationHelper.updateEndLocation(location);
//                update();
            }
        }

        @Override
        public void remove() {
            navigationHelper.stop();
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(myUser.getLocation() == null) return true;
            switch(event) {
                case CREATE_CONTEXT_MENU:
                    Menu menu = (Menu) object;
                    if(!showNavigation && myUser != State.getInstance().getMe()){
                        menu.add(0, R.string.show_navigation, Menu.NONE, R.string.show_navigation).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(SHOW_NAVIGATION);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_navigation_black_24dp_xml);
                    }
                    if(showNavigation) {
                        menu.add(0, R.string.hide_navigation, Menu.NONE, R.string.hide_navigation).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(HIDE_NAVIGATION);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_navigation_outline_black_24dp_xml);
                    }
                    if(myUser != State.getInstance().getMe()) {
                        menu.add(0, R.string.navigate_on_maps, Menu.NONE, R.string.navigate_on_maps).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                new NavigationStarter(context, myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()).start();
                                return false;
                            }
                        }).setIcon(R.drawable.ic_directions_black_24dp);
                    }
                    break;
                case SHOW_NAVIGATION:
                    showNavigation = true;
                    if(myUser == State.getInstance().getMe()) break;

                    myUser.getProperties().saveFor(TYPE, showNavigation);

                    navigationHelper.setStartLocation(State.getInstance().getMe().getLocation());
                    navigationHelper.setEndLocation(myUser.getLocation());
                    navigationHelper.start();

                    State.getInstance().fire(SHOW_NAVIGATION);
                    break;
                case HIDE_NAVIGATION:
                    showNavigation = false;
                    myUser.getProperties().saveFor(TYPE, null);
                    navigationHelper.stop();

                    break;
//                case SELECT_USER:
//                    updateAll();
//                    break;
            }
            return true;
        }


            /*boolean needUpdate = false;
            if(locationChanged(State.getInstance().getMe().getLocation(), previousMeLocation) && !PolyUtil.isLocationOnPath(Utils.latLng(State.getInstance().getMe().getLocation()), track.getPoints(), false, 20)) {
                needUpdate = true;
            } else if(locationChanged(myUser.getLocation(), previousLocation) && !PolyUtil.isLocationOnPath(Utils.latLng(myUser.getLocation()), track.getPoints(), false, 20)) {
                needUpdate = true;
            }
            // removing points that passed already
            if(!needUpdate) {
                points = track.getPoints();
                LatLng removed = null;
                if(locationChanged(State.getInstance().getMe().getLocation(), previousMeLocation)) {
                    while(PolyUtil.isLocationOnPath(Utils.latLng(State.getInstance().getMe().getLocation()), points, false, 20)) {
                        removed = points.remove(0);
                        countRemoved++;
                    }
                    if(removed != null) {
                        points.add(0, removed);
                    }
                } else if(locationChanged(myUser.getLocation(), previousLocation)) {
                    while(PolyUtil.isLocationOnPath(Utils.latLng(State.getInstance().getMe().getLocation()), points, false, 20)) {
                        removed = points.remove(points.size()-1);
                        countRemoved++;
                    }
                    if(removed != null) {
                        points.add(removed);
                    }
                }
            }
*/

            /*new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        if (!changed) {
                            double distanceToMe = SphericalUtil.computeDistanceBetween(mePosition, userPosition);
                            if (distanceToMe < 30) {
                                changed = true;
                            }
                        }
                        if (!changed && previousMeLocation != null) {
                            if (locationChanged(me.getLocation(), previousMeLocation)) {
                                changed = true;
                            }
                        }
                        if (!changed && previousLocation != null) {
                            if (locationChanged(myUser.getLocation(), previousLocation)) {
                                changed = true;
                            }
                        }

                            if (distance <= HIDE_TRACK_IF_DISTANCE_LESS_THAN) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        remove();
                                    }
                                });
                                previousDistance = distance;
                                return;
                            } else if (distance > SHOW_TRACK_IF_DISTANCE_BIGGER_THAN && previousDistance > 0 && previousDistance < SHOW_TRACK_IF_DISTANCE_BIGGER_THAN && track == null) {
                                previousDistance = distance;
                            } else if (distance > HIDE_TRACK_IF_DISTANCE_LESS_THAN && distance <= SHOW_TRACK_IF_DISTANCE_BIGGER_THAN && track == null) {
                                previousDistance = distance;
                                return;
                            }
                            previousDistance = distance;
                        }
                        updateMarkerPosition(mePosition, userPosition);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();*/

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            buttonsView.findViewById(R.id.ib_navigation_driving).getBackground().setAlpha(150);
            ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_driving)).setColorFilter(Color.argb(120, 255, 255, 255));

            buttonsView.findViewById(R.id.ib_navigation_walking).getBackground().setAlpha(150);
            ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_walking)).setColorFilter(Color.argb(120, 255, 255, 255));

            buttonsView.findViewById(R.id.ib_navigation_bicycling).getBackground().setAlpha(150);
            ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_bicycling)).setColorFilter(Color.argb(120, 255, 255, 255));

            switch (view.getId()) {
                case R.id.ib_navigation_driving:
                    ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_driving)).clearColorFilter();
                    mode = NavigationHelper.Mode.DRIVING;
                    break;
                case R.id.ib_navigation_walking:
                    ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_walking)).clearColorFilter();
                    mode = NavigationHelper.Mode.WALKING;
                    break;
                case R.id.ib_navigation_bicycling:
                    ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_bicycling)).clearColorFilter();
                    mode = NavigationHelper.Mode.BICYCLING;
                    break;
            }
            updateAll();
        }
    };

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.ib_navigation_driving:
                    options(R.id.ib_navigation_driving);
                    break;
                case R.id.ib_navigation_walking:
                    options(R.id.ib_navigation_walking);
                    break;
                case R.id.ib_navigation_bicycling:
                    options(R.id.ib_navigation_bicycling);
                    break;
            }
            return false;
        }
    };

    private Runnable hideButtons = new Runnable() {
        @Override
        public void run() {
            buttonsView.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(SHOW_NAVIGATION).setId("navigation_intro").setView(context.findViewById(R.id.layout_navigation_mode)).setTitle("Navigation").setDescription("You can switch between different modes of navigation using these buttons. Also, long touch calls additional options."));

        return rules;
    }

    final Handler handler = new Handler(Looper.getMainLooper());
    EventBus.Runner runner = new EventBus.Runner() {
        @Override
        public void post(Runnable runnable) {
            handler.post(runnable);
        }
    };

}
