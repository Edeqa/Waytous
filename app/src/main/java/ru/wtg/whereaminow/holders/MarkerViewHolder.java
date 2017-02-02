package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SmoothInterpolated;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NUMBER;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.CURRENT_VALUE;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.TIME_ELAPSED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;

/**
 * Created 11/18/16.
 */
public class MarkerViewHolder extends AbstractViewHolder<MarkerViewHolder.MarkerView> {

    public static final String TYPE = "marker";
    static final String MARKER_CLICK = "marker_click";
    private final Context context;

    private GoogleMap map;
    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            State.getInstance().fire(MARKER_CLICK, marker);
            return true;
        }
    };



    public MarkerViewHolder(MainActivity context) {
        this.context = context;

        setMap(context.getMap());
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public MarkerView create(MyUser myUser) {
        if(myUser == null || map == null || myUser.getLocation() == null || myUser.getLocation().getProvider() == null || !myUser.isUser()) return null;
        return new MarkerView(myUser);
    }

    public MarkerViewHolder setMap(GoogleMap map) {
        this.map = map;

        map.setOnMarkerClickListener(onMarkerClickListener);
//        map.setInfoWindowAdapter(infoWindowAdapter);

        return this;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case MARKER_CLICK:
                Marker marker = (Marker) object;
                Bundle b = (Bundle) marker.getTag();
                if(b.getString(TYPE, null).equals(TYPE)){
                    if(marker.getTag() != null) {
                        int number = b.getInt(RESPONSE_NUMBER);
                        State.getInstance().getUsers().forUser(number, new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(CameraViewHolder.CAMERA_NEXT_ORIENTATION);
                            }
                        });
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public ArrayList<IntroRule> getIntro() {

//        MarkerView markerView = (MarkerView) State.getInstance().getMe().getEntity(TYPE);
//        LatLng latLng = new LatLng(markerView.myUser.getLocation().getLatitude(), markerView.myUser.getLocation().getLongitude());

        ArrayList<IntroRule> rules = new ArrayList<>();

        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("marker_intro").setViewId(R.id.map).setLinkTo(IntroRule.LINK_TO_CENTER_OF_VIEW).setTitle("Marker").setDescription("This one shows your position or positions of your friends on the map. Each marker has a different color but your color is always blue. Try to click on the marker a couple of times."));
//        rules.put(new IntroRule().setEvent(PREPARE_FAB).setId("fab_intro_menu").setView(fab_buttons).setTitle("Click any item to perform some action"));
//        rules.put(new IntroRule().setId("menu_set_name").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setTitle("Click menu"));

        return rules;
    }

/*
    private GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {
            Bundle m = (Bundle) marker.getTag();
            if(m != null) {
                MyUser user = State.getInstance().getUsers().getUsers().get(m.getInt("number"));
                return ((MarkerView)user.getEntity(TYPE)).infoWindow();
//                System.out.println("WINFOW:"+user.getProperties().getDisplayName());

            }
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            System.out.println("CONTENTS:"+marker.getTag());
            return null;
        }
    };
*/

    class MarkerView extends AbstractView {
        private Circle circle;
        private Marker marker;
//        private MyUser myUser;

        MarkerView(MyUser myUser){
//            this.myUser = myUser;

            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Bitmap bitmap = Utils.renderBitmap(context,R.drawable.navigation_marker,myUser.getProperties().getColor(),size,size);

            marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()))
                    .rotation(myUser.getLocation().getBearing())
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude())).radius(myUser.getLocation().getAccuracy())
                    .fillColor(Color.TRANSPARENT).strokeColor(myUser.getProperties().getColor()).strokeWidth(3f);
            circle = map.addCircle(circleOptions);

            Bundle b = new Bundle();
            b.putString(TYPE, TYPE);
            b.putInt(RESPONSE_NUMBER, myUser.getProperties().getNumber());
            marker.setTag(b);

        }

        @Override
        public void remove() {
            marker.remove();
            marker = null;
            circle.remove();
            circle = null;
        }

        @Override
        public boolean dependsOnLocation(){
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {

            final LatLng startPosition = marker.getPosition();
            final LatLng finalPosition = new LatLng(location.getLatitude(), location.getLongitude());

            final float startRotation = marker.getRotation();
            final float finalRotation = location.getBearing();

            final double startRadius = circle.getRadius();
            final double finalRadius = location.getAccuracy();

            new SmoothInterpolated(new SimpleCallback<Float[]>() {
                @Override
                public void call(Float[] value) {
                    final LatLng currentPosition = new LatLng(
                            startPosition.latitude*(1-value[TIME_ELAPSED])+finalPosition.latitude*value[TIME_ELAPSED],
                            startPosition.longitude*(1-value[TIME_ELAPSED])+finalPosition.longitude*value[TIME_ELAPSED]);

                    final float rot = value[CURRENT_VALUE] * finalRotation + (1 - value[CURRENT_VALUE]) * startRotation;
                    final float currentRadius = (float) (value[CURRENT_VALUE] * finalRadius + (1 - value[CURRENT_VALUE]) * startRadius);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            if(marker != null) {
                                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                                marker.setPosition(currentPosition);

                                circle.setCenter(currentPosition);
                                circle.setRadius(currentRadius);
                            }
                        }
                    });
                }
            }).execute();

        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event){
                case CHANGE_NUMBER:
                    int number = (int) object;
                    Bundle b = new Bundle();
                    b.putString(TYPE, TYPE);
                    b.putInt(RESPONSE_NUMBER, number);
                    marker.setTag(b);
                    break;
            }
            return true;
        }
    }



}
