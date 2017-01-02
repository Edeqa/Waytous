package ru.wtg.whereaminow.holders;

import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
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

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SmoothInterpolated;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.TRACKING_DISABLED;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.TIME_ELAPSED;
import static ru.wtg.whereaminow.holders.CameraViewHolder.CAMERA_UPDATED;

/**
 * Created 11/24/16.
 */

public class DistanceViewHolder extends AbstractViewHolder<DistanceViewHolder.DistanceView> {
    private static final String TYPE = "distance";

    private static final String SHOW_DISTANCE = "show_distance";
    private static final String HIDE_DISTANCE = "hide_distance";
    private static final String SHOW_DISTANCES = "show_distances";
    private static final String HIDE_DISTANCES = "hide_distances";

    private GoogleMap map;
    private ArrayList<DistanceMark> marks;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public DistanceView create(MyUser myUser) {
        if (myUser == null) return null;
        return new DistanceView(myUser);
    }

    public DistanceViewHolder setMap(GoogleMap map) {
        this.map = map;
        marks = new ArrayList<>();
        return this;
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
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
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
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
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
                optionsMenu.findItem(R.string.show_distances).setVisible(State.getInstance().getUsers().getCountAllSelected()>1);
                optionsMenu.findItem(R.string.hide_distances).setVisible(marks.size() > 0);
                break;
            case SHOW_DISTANCES:
                State.getInstance().getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, final MyUser user) {
                        user.fire(SHOW_DISTANCE);
                    }
                });
                break;
            case TRACKING_DISABLED:
            case HIDE_DISTANCES:
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.getEntity(TYPE).remove();
                        myUser.getProperties().saveFor(TYPE, null);
                    }
                });
                break;
            case CAMERA_UPDATED:
                for(DistanceMark entry: marks) {
                    if(entry != null) {
                        entry.update();
                    }
                }
                break;
        }
        return true;
    }

    class DistanceView extends AbstractView {

        private boolean show = false;
        DistanceView(final MyUser myUser){
            this.myUser = myUser;

            Serializable value = myUser.getProperties().loadFor(TYPE);
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
                    entry.update();
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
                    ContextMenu contextMenu = (ContextMenu) object;

                    contextMenu.add(R.string.show_distance).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(SHOW_DISTANCE);
                            return false;
                        }
                    }).setVisible(!show);
                    contextMenu.add(R.string.hide_distance).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(HIDE_DISTANCE);
                            return false;
                        }
                    }).setVisible(show);
                    break;
                case SHOW_DISTANCE:
                    show = true;
                    myUser.getProperties().saveFor(TYPE, true);
                    fetchDistanceMark(State.getInstance().getMe(),myUser);
                    /*State.getInstance().getUsers().forUser(myUser.getProperties().getNumber(),new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser user) {
                            if(user != myUser) {
                                fetchDistanceMark(myUser, user);

                                ArrayList<Integer> save = new ArrayList<>();
                                for(DistanceMark entry:marks) {
                                    if(entry.firstUser == myUser || entry.secondUser == myUser) {
                                        save.add(entry.firstUser == myUser ? entry.secondUser.getProperties().getNumber() : entry.firstUser.getProperties().getNumber());
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
                    myUser.getProperties().saveFor(TYPE, null);
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
        private IconGenerator iconFactory;

        public DistanceMark(MyUser user1, MyUser user2) {
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
            LatLng markerPosition = SphericalUtil.interpolate(firstPosition(), secondPosition(), .5);
            double distance = SphericalUtil.computeDistanceBetween(firstPosition(), secondPosition());
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(Utils.formatLengthToLocale(distance))));
            marker.setPosition(markerPosition);
        }
        private LatLng firstPosition(){
            return new LatLng(firstUser.getLocation().getLatitude(), firstUser.getLocation().getLongitude());
        }
        private LatLng secondPosition(){
            return new LatLng(secondUser.getLocation().getLatitude(), secondUser.getLocation().getLongitude());
        }
        public void update(){

            final LatLng firstUserInitial = line.getPoints().get(0);
            final LatLng secondUserInitial = line.getPoints().get(1);
            final LatLngBounds boundsForName = map.getProjection().getVisibleRegion().latLngBounds;
            final LatLngBounds bounds = Utils.reduce(boundsForName, .9);

            new SmoothInterpolated(new SimpleCallback<Float[]>() {
                @Override
                public void call(Float[] value) {
                    if(line != null && marker != null) {
                        final LatLng firstCurrent = new LatLng(
                                firstUserInitial.latitude * (1 - value[TIME_ELAPSED]) + firstPosition().latitude * value[TIME_ELAPSED],
                                firstUserInitial.longitude * (1 - value[TIME_ELAPSED]) + firstPosition().longitude * value[TIME_ELAPSED]);

                        final LatLng secondCurrent = new LatLng(
                                secondUserInitial.latitude * (1 - value[TIME_ELAPSED]) + secondPosition().latitude * value[TIME_ELAPSED],
                                secondUserInitial.longitude * (1 - value[TIME_ELAPSED]) + secondPosition().longitude * value[TIME_ELAPSED]);

                        String title = Utils.formatLengthToLocale(SphericalUtil.computeDistanceBetween(firstCurrent, secondCurrent));

                        LatLng markerPosition = SphericalUtil.interpolate(firstCurrent, secondCurrent, .5);
                        if(!bounds.contains(markerPosition) && (bounds.contains(firstCurrent) || bounds.contains(secondCurrent))) {
                            double fract = 0.5;
                            while (!bounds.contains(markerPosition)) {
                                fract = fract + (bounds.contains(firstCurrent) ? -1 : +1) * .05;
                                if(fract < 0 || fract > 1) break;
                                markerPosition = SphericalUtil.interpolate(firstCurrent, secondCurrent, fract);
                            }
                        }
                        if(!boundsForName.contains(firstCurrent) || !boundsForName.contains(secondCurrent)) {
                            title += "\n" + (boundsForName.contains(firstCurrent) ? secondUser : firstUser).getProperties().getDisplayName();
                        }
                        final String text = title;
                        final LatLng finalMarkerPosition = markerPosition;
//                        System.out.println("POSITION:"+secondUser.getProperties().getDisplayName()+":"+markerPosition);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            public void run() {
                                if(line != null)
                                    line.setPoints(Arrays.asList(firstCurrent, secondCurrent));
                                if(marker != null) {
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text)));
                                    marker.setPosition(finalMarkerPosition);
                                }
                            }
                        });


                    }
                }
            }).execute();
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
//        mark.update();
            return mark;
        }
        return null;
    }

}
