package ru.wtg.whereaminow.holders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.NavigationStarter;
import ru.wtg.whereaminow.helpers.SavedLocation;
import ru.wtg.whereaminow.helpers.SnackbarMessage;
import ru.wtg.whereaminow.helpers.UserMessage;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_DRAWER;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.MAKE_INACTIVE;
import static ru.wtg.whereaminow.State.PREPARE_DRAWER;
import static ru.wtg.whereaminow.State.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.holders.CameraViewHolder.UPDATE_CAMERA;
import static ru.wtg.whereaminow.holders.MarkerViewHolder.MARKER_CLICK;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ACCURACY;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ALTITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_BEARING;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LATITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LONGITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_SPEED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_TIMESTAMP;

/**
 * Created 11/27/16.
 */
public class SavedLocationsViewHolder extends AbstractViewHolder<SavedLocationsViewHolder.SavedLocationView> {

    public static final String TYPE = "saved_locations";

    private static final String SHOW_SAVED_LOCATION = "show_saved_locations";
    private static final String HIDE_SAVED_LOCATION = "hide_saved_locations";
    private static final String DELETE_SAVED_LOCATION = "delete_saved_locations";

    private final AppCompatActivity context;
    private GoogleMap map;
    private SavedLocation.SavedLocationsAdapter adapter;
    private boolean donotscroll = false;

    public SavedLocationsViewHolder(AppCompatActivity context) {
        this.context = context;
        SavedLocation.init(context);

    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public SavedLocationView create(MyUser myUser) {
        if(myUser == null || myUser.getLocation() == null) return null;
        if(!myUser.getLocation().getProvider().equals(TYPE)) return null;
        return new SavedLocationView(myUser);
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
                optionsMenu.add(Menu.NONE, R.string.menu_save_location, Menu.NONE, R.string.menu_save_location).setVisible(false).setOnMenuItemClickListener(onMenuItemClickListener);

                optionsMenu.add(Menu.NONE, 1919191919, Menu.NONE, "Test location").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
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
                });


                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.menu_save_location).setVisible(State.getInstance().getUsers().getCountSelected() == 1);
                break;
            case CREATE_DRAWER:
                MenuItem menuItem = (MenuItem) object;
                Menu navigationMenu = menuItem.getSubMenu();
                MenuItem item = navigationMenu.findItem(R.string.saved_locations);
                if(item == null) {
                    item = navigationMenu.add(Menu.NONE, R.string.saved_locations, Menu.NONE, context.getString(R.string.saved_locations));
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
                int count = SavedLocation.getCount();
                navigationMenu.findItem(R.string.saved_locations).setVisible(count > 0);
                if(count>0) {
                    menuItem.setVisible(true);
                }
                break;
            case MARKER_CLICK:
                Marker marker = (Marker) object;
                Bundle b = (Bundle) marker.getTag();
                if(b != null && b.getString(MarkerViewHolder.TYPE, null).equals(TYPE)){
                    if(marker.getTag() != null) {
                        int number = b.getInt(RESPONSE_NUMBER);
//                        SavedLocation savedLocation = (SavedLocation) Utils.deserializeFromString();
                        System.out.println(number);
//                        State.getInstance().getUsers().forUser(number, new MyUsers.Callback() {
//                            @Override
//                            public void call(Integer number, MyUser myUser) {
//                                myUser.fire(CameraViewHolder.CAMERA_NEXT_ORIENTATION);
//                            }
//                        });
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
                        o.put(USER_TIMESTAMP, savedLocation.getTimestamp());
                        MyUser x = State.getInstance().getUsers().addUser(o);

                        x.createViews();
                        State.getInstance().fire(USER_JOINED, x);
                        State.getInstance().getUsers().forUser(x.getProperties().getNumber(),new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.createViews();
                                myUser.fire(SELECT_USER, 0);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        return true;
    }

    public SavedLocationsViewHolder setMap(GoogleMap map) {
        this.map = map;
        return this;
    }


    OnMenuItemClickListener onMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(State.getInstance().getUsers().getCountSelected() == 1) {

                State.getInstance().getUsers().forSelectedUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        final SavedLocation loc = new SavedLocation(context);
                        loc.setLatitude(myUser.getLocation().getLatitude());
                        loc.setLongitude(myUser.getLocation().getLongitude());
                        loc.setTimestamp(new Date().getTime());
                        loc.setUsername(myUser.getProperties().getDisplayName());
                        loc.save(context);

                        //noinspection unchecked
                        new SnackbarMessage().setText("Location saved").setAction("Show",new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_SAVED_LOCATION, loc);
                            }
                        }).setOnClickListener(new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                showLocations();
                            }
                        }).show();

                    }
                });
            }
            return false;
        }
    };


    class SavedLocationView extends AbstractView {
        private Marker marker;
        private MyUser myUser;

        SavedLocationView(MyUser myUser){
            this.myUser = myUser;

            createMarker();
        }

        private void createMarker() {
            myUser.getProperties().setButtonView(R.layout.view_location_button);

            IconGenerator iconFactory = new IconGenerator(context);
            iconFactory.setColor(Color.rgb(0,235,0));
            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(myUser.getProperties().getDisplayName()))).
                    position( new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude())).
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
                    int number = (int) object;
                    Bundle b = new Bundle();
                    b.putString(TYPE, TYPE);
                    b.putInt(RESPONSE_NUMBER, number);
                    marker.setTag(b);
                    break;
                case CHANGE_NAME:
                    String name = (String) object;
                    if(name != null && name.length() > 0){
                        marker.remove();
                        createMarker();
                    }
                    break;
                case CREATE_CONTEXT_MENU:
                    ContextMenu menu = (ContextMenu) object;
                    if(myUser.getLocation() != null && myUser.getLocation().getProvider() != null && myUser.getLocation().getProvider().equals(TYPE)) {
                        menu.add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
//                                int number = myUser.getProperties().getNumber() - 10000;
//                                SavedLocation saved = SavedLocation.getItemByNumber(context, number);
//                                editLocation(saved);
                                return false;
                            }
                        });
                        menu.add("Hide").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(HIDE_SAVED_LOCATION, myUser);
                                return false;
                            }
                        });
                        menu.add("Remove").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(DELETE_SAVED_LOCATION);
                                return false;
                            }
                        });
                    }
                    break;
                case HIDE_SAVED_LOCATION:
                    State.getInstance().getUsers().forUser(myUser.getProperties().getNumber(), new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.removeViews();
                            myUser.fire(MAKE_INACTIVE);
                            State.getInstance().fire(USER_DISMISSED, myUser);
                            myUser.fire(USER_DISMISSED);
                            State.getInstance().fire(UPDATE_CAMERA);
                        }
                    });
                    break;
                case DELETE_SAVED_LOCATION:
                    State.getInstance().getUsers().forUser(myUser.getProperties().getNumber(), new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.fire(HIDE_SAVED_LOCATION);
                        }
                    });
//                    SavedLocation saved = SavedLocation.getItemByNumber(context, myUser.getProperties().getNumber() - 10000);
//                    if(saved != null) saved.deleteByItem(null);
                    break;
            }
            return true;
        }

    }

    private void showLocations() {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();

        @SuppressLint("InflateParams") final View content = context.getLayoutInflater().inflate(R.layout.dialog_items, null);

        final RecyclerView list = (RecyclerView) content.findViewById(R.id.list_items);

        adapter = new SavedLocation.SavedLocationsAdapter(context, list);
        context.getSupportLoaderManager().initLoader(1, null, adapter);

        adapter.setOnLeftSwipeListener(new SimpleCallback<Integer>() {
            @Override
            public void call(final Integer position) {
                editLocation(SavedLocation.getItemByPosition(position));
            }
        });
        adapter.setOnRightSwipeListener(new SimpleCallback<Integer>() {
            @Override
            public void call(final Integer position) {
//                SavedLocation.getDb().
                SavedLocation.getDb().deleteByPosition(position);
                adapter.notifyItemRemoved(position);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        donotscroll = true;
//                        updateDialog();
                    }
                }, 500);
                /*State.getInstance().getUsers().forUser((int)(10000 + arg.getNumber()), new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(DELETE_SAVED_LOCATION);
                    }
                });*/

            }
        });

        adapter.setOnCursorReloadListener(new SimpleCallback<Cursor>() {
            @Override
            public void call(Cursor cursor) {
                dialog.setTitle("Locations (" + adapter.getItemCount() + ")");
                if(!donotscroll) list.scrollToPosition(cursor.getCount() - 1);
                donotscroll = false;
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
            public void call(SavedLocation savedLocation) {
                new NavigationStarter(context, savedLocation.getLatitude(), savedLocation.getLongitude()).start();
            }
        });

        dialog.setTitle("Locations (" + adapter.getItemCount() + ")");
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
//                dialog = null;
            }
        });
        dialog.setOnCancelListener(null/*new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
//                dialog = null;
            }
        }*/);
        dialog.setView(content);
        dialog.show();

    }


    private void updateDialog(){
        context.getSupportLoaderManager().getLoader(1).forceLoad();
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
                    savedLocation.save(context);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    State.getInstance().getUsers().forUser((int)savedLocation.getNumber() + 10000, new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.fire(CHANGE_NAME, savedLocation.getUsername());
                        }
                    });
                }
                if(etComment.getText().toString().length() > 0) {
                    savedLocation.setTitle(etComment.getText().toString());
                    savedLocation.save(context);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
        dialog.setView(content);
        dialog.show();

    }


}
