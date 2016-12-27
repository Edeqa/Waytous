package ru.wtg.whereaminow.holders;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;

import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.SELECT_SINGLE_USER;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.TRACKING_STOPPED;
import static ru.wtg.whereaminow.holders.CameraViewHolder.UPDATE_CAMERA;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created 11/24/16.
 */

public class StreetsViewHolder extends AbstractViewHolder<StreetsViewHolder.StreetsView> implements OnStreetViewPanoramaReadyCallback {
    private static final String TYPE = "streets";

    private static final String SHOW_STREETS = "show_streets";
    private static final String HIDE_STREETS = "hide_streets";
    private final AppCompatActivity context;

    private GoogleMap map;
    private View streetViewLayout;
    private SupportStreetViewPanoramaFragment streetView;
    private StreetViewPanorama panorama;
    private StreetViewPanoramaCamera camera;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public StreetsView create(MyUser myUser) {
        if (myUser == null) return null;
        return new StreetsView(myUser);
    }

    public StreetsViewHolder(AppCompatActivity context) {
        this.context = context;
    }

    public StreetsViewHolder setMap(GoogleMap map) {
        this.map = map;
        return this;
    }

    public StreetsViewHolder setStreetViewLayout(final View streetViewLayout) {
        this.streetViewLayout = streetViewLayout;
        streetView = (SupportStreetViewPanoramaFragment) context.getSupportFragmentManager().findFragmentById(R.id.street_view);
        streetViewLayout.findViewById(R.id.ib_street_view_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                streetViewLayout.setVisibility(View.GONE);
            }
        });
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
                optionsMenu.add(Menu.NONE, R.string.show_street_view, Menu.NONE, R.string.show_street_view).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        int height = (int) (State.getInstance().getResources().getDisplayMetrics().heightPixels / 2.5);

                        streetViewLayout.getLayoutParams().height = height;
                        streetView.getStreetViewPanoramaAsync(StreetsViewHolder.this);
                        streetViewLayout.setVisibility(View.VISIBLE);
                        State.getInstance().fire(UPDATE_CAMERA);
//                        .setMinimumHeight(height);

                        return false;
                    }
                });
                optionsMenu.add(Menu.NONE, R.string.hide_street_view, Menu.NONE, R.string.hide_street_view).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        streetViewLayout.setVisibility(View.GONE);
                        State.getInstance().fire(UPDATE_CAMERA);
                        return false;
                    }
                }).setVisible(false);
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                if(State.getInstance().getUsers().getCountAllSelected() != 1) {
                    optionsMenu.findItem(R.string.show_street_view).setVisible(false);
                    optionsMenu.findItem(R.string.hide_street_view).setVisible(false);
                } else if(streetViewLayout.getVisibility() == View.GONE) {
                    optionsMenu.findItem(R.string.show_street_view).setVisible(true);
                    optionsMenu.findItem(R.string.hide_street_view).setVisible(false);
                } else {
                    optionsMenu.findItem(R.string.show_street_view).setVisible(false);
                    optionsMenu.findItem(R.string.hide_street_view).setVisible(true);
                }
                break;
            case TRACKING_STOPPED:
                streetViewLayout.setVisibility(View.GONE);
                break;
        }
        return true;
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        panorama = streetViewPanorama;

        panorama.setOnStreetViewPanoramaChangeListener(new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
            @Override
            public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                if (streetViewPanoramaLocation != null && streetViewPanoramaLocation.links != null) {
                    panorama.animateTo(camera, LOCATION_UPDATES_DELAY);
                } else {
                    System.out.println("STREETVIEW NOT AVAILABLE");
                }
            }
        });

        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                if(myUser.getProperties().isSelected()) {
                    myUser.getEntity(StreetsViewHolder.TYPE).onChangeLocation(myUser.getLocation());
                }
            }
        });
//        findViewById(R.id.streetViewLayout).setVisibility(View.VISIBLE);
//        this.panorama = panorama;

//        if(state.getScreenMode() == CurrentState.SCREEN_SPLIT_STREET_VIEW_FRIEND){
//            findViewById(R.id.mapViewLayout).setVisibility(View.VISIBLE);
//        }
//
//        panorama.setPosition(state.friend.getPosition().getLatLng());
    }

    class StreetsView extends AbstractView {

        StreetsView(MyUser myUser){
            this.myUser = myUser;
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
            if(State.getInstance().getUsers().getCountAllSelected() != 1) {
                streetViewLayout.setVisibility(View.GONE);
                return;
            }
            if(!myUser.getProperties().isSelected()) return;
            if(panorama == null || streetViewLayout.getVisibility() != View.VISIBLE) return;

            camera = new StreetViewPanoramaCamera.Builder().bearing(location.getBearing()).build();
            panorama.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        }

        @Override
        public void remove() {
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(myUser.getLocation() == null) return true;
            if(panorama == null) return true;

            switch(event) {
                case SELECT_SINGLE_USER:
                    if(State.getInstance().getUsers().getCountAllSelected() == 1) {
                        onChangeLocation(myUser.getLocation());
                    }
                    break;
                case SELECT_USER:
                    if(State.getInstance().getUsers().getCountAllSelected() > 1) {
                        streetViewLayout.setVisibility(View.GONE);
                    } else if(State.getInstance().getUsers().getCountAllSelected() == 1) {
                        onChangeLocation(myUser.getLocation());
                    }
                    break;
            }
            return true;
        }

    }
}
