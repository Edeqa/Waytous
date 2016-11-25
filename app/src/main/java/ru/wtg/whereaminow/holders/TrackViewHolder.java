package ru.wtg.whereaminow.holders;

import android.location.Location;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.helpers.MyUser.HIDE_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_HIDE_ALL_TRACKS;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_HIDE_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_SHOW_ALL_TRACKS;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_SHOW_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.SHOW_TRACK;

/**
 * Created 11/24/16.
 */

public class TrackViewHolder extends AbstractViewHolder<TrackViewHolder.TrackView> {
    private static final String TYPE = "track";

    private GoogleMap map;

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
        public void onEvent(int event, Object object) {
            switch(event) {
                case MENU_ITEM_SHOW_TRACK:
                    MenuItem item = (MenuItem) object;
                    item.setVisible(!myUser.getProperties().isShowTrack());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(SHOW_TRACK);
                            return false;
                        }
                    });
                    break;
                case MENU_ITEM_HIDE_TRACK:
                    item = (MenuItem) object;
                    item.setVisible(myUser.getProperties().isShowTrack());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(HIDE_TRACK);
                            return false;
                        }
                    });
                    break;
                case MENU_ITEM_SHOW_ALL_TRACKS:
                    item = (MenuItem) object;
                    if(!myUser.getProperties().isShowTrack()) item.setVisible(true);
                    break;
                case MENU_ITEM_HIDE_ALL_TRACKS:
                    item = (MenuItem) object;
                    if(myUser.getProperties().isShowTrack()) item.setVisible(true);
                    break;
                case SHOW_TRACK:
                    myUser.getProperties().setShowTrack(true);

                    if(track != null) break;
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
