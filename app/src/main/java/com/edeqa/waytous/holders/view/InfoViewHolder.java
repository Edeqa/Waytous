package com.edeqa.waytous.holders.view;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;


/**
 * Created 11/24/16.
 */

@SuppressWarnings("WeakerAccess")
public class InfoViewHolder extends AbstractViewHolder<InfoViewHolder.InfoView> {

    public static final String SHOW_INFO = "show_info"; //NON-NLS
    private static final String TYPE = "info"; //NON-NLS
    private static final int DELAY_BEFORE_HIDE = 5;


    private TextView view;
    private Handler handler;
    private Runnable hideInfoView = new Runnable() {
        @Override
        public void run() {
            view.setVisibility(View.GONE);
        }
    };

    public InfoViewHolder(MainActivity context) {
        super(context);
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
            super(myUser);
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
