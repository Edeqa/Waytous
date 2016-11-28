package ru.wtg.whereaminow.holders;

import android.location.Location;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;

import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;

/**
 * Created 11/24/16.
 */

public class TrackViewHolder extends AbstractViewHolder<TrackViewHolder.TrackView> {
    private static final String TYPE = "track";

    public static final String SHOW_TRACK = "show_track";
    public static final String HIDE_TRACK = "hide_track";

    private GoogleMap map;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String[] getOwnEvents() {
        return new String[]{SHOW_TRACK,HIDE_TRACK};
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

    class TrackView extends AbstractView {
        private Polyline track;
        private List<LatLng> points;

        TrackView(MyUser myUser){
            this.myUser = myUser;
            if(myUser.getProperties().isShowTrack()){
                myUser.fire(SHOW_TRACK);
            }
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            if(!myUser.getProperties().isShowTrack()) return;
            if(track != null && points != null && location != null){
                points.add(new LatLng(location.getLatitude(),location.getLongitude()));
                track.setPoints(points);
            }
        }

        @Override
        public void remove() {
            if(track != null) {
                track.remove();
                points = null;
                track = null;
            }
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event) {
                case CREATE_OPTIONS_MENU:
                    Menu optionsMenu = (Menu) object;

                    final MenuItem item1 = optionsMenu.findItem(R.id.action_show_tracks);
                    item1.setVisible(false);
                    State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            if(!myUser.getProperties().isShowTrack()) item1.setVisible(true);
                        }
                    });
                    item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.fire(SHOW_TRACK);
                                }
                            });
                            return false;
                        }
                    });

                    final MenuItem item2 = optionsMenu.findItem(R.id.action_hide_tracks);
                    item2.setVisible(false);
                    State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            if(myUser.getProperties().isShowTrack()) item2.setVisible(true);
                        }
                    });
                    item2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.fire(HIDE_TRACK);
                                }
                            });
                            return false;
                        }
                    });
                    break;
                case CREATE_CONTEXT_MENU:
                    ContextMenu contextMenu = (ContextMenu) object;

                    MenuItem item = contextMenu.findItem(R.id.action_show_track);
                    item.setVisible(!myUser.getProperties().isShowTrack());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(SHOW_TRACK);
                            return false;
                        }
                    });

                    item = contextMenu.findItem(R.id.action_hide_track);
                    item.setVisible(myUser.getProperties().isShowTrack());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(HIDE_TRACK);
                            return false;
                        }
                    });
                    break;
                case SHOW_TRACK:
                    if(track != null) break;
                    myUser.getProperties().setShowTrack(true);
                    track = map.addPolyline(new PolylineOptions().width(10).color(myUser.getProperties().getColor()).geodesic(true).zIndex(100f));
                    points = getTrail();
                    track.setPoints(points);

                    onChangeLocation(myUser.getLocation());
                    break;
                case HIDE_TRACK:
                    myUser.getProperties().setShowTrack(false);
                    remove();
                    break;
            }
            return true;
        }

        List<LatLng> getTrail(){
            List<LatLng> points = new ArrayList<>();
            for(Location location: myUser.getLocations()){
                points.add(new LatLng(location.getLatitude(),location.getLongitude()));
            }
            return points;
        }
    }
}
