package ru.wtg.whereaminow.holders;

import android.graphics.Color;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SmoothInterpolated;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.TRACKING_ACCEPTED;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.TIME_ELAPSED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;

/**
 * Created 11/24/16.
 */

public class DistantionViewHolder extends AbstractViewHolder<DistantionViewHolder.DistantionView> {
    private static final String TYPE = "distantion";

    private static final String SHOW_DISTANTIONS = "show_distantions";
    private static final String HIDE_DISTANTIONS = "hide_distantions";

    private GoogleMap map;
    private ArrayList<DistantionMark> marks;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public DistantionView create(MyUser myUser) {
        if (myUser == null) return null;
        return new DistantionView(myUser);
    }

    public DistantionViewHolder setMap(GoogleMap map) {
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

                optionsMenu.add(Menu.NONE, R.string.show_distantions, Menu.NONE, R.string.show_distantions).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                State.getInstance().fire(SHOW_DISTANTIONS);
                            }
                        });
                        return false;
                    }
                });

                optionsMenu.add(Menu.NONE, R.string.hide_distantions, Menu.NONE, R.string.hide_distantions).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                State.getInstance().fire(HIDE_DISTANTIONS);
                            }
                        });
                        return false;
                    }
                });
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.show_distantions).setVisible(State.getInstance().getUsers().getCountAllSelected()>1);
                optionsMenu.findItem(R.string.hide_distantions).setVisible(marks.size() > 0);
                break;
            case SHOW_DISTANTIONS:
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, final MyUser user1) {
                        if(user1.getProperties().isSelected()) {
                            State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser user2) {
                                    if(user1 != user2 && user2.getProperties().isSelected()) {
                                        fetchDistantionMark(user1, user2);
                                    }
                                }
                            });
                        }
                    }
                });
                break;
            case HIDE_DISTANTIONS:
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.getEntity(TYPE).remove();
                        myUser.getProperties().saveFor(TYPE, null);
                    }
                });
                break;
        }
        return true;
    }

    class DistantionView extends AbstractView {

        DistantionView(final MyUser myUser){
            this.myUser = myUser;

            ArrayList<Integer> saved = (ArrayList<Integer>) myUser.getProperties().loadFor(TYPE);
            if(saved != null && saved.size() > 0){
//                marks.clear();
                HashMap<Integer, MyUser> users = State.getInstance().getUsers().getUsers();
                for(Integer entry: saved) {
                    if(users.containsKey(entry)) {
                        MyUser user = users.get(entry);
                        fetchDistantionMark(myUser, user);
                    }
                }
            System.out.println("DISTANTIONS:"+saved);
            }
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
            for(DistantionMark entry: marks){
                if(entry.firstUser == myUser || entry.secondUser == myUser) {
                    entry.update();
                }
            }
        }

        @Override
        public void remove() {
            Iterator<DistantionMark> iter = marks.iterator();
            while(iter.hasNext()) {
                DistantionMark entry = iter.next();
                if(entry.firstUser == myUser || entry.secondUser == myUser) {
                    entry.line.remove();
                    entry.line = null;
                    entry.marker.remove();
                    entry.marker = null;
                    iter.remove();
                }
            }
//            myUser.getProperties().saveFor(TYPE, null);

        }

        @Override
        public boolean onEvent(String event, Object object) {
//            if(myUser.getLocation() != null) return true;
            switch(event) {
                case CREATE_CONTEXT_MENU:
                    ContextMenu contextMenu = (ContextMenu) object;

                        contextMenu.add("Show distantions").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(SHOW_DISTANTIONS);
                                return false;
                            }
                        });
                        contextMenu.add("Hide distantions").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(HIDE_DISTANTIONS);
                                return false;
                            }
                        });
                    break;
                case SHOW_DISTANTIONS:
                    State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser user) {
                            if(user != myUser) {
                                fetchDistantionMark(myUser, user);

                                ArrayList<Integer> save = new ArrayList<>();
                                for(DistantionMark entry:marks) {
                                    if(entry.firstUser == myUser || entry.secondUser == myUser) {
                                        save.add(entry.firstUser == myUser ? entry.secondUser.getProperties().getNumber() : entry.firstUser.getProperties().getNumber());
                                    }
                                }
                                if(save.size() > 0) {
                                    myUser.getProperties().saveFor(TYPE, save);
                                }
                            }
                        }
                    });


                    break;
                case HIDE_DISTANTIONS:
                    remove();
                    myUser.getProperties().saveFor(TYPE, null);
                    break;
            }
            return true;
        }
    }

    private class DistantionMark {
        Polyline line;
        Marker marker;
        MyUser firstUser;
        MyUser secondUser;
        private IconGenerator iconFactory;

        public DistantionMark(MyUser user1, MyUser user2) {
            firstUser = user1;
            secondUser = user2;

            iconFactory = new IconGenerator(State.getInstance());
            iconFactory.setColor(Color.argb(100,100,100,100));

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
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(formatNumber(distance))));
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

            new SmoothInterpolated(new SimpleCallback<Float[]>() {
                @Override
                public void call(Float[] value) {
                    if(line != null && marker != null) {
                        LatLng firstCurrent = new LatLng(
                                firstUserInitial.latitude * (1 - value[TIME_ELAPSED]) + firstPosition().latitude * value[TIME_ELAPSED],
                                firstUserInitial.longitude * (1 - value[TIME_ELAPSED]) + firstPosition().longitude * value[TIME_ELAPSED]);

                        LatLng secondCurrent = new LatLng(
                                secondUserInitial.latitude * (1 - value[TIME_ELAPSED]) + secondPosition().latitude * value[TIME_ELAPSED],
                                secondUserInitial.longitude * (1 - value[TIME_ELAPSED]) + secondPosition().longitude * value[TIME_ELAPSED]);

                        line.setPoints(Arrays.asList(firstCurrent, secondCurrent));
                        double distance = SphericalUtil.computeDistanceBetween(firstCurrent, secondCurrent);
                        LatLng markerPosition = SphericalUtil.interpolate(firstCurrent, secondCurrent, .5);

                        IconGenerator iconFactory = new IconGenerator(State.getInstance());
                        iconFactory.setColor(Color.argb(100, 100, 100, 100));
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(formatNumber(distance))));
                        marker.setPosition(markerPosition);
                    }
                }
            }).execute();
        }

        private String formatNumber(double distance) {
            String unit = "m";
            if (distance < 1) {
                distance *= 1000;
                unit = "mm";
            } else if (distance > 1000) {
                distance /= 1000;
                unit = "km";
            }
            return String.format("%4.1f%s", distance, unit);
        }

    }
    private DistantionMark fetchDistantionMark(MyUser user1, MyUser user2) {
        for(DistantionMark entry: marks) {
            if(user1 == entry.firstUser && user2 == entry.secondUser) return entry;
            if(user2 == entry.firstUser && user1 == entry.secondUser) return entry;
        }
        DistantionMark mark = new DistantionMark(user1, user2);
        marks.add(mark);
//        mark.update();
        return mark;
    }

}
