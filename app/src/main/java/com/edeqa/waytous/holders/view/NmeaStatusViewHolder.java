package com.edeqa.waytous.holders.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;

import java.io.Serializable;
import java.util.Iterator;


import static com.edeqa.waytous.helpers.Events.ACTIVITY_PAUSE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;

/**
 * Created 01/13/17.
 */
@SuppressWarnings({"deprecation", "WeakerAccess", "unused"})
public class NmeaStatusViewHolder extends AbstractViewHolder implements GpsStatus.Listener {

    public static final String SHOW_NMEA_STATUS = "show_nmea_status"; //NON-NLS
    public static final String HIDE_NMEA_STATUS = "hide_nmea_status"; //NON-NLS

    private boolean showStatus = false;
    private ViewGroup layoutSatellites;
    private LinearLayout layoutGpsStatus;

    public NmeaStatusViewHolder(MainActivity context) {
        super(context);

        layoutGpsStatus = (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_nmea_status, null);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.bottomMargin = Utils.adaptedSize(context, 64);
//        params.setMargins(Utils.adaptedSize(context,8), 0, Utils.adaptedSize(context, 8), Utils.adaptedSize(context, 50));
        layoutGpsStatus.setLayoutParams(params);
        layoutSatellites = layoutGpsStatus.findViewById(R.id.layout_satellites);
        layoutGpsStatus.findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int v = layoutGpsStatus.findViewById(R.id.layout_nmea_info).getVisibility();
                if(v == View.VISIBLE) {
                    layoutGpsStatus.findViewById(R.id.layout_nmea_info).setVisibility(View.GONE);
                } else {
                    layoutGpsStatus.findViewById(R.id.layout_nmea_info).setVisibility(View.VISIBLE);
                }
            }
        });
        layoutGpsStatus.findViewById(R.id.layout_nmea_info).measure(0,0);
        int height = layoutGpsStatus.findViewById(R.id.layout_nmea_info).getMeasuredHeight();
        layoutGpsStatus.findViewById(R.id.layout_nmea_info).setVisibility(View.GONE);
        params.height = height;
        layoutGpsStatus.setLayoutParams(params);
        layoutGpsStatus.setVisibility(View.INVISIBLE);

//        height = Utils.adaptedSize(context, 180);

        Serializable object = State.getInstance().getPropertiesHolder().loadFor(getType());
        if(object != null) {
            showStatus = (boolean) object;
            if(showStatus) State.getInstance().fire(SHOW_NMEA_STATUS);
        }
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("MissingPermission")
    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case ACTIVITY_RESUME:
                if(showStatus) {
                    State.getInstance().fire(SHOW_NMEA_STATUS);
                }
                break;
            case ACTIVITY_PAUSE:
                if(showStatus) {
                    State.getInstance().fire(SHOW_NMEA_STATUS);
                }
                break;
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.show_nmea_status, Menu.NONE, R.string.show_nmea_status).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showStatus = true;
                        State.getInstance().fire(SHOW_NMEA_STATUS);
                        State.getInstance().getPropertiesHolder().saveFor(getType(), showStatus);
                        return false;
                    }
                });
                optionsMenu.add(Menu.NONE, R.string.hide_nmea_status, Menu.NONE, R.string.hide_nmea_status).setVisible(false).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showStatus = false;
                        State.getInstance().fire(HIDE_NMEA_STATUS);
                        State.getInstance().getPropertiesHolder().saveFor(getType(), showStatus);
                        return false;
                    }
                });
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.show_nmea_status).setVisible(!showStatus);
                optionsMenu.findItem(R.string.hide_nmea_status).setVisible(showStatus);
                break;
            case SHOW_NMEA_STATUS:
//                layoutGpsStatus.findViewById(R.id.layout_nmea_info).setVisibility(View.GONE);
                ViewGroup mainView = context.findViewById(R.id.content);
                mainView.addView(layoutGpsStatus);
                layoutGpsStatus.setVisibility(View.VISIBLE);
                ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).addGpsStatusListener(this);
                break;
            case HIDE_NMEA_STATUS:
                layoutGpsStatus.setVisibility(View.INVISIBLE);
                ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).removeGpsStatusListener(this);
                mainView = context.findViewById(R.id.content);
                mainView.removeView(layoutGpsStatus);

                break;
        }
        return true;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        @SuppressWarnings("ConstantConditions") @SuppressLint("MissingPermission") GpsStatus mStatus = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).getGpsStatus(null); //NON-NLS
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
//                info = "gps started";
//                tvAlt.setText(info);
                // Do Something with mStatus info
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
//                info = "gps stopped";
//                tvAlt.setText(info);
                // Do Something with mStatus info
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
//                info = "gps first fix: " + mStatus.getTimeToFirstFix();
//                tvAlt.setText(info);
                // Do Something with mStatus info
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                int total = 0;
                int active = 0;
                int fix = 0;
                float maxSnr = 0;
                layoutSatellites.removeAllViews();
                Iterator<GpsSatellite> iter = mStatus.getSatellites().iterator();

                while(iter.hasNext()){
                    GpsSatellite entry = iter.next();
                    total++;
                    if(entry.getSnr() > maxSnr) maxSnr = entry.getSnr();

                    if(entry.getSnr() < 10) continue;

                    LinearLayout view = (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_nmea_status_satellite, null);
                    int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, context.getResources().getDisplayMetrics());

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
                    view.setLayoutParams(params);

                    ProgressBar bar = view.findViewById(R.id.bar);

//                    bar.setMax(50);
                    bar.setProgress((int) entry.getSnr());
//                    bar.setProgress(entry.);

                    TextView number = view.findViewById(R.id.number);
                    TextView snr = view.findViewById(R.id.snr);
                    snr.setText(String.valueOf(entry.getSnr()));
                    number.setText(String.valueOf(entry.getPrn()));//+"\n"+/*entry.getAzimuth()+"\n"+*/entry.getElevation()+"\n"+
//                            (entry.usedInFix() ? "F" : "-") +
//                            (entry.hasAlmanac() ? "A" : "-")+
//                            (entry.hasEphemeris() ? "E" : "-"));
                    layoutSatellites.addView(view);

                    active++;
                    if(entry.usedInFix()) {
                        fix ++;
                        number.setTextColor(Color.BLACK);
                        snr.setTextColor(Color.BLUE);
                    } else if(entry.getSnr() > 10) {
                        number.setTextColor(Color.BLACK);
                        snr.setTextColor(Color.BLACK);
                    }
                }
                iter = mStatus.getSatellites().iterator();
                while(iter.hasNext()){
                    GpsSatellite entry = iter.next();
                    if(entry.getSnr() >= 10) continue;

                    LinearLayout view = (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_nmea_status_satellite, null);
                    int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, context.getResources().getDisplayMetrics());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
                    view.setLayoutParams(params);

                    ProgressBar bar = view.findViewById(R.id.bar);

//                    bar.setMax(50);
                    bar.setProgress((int) entry.getSnr());
//                    bar.setProgress(entry.);

                    TextView number = view.findViewById(R.id.number);
                    TextView snr = view.findViewById(R.id.snr);
                    snr.setText(String.valueOf(+entry.getSnr()));
                    number.setText(String.valueOf(entry.getPrn()));//+"\n"+/*entry.getAzimuth()+"\n"+*/entry.getElevation()+"\n"+
//                            (entry.usedInFix() ? "F" : "-") +
//                            (entry.hasAlmanac() ? "A" : "-")+
//                            (entry.hasEphemeris() ? "E" : "-"));
                    layoutSatellites.addView(view);

                    number.setTextColor(Color.GRAY);
                    snr.setTextColor(Color.GRAY);
                }

                ((TextView)layoutGpsStatus.findViewById(R.id.total)).setText(String.valueOf(total));
                ((TextView)layoutGpsStatus.findViewById(R.id.active)).setText(String.valueOf(active));
                ((TextView)layoutGpsStatus.findViewById(R.id.fix)).setText(String.valueOf(fix));
                ((TextView)layoutGpsStatus.findViewById(R.id.max_snr)).setText(String.valueOf(maxSnr));

//                info = "gps satellite status: " + i;
//                tvAlt.setText(info);

                // Do Something with mStatus info
                break;
        }
    }
}
