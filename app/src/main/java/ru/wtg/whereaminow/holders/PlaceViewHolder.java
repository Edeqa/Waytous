package ru.wtg.whereaminow.holders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.abstracts.AbstractView;
import ru.wtg.whereaminow.abstracts.AbstractViewHolder;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_RESULT;
import static ru.wtg.whereaminow.State.EVENTS.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.EVENTS.MAKE_ACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.MAKE_INACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.EVENTS.SELECT_USER;
import static ru.wtg.whereaminow.State.EVENTS.UNSELECT_USER;
import static ru.wtg.whereaminow.holders.CameraViewHolder.UPDATE_CAMERA;
import static ru.wtg.whereaminow.holders.NavigationViewHolder.HIDE_NAVIGATION;
import static ru.wtg.whereaminow.holders.NavigationViewHolder.SHOW_NAVIGATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TIMESTAMP;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ADDRESS;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LATITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LONGITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

/**
 * Created 2/4/17.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class PlaceViewHolder extends AbstractViewHolder<PlaceViewHolder.PlaceView> implements Serializable, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    transient static final long serialVersionUID = -6395904747342820059L;

    private transient static final String SHOW_PLACE = "show_place";
    private transient static final String HIDE_PLACE = "hide_place";

    transient private static final String TYPE = "place";

    transient private static final int REQUEST_CODE_AUTOCOMPLETE_PLACE = 3;

    transient private GoogleMap map;
    transient private GoogleApiClient mGoogleApiClient;

    private ArrayList<Map<String,Serializable>> places = new ArrayList<>();

    public PlaceViewHolder(MainActivity context) {
        super(context);

        setMap(context.getMap());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PlaceView create(MyUser myUser) {
        if (myUser == null) return null;
        return new PlaceView(myUser);
    }

    public PlaceViewHolder setMap(GoogleMap map) {
        this.map = map;

        PlaceViewHolder m = (PlaceViewHolder) State.getInstance().getPropertiesHolder().loadFor(TYPE);
        if(m != null) {
            places = m.places;
            if(places != null) {
                for (Map<String, Serializable> x : places) {
                    try {
                        State.getInstance().fire(SHOW_PLACE, x);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return this;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.hide_places, Menu.NONE, R.string.hide_places).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(HIDE_PLACE);
                            }
                        });
                        return false;
                    }
                });
                setupToolbar();
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                final MenuItem menuItemHideNavigations = optionsMenu.findItem(R.string.hide_places);
                menuItemHideNavigations.setVisible(false);
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        PlaceView view = ((PlaceView) myUser.getEntity(TYPE));
                        /*if(view != null && view.showNavigation) {
                            menuItemHideNavigations.setVisible(true);
                        }*/
                    }
                });
                break;
            case SHOW_PLACE:
                Map map = (Map) object;
                try {
                    JSONObject o = new JSONObject(map);
                    MyUser x = State.getInstance().getUsers().addUser(o);
                    x.fire(MAKE_ACTIVE);

                    x.createViews();
                    State.getInstance().fire(USER_JOINED, x);
                    State.getInstance().getUsers().forUser(x.getProperties().getNumber(),new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.createViews();
                            myUser.fire(SELECT_USER);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case ACTIVITY_RESUME:
                if(mGoogleApiClient != null){
                    mGoogleApiClient.connect();
                }
                break;
            case ACTIVITY_PAUSE:
                if(mGoogleApiClient != null) {
                    mGoogleApiClient.disconnect();
                }
                break;
            case ACTIVITY_RESULT:
                Bundle m = (Bundle) object;
                if(m != null){
                    int requestCode = m.getInt("requestCode");
                    if(requestCode == REQUEST_CODE_AUTOCOMPLETE_PLACE) {
                        int resultCode = m.getInt("resultCode");
                        Intent data = m.getParcelable("data");

                        Place place = PlaceAutocomplete.getPlace(context, data);
                        if(place != null) {
                            Map<String, Serializable> mm = new HashMap<>();

                            mm.put(USER_PROVIDER, TYPE);
                            mm.put(USER_LATITUDE, place.getLatLng().latitude);
                            mm.put(USER_LONGITUDE, place.getLatLng().longitude);
                            mm.put(USER_NUMBER, REQUEST_CODE_AUTOCOMPLETE_PLACE * 10000 + places.size());
                            mm.put(USER_COLOR, Color.rgb(0, 200, 200));
                            mm.put(USER_NAME, place.getName().subSequence(0, place.getName().toString().length() > 9 ? 9 : place.getName().toString().length()).toString());
                            mm.put("id", place.getId());
                            mm.put(USER_ADDRESS, place.getAddress().toString());
                            mm.put(REQUEST_TIMESTAMP, new Date().getTime());

                            for (Map<String, Serializable> x : places) {
                                if (x.containsKey("id") && place.getId().equals(x.get("id"))) {
                                    int number = (int) x.get("number");

                                    State.getInstance().getUsers().forUser(number, new MyUsers.Callback() {
                                        @Override
                                        public void call(Integer number, MyUser myUser) {
                                            myUser.fire(MAKE_ACTIVE);
                                            myUser.fire(SELECT_USER);
                                            myUser.fire(SHOW_NAVIGATION);
                                        }
                                    });
                                    return true;
                                }
                            }

                            places.add(mm);
                            State.getInstance().getPropertiesHolder().saveFor(TYPE, this);

                            State.getInstance().fire(SHOW_PLACE, mm);
                        }
                    }
                }
                break;
        }
        return true;
    }

    class PlaceView extends AbstractView {
        private Marker marker;

        PlaceView(MyUser myUser){
            this.myUser = myUser;

            Boolean props = (Boolean) myUser.getProperties().loadFor(TYPE);

            if(isPlace(myUser)){
                createMarker();
            }
        }

        private void createMarker() {
            myUser.getProperties().setButtonView(R.layout.view_navigation_button);

            int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
            Bitmap bitmap = Utils.renderBitmap(context,R.drawable.ic_place_black_24dp,myUser.getProperties().getColor(),size,size);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()))
                    .rotation(myUser.getLocation().getBearing())
                    .anchor(0.5f, 1.0f)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap));

            marker = map.addMarker(markerOptions);
            marker.setAlpha(.5F);

            Bundle b = new Bundle();
            b.putString(MarkerViewHolder.TYPE, TYPE);
            b.putInt(RESPONSE_NUMBER, myUser.getProperties().getNumber());
            marker.setTag(b);
            myUser.fire(SHOW_NAVIGATION);
        }


        @Override
        public boolean dependsOnLocation() {
            return false;
        }

        @Override
        public void remove() {
//            System.out.println("REMOVENAVVIEW:"+myUser.getProperties().getDisplayName()+":"+track);
            if(marker != null){
                marker.remove();
                marker = null;
            }
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(myUser.getLocation() == null) return true;
            switch(event) {
                case CREATE_CONTEXT_MENU:
                    Menu menu = (Menu) object;
                    if(isPlace(myUser)) {
                        menu.add(0, R.string.hide, Menu.NONE, R.string.hide).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(HIDE_PLACE, myUser);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_location_off_black_24dp);
                    }
                    break;
                case HIDE_PLACE:
                    myUser.removeViews();
                    myUser.fire(HIDE_NAVIGATION);
                    myUser.fire(MAKE_INACTIVE);
                    myUser.fire(UNSELECT_USER);

//                    State.getInstance().fire(USER_DISMISSED, myUser);
//                    myUser.fire(USER_DISMISSED);
                    State.getInstance().getUsers().getUsers().remove(myUser.getProperties().getNumber());
                    State.getInstance().fire(UPDATE_CAMERA);

                    for(int i=0;i<places.size();i++){
                        if(((int) places.get(i).get("number")) == myUser.getProperties().getNumber()) {
                            places.remove(i);
                            break;
                        }
                    }
                    State.getInstance().getPropertiesHolder().saveFor(TYPE, PlaceViewHolder.this);
                    break;
            }
            return true;
        }
    }

    private boolean isPlace(MyUser user) {
        return user != null && user.getLocation() != null && user.getLocation().getProvider().equals(TYPE);
    }


    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(context, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


/*        final MenuItem searchItem = toolbar.getMenu().add(Menu.NONE, R.string.search, Menu.NONE, R.string.search);
        searchItem.setVisible(false);

        final SearchView searchView = new SearchView(context);
        searchItem.setActionView(searchView);
        searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        final SimpleCallback<String> setFilter = new SimpleCallback<String>() {
            @Override
            public void call(String text) {
                System.out.println("FILTER:"+text);
            }
        };
        searchView.setQueryHint("Address or place");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchItem.collapseActionView();
                setFilter.call(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                setFilter.call(s);
                return false;
            }
        });*/

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(context);
                    context.startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_PLACE);
                } catch (GooglePlayServicesRepairableException e) {
                    GoogleApiAvailability.getInstance().getErrorDialog(context, e.getConnectionStatusCode(), 0 /* requestCode */).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    String message = "Google Play Services is not available: " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode);
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

//                searchItem.expandActionView();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("onConnected:"+bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("onConnectionSuspended:"+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("onConnectionFailed:"+connectionResult);
    }

}
