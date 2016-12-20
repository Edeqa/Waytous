package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SmoothInterpolated;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.CURRENT_VALUE;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.TIME_ELAPSED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;

/**
 * Created 11/18/16.
 */
public class MarkerViewHolder extends AbstractViewHolder<MarkerViewHolder.MarkerView> {

    public static final String MARKER_CLICK = "marker_click";

    public static final String TYPE = "marker";

    private final Context context;

    private GoogleMap map;

    public MarkerViewHolder(Context context) {
        this.context = context;
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

    class MarkerView extends AbstractView {
        private Marker marker;
        private MyUser myUser;

        MarkerView(MyUser myUser){
            this.myUser = myUser;

            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Bitmap bitmap = Utils.renderBitmap(context,R.drawable.navigation_marker,myUser.getProperties().getColor(),size,size);

            marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()))
                    .rotation(myUser.getLocation().getBearing())
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

            Bundle b = new Bundle();
            b.putString(TYPE, TYPE);
            b.putInt(RESPONSE_NUMBER, myUser.getProperties().getNumber());
            marker.setTag(b);

        }

        @Override
        public void remove() {
            marker.remove();
            marker = null;
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

            new SmoothInterpolated(new SimpleCallback<Float[]>() {
                        @Override
                        public void call(Float[] value) {
                            LatLng currentPosition = new LatLng(
                                    startPosition.latitude*(1-value[TIME_ELAPSED])+finalPosition.latitude*value[TIME_ELAPSED],
                                    startPosition.longitude*(1-value[TIME_ELAPSED])+finalPosition.longitude*value[TIME_ELAPSED]);

                            float rot = value[CURRENT_VALUE] * finalRotation + (1 - value[CURRENT_VALUE]) * startRotation;

                            if(marker != null) {
                                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                                marker.setPosition(currentPosition);
                            }
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

    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            State.getInstance().fire(MARKER_CLICK, marker);
            return true;
        }
    };

}
