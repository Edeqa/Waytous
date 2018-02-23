package com.edeqa.waytous.holders.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.core.Twitter;

import java.util.ArrayList;
import java.util.Map;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESULT;
import static com.edeqa.waytous.helpers.Events.PREPARE_FAB;
import static com.edeqa.waytous.holders.property.MessagesHolder.WELCOME_MESSAGE;


/**
 * Created 12/03/16.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class SocialViewHolder extends AbstractViewHolder {

    private CallbackManager callbackManager;
    private String welcomeMessage;
    private AlertDialog shareDialog;

    public SocialViewHolder(MainActivity context) {
        super(context);
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        AppEventsLogger.activateApp(context);
        Twitter.initialize(context);
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
            case FabViewHolder.PREPARE_SHARE_BUTTONS:
                //noinspection unchecked
                Map<String,Object> d = (Map<String, Object>) object;
                LinearLayout layout = (LinearLayout) d.get("layout");
                shareDialog = (AlertDialog) d.get("dialog");

                Button button = (Button) context.getLayoutInflater().inflate(R.layout.view_share_button, null);

                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_facebook_black, 0, 0, 0);
                button.setText(R.string.share_to_facebook);
                button.setOnClickListener(onFacebookClickListener);

                layout.addView(button);

                button = (Button) context.getLayoutInflater().inflate(R.layout.view_share_button, null);

                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_twitter_black, 0, 0, 0);
                button.setText(R.string.share_to_twitter);
                button.setOnClickListener(onTwitterClickListener);

                layout.addView(button);

                break;
            case ACTIVITY_RESULT:
                Bundle m = (Bundle) object;
                if(m != null){
                    int requestCode = m.getInt("requestCode");
                    if(CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode() == requestCode) {
                        int resultCode = m.getInt("resultCode");
                        Intent data = m.getParcelable("data");
                        callbackManager.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
            case WELCOME_MESSAGE:
                if(object != null) {
                    welcomeMessage = (String) object;
                }
                break;

        }
        return true;
    }

    private final View.OnClickListener onFacebookClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            fab.close(true);
            shareDialog.dismiss();
            if (ShareDialog.canShow(ShareLinkContent.class)) {
                String message = String.format(context.getString(R.string.click_here_to_follow_me_using_s), context.getString(R.string.app_name));
                if(welcomeMessage != null && welcomeMessage.length() > 0){
                    message = welcomeMessage;
                }

                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle(String.format(context.getString(R.string.follow_me_with_s), context.getString(R.string.app_name)))
                        .setContentDescription(message)
                        .setContentUrl(Uri.parse(State.getInstance().getTracking().getTrackingUri()))
                        .setImageUrl(Uri.parse(context.getString(R.string.logo_link)))
                        .build();

                ShareDialog shareDialog = new ShareDialog(context);
                // this part is optional
                callbackManager = CallbackManager.Factory.create();
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Utils.log(SocialViewHolder.this, "onFacebookClickListener:", "Facebook onsuccess");
                    }

                    @Override
                    public void onCancel() {
                        Utils.log(SocialViewHolder.this, "onFacebookClickListener:", "Facebook oncancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Utils.log(SocialViewHolder.this, "onFacebookClickListener:", "Facebook onerror");

                    }
                });
                Utils.log(SocialViewHolder.this, "onFacebookClickListener:", "CallbackManager="+callbackManager);
                shareDialog.show(linkContent);
            }
        }
    };

    private final View.OnClickListener onTwitterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            shareDialog.dismiss();
            String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s&url=%s", context.getString(R.string.follow_me_with_s, context.getString(R.string.app_name)), State.getInstance().getTracking().getTrackingUri());

            Uri uri = Uri.parse(tweetUrl);
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    };

    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("facebook_intro").setViewId(R.string.share_to_facebook).setTitle("Here you can").setDescription("Share this group to Facebook."));

        return rules;
    }

}
