package ru.wtg.whereaminow.helpers;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static ru.wtg.whereaminow.helpers.MyUser.ASSIGN_TO_CAMERA;
import static ru.wtg.whereaminow.helpers.MyUser.REFUSE_FROM_CAMERA;

/**
 * Created by tujger on 11/18/16.
 */
public class AddressViewHolder implements ViewHolder<AddressViewHolder.AddressView> {
    public static final String TYPE = "address";
    private SimpleCallback<String> callback;

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public AddressView createView(MyUser myUser) {
        if (myUser == null) return null;

        return new AddressView(myUser);
    }

    public AddressViewHolder setCallback(SimpleCallback<String> callback) {
        this.callback = callback;
        return this;
    }

    public class AddressView implements AbstractView {
        private MyUser myUser;

        public AddressView(MyUser myUser){
            this.myUser = myUser;
        }

        @Override
        public void remove() {
            myUser.fire(REFUSE_FROM_CAMERA);
            setTitle(null);
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
        public void setNumber(int number) {
        }

        @Override
        public int getNumber() {
            return myUser.getNumber();
        }

        @Override
        public void onEvent(int event, Object object) {
            switch(event){
                case ASSIGN_TO_CAMERA:
                    myUser.setSelected(true);
                    /*if(State.getInstance().getCamera(0).getUsers().size()>1) {
                        callback.call(null);
                        return;
                    } else {
                        callback.call("...");
                    }*///TODO
                    onChangeLocation(myUser.getLocation());
                    break;
                case REFUSE_FROM_CAMERA:
                    myUser.setSelected(false);
            }
        }

        private void resolveAddress(final Location location) {
//            System.out.println("RESOLVE ADDRESS"+location);
            if(!myUser.isSelected() || location == null){
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
                        e.printStackTrace();

                    }
                }
            }).start();
        }
    }

    private void setTitle(final String text){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                callback.call(text);
            }
        });
    }
}
