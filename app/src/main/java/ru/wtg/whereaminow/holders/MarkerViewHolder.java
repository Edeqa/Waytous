package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.text.style.TtsSpan;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created 11/18/16.
 */
public class MarkerViewHolder extends AbstractViewHolder<MarkerViewHolder.MarkerView> {
    private static final String TYPE = "marker";
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
    public String[] getOwnEvents() {
        return new String[0];
    }

    @Override
    public MarkerView create(MyUser myUser) {
        if(myUser == null || myUser.getLocation() == null || map == null) return null;
        int number = myUser.getProperties().getNumber();
        MarkerView markerView = this.new MarkerView(myUser);
        Bitmap bitmap;
        int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
//        if(isDraft()) {
//            bitmap = Utils.renderBitmap(context, R.drawable.navigation_marker,Color.GRAY,size,size);
//        } else {
            bitmap = Utils.renderBitmap(context,R.drawable.navigation_marker,myUser.getProperties().getColor(),size,size);
//        }
        Marker marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()))
                .rotation(myUser.getLocation().getBearing())
                .anchor(0.5f, 0.5f)
                .flat(true)
//                .title("Melbourne")
//                .snippet("Population: 4,137,400")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
//        marker.showInfoWindow();
//        }
        markerView.setMarker(marker);
        markerView.setNumber(number);
        return markerView;
    }

    public MarkerViewHolder setMap(GoogleMap map) {
        this.map = map;
        return this;
    }

    class MarkerView extends AbstractView {
        private Marker marker;
//        private int number;
        private MyUser myUser;

        MarkerView(MyUser myUser){
            this.myUser = myUser;
        }

        void setMarker(Marker marker){
            this.marker = marker;
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

            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final Interpolator interpolator = new AccelerateDecelerateInterpolator();
            final float durationInMs = LOCATION_UPDATES_DELAY;
            handler.post(new Runnable() {
                long elapsed;
                float t, v;

                @Override
                public void run() {
//                if(marker == null) return;
                    elapsed = SystemClock.uptimeMillis() - start;
                    t = elapsed / durationInMs;
                    v = interpolator.getInterpolation(t);

                    LatLng currentPosition = new LatLng(
                            startPosition.latitude*(1-t)+finalPosition.latitude*t,
                            startPosition.longitude*(1-t)+finalPosition.longitude*t);

                    float rot = v * finalRotation + (1 - v) * startRotation;

                    if(marker != null) {
                        marker.setRotation(-rot > 180 ? rot / 2 : rot);
                        marker.setPosition(currentPosition);
                    }

                    if (t < 1) {
                        handler.postDelayed(this, 16);
                    }

                }
            });

        }

        public void setNumber(int number) {
//            this.number = number;
            marker.setTag(number);
        }

//        public int getNumber() {
//            return number;
//        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event){
                case CHANGE_NUMBER:
                    int number = (int) object;
                    setNumber(number);
                    break;
            }
            return true;
        }
    }

}
