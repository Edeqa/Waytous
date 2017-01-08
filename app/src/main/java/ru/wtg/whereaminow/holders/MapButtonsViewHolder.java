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
        try {
            View myLocationButton = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");

            myLocationButton.setVisibility(View.VISIBLE);
            myLocationButton.setEnabled(true);
            myLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CameraViewHolder camera = (CameraViewHolder) State.getInstance().getEntityHolder(CameraViewHolder.TYPE);
                    if (camera != null) {
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
        } catch(Exception e){
            e.printStackTrace();
        }
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


 /*   @Override
    public ArrayList<IntroRule> getIntro() {

//        MarkerView markerView = (MarkerView) State.getInstance().getMe().getEntity(TYPE);
//        LatLng latLng = new LatLng(markerView.myUser.getLocation().getLatitude(), markerView.myUser.getLocation().getLongitude());

        ArrayList<IntroRule> rules = new ArrayList<>();
//        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("map_button_my_location").setViewTag("GoogleMapMyLocationButton").setTitle("My location").setDescription("Click here to center active member."));
//        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("fab_intro_menu").setView(fab_buttons).setTitle("Click any item to perform some action"));
//        rules.add(new IntroRule().setId("menu_set_name").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setTitle("Click menu"));

        return rules;
    }
*/
}
