package ru.wtg.whereaminow.holders;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.abstracts.AbstractView;
import ru.wtg.whereaminow.abstracts.AbstractViewHolder;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SavedLocation;
import ru.wtg.whereaminow.helpers.SystemMessage;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NAME;
import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_DRAWER;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.EVENTS.DROPPED_TO_USER;
import static ru.wtg.whereaminow.State.EVENTS.MAKE_ACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.MAKE_INACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.PREPARE_DRAWER;
import static ru.wtg.whereaminow.State.EVENTS.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.EVENTS.SELECT_USER;
import static ru.wtg.whereaminow.holders.CameraViewHolder.UPDATE_CAMERA;
import static ru.wtg.whereaminow.holders.MarkerViewHolder.MARKER_CLICK;
import static ru.wtg.whereaminow.holders.NavigationViewHolder.SHOW_NAVIGATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DELIVERY_CONFIRMATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PUSH;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_SAVED_LOCATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TIMESTAMP;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ACCURACY;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ADDRESS;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ALTITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_BEARING;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DESCRIPTION;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LATITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LONGITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_SPEED;

/**
 * Created 11/27/16.
 */
public class SavedLocationViewHolder extends AbstractViewHolder<SavedLocationViewHolder.SavedLocationView> {

    public static final String TYPE = REQUEST_SAVED_LOCATION;

    private static final String SAVE_LOCATION = "save_location";
    private static final String SHOW_SAVED_LOCATION = "show_saved_location";
    private static final String HIDE_SAVED_LOCATION = "hide_saved_location";
    private static final String DELETE_SAVED_LOCATION = "delete_saved_location";
    private static final String SHOW_SAVED_LOCATIONS = "show_saved_locations";
    private static final String SHARE_SAVED_LOCATION = "share_saved_locations";
    private static final String SEND_SAVED_LOCATION = "send_saved_locations";

    private final AppCompatActivity context;
    private GoogleMap map;
    private SavedLocation.SavedLocationsAdapter adapter;
    private Toolbar toolbar;
    private AlertDialog dialog;
    private String filterMessage;

    public SavedLocationViewHolder(MainActivity context) {
        this.context = context;
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

        if(o.has("key")){
            key = o.getString("key");
            if(SavedLocation.getItemByFieldValue("key", key) != null) return;
        }

        final String finalAddress = address;
        final String finalDescription = description;
        final String finalName = name;
        final String finalKey = key;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                final AlertDialog dialog = new AlertDialog.Builder(context).create();
                dialog.setTitle("Add location");
                dialog.setMessage("You've got the location from " + State.getInstance().getUsers().getUsers().get(number).getProperties().getDisplayName()
                        + ": " + (finalName == null ? "" : finalName) + (finalAddress == null ? "" : ", address: " + finalAddress) + (finalDescription == null ? "" : ", description: " + finalDescription) + ". Add it to your saved locations list?");

                dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final SavedLocation loc = new SavedLocation(context);
                        loc.setLatitude(lat);
                        loc.setLongitude(lng);
                        loc.setTimestamp(new Date().getTime());
                        loc.setUsername(finalName);
                        loc.setAddress(finalAddress);
                        loc.setKey(finalKey);
                        loc.save(context);

                        reloadCursor();
                        //noinspection unchecked
                        new SystemMessage(context).setText("Location saved").setAction("Show", new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_SAVED_LOCATION, loc);
                            }
                        }).setOnClickListener(new SimpleCallback() {
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
                        System.out.println("NOADD");
                    }
                });
                dialog.show();
            }
        });

        System.out.println("GOT SAVED:"+o);

    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.save_location, Menu.NONE, R.string.save_location).setVisible(false).setOnMenuItemClickListener(onMenuItemClickListener);

                /*optionsMenu.put(Menu.NONE, 1919191919, Menu.NONE, "Test location").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
//                        newMessage(State.getInstance().getMe(), false);
//                        State.getInstance().fire(SEND_MESSAGE, "Test message");

                        final SavedLocation loc = new SavedLocation(context);
                        loc.setLatitude(74.0);
                        loc.setLongitude(35.0);
                        loc.setTimestamp(new Date().getTime());
                        loc.setUsername(State.getInstance().getMe().getProperties().getDisplayName());
                        loc.save(context);

                        return false;
                    }
                });*/
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.save_location).setVisible(State.getInstance().getUsers().getCountSelected() == 1);
                break;
            case CREATE_DRAWER:
                MenuItem menuItem = (MenuItem) object;
                Menu navigationMenu = menuItem.getSubMenu();
                MenuItem item = navigationMenu.findItem(R.string.locations);
                if(item == null) {
                    item = navigationMenu.add(Menu.NONE, R.string.locations, Menu.NONE, context.getString(R.string.locations));
                }
                item.setIcon(R.drawable.ic_pin_drop_black_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                showLocations();
                                return false;
                            }
                        });
                break;
            case PREPARE_DRAWER:
                menuItem = (MenuItem) object;
                navigationMenu = menuItem.getSubMenu();
                SavedLocation.getDb().removeRestriction("search");
                int count = SavedLocation.getCount();
                navigationMenu.findItem(R.string.locations).setVisible(count > 0);
                if(count>0) {
                    menuItem.setVisible(true);
                }
                break;
            case MARKER_CLICK:
                final Marker marker = (Marker) object;
                Bundle b = (Bundle) marker.getTag();
                if(b != null && b.getString(MarkerViewHolder.TYPE, null).equals(TYPE)){
                    if(marker.getTag() != null) {
                        int number = b.getInt(RESPONSE_NUMBER);
                        if(number > 10000) {
                            State.getInstance().getUsers().forUser(number, new MyUsers.Callback() {
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
                        o.put(REQUEST_TIMESTAMP, savedLocation.getTimestamp());
                        MyUser x = State.getInstance().getUsers().addUser(o);
                        x.fire(MAKE_ACTIVE);
//                        x.setUser(true);

                        x.createViews();
                        State.getInstance().fire(USER_JOINED, x);
                        State.getInstance().getUsers().forUser(x.getProperties().getNumber(),new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.createViews();
//                                myUser.fire(SELECT_USER);

                                //noinspection unchecked
                                new SystemMessage(context).setText(savedLocation.getUsername() + (
                                        savedLocation.getTitle() != null && savedLocation.getTitle().length() > 0 ? ": "+savedLocation.getTitle() : ""
                                        )).setAction("Edit", new SimpleCallback() {
                                    @Override
                                    public void call(Object arg) {
                                        editLocation(savedLocation);
                                    }
                                }).setOnClickListener(new SimpleCallback() {
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
                    SavedLocation.getDb().deleteByItem(saved);

                    System.out.println("POSITION:"+position);
                    System.out.println("SAVED:"+saved);
                    State.getInstance().getUsers().forUser((int) (saved.getNumber() + 10000), new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                    System.out.println("USER:"+myUser);
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
        }
        return true;
    }

    public SavedLocationViewHolder setMap(GoogleMap map) {
        this.map = map;

//        map.setOnMarkerClickListener(onMarkerClickListener);
//
//        map.setInfoWindowAdapter(infoWindowAdapter);

        return this;
    }

    private boolean isSavedLocation(MyUser user) {
        return user != null && user.getLocation() != null && user.getLocation().getProvider().equals(TYPE);
    }

    private void showLocations() {

        dialog = new AlertDialog.Builder(context).create();
        final View content = context.getLayoutInflater().inflate(R.layout.dialog_items, null);
        final RecyclerView list = (RecyclerView) content.findViewById(R.id.list_items);

        setupFooter(content);

        adapter = new SavedLocation.SavedLocationsAdapter(context, list);
        adapter.setEmptyView(content.findViewById(R.id.tv_placeholder));

        dialog.setCustomTitle(setupToolbar());

        context.getSupportLoaderManager().initLoader(1, null, adapter);

        adapter.setOnLeftSwipeListener(new SimpleCallback<Integer>() {
            @Override
            public void call(final Integer position) {
                reloadCursor();
                editLocation(SavedLocation.getItemByPosition(position));
            }
        });
        adapter.setOnRightSwipeListener(new SimpleCallback<Integer>() {
            @Override
            public void call(final Integer position) {
                State.getInstance().fire(DELETE_SAVED_LOCATION, position);
            }
        });

        adapter.setOnCursorReloadListener(new SimpleCallback<Cursor>() {
            @Override
            public void call(Cursor cursor) {
                if(toolbar != null) {
                    toolbar.setTitle("Locations (" + cursor.getCount() + ")" + (filterMessage != null && filterMessage.length() > 0 ? " ["+filterMessage+"]" : ""));
//                    toolbar.setTitle("Locations (" + adapter.getItemCount() + ")");
                }
            }
        });

        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(new SimpleCallback<SavedLocation>() {
            @Override
            public void call(SavedLocation savedLocation) {
                State.getInstance().fire(SHOW_SAVED_LOCATION, savedLocation);
                dialog.dismiss();
            }
        });
        adapter.setOnLocationClickListener(new SimpleCallback<SavedLocation>() {
            @Override
            public void call(final SavedLocation savedLocation) {
                State.getInstance().fire(SHOW_SAVED_LOCATION, savedLocation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        State.getInstance().getUsers().forUser((int)savedLocation.getNumber() + 10000, new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(SHOW_NAVIGATION);
                            }
                        });

                    }
                }, 500);
                dialog.dismiss();
//                new NavigationStarter(context, savedLocation.getLatitude(), savedLocation.getLongitude()).start();
            }
        });


        toolbar.setTitle("Locations (" + adapter.getItemCount() + ")");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Show all", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(int j=0; j < SavedLocation.getCount(); j++){
                    SavedLocation s = SavedLocation.getItemByPosition(j);
                    State.getInstance().fire(SHOW_SAVED_LOCATION, s);
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hide all", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(HIDE_SAVED_LOCATION);
                    }
                });
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.setOnCancelListener(null);
        dialog.setView(content);

        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        dialog.show();

        Utils.resizeDialog(context, dialog, Utils.MATCH_SCREEN, LinearLayout.LayoutParams.WRAP_CONTENT);

//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        setFilterAndReload(filterMessage);
    }

    private AppBarLayout setupToolbar() {

        AppBarLayout layoutToolbar = (AppBarLayout) context.getLayoutInflater().inflate(R.layout.view_action_bar, null);
        toolbar = (Toolbar) layoutToolbar.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE,mMode);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        toolbar.inflateMenu(R.menu.dialog_saved_locations_menu);
        final MenuItem searchItem = toolbar.getMenu().findItem(R.id.search_location);
        searchItem.getIcon().setColorFilter(Color.WHITE,mMode);

        final SimpleCallback<String> setFilter = new SimpleCallback<String>() {
            @Override
            public void call(String text) {
                setFilterAndReload(text);
            }
        };
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchItem.collapseActionView();
                filterMessage = query;
                setFilter.call(filterMessage);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                filterMessage = s;
                setFilter.call(filterMessage);
                return false;
            }
        });

        return layoutToolbar;
    }

    private LinearLayout setupFooter(View content) {
        final LinearLayout layoutFooter = (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_message_send, null);
        ViewGroup placeFooter = (ViewGroup) content.findViewById(R.id.layout_footer);

        placeFooter.addView(layoutFooter);

        return layoutFooter;
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

        final EditText etTitle = (EditText) content.findViewById(R.id.et_saved_location_title);
        final EditText etComment = (EditText) content.findViewById(R.id.et_saved_location_comment);
        etTitle.setText(savedLocation.getUsername());
        etComment.setText(savedLocation.getTitle());

        dialog.setTitle("Saved Location");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(etTitle.getText().toString().length() > 0) {
                    savedLocation.setUsername(etTitle.getText().toString());
                    State.getInstance().getUsers().forUser((int)savedLocation.getNumber() + 10000, new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.fire(CHANGE_NAME, savedLocation.getUsername());
                        }
                    });
                }
                if(etComment.getText().toString().length() > 0) {
                    savedLocation.setTitle(etComment.getText().toString());
                }
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
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Delete", new DialogInterface.OnClickListener() {
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
        private MyUser myUser;

        SavedLocationView(MyUser myUser){
            this.myUser = myUser;
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
                    if(myUser.isUser()) {
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
                                System.out.println("NUMBER:"+number);
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
                        State.getInstance().fire(UPDATE_CAMERA);
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
                                State.getInstance().fire(UPDATE_CAMERA);
                            }
                        }
                    });
*/
                    break;
                case SHARE_SAVED_LOCATION:
                    System.out.println("SHARE_SAVED_LOCATION");
                    //TODO
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
                        dialog.setTitle("Send location");
                        dialog.setMessage("You're going to send the location " + myUser.getProperties().getDisplayName()
                                + " to " + (user == null ? "all" : user.getProperties().getDisplayName()) + ". Continue?");

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
                                            .put(REQUEST_DELIVERY_CONFIRMATION, Utils.getUnique())
                                            .send(REQUEST_SAVED_LOCATION);
                                } else if (user == null) {
                                    State.getInstance().getTracking()
                                            .put(USER_LATITUDE, savedLocation.getLatitude())
                                            .put(USER_LONGITUDE, savedLocation.getLongitude())
                                            .put(USER_ADDRESS, savedLocation.getAddress())
                                            .put(USER_NAME, savedLocation.getUsername())
                                            .put(USER_DESCRIPTION, savedLocation.getTitle())
                                            .put(REQUEST_PUSH, true)
                                            .put(REQUEST_DELIVERY_CONFIRMATION, Utils.getUnique())
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
                        System.out.println("CLICKTO:" + myUser.getProperties().getDisplayName());
                        marker.showInfoWindow();
                    }
                    break;
                case SAVE_LOCATION:
                    if(myUser.isUser()) {
                        final SavedLocation loc = new SavedLocation(context);
                        loc.setLatitude(myUser.getLocation().getLatitude());
                        loc.setLongitude(myUser.getLocation().getLongitude());
                        loc.setTimestamp(new Date().getTime());
                        loc.setUsername(myUser.getProperties().getDisplayName());
                        loc.save(context);

                        //noinspection unchecked
                        new SystemMessage(context).setText("Location saved").setAction("Show", new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_SAVED_LOCATION, loc);
                            }
                        }).setOnClickListener(new SimpleCallback() {
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
                State.getInstance().getUsers().forSelectedUsers(new MyUsers.Callback() {
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
