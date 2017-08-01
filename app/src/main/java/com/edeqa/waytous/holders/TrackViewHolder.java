package com.edeqa.waytous.holders;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SmoothInterpolated;
import com.edeqa.waytous.interfaces.Runnable1;
import com.edeqa.waytous.interfaces.Runnable2;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.SmoothInterpolated.TIME_ELAPSED;


/**
 * Created 11/24/16.
 */

public class TrackViewHolder extends AbstractViewHolder<TrackViewHolder.TrackView> {
    private static final String TYPE = "track";

    private static final String SHOW_TRACK = "show_track";
    private static final String HIDE_TRACK = "hide_track";

    private GoogleMap map;

    public TrackViewHolder(MainActivity context) {
        super(context);
        setMap(context.getMap());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public TrackView create(MyUser myUser) {
        if (myUser == null) return null;
        return new TrackView(myUser);
    }

    public TrackViewHolder setMap(GoogleMap map) {
        this.map = map;
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

                optionsMenu.add(Menu.NONE, R.string.show_tracks, Menu.NONE, R.string.show_tracks).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(SHOW_TRACK);
                            }
                        });
                        return false;
                    }
                });

                optionsMenu.add(Menu.NONE, R.string.hide_tracks, Menu.NONE, R.string.hide_tracks).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(HIDE_TRACK);
                            }
                        });
                        return false;
                    }
                });
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                final MenuItem menuItemShowTracks = optionsMenu.findItem(R.string.show_tracks);
                menuItemShowTracks.setVisible(false);
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        TrackView trackView = ((TrackView) myUser.getView(TYPE));
                        if(trackView != null && !trackView.showtrack) {
                            menuItemShowTracks.setVisible(true);
                        }
                    }
                });
                final MenuItem menuItemHideTracks = optionsMenu.findItem(R.string.hide_tracks);
                menuItemHideTracks.setVisible(false);
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        TrackView trackView = ((TrackView) myUser.getView(TYPE));
                        if(trackView != null && trackView.showtrack) {
                            menuItemHideTracks.setVisible(true);
                        }
                    }
                });

                break;
        }
        return true;
    }

    class TrackView extends AbstractView {
        private Polyline track;
        private List<LatLng> points;
        boolean showtrack;
        private int counter;

        TrackView(MyUser myUser){
            super(myUser);
            Boolean props = (Boolean) myUser.getProperties().loadFor(TYPE);
            showtrack = !(props == null || !props);
            if(showtrack){
                onEvent(SHOW_TRACK, null);
            }
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
            if(!showtrack) return;
            if(track != null && points != null && location != null){
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {

                        if(myUser.getLocations().size()>1) {
                            if(myUser.getLocations().size() < counter) {
                                points = getTrail();
                            }
                            counter = myUser.getLocations().size();

                            final Location startPosition = myUser.getLocations().get(myUser.getLocations().size() - 2);
                            new SmoothInterpolated(new Runnable1<Float[]>() {
                                @Override
                                public void call(Float[] value) {
                                    LatLng current = new LatLng(
                                            startPosition.getLatitude()*(1-value[TIME_ELAPSED])+location.getLatitude()*value[TIME_ELAPSED],
                                            startPosition.getLongitude()*(1-value[TIME_ELAPSED])+location.getLongitude()*value[TIME_ELAPSED]);

                                    points.add(current);
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        public void run() {
                                            try {
                                                if (track != null)
                                                    track.setPoints(points);
                                            } catch(NullPointerException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }).execute();
                        }
//                    }
//                }).start();

            }
        }

        @Override
        public void remove() {
            if(track != null){
                track.remove();
                track = null;
            }
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(myUser.getLocation() == null || !myUser.isUser()) return true;
            switch(event) {
                case CREATE_CONTEXT_MENU:
                    Menu menu = (Menu) object;
                    if(!showtrack) {
                        menu.add(0, R.string.show_track, Menu.NONE, R.string.show_track).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(SHOW_TRACK);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_title_black_24px);
                    } else {
                        menu.add(0, R.string.hide_track, Menu.NONE, R.string.hide_track).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(HIDE_TRACK);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_format_strikethrough_black_24dp);
                    }
                    break;
                case SHOW_TRACK:
                    showtrack = true;
                    myUser.getProperties().saveFor(TYPE, showtrack);

                    if(track != null) break;
                    try {
                        float density = State.getInstance().getResources().getDisplayMetrics().density;
                        float width = (int) (10 * density);

                        int color = (myUser.getProperties().getColor() & 0x00FFFFFF) | 0x77000000;

                        track = map.addPolyline(new PolylineOptions().width(width).color(color).geodesic(true).zIndex(100f));
                        points = getTrail();
                        track.setPoints(points);

                        onChangeLocation(myUser.getLocation());
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case HIDE_TRACK:
                    showtrack = false;
                    myUser.getProperties().saveFor(TYPE, showtrack);

                    remove();
                    break;
            }
            return true;
        }

        List<LatLng> getTrail(){
            List<LatLng> points = new ArrayList<>();
            for(Location location: myUser.getLocations()){
                try {
                    points.add(new LatLng(location.getLatitude(), location.getLongitude()));
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            points.remove(points.size()-1);
            return points;
        }
    }
}
