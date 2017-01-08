package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.util.ArrayList;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.State.ACTIVITY_RESULT;
import static ru.wtg.whereaminow.State.PREPARE_FAB;

/**
 * Created 12/03/16.
 */
public class FacebookViewHolder extends AbstractViewHolder {

    public static final String TYPE = "facebook";

    private Activity context;
    private CallbackManager callbackManager;
    private FabViewHolder fab;

    public FacebookViewHolder(Activity context) {
        this.context = context;
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        AppEventsLogger.activateApp(context);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case PREPARE_FAB:
                fab = (FabViewHolder) object;
                if(State.getInstance().tracking_active()) {
                    fab.add(R.string.share_to_facebook, R.drawable.ic_facebook_white).setOnClickListener(onClickListener);
                }
                break;
            case ACTIVITY_RESULT:
                Bundle m = (Bundle) object;
                if(m != null){
                    int requestCode = m.getInt("requestCode");
                    int resultCode = m.getInt("resultCode");
                    Intent data = m.getParcelable("data");
                    callbackManager.onActivityResult(requestCode, resultCode, data);
                }
        }
        return true;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            fab.close(true);
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                String welcomeMessage = "Click here to follow me\nusing the Where Am I Now service";
                if(State.getInstance().getTracking() != null  && State.getInstance().getTracking().getWelcomeMessage() != null
                        && State.getInstance().getTracking().getWelcomeMessage().length() > 0){
                    welcomeMessage = State.getInstance().getTracking().getWelcomeMessage();
                }

                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("Follow me at Where Am I Now")
                        .setContentDescription(welcomeMessage)
                        .setContentUrl(Uri.parse("https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken()))
                        .setImageUrl(Uri.parse("https://github.com/tujger/WhereAmINow/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png?raw=true"))
                        .build();

                ShareDialog shareDialog = new ShareDialog(context);
                // this part is optional
                callbackManager = CallbackManager.Factory.create();
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        System.out.println("Facebook onsuccess");
                    }

                    @Override
                    public void onCancel() {
                        System.out.println("Facebook oncancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        System.out.println("Facebook onerror");

                    }
                });
                System.out.println("CALLBACKMANAGER:"+callbackManager);
                shareDialog.show(linkContent);
            }
        }
    };

    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("facebook_intro").setViewId(R.string.share_to_facebook).setTitle("Here you can").setDescription("Share this group to Facebook."));

        return rules;
    }

}
