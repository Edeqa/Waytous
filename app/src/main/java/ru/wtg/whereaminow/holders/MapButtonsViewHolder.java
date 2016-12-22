package ru.wtg.whereaminow.holders;

import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.SupportMapFragment;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;

/**
 * Created 11/29/16.
 */
public class MapButtonsViewHolder extends AbstractViewHolder {

    public MapButtonsViewHolder(SupportMapFragment mapFragment) {

        View myLocationButton  = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");

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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(positionWidth, positionHeight);
        int margin = positionWidth / 5;
        params.setMargins(margin, 0, 0, margin);
        params.addRule(RelativeLayout.BELOW, myLocationButton.getId());
        params.addRule(RelativeLayout.ALIGN_LEFT, myLocationButton.getId());
        zoomButtons.setLayoutParams(params);

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

}
