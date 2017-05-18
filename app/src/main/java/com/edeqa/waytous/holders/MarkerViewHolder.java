package com.edeqa.waytous.holders;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SmoothInterpolated;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Callable1;
import com.edeqa.waytous.interfaces.Callable2;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.edeqa.waytous.State.EVENTS.ACTIVITY_RESUME;
import static com.edeqa.waytous.State.EVENTS.CHANGE_NUMBER;
import static com.edeqa.waytous.State.EVENTS.MARKER_CLICK;
import static com.edeqa.waytous.helpers.SmoothInterpolated.CURRENT_VALUE;
import static com.edeqa.waytous.helpers.SmoothInterpolated.TIME_ELAPSED;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_NUMBER;


/**
 * Created 11/18/16.
 */
public class MarkerViewHolder extends AbstractViewHolder<MarkerViewHolder.MarkerView> {

    public static final String TYPE = "marker";

    private GoogleMap map;
    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            State.getInstance().fire(MARKER_CLICK, marker);
            return true;
        }
    };



    public MarkerViewHolder(MainActivity context) {
        super(context);

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
                        State.getInstance().getUsers().forUser(number, new Callable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(MARKER_CLICK);
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

            new SmoothInterpolated(new Callable1<Float[]>() {
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
