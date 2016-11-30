package ru.wtg.whereaminow.holders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.EntityHolder;

import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.NEW_MESSAGE;
import static ru.wtg.whereaminow.State.PRIVATE_MESSAGE;
import static ru.wtg.whereaminow.State.SEND_MESSAGE;
import static ru.wtg.whereaminow.State.SHOW_MESSAGES;
import static ru.wtg.whereaminow.State.USER_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;

/**
 * Created 11/29/16.
 */
public class MapButtonsViewHolder extends AbstractViewHolder {

    public MapButtonsViewHolder(SupportMapFragment mapFragment) {

        View myLocationButton  = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(myLocationButton.getLayoutParams().width, myLocationButton.getLayoutParams().height);

        myLocationButton.setVisibility(View.VISIBLE);
        myLocationButton.setEnabled(true);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraViewHolder camera = (CameraViewHolder) State.getInstance().getEntityHolder(CameraViewHolder.TYPE);
                if(camera != null){
                    camera.onMyLocationButtonClickListener.onMyLocationButtonClick();
                }
            }
        });

        View zoomButtons = (View) mapFragment.getView().findViewWithTag("GoogleMapZoomInButton").getParent();

        int positionWidth = zoomButtons.getLayoutParams().width;
        int positionHeight = zoomButtons.getLayoutParams().height;

        RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(positionWidth, positionHeight);
        int margin = positionWidth / 5;
        zoomParams.setMargins(margin, 0, 0, margin);
        zoomParams.addRule(RelativeLayout.BELOW, myLocationButton.getId());
        zoomParams.addRule(RelativeLayout.ALIGN_LEFT, myLocationButton.getId());
        zoomButtons.setLayoutParams(zoomParams);

    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public boolean dependsOnEvent() {
        return false;
    }

    @Override
    public String[] getOwnEvents() {
        return new String[0];
    }

}
