package ru.wtg.whereaminow;

import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.View;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class IntroActivity extends MaterialIntroActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.colorPrimary)
                .buttonsColor(R.color.colorAccent)
                .image(R.drawable.intro_picture_1)
                .title("Sometimes...")
                .description("...we have to follow somebody, - walking, driving, bicycling, anyway. We can not know the destination point just because our leader leads us. But it is very easy to loss him! We can call and ask his location or we can be...")
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.colorPrimary)
                .buttonsColor(R.color.colorAccent)
                .image(R.drawable.intro_picture_2)
                .title(getString(R.string.app_name))
                .description("With this application we can create the navigation group and lead our friends or we can join to existing group. We can watch distances between us, our tracks, paths to everybody in the group. And more...")
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.colorPrimary)
                .buttonsColor(R.color.colorAccent)
                .image(R.drawable.intro_picture_3)
                .title("How much is it?")
                .description("Basically, it is free. We can use most of this functionality without any payment. Also, we can invite our friends without Android-devices - the device-independent version allows to join the group using web-browser.")
                .build());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.colorPrimary)
                        .buttonsColor(R.color.colorAccent)
//                        .possiblePermissions(new String[]{android.Manifest.permission.CALL_PHONE, android.Manifest.permission.READ_SMS})
                        .neededPermissions(new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
                        .image(R.drawable.intro_picture_4)
                        .title("What else?")
                        .description("Application must continuously get our locations. It seems using the GPS-sensor. Then we have grant application to access our location. Why not do it here? ")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Thank you for your trust!");
                    }
                }, "Grant"));
    }

}
