package com.edeqa.waytous.holders.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;
import com.edeqa.waytous.helpers.SmoothInterpolated;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Runnable1;
import com.edeqa.waytous.interfaces.Runnable2;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NUMBER;
import static com.edeqa.waytous.helpers.Events.MARKER_CLICK;
import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.SmoothInterpolated.CURRENT_VALUE;
import static com.edeqa.waytous.helpers.SmoothInterpolated.TIME_ELAPSED;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.CREATE_SETTINGS;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_NUMBER;


/**
 * Created 11/18/16.
 */
@SuppressWarnings("WeakerAccess")
public class MarkerViewHolder extends AbstractViewHolder<MarkerViewHolder.MarkerView> {

    public static final String TYPE = "marker"; //NON-NLS

    public static final String PREFERENCE_MARKER_ACCURACY = "marker_accuracy"; //NON-NLS

    private boolean showAccuracy;

    private GoogleMap map;

    @Override
    public String getType() {
        return TYPE;
    }

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
        showAccuracy = State.getInstance().getSharedPreferences().getBoolean(PREFERENCE_MARKER_ACCURACY, false);
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
                if(b.getString(getType(), null).equals(getType())){
                    if(marker.getTag() != null) {
                        int number = b.getInt(RESPONSE_NUMBER);
                        State.getInstance().getUsers().forUser(number, new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                if(myUser.getProperties().isSelected() && State.getInstance().getUsers().getCountSelectedTotal() == 1) {
                                    myUser.fire(MARKER_CLICK);
                                } else {
                                    myUser.fire(SELECT_SINGLE_USER);
                                }
                            }
                        });
                    }
                }
                break;
            case CREATE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;
                item.add(new SettingItem.Group(getType()).setTitle(context.getString(R.string.marker)));
                item.add(new SettingItem.Checkbox(PREFERENCE_MARKER_ACCURACY).setTitle(context.getString(R.string.accuracy_circle)).setGroupId(getType()).setMessage(context.getString(R.string.shows_accuracy_circle_around_the_marker_on_map)).setCallback(new Runnable1<Boolean>() {
                    @Override
                    public void call(Boolean arg) {
                        showAccuracy = arg;
                        State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer arg1, MyUser user) {
                                user.removeViews();
                                user.createViews();
                            }
                        });
                    }
                }));
                break;
        }
        return true;
    }

    @Override
    public ArrayList<IntroRule> getIntro() {

//        MarkerView markerView = (MarkerView) State.getInstance().getMe().getEntity(TYPE);
//        LatLng latLng = new LatLng(markerView.myUser.getLocation().getLatitude(), markerView.myUser.getLocation().getLongitude());

        ArrayList<IntroRule> rules = new ArrayList<>();

        //noinspection HardCodedStringLiteral
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

        MarkerView(MyUser myUser){
            super(MarkerViewHolder.this.context, myUser);

            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Bitmap bitmap = Utils.renderBitmap(context,R.drawable.navigation_marker,myUser.getProperties().getColor(),size,size);

            marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()))
                    .rotation(myUser.getLocation().getBearing())
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

            if(showAccuracy) {
                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude())).radius(myUser.getLocation().getAccuracy())
                        .fillColor(Color.TRANSPARENT).strokeColor(myUser.getProperties().getColor()).strokeWidth(3f);
                circle = map.addCircle(circleOptions);
            }

            Bundle b = new Bundle();
            b.putString(MarkerViewHolder.this.getType(), MarkerViewHolder.this.getType());
            b.putInt(RESPONSE_NUMBER, myUser.getProperties().getNumber());
            marker.setTag(b);

        }

        @Override
        public void remove() {
            marker.remove();
            marker = null;
            if(circle != null) {
                circle.remove();
                circle = null;
            }
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

            final double[] circleValues = new double[3];
            final double finalRadius;
            if(circle != null) {
                circleValues[0] = circle.getRadius(); // startradius
                circleValues[1] = location.getAccuracy(); //finalradius
            }

            new SmoothInterpolated(new Runnable1<Float[]>() {
                @Override
                public void call(Float[] value) {
                    final LatLng currentPosition = new LatLng(
                            startPosition.latitude*(1-value[TIME_ELAPSED])+finalPosition.latitude*value[TIME_ELAPSED],
                            startPosition.longitude*(1-value[TIME_ELAPSED])+finalPosition.longitude*value[TIME_ELAPSED]);

                    final float rot = value[CURRENT_VALUE] * finalRotation + (1 - value[CURRENT_VALUE]) * startRotation;

                    if(circle != null) {
                        circleValues[2] = (float) (value[CURRENT_VALUE] * circleValues[1] + (1 - value[CURRENT_VALUE]) * circleValues[0]);
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            if(marker != null) {
                                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                                marker.setPosition(currentPosition);

                                if(circle != null) {
                                    circle.setCenter(currentPosition);
                                    circle.setRadius(circleValues[2]);
                                }
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
                    b.putString(MarkerViewHolder.this.getType(), MarkerViewHolder.this.getType());
                    b.putInt(RESPONSE_NUMBER, number);
                    marker.setTag(b);
                    break;
            }
            return true;
        }
    }



}
