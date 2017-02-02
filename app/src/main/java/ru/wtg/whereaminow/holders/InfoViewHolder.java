package ru.wtg.whereaminow.holders;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SmoothInterpolated;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.EVENTS.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.EVENTS.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.TIME_ELAPSED;

/**
 * Created 11/24/16.
 */

public class InfoViewHolder extends AbstractViewHolder<InfoViewHolder.InfoView> {

    public static final String SHOW_INFO = "show_info";
    private static final String TYPE = "info";
    private static final int DELAY_BEFORE_HIDE = 5;

    private final MainActivity context;

    private TextView view;
    private Handler handler;
    private Runnable hideInfoView = new Runnable() {
        @Override
        public void run() {
            view.setVisibility(View.GONE);
        }
    };

    public InfoViewHolder(MainActivity context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        setView();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public InfoView create(MyUser myUser) {
        if (myUser == null) return null;
        return new InfoView(myUser);
    }

    private void setView() {
        view = (TextView) context.findViewById(R.id.tv_info);
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case SHOW_INFO:
                handler.removeCallbacks(hideInfoView);
                if(object != null) {
                    String text = object.toString();
                    view.setText(text);
                    view.setVisibility(View.VISIBLE);
                    handler.postDelayed(hideInfoView, DELAY_BEFORE_HIDE * 1000);
                } else {
                    view.setVisibility(View.GONE);
                }
                break;
        }
        return true;
    }

    class InfoView extends AbstractView {
        InfoView(MyUser myUser){
            this.myUser = myUser;
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SHOW_INFO:
                    handler.removeCallbacks(hideInfoView);
                    if(object != null) {
                        String text = (String) object;
                        view.setText(text);
                        view.setVisibility(View.VISIBLE);
                        handler.postDelayed(hideInfoView, DELAY_BEFORE_HIDE * 1000);
                    } else {
                        view.setVisibility(View.GONE);
                    }
                    break;
            }
            return true;
        }
    }
}
