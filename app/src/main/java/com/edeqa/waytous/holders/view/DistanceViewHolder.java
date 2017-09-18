package com.edeqa.waytous.holders.view;

import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SmoothInterpolated;
import com.edeqa.waytous.helpers.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.SmoothInterpolated.TIME_ELAPSED;


/**
 * Created 11/24/16.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class DistanceViewHolder extends AbstractViewHolder<DistanceViewHolder.DistanceView> {

    private static final String SHOW_DISTANCE = "show_distance"; //NON-NLS
    private static final String HIDE_DISTANCE = "hide_distance"; //NON-NLS
    private static final String SHOW_DISTANCES = "show_distances"; //NON-NLS
    private static final String HIDE_DISTANCES = "hide_distances"; //NON-NLS

    private GoogleMap map;
    private ArrayList<DistanceMark> marks;

    public DistanceViewHolder(MainActivity context) {
        super(context);
        this.map = context.getMap();
        marks = new ArrayList<>();
    }

    @Override
    public DistanceView create(MyUser myUser) {
        if (myUser == null || myUser.getLocation() == null) return null;
        return new DistanceView(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;

                optionsMenu.add(Menu.NONE, R.string.show_distances, Menu.NONE, R.string.show_distances).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                State.getInstance().fire(SHOW_DISTANCES);
                            }
                        });
                        return false;
                    }
                });

                optionsMenu.add(Menu.NONE, R.string.hide_distances, Menu.NONE, R.string.hide_distances).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                State.getInstance().fire(HIDE_DISTANCES);
                            }
                        });
                        return false;
                    }
                });
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.show_distances).setVisible(State.getInstance().getUsers().getCountSelectedTotal()>1);
                optionsMenu.findItem(R.string.hide_distances).setVisible(marks.size() > 0);
                break;
            case SHOW_DISTANCES:
                State.getInstance().getUsers().forAllUsersExceptMe(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, final MyUser user) {
                        user.fire(SHOW_DISTANCE);
                    }
                });
                break;
            case TRACKING_DISABLED:
            case HIDE_DISTANCES:
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(HIDE_DISTANCE);
                    }
                });
                break;
            case CameraViewHolder.CAMERA_UPDATED:
                for(DistanceMark entry: marks) {
                    if(entry != null) {
                        entry.update(false);
                        ArrayList<LatLng> points = new ArrayList<>();
                        points.add(entry.firstPosition());
                        points.add(entry.secondPosition());
                        Utils.updateMarkerPosition(map, entry.marker, points);
                    }
                }
                break;
        }
        return true;
    }

    class DistanceView extends AbstractView {

        private boolean show = false;

        DistanceView(final MyUser myUser){
            super(myUser);

            Serializable value = myUser.getProperties().loadFor(getType());
            if(value != null && (Boolean) value) {
                show = true;
                fetchDistanceMark(State.getInstance().getMe(),myUser);
//                marks.clear();
                /*HashMap<Integer, MyUser> users = State.getInstance().getUsers().getUsers();
                for(Integer entry: saved) {
                    if(users.containsKey(entry)) {
                        MyUser user = users.get(entry);
                        fetchDistanceMark(myUser, user);
                    }
                }
                System.out.println("DISTANCES:"+saved);*/
            }
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
            for(DistanceMark entry: marks){
                if(entry.firstUser == myUser || entry.secondUser == myUser) {
                    entry.update(true);
                }
            }
        }

        @Override
        public void remove() {
            Iterator<DistanceMark> iter = marks.iterator();
            while(iter.hasNext()) {
                DistanceMark entry = iter.next();
                if(entry.firstUser == myUser || entry.secondUser == myUser) {
                    entry.line.remove();
                    entry.line = null;
                    entry.marker.remove();
                    entry.marker = null;
                    iter.remove();
                }
            }
        }

        @Override
        public boolean onEvent(String event, Object object) {
//            if(myUser.getLocation() != null) return true;
            switch(event) {
                case CREATE_CONTEXT_MENU:
                    if(myUser == State.getInstance().getMe()) break;
                    Menu contextMenu = (Menu) object;

                    contextMenu.add(0, R.string.show_distance, Menu.NONE, R.string.show_distance).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(SHOW_DISTANCE);
                            return false;
                        }
                    }).setIcon(R.drawable.ic_settings_ethernet_black_24dp).setVisible(!show);
                    contextMenu.add(0, R.string.hide_distance, Menu.NONE, R.string.hide_distance).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(HIDE_DISTANCE);
                            return false;
                        }
                    }).setIcon(R.drawable.ic_code_black_24dp).setVisible(show);
                    break;
                case SHOW_DISTANCE:
                    show = true;
                    myUser.getProperties().saveFor(getType(), true);
                    fetchDistanceMark(State.getInstance().getMe(),myUser);
                    /*State.getInstance().getUsers().forUser(myUser.getProperties().getNumber(),new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser user) {
                            if(user != myUser) {
                                fetchDistanceMark(myUser, user);

                                ArrayList<Integer> save = new ArrayList<>();
                                for(DistanceMark entry:marks) {
                                    if(entry.firstUser == myUser || entry.secondUser == myUser) {
                                        save.put(entry.firstUser == myUser ? entry.secondUser.getProperties().getNumber() : entry.firstUser.getProperties().getNumber());
                                    }
                                }
                                if(save.size() > 0) {
                                    myUser.getProperties().saveFor(TYPE, save);
                                }
                            }
                        }
                    });*/
                    break;
                case HIDE_DISTANCE:
                    remove();
                    show = false;
                    myUser.getProperties().saveFor(getType(), null);
                    break;
//                case SELECT_USER:
//                case UNSELECT_USER:
//                    for(DistanceMark entry: marks) {
//                        if(entry != null) {
//                            entry.update();
//                        }
//                    }
//                    break;
            }
            return true;
        }
    }

    private class DistanceMark {
        Polyline line;
        Marker marker;
        MyUser firstUser;
        MyUser secondUser;
        BitmapDescriptor icon;
//        LatLng markerPosition;
        LatLngBounds boundsForName;
        LatLngBounds bounds;

        String title;

        private IconGenerator iconFactory;

        DistanceMark(MyUser user1, MyUser user2) {
            firstUser = user1;
            secondUser = user2;

            iconFactory = new IconGenerator(State.getInstance());
            iconFactory.setColor(Color.argb(150,80,80,80));
            iconFactory.setTextAppearance(R.style.iconDistanceMarkerText);

            line = map.addPolyline(new PolylineOptions().geodesic(true).width(
                    (int) (2 * State.getInstance().getResources().getDisplayMetrics().density)
            ).color(Color.argb(200,100,100,100)));

            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(""))).
                            position(SphericalUtil.interpolate(firstPosition(), secondPosition(), .5)).
                            anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

            marker = map.addMarker(markerOptions);

            line.setPoints(Arrays.asList(firstPosition(),secondPosition()));
//            LatLng markerPosition = SphericalUtil.interpolate(firstPosition(), secondPosition(), .5);
            double distance = SphericalUtil.computeDistanceBetween(firstPosition(), secondPosition());
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(Misc.distanceToString(distance))));
//            marker.setPosition(markerPosition);
        }

        private LatLng firstPosition(){
            return new LatLng(firstUser.getLocation().getLatitude(), firstUser.getLocation().getLongitude());
        }

        private LatLng secondPosition(){
            return new LatLng(secondUser.getLocation().getLatitude(), secondUser.getLocation().getLongitude());
        }

        public void update(boolean animate){

            final LatLng firstUserInitial = line.getPoints().get(0);
            final LatLng secondUserInitial = line.getPoints().get(1);

            if(animate) {
                new SmoothInterpolated(new Runnable1<Float[]>() {
                    @Override
                    public void call(Float[] value) {
                        if (line != null && marker != null) {
                            LatLng firstCurrent = new LatLng(
                                    firstUserInitial.latitude * (1 - value[TIME_ELAPSED]) + firstPosition().latitude * value[TIME_ELAPSED],
                                    firstUserInitial.longitude * (1 - value[TIME_ELAPSED]) + firstPosition().longitude * value[TIME_ELAPSED]);

                            LatLng secondCurrent = new LatLng(
                                    secondUserInitial.latitude * (1 - value[TIME_ELAPSED]) + secondPosition().latitude * value[TIME_ELAPSED],
                                    secondUserInitial.longitude * (1 - value[TIME_ELAPSED]) + secondPosition().longitude * value[TIME_ELAPSED]);

                            updateLineAndMarker(firstCurrent, secondCurrent);

                        }
                    }
                }).execute();
            } else {
                updateLineAndMarker(firstPosition(), secondPosition());
            }
        }

        private void updateLineAndMarker(final LatLng firstPosition, final LatLng secondPosition) {
            title = Misc.distanceToString(SphericalUtil.computeDistanceBetween(firstPosition, secondPosition));

            bounds = map.getProjection().getVisibleRegion().latLngBounds;
            boundsForName = Utils.reduce(bounds, 0.9);

//            if(!boundsForName.contains(markerPosition) && (bounds.contains(firstPosition) || bounds.contains(secondPosition))) {
//                if(bounds.contains(firstPosition)) {
//                    LatLng position = secondPosition;
//                    int counter = 0;
//                    while(!boundsForName.contains(markerPosition)) {
//                        markerPosition = SphericalUtil.interpolate(firstPosition, position, .5);
//                        position = markerPosition;
//                        if(counter++ > 10) break;
//                    }
//                } else if (bounds.contains(secondPosition)) {
//                    LatLng position = firstPosition;
//                    int counter = 0;
//                    while(!boundsForName.contains(markerPosition)) {
//                        markerPosition = SphericalUtil.interpolate(position, secondPosition, .5);
//                        position = markerPosition;
//                        if(counter++ > 10) break;
//                    }
//                }
//            }
            if(!boundsForName.contains(firstPosition) || !boundsForName.contains(secondPosition)) {
                title += "\n" + (boundsForName.contains(firstPosition) ? secondUser : firstUser).getProperties().getDisplayName();
            }

            icon = BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(title));
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    if (line != null)
                        line.setPoints(Arrays.asList(firstPosition, secondPosition));
                    if (marker != null) {
                        marker.setIcon(icon);
                    }
                }
            });
            ArrayList<LatLng> points = new ArrayList<>();
            points.add(firstPosition);
            points.add(secondPosition);
            Utils.updateMarkerPosition(map, marker, points);
        }
    }

    private DistanceMark fetchDistanceMark(MyUser user1, MyUser user2) {
        for(DistanceMark entry: marks) {
            if(user1 == entry.firstUser && user2 == entry.secondUser) return entry;
            if(user2 == entry.firstUser && user1 == entry.secondUser) return entry;
        }
        if(user1 != null && user1.getLocation() != null && user2 != null && user2.getLocation() != null) {
            DistanceMark mark = new DistanceMark(user1, user2);
            marks.add(mark);
            mark.update(false);
            return mark;
        }
        return null;
    }

}
