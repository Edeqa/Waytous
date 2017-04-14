package ru.wtg.whereaminow.holders;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.abstracts.AbstractView;
import ru.wtg.whereaminow.abstracts.AbstractViewHolder;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.EVENTS.SELECT_USER;
import static ru.wtg.whereaminow.State.EVENTS.UNSELECT_USER;

/**
 * Created 11/18/16.
 */
@SuppressWarnings("WeakerAccess")
public class AddressViewHolder extends AbstractViewHolder<AddressViewHolder.AddressView> {
    
    private static final String TYPE = "address";
    private SimpleCallback<String> callback;

    public AddressViewHolder(final MainActivity context) {
        super(context);
        setCallback(new SimpleCallback<String>() {
            @Override
            public void call(String text) {
                if(context.getSupportActionBar() != null) {
                    context.getSupportActionBar().setSubtitle(text);
                }
            }
        });
    }

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
                        String req = context.getString(R.string.address_request_template, location.getLatitude(), location.getLongitude());
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
