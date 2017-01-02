package ru.wtg.whereaminow.holders;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.UNSELECT_USER;

/**
 * Created 11/18/16.
 */
public class AddressViewHolder extends AbstractViewHolder<AddressViewHolder.AddressView> {
    
    private static final String TYPE = "address";
    private SimpleCallback<String> callback;

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public AddressView create(MyUser myUser) {
        if (myUser == null) return null;
        return new AddressView(myUser);
    }

    public AddressViewHolder setCallback(SimpleCallback<String> callback) {
        this.callback = callback;
        return this;
    }

    private void setTitle(final String text){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                callback.call(text);
            }
        });
    }

    class AddressView extends AbstractView {
        AddressView(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean dependsOnLocation(){
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            resolveAddress(location);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SELECT_USER:
                case UNSELECT_USER:
                    if(State.getInstance().getUsers().getCountAllSelected() > 1){
                        callback.call(null);
                        return true;
                    } else {
                        callback.call("...");
                        onChangeLocation(myUser.getLocation());
                    }
                    break;
            }
            return true;
        }

        private void resolveAddress(final Location location) {
            if(!myUser.getProperties().isSelected() || location == null || State.getInstance().getUsers().getCountAllSelected() > 1){
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String req = "http://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&zoom=18&addressdetails=1";
                        final String res = Utils.getUrl(req);
                        try {
                            JSONObject address = new JSONObject(res);
                            setTitle(address.getString("display_name"));
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                            setTitle(null);
                        }
                    } catch (IOException | NullPointerException e) {
                        //e.printStackTrace();

                    }
                }
            }).start();
        }
    }

}
