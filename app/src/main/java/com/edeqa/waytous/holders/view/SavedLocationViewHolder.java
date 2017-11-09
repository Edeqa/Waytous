package com.edeqa.waytous.holders.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.EditText;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Callable1;
import com.edeqa.helpers.interfaces.Callable2;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.helpers.interfaces.Runnable3;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.CustomListDialog;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SavedLocation;
import com.edeqa.waytous.helpers.ShareSender;
import com.edeqa.waytous.helpers.SyncFB;
import com.edeqa.waytous.helpers.SystemMessage;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Sync;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_DELIVERY_CONFIRMATION;
import static com.edeqa.waytous.Constants.REQUEST_KEY;
import static com.edeqa.waytous.Constants.REQUEST_PUSH;
import static com.edeqa.waytous.Constants.REQUEST_SAVED_LOCATION;
import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.RESPONSE_PRIVATE;
import static com.edeqa.waytous.Constants.USER_ACCURACY;
import static com.edeqa.waytous.Constants.USER_ADDRESS;
import static com.edeqa.waytous.Constants.USER_ALTITUDE;
import static com.edeqa.waytous.Constants.USER_BEARING;
import static com.edeqa.waytous.Constants.USER_COLOR;
import static com.edeqa.waytous.Constants.USER_DESCRIPTION;
import static com.edeqa.waytous.Constants.USER_DISMISSED;
import static com.edeqa.waytous.Constants.USER_JOINED;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_NAME;
import static com.edeqa.waytous.Constants.USER_NUMBER;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Constants.USER_SPEED;
import static com.edeqa.waytous.Firebase.KEYS;
import static com.edeqa.waytous.Firebase.SYNCED;
import static com.edeqa.waytous.Firebase.TIMESTAMP;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NUMBER;
import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.DROPPED_TO_USER;
import static com.edeqa.waytous.helpers.Events.MAKE_ACTIVE;
import static com.edeqa.waytous.helpers.Events.MAKE_INACTIVE;
import static com.edeqa.waytous.helpers.Events.MARKER_CLICK;
import static com.edeqa.waytous.helpers.Events.PREPARE_DRAWER;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.SYNC_PROFILE;
import static junit.framework.Assert.assertTrue;


/**
 * Created 11/27/16.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SavedLocationViewHolder extends AbstractViewHolder<SavedLocationViewHolder.SavedLocationView> {

    public static final String TYPE = REQUEST_SAVED_LOCATION;

    private static final String SAVE_LOCATION = "save_location"; //NON-NLS
    private static final String SHOW_SAVED_LOCATION = "show_saved_location"; //NON-NLS
    private static final String HIDE_SAVED_LOCATION = "hide_saved_location"; //NON-NLS
    private static final String DELETE_SAVED_LOCATION = "delete_saved_location"; //NON-NLS
    private static final String SHOW_SAVED_LOCATIONS = "show_saved_locations"; //NON-NLS
    private static final String SHARE_SAVED_LOCATION = "share_saved_locations"; //NON-NLS
    private static final String SEND_SAVED_LOCATION = "send_saved_locations"; //NON-NLS

    private GoogleMap map;
    private SavedLocation.SavedLocationsAdapter adapter;
    private Toolbar toolbar;
    private CustomListDialog dialog;
    private String filterMessage;

    public SavedLocationViewHolder(MainActivity context) {
        super(context);
        SavedLocation.init(context);
        setMap(context.getMap());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public SavedLocationView create(MyUser myUser) {
        if(myUser == null || myUser.getLocation() == null) return null;
//        if(!myUser.getLocation().getProvider().equals(TYPE)) return null;
        return new SavedLocationView(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean isSaveable() {
        return true;
    }

    @Override
    public boolean isEraseable() {
        return false;
    }

    @Override
    public void perform(JSONObject o) throws JSONException {

        String address = null;
        String name = null;
        String description = null;
        String key = null;
        final Float lat, lng;
        final int number;
        number = o.getInt(USER_NUMBER);
        lat = (float) o.getDouble(USER_LATITUDE);
        lng = (float) o.getDouble(USER_LONGITUDE);
        if(o.has(USER_NAME)) name = o.getString(USER_NAME);
        if(o.has(USER_ADDRESS)) address = o.getString(USER_ADDRESS);
        if(o.has(USER_DESCRIPTION)) description = o.getString(USER_DESCRIPTION);

        if(o.has(REQUEST_KEY)){
            key = o.getString(REQUEST_KEY);
            if(SavedLocation.getItemByFieldValue(REQUEST_KEY, key) != null) return;
        }

        final String finalAddress = address;
        final String finalDescription = description;
        final String finalName = name;
        final String finalKey = key;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                final AlertDialog dialog = new AlertDialog.Builder(context).create();
                dialog.setTitle(context.getString(R.string.add_location));
                dialog.setMessage(context.getString(R.string.you_have_got_the_location_from_s, State.getInstance().getUsers().getUsers().get(number).getProperties().getDisplayName(), finalName == null ? "" : finalName, finalAddress == null ? "" : ", address: " + finalAddress, finalDescription == null ? "" : ", description: " + finalDescription));

                dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final SavedLocation loc = new SavedLocation(context);
                        loc.setLatitude(lat);
                        loc.setLatitude(lat);
                        loc.setLongitude(lng);
                        loc.setTimestamp(new Date().getTime());
                        loc.setUsername(finalName);
                        loc.setAddress(finalAddress);
                        loc.setKey(finalKey);
                        loc.setSynced(new Date().getTime());
                        loc.save(context);

                        reloadCursor();
                        //noinspection unchecked
                        new SystemMessage(context).setText(context.getString(R.string.location_saved)).setAction(context.getString(R.string.show), new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_SAVED_LOCATION, loc);
                            }
                        }).setOnClickListener(new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_SAVED_LOCATIONS);
                            }
                        }).showSnack();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final SavedLocation loc = new SavedLocation(context);
                        loc.setKey(finalKey);
                        loc.setDeleted(true);
                        loc.setSynced(new Date().getTime());
                        loc.save(context);
                    }
                });
                dialog.show();
            }
        });

        Utils.log(SavedLocationViewHolder.this, "perform:", "Saved="+o.toString()); //NON-NLS

    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.save_location, Menu.NONE, R.string.save_location).setVisible(false).setOnMenuItemClickListener(onMenuItemClickListener);
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.save_location).setVisible(State.getInstance().getUsers().getCountSelected() == 1);
                break;
            case CREATE_DRAWER:
                DrawerViewHolder.ItemsHolder adder = (DrawerViewHolder.ItemsHolder) object;
                adder.add(R.id.drawer_section_navigation, R.string.locations, R.string.locations, R.drawable.ic_pin_drop_black_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                showLocations();
                                return false;
                            }
                        }).setVisible(false);
                break;
            case PREPARE_DRAWER:
                adder = (DrawerViewHolder.ItemsHolder) object;
                SavedLocation.getDb().removeRestriction("search");
                int count = SavedLocation.getCount();
                adder.findItem(R.string.locations).setVisible(count > 0);
                break;
            case MARKER_CLICK:
                final Marker marker = (Marker) object;
                Bundle b = (Bundle) marker.getTag();
                if(b != null && b.getString(MarkerViewHolder.TYPE, null).equals(TYPE)){
                    if(marker.getTag() != null) {
                        int number = b.getInt(RESPONSE_NUMBER);
                        if(number > 10000) {
                            State.getInstance().getUsers().forUser(number, new Runnable2<Integer, MyUser>() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.fire(MARKER_CLICK, marker);
                                }
                            });
                        }
                    }
                }
                break;
            case SHOW_SAVED_LOCATION:
                final SavedLocation savedLocation = (SavedLocation) object;
                if(savedLocation != null) {
                    try {
                        JSONObject o = new JSONObject();
                        o.put(USER_PROVIDER, TYPE);
                        o.put(USER_LATITUDE, savedLocation.getLatitude());
                        o.put(USER_LONGITUDE, savedLocation.getLongitude());
                        o.put(USER_ALTITUDE, 100);
                        o.put(USER_ACCURACY, 1);
                        o.put(USER_BEARING, 0);
                        o.put(USER_SPEED, 0);
                        o.put(USER_NUMBER, 10000 + savedLocation.getNumber());
                        o.put(USER_COLOR, Color.rgb(0,155,0));
                        o.put(USER_NAME, savedLocation.getUsername());
                        o.put(USER_DESCRIPTION, savedLocation.getTitle());
                        o.put(REQUEST_TIMESTAMP, savedLocation.getTimestamp());
                        final MyUser x = State.getInstance().getUsers().addUser(o);
                        x.fire(MAKE_ACTIVE);
//                        x.setUser(true);

                        x.createViews();
                        State.getInstance().fire(USER_JOINED, x);
                        State.getInstance().getUsers().forUser(x.getProperties().getNumber(),new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.createViews();
//                                myUser.fire(SELECT_USER);

                                //noinspection unchecked
                                new SystemMessage(context).setText(savedLocation.getUsername() + (
                                        savedLocation.getTitle() != null && savedLocation.getTitle().length() > 0 ? ": "+savedLocation.getTitle() : ""
                                        )).setAction(context.getString(R.string.edit), new Runnable1() {
                                    @Override
                                    public void call(Object arg) {
                                        editLocation(savedLocation);
                                    }
                                }).setOnClickListener(new Runnable1() {
                                    @Override
                                    public void call(Object arg) {
                                        State.getInstance().fire(SHOW_SAVED_LOCATIONS);
                                    }
                                }).showSnack();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SHOW_SAVED_LOCATIONS:
                showLocations();
                break;
            case DELETE_SAVED_LOCATION:
                if(object != null){
                    int position = -1;
                    SavedLocation saved;
                    if(object instanceof SavedLocation) {
                        saved = (SavedLocation) object;
                    } else {
                        position = (int) object;
                        saved = SavedLocation.getItemByPosition(position);
                        if(adapter != null) adapter.notifyItemRemoved(position);
                    }
                    saved.setSynced(new Date().getTime());
                    saved.delete(context);
                    State.getInstance().getUsers().forUser((int) (saved.getNumber() + 10000), new Runnable2<Integer, MyUser>() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.fire(HIDE_SAVED_LOCATION);
                        }
                    });
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reloadCursor();
                        }
                    },500);
                }
                break;
            case SYNC_PROFILE:
                try {
                    final ArrayList<Map<String,Object>> values = new ArrayList<>();
                    SyncFB sync = new SyncFB()
                            .setType(Sync.Type.ACCOUNT_PRIVATE)
                            .setKey(REQUEST_SAVED_LOCATION)
                            .setUid(State.getInstance().fetchUid())
                            .setReference(FirebaseDatabase.getInstance().getReference())
                            .setOnGetValue(new Callable2<Object, String, Object>() {
                                @Override
                                public Object call(String key, Object value) {
                                    return value;
                                }
                            })
                            .setOnSaveLocalValue(new Runnable3<String, Object, Object>() {
                                @Override
                                public void call(String key, Object newLocation, Object oldLocation) {
                                    Utils.log("LOCALVALUE:" + key, newLocation,  oldLocation);
                                    if(newLocation instanceof Map) {
                                        if (((Map) newLocation).size() > 2 && ((Map) newLocation).containsKey(USER_LATITUDE)) {
                                            SavedLocation loc = SavedLocation.newLocation(context, (Map) newLocation);
                                            if(loc != null) {
                                                loc.save(context);
                                            }
                                        } else {
                                            SavedLocation loc = SavedLocation.getItemByFieldValue("key", (String) ((Map) newLocation).get(KEYS));
                                            if(loc != null) {
                                                loc.delete(context);
                                            }
                                        }
                                    }
                                }
                            })
                            .setOnSaveRemoteValue(new Runnable3<String, Object, Object>() {
                                @Override
                                public void call(String key, Object newLocation, Object oldLocation) {
                                    Utils.log("REMOTEVALUE:" + key,  newLocation,  oldLocation);
                                    if(newLocation instanceof Map && ((Map) newLocation).size() > 2 && ((Map) newLocation).containsKey(USER_LATITUDE)) {
                                        SavedLocation loc = SavedLocation.newLocation(context, (Map) newLocation);
                                        loc.save(context);
                                    }
                                }
                            })
                            .setOnFinish(new Runnable2<Sync.Mode, String>() {
                                @Override
                                public void call(Sync.Mode mode, String key) {
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            reloadCursor();
                                        }
                                    },500);
                                }
                            });

                    if(sync.ready()) {
                        Map<String,Object> fake;

                        Cursor cursor = SavedLocation.getDb().getAll();
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            fake = new HashMap<>();
                            SavedLocation location = SavedLocation.getItemByCursor(cursor);
                            if(location != null) {
                                Utils.log("LOCATION:", location);

                                fake.put(KEYS, location.getKey());
                                if(location.getTimestamp() > 0) fake.put(Firebase.TIMESTAMP, location.getTimestamp());
                                if(location.getSynced() > 0) fake.put(SYNCED, location.getSynced());
                                if(location.getProvider() != null) fake.put(USER_PROVIDER, location.getProvider());
                                if(location.getTitle() != null) fake.put(SavedLocation.DESCRIPTION, location.getTitle());
                                if(location.getAddress() != null) fake.put(SavedLocation.ADDRESS, location.getAddress());
                                if(location.getLatitude() != 0D) fake.put(USER_LATITUDE, location.getLatitude());
                                if(location.getLongitude() != 0D) fake.put(USER_LONGITUDE, location.getLongitude());
                                if(location.getUsername() != null) fake.put(SavedLocation.USERNAME, location.getUsername());
                                if(location.getNumber() != 0) fake.put(SavedLocation.NUMBER, location.getNumber());

                                values.add(fake);
                                Utils.log("FAKE:", fake);
                                cursor.moveToNext();
                            }
                        }
                        sync.syncValues(values);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    public SavedLocationViewHolder setMap(GoogleMap map) {
        this.map = map;

//        map.setOnMarkerClickListener(onMarkerClickListener);
//        map.setInfoWindowAdapter(infoWindowAdapter);

        return this;
    }

    private boolean isSavedLocation(MyUser user) {
        return user != null && user.getLocation() != null && user.getLocation().getProvider().equals(TYPE);
    }

    @SuppressWarnings("unchecked")
    private void showLocations() {

        dialog = new CustomListDialog(context);

        final RecyclerView list = dialog.getList();
        adapter = new SavedLocation.SavedLocationsAdapter(context, list);
        adapter.setEmptyView(dialog.getLayout().findViewById(R.id.tv_placeholder));

        dialog.setAdapter(adapter);
        dialog.setMenu(R.menu.dialog_saved_locations_menu);
        dialog.setFlat(true);

        dialog.setSearchListener(new Callable1<Boolean, String>() {
            @Override
            public Boolean call(String query) {
                filterMessage = query;
                setFilterAndReload(query);
                return false;
            }
        });

        context.getSupportLoaderManager().initLoader(1, null, adapter);
        adapter.setOnLeftSwipeListener(new Runnable1<Integer>() {
            @Override
            public void call(final Integer position) {
                reloadCursor();
                editLocation(SavedLocation.getItemByPosition(position));
            }
        });
        adapter.setOnRightSwipeListener(new Runnable1<Integer>() {
            @Override
            public void call(final Integer position) {
                State.getInstance().fire(DELETE_SAVED_LOCATION, position);
            }
        });
        adapter.setOnCursorReloadListener(new Runnable1<Cursor>() {
            @Override
            public void call(Cursor cursor) {
                    dialog.setTitle(context.getString(R.string.locations_d, cursor.getCount()) + (filterMessage != null && filterMessage.length() > 0 ? " ["+filterMessage+"]" : ""));
            }
        });

        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(new Runnable1<SavedLocation>() {
            @Override
            public void call(SavedLocation savedLocation) {
                State.getInstance().fire(SHOW_SAVED_LOCATION, savedLocation);
                dialog.dismiss();
            }
        });
        adapter.setOnLocationClickListener(new Runnable1<SavedLocation>() {
            @Override
            public void call(final SavedLocation savedLocation) {
                State.getInstance().fire(SHOW_SAVED_LOCATION, savedLocation);
                State.getInstance().fire(new Runnable() {
                    @Override
                    public void run() {
                        State.getInstance().getUsers().forUser((int)savedLocation.getNumber() + 10000, new Runnable2<Integer, MyUser>() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(NavigationViewHolder.SHOW_NAVIGATION);
                            }
                        });

                    }
                });
                dialog.dismiss();
//                new NavigationStarter(context, savedLocation.getLatitude(), savedLocation.getLongitude()).start();
            }
        });

        dialog.setTitle(context.getString(R.string.locations_d, adapter.getItemCount()));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.show_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(int j=0; j < SavedLocation.getCount(); j++){
                    SavedLocation s = SavedLocation.getItemByPosition(j);
                    State.getInstance().fire(SHOW_SAVED_LOCATION, s);
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.hide_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(HIDE_SAVED_LOCATION);
                    }
                });
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.setOnCancelListener(null);


        dialog.show();

        setFilterAndReload(filterMessage);

    }


    private void setFilterAndReload(String filter) {
        if(filter != null && filter.length() > 0) {
            SavedLocation.getDb().addRestriction("search","title_ LIKE ? OR username_ LIKE ? OR address_ LIKE ?", new String[]{"%"+filter+"%", "%"+filter+"%", "%"+filter+"%"});
        } else {
            SavedLocation.getDb().removeRestriction("search");
        }
        reloadCursor();
    }

    private void reloadCursor(){
        if(context.getSupportLoaderManager().getLoader(1) != null) {
            context.getSupportLoaderManager().getLoader(1).forceLoad();
        }
    }

    private void editLocation(final SavedLocation savedLocation) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();

        @SuppressLint("InflateParams") final View content = context.getLayoutInflater().inflate(R.layout.dialog_saved_location, null);

        final EditText etTitle = content.findViewById(R.id.et_saved_location_title);
        final EditText etComment = content.findViewById(R.id.et_saved_location_comment);
        etTitle.setText(savedLocation.getUsername());
        etComment.setText(savedLocation.getTitle());

        dialog.setTitle(context.getString(R.string.saved_location));
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(etTitle.getText().toString().length() > 0) {
                    savedLocation.setUsername(etTitle.getText().toString());
                    State.getInstance().getUsers().forUser((int)savedLocation.getNumber() + 10000, new Runnable2<Integer, MyUser>() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.fire(CHANGE_NAME, savedLocation.getUsername());
                            myUser.removeViews();
                            myUser.createViews();
                        }
                    });
                }
                if(etComment.getText().toString().length() > 0) {
                    savedLocation.setTitle(etComment.getText().toString());
                    State.getInstance().getUsers().forUser((int)savedLocation.getNumber() + 10000, new Runnable2<Integer, MyUser>() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.getProperties().setDescription(etComment.getText().toString());
                        }
                    });
                }
                savedLocation.setSynced(new Date().getTime());
                savedLocation.save(context);
                reloadCursor();

            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                reloadCursor();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                State.getInstance().fire(DELETE_SAVED_LOCATION, savedLocation);
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                reloadCursor();
            }
        });
        dialog.setView(content);
        dialog.show();

    }

    class SavedLocationView extends AbstractView {
        private Marker marker;

        @Override
        public List<String> events() {
            List<String> list = new ArrayList<>();
            return list;
        }

        SavedLocationView(MyUser myUser){
            super(SavedLocationViewHolder.this.context, myUser);
            if(isSavedLocation(myUser)) {
                createMarker();
            }
        }

        private void createMarker() {
            myUser.getProperties().setButtonView(R.layout.view_location_button);

            IconGenerator iconFactory = new IconGenerator(context);
            iconFactory.setColor(Color.rgb(0,235,0));
            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(myUser.getProperties().getDisplayName()))).
                    position(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude())).
                    anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

            marker = map.addMarker(markerOptions);

            Bundle b = new Bundle();
            b.putString(MarkerViewHolder.TYPE, TYPE);
            b.putInt(RESPONSE_NUMBER, myUser.getProperties().getNumber());
            marker.setTag(b);
        }

        @Override
        public void remove() {
            if(marker != null){
                marker.remove();
            }
            marker = null;
        }

        @Override
        public boolean dependsOnLocation(){
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event){
                case CHANGE_NUMBER:
                    if(isSavedLocation(myUser)) {
                        int number = (int) object;
                        Bundle b = new Bundle();
                        b.putString(TYPE, TYPE);
                        b.putInt(RESPONSE_NUMBER, number);
                        marker.setTag(b);
                    }
                    break;
                case CHANGE_NAME:
                    if(isSavedLocation(myUser)) {
                        String name = (String) object;
                        if (name != null && name.length() > 0) {
                            marker.remove();
                            createMarker();
                        }
                    }
                    break;
                case CREATE_CONTEXT_MENU:
                    Menu menu = (Menu) object;
                    if(!isSavedLocation(myUser)) {
                        menu.add(0, R.string.save_location, Menu.NONE, R.string.save_location).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(SAVE_LOCATION);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_pin_drop_black_24dp);
                    }
                    if(isSavedLocation(myUser)) {
                        menu.add(0, R.string.edit, Menu.NONE, R.string.edit).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int number = myUser.getProperties().getNumber() - 10000;

//                                SavedLocation.getItemByPosition(position)
                                Utils.log(SavedLocationView.this, "onEvent:","number="+number); //NON-NLS
                                SavedLocation saved = SavedLocation.getItemByNumber(number);
                                editLocation(saved);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_mode_edit_black_24dp);
                        menu.add(0, R.string.hide, Menu.NONE, R.string.hide).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(HIDE_SAVED_LOCATION, myUser);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_location_off_black_24dp);
                        /*menu.put("Remove").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(DELETE_SAVED_LOCATION);
                                return false;
                            }
                        });*/
                        menu.add(0, R.string.share, Menu.NONE, R.string.share).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(SHARE_SAVED_LOCATION, myUser);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_share_black_24dp);
                        menu.add(0, R.string.send, Menu.NONE, R.string.send).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(SEND_SAVED_LOCATION);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_chat_black_24dp);
                    }
                    break;
                case HIDE_SAVED_LOCATION:
                    if(isSavedLocation(myUser)) {
                        myUser.removeViews();
                        myUser.fire(MAKE_INACTIVE);
                        State.getInstance().fire(USER_DISMISSED, myUser);
//                        myUser.fire(USER_DISMISSED);
                        State.getInstance().fire(CameraViewHolder.CAMERA_UPDATE);
                    }
/*
                    State.getInstance().getUsers().forUser(myUser.getProperties().getNumber(), new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            if(isSavedLocation(myUser)) {
                                myUser.removeViews();
                                myUser.fire(MAKE_INACTIVE);
                                State.getInstance().fire(USER_DISMISSED, myUser);
                                myUser.fire(USER_DISMISSED);
                                State.getInstance().fire(CAMERA_UPDATE);
                            }
                        }
                    });
*/
                    break;
                case SHARE_SAVED_LOCATION:
                    if(isSavedLocation(myUser)){
                        final MyUser user = (MyUser) object;

                        final SavedLocation savedLocation = SavedLocation.getItemByNumber(myUser.getProperties().getNumber() - 10000);

                        new ShareSender(context).send(context.getString(R.string.share_location_to), context.getString(R.string.look_at_s, user.getProperties().getDisplayName()),
//                                "https://www.google.com/maps/@"+savedLocation.getLatitude()+","+savedLocation.getLongitude()+",14z");
                                String.format("http://maps.google.com/maps?z=14&q=loc:%s,%s", savedLocation.getLatitude(), savedLocation.getLongitude()));

//                        http://maps.google.com/maps?z=14&q=loc:38.93440628051758,-77.35896301269531
//                                "http://maps.google.com/maps?z=14&ll=" + savedLocation.getLatitude() + "," + savedLocation.getLongitude());
                    }
                    break;
                case DROPPED_TO_USER:
                    final MyUser toUser = (MyUser) object;
                    if(toUser.isUser() && toUser != State.getInstance().getMe()) {
                        myUser.fire(SEND_SAVED_LOCATION, toUser);
                    }
                    break;
                case SEND_SAVED_LOCATION:
                    if(isSavedLocation(myUser)){
                        final MyUser user = (MyUser) object;

                        final SavedLocation savedLocation = SavedLocation.getItemByNumber(myUser.getProperties().getNumber() - 10000);

                        final AlertDialog dialog = new AlertDialog.Builder(context).create();
                        dialog.setTitle(context.getString(R.string.send_location));
                        dialog.setMessage(context.getString(R.string.youre_going_to_send_the_location_s_to_s_continue, myUser.getProperties().getDisplayName(), user == null ? context.getString(R.string.all) : user.getProperties().getDisplayName()));

                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(user != null && !isSavedLocation(user) && user != State.getInstance().getMe()){
                                    State.getInstance().getTracking()
                                            .put(USER_LATITUDE, savedLocation.getLatitude())
                                            .put(USER_LONGITUDE, savedLocation.getLongitude())
                                            .put(USER_ADDRESS, savedLocation.getAddress())
                                            .put(USER_NAME, savedLocation.getUsername())
                                            .put(USER_DESCRIPTION, savedLocation.getTitle())
                                            .put(REQUEST_PUSH, true)
                                            .put(RESPONSE_PRIVATE, user.getProperties().getNumber())
                                            .put(REQUEST_DELIVERY_CONFIRMATION, Misc.getUnique())
                                            .send(REQUEST_SAVED_LOCATION);
                                } else if (user == null) {
                                    State.getInstance().getTracking()
                                            .put(USER_LATITUDE, savedLocation.getLatitude())
                                            .put(USER_LONGITUDE, savedLocation.getLongitude())
                                            .put(USER_ADDRESS, savedLocation.getAddress())
                                            .put(USER_NAME, savedLocation.getUsername())
                                            .put(USER_DESCRIPTION, savedLocation.getTitle())
                                            .put(REQUEST_PUSH, true)
                                            .put(REQUEST_DELIVERY_CONFIRMATION, Misc.getUnique())
                                            .send(REQUEST_SAVED_LOCATION);
                                }

                            }
                        });
                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        dialog.show();
                    }
                    break;
                case MARKER_CLICK:
                    if(isSavedLocation(myUser)) {
                        Marker marker = (Marker) object;
                        Utils.log(SavedLocationView.this, "onEvent:","MARKER_CLICK="+ myUser.getProperties().getDisplayName()); //NON-NLS
                        marker.showInfoWindow();
                    }
                    break;
                case SAVE_LOCATION:
                    if(!isSavedLocation(myUser)) {
                        final SavedLocation loc = new SavedLocation(context);
                        loc.setLatitude(myUser.getLocation().getLatitude());
                        loc.setLongitude(myUser.getLocation().getLongitude());
                        loc.setTimestamp(new Date().getTime());
                        loc.setUsername(myUser.getProperties().getDisplayName());
                        loc.save(context);

                        //noinspection unchecked
                        new SystemMessage(context).setText(context.getString(R.string.location_saved)).setAction(context.getString(R.string.show), new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_SAVED_LOCATION, loc);
                            }
                        }).setOnClickListener(new Runnable1() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_SAVED_LOCATIONS);
                            }
                        }).showSnack();
                    }
                    break;
            }
            return true;
        }


/*
        @Override
        public View infoWindow() {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            int buttonView = myUser.getProperties().getImageResource();
            View view = (LinearLayout) inflater.inflate(R.layout.view_saved_location_info_window, null);

            return view;
        }
*/
    }

/*
    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
//        rules.put(new IntroRule().setEvent(SHOW_SAVED_LOCATION).setId("saved_location_show_location").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU_ITEM).setViewId(R.string.menu_save_location).setTitle("Here you can").setDescription("Save current location of you or another member of group and showSnack it later on the map."));

        return rules;
    }
*/


    private OnMenuItemClickListener onMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(State.getInstance().getUsers().getCountSelected() == 1) {
                State.getInstance().getUsers().forSelectedUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(SAVE_LOCATION);
                    }
                });
            }
            return false;
        }
    };

}
