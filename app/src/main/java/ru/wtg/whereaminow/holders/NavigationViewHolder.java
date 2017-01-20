package ru.wtg.whereaminow.holders;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.NavigationStarter;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.EVENTS.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.EVENTS.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.holders.CameraViewHolder.CAMERA_UPDATED;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_NIGHT;

/**
 * Created 12/29/16.
 */

public class NavigationViewHolder extends AbstractViewHolder<NavigationViewHolder.NavigationView> implements Serializable {

    static final long serialVersionUID = -6395904747332820058L;
    static final String SHOW_NAVIGATION = "show_navigation";
    private static final String TYPE = "Navigation";
    private static final String HIDE_NAVIGATION = "hide_navigation";

    private static final String NAVIGATION_MODE_DRIVING = "navigation_mode_driving";
    private static final String NAVIGATION_MODE_WALKING = "navigation_mode_walking";
    private static final String NAVIGATION_MODE_BICYCLING = "navigation_mode_bicycling";
    private static final String PREFERENCE_MODE = "navigation_mode";
    private static final String PREFERENCE_AVOID_HIGHWAYS = "navigation_avoid_highways";
    private static final String PREFERENCE_AVOID_TOLLS = "navigation_avoid_tolls";
    private static final String PREFERENCE_AVOID_FERRIES = "navigation_avoid_ferries";

    private static final int REBUILD_TRACK_IF_LOCATION_CHANGED_IN_METERS = 10;
    private static final int HIDE_TRACK_IF_DISTANCE_LESS_THAN = 10;
    private static final int SHOW_TRACK_IF_DISTANCE_BIGGER_THAN = 20;
    private final AppCompatActivity context;
    transient private GoogleMap map;
    private String mode = NAVIGATION_MODE_DRIVING;
    private View buttonsView;
    private int iconNavigationStyle;
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
                    mode = NAVIGATION_MODE_DRIVING;
                    break;
                case R.id.ib_navigation_walking:
                    ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_walking)).clearColorFilter();
                    mode = NAVIGATION_MODE_WALKING;
                    break;
                case R.id.ib_navigation_bicycling:
                    ((ImageButton)buttonsView.findViewById(R.id.ib_navigation_bicycling)).clearColorFilter();
                    mode = NAVIGATION_MODE_BICYCLING;
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


    public NavigationViewHolder(MainActivity context) {
        this.context = context;

        iconNavigationStyle = R.style.iconNavigationMarkerTextDay;

        setMap(context.getMap());
        setButtonsView(context.findViewById(R.id.layout_navigation_mode));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public NavigationView create(MyUser myUser) {
        if (myUser == null) return null;
        return new NavigationView(myUser);
    }

    public NavigationViewHolder setMap(GoogleMap map) {
        this.map = map;

        PropertiesHolder propertiesHolder = (PropertiesHolder) State.getInstance().getEntityHolder(PropertiesHolder.TYPE);

        NavigationViewHolder m = (NavigationViewHolder) propertiesHolder.loadFor(TYPE);
        if(m != null) {
            mode = m.mode;
        }
        return this;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case CAMERA_UPDATED:
                updateAll();
                break;
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.hide_navigations, Menu.NONE, R.string.hide_navigations).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(HIDE_NAVIGATION);
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
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        NavigationView view = ((NavigationView) myUser.getEntity(TYPE));
                        if(view != null && view.showNavigation) {
                            menuItemHideNavigations.setVisible(true);
                        }
                    }
                });
                break;
            case SHOW_NAVIGATION:
                buttonsView.setVisibility(View.VISIBLE);
                break;
            case REQUEST_MODE_NIGHT:
                iconNavigationStyle = R.style.iconNavigationMarkerTextNight;
                break;
            case REQUEST_MODE_DAY:
                iconNavigationStyle = R.style.iconNavigationMarkerTextDay;
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
            case NAVIGATION_MODE_DRIVING:
                buttonsView.findViewById(R.id.ib_navigation_driving).performClick();
                break;
            case NAVIGATION_MODE_WALKING:
                buttonsView.findViewById(R.id.ib_navigation_walking).performClick();
                break;
            case NAVIGATION_MODE_BICYCLING:
                buttonsView.findViewById(R.id.ib_navigation_bicycling).performClick();
                break;
        }
        return this;
    }

    private void updateAll() {
        State.getInstance().getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                NavigationView view = (NavigationView) myUser.getEntity(TYPE);
                if(view != null && view.showNavigation){
                    view.previousLocation = null;
                    view.update();
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

        dialog.setTitle("Options");
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
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(SHOW_NAVIGATION).setId("navigation_intro").setView(context.findViewById(R.id.layout_navigation_mode)).setTitle("Navigation").setDescription("You can switch between different modes of navigation using these buttons. Also, long touch calls additional options."));

        return rules;
    }

    class NavigationView extends AbstractView {
        volatile private Polyline track;
        private Polyline trackCenter;
        private boolean showNavigation = false;
        private Location previousLocation;
        private Location previousMeLocation;
        private Marker marker;
        private IconGenerator iconFactory;
        private int previousDistance;

        NavigationView(MyUser myUser){
            this.myUser = myUser;

            Boolean props = (Boolean) myUser.getProperties().loadFor(TYPE);
            showNavigation = !(props == null || !props);
            if(showNavigation){
                update();//onChangeLocation(myUser.getLocation());
//                myUser.fire(SHOW_NAVIGATION);
            }
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
            if(myUser == State.getInstance().getMe()) {
                State.getInstance().getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        NavigationView view = (NavigationView) myUser.getEntity(TYPE);
                        if(view != null && view.showNavigation) {
                            view.update();
                        }
                    }
                });
            } else if (showNavigation && location != null) {
                update();
            }
        }

        @Override
        public void remove() {
//            System.out.println("REMOVENAVVIEW:"+myUser.getProperties().getDisplayName()+":"+track);
            if(track != null){
                track.remove();
                track = null;
                trackCenter.remove();
                trackCenter = null;
                marker.remove();
                marker = null;
            }
            buttonsView.setVisibility(View.INVISIBLE);
            State.getInstance().getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                @Override
                public void call(Integer number, MyUser myUser) {
                    if(myUser!= null && myUser.getEntity(TYPE) != null && ((NavigationView)myUser.getEntity(TYPE)).track != null) buttonsView.setVisibility(View.VISIBLE);
                }
            });
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
                        }).setIcon(R.drawable.ic_navigation_black_outline_24dp_xml);
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

                    onChangeLocation(myUser.getLocation());

                    State.getInstance().fire(SHOW_NAVIGATION);
                    break;
                case HIDE_NAVIGATION:
                    showNavigation = false;
                    previousLocation = null;
                    myUser.getProperties().saveFor(TYPE, null);
                    remove();

                    break;
//                case SELECT_USER:
//                    updateAll();
//                    break;
            }
            return true;
        }



        private boolean locationChanged(Location current, Location previous) {
            LatLng currentPosition = Utils.latLng(current);
            LatLng previousPosition = Utils.latLng(previous);
            double distance = SphericalUtil.computeDistanceBetween(currentPosition, previousPosition);
            return distance > REBUILD_TRACK_IF_LOCATION_CHANGED_IN_METERS;
        }

        private void update() {
            if(track == null && State.getInstance().getMe().getLocation() != null && myUser.getLocation() != null) {
                final int color = (myUser.getProperties().getColor() & 0x00FFFFFF) | 0xAA000000;
                iconFactory = new IconGenerator(State.getInstance());
                iconFactory.setColor(color);

                iconFactory.setTextAppearance(iconNavigationStyle);

                final float density = State.getInstance().getResources().getDisplayMetrics().density;
                track = map.addPolyline(new PolylineOptions().width((int) (8 * density)).color(color).geodesic(true).zIndex(100f));
                trackCenter = map.addPolyline(new PolylineOptions().width((int) (2 * density)).color(Color.WHITE).geodesic(true).zIndex(100f));

                MarkerOptions markerOptions = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("")))
                        .position(SphericalUtil.interpolate(Utils.latLng(State.getInstance().getMe().getLocation()), Utils.latLng(myUser.getLocation()), .5))
                        .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV())
                        .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Setting up...")));

                marker = map.addMarker(markerOptions);
                marker.setVisible(false);
                buttonsView.setVisibility(View.INVISIBLE);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        final MyUser me = State.getInstance().getMe();
                        final LatLng mePosition = Utils.latLng(me.getLocation());
                        final LatLng userPosition = Utils.latLng(myUser.getLocation());

                        boolean changed = false;
                        if(previousMeLocation == null || previousLocation == null) {
                            changed = true;
                        }
                        if(!changed) {
                            double distanceToMe = SphericalUtil.computeDistanceBetween(mePosition, userPosition);
                            if(distanceToMe < 30) {
                                changed = true;
                            }
                        }
                        if(!changed && previousMeLocation != null) {
                            if(locationChanged(me.getLocation(),previousMeLocation)) {
                                changed = true;
                            }
                        }
                        if(!changed && previousLocation != null) {
                            if(locationChanged(myUser.getLocation(),previousLocation)) {
                                changed = true;
                            }
                        }
                        previousLocation = myUser.getLocation();
                        previousMeLocation = me.getLocation();

                        if(!changed) {
                            return;
                        }

                        String req = "https://maps.googleapis.com/maps/api/directions/json?"
                                + "origin=" + me.getLocation().getLatitude() + "," + me.getLocation().getLongitude() + "&"
                                + "destination=" + myUser.getLocation().getLatitude() + "," + myUser.getLocation().getLongitude() +"&"
                                + "mode=";

                        switch (mode) {
                            case NAVIGATION_MODE_DRIVING:
                                req += "driving";
                                break;
                            case NAVIGATION_MODE_WALKING:
                                req += "walking";
                                break;
                            case NAVIGATION_MODE_BICYCLING:
                                req += "bicycling";
                                break;
                        }

                        if(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_HIGHWAYS, false)) req += "&avoid=highways";
                        if(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_TOLLS, false)) req += "&avoid=tolls";
                        if(State.getInstance().getBooleanPreference(PREFERENCE_AVOID_FERRIES, false)) req += "&avoid=ferries";

                        Log.i(TYPE,req);
                        final String res = Utils.getUrl(req);
                        JSONObject o = new JSONObject(res);

                        final String text = o.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                        final List<LatLng> points = PolyUtil.decode(text);
                        final String title = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");

                        int distance = o.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");

                        if (distance <= HIDE_TRACK_IF_DISTANCE_LESS_THAN) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    remove();
                                }
                            });
                            previousDistance = distance;
                            return;
                        } else if (distance > SHOW_TRACK_IF_DISTANCE_BIGGER_THAN && previousDistance>0 && previousDistance < SHOW_TRACK_IF_DISTANCE_BIGGER_THAN && track == null) {
                            previousDistance = distance;
                        } else if (distance > HIDE_TRACK_IF_DISTANCE_LESS_THAN && distance <= SHOW_TRACK_IF_DISTANCE_BIGGER_THAN && track == null) {
                            previousDistance = distance;
                            return;
                        }
                        previousDistance = distance;

//                        iconFactory = new IconGenerator(State.getInstance());
//                        iconFactory.setColor(color);
//                        iconFactory.setTextAppearance(R.style.iconNavigationMarkerText);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (track != null) {
                                        String text = title;
                                        LatLng markerPosition = Utils.findPoint(points, .5);
                                        LatLngBounds bounds = Utils.reduce(map.getProjection().getVisibleRegion().latLngBounds, .8);
                                        if (!bounds.contains(markerPosition) && (bounds.contains(mePosition) || bounds.contains(userPosition))) {
                                            if (!bounds.contains(markerPosition)) {
                                                double fract = 0.5;
                                                while (!bounds.contains(markerPosition)) {
                                                    fract = fract + (bounds.contains(mePosition) ? -1 : +1) * .01;
                                                    if (fract < 0 || fract > 1) break;
                                                    markerPosition = Utils.findPoint(points, fract);
                                                }
                                            }
                                        }

                                        if (points != null) {
                                            track.setPoints(points);
                                            trackCenter.setPoints(points);
                                        }
                                        bounds = map.getProjection().getVisibleRegion().latLngBounds;
                                        if (!bounds.contains(mePosition) || !bounds.contains(userPosition)) {
                                            text += "\n" + myUser.getProperties().getDisplayName();
                                        }
                                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text)));
                                        marker.setPosition(markerPosition);
                                        marker.setVisible(true);
                                    }
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }


}
