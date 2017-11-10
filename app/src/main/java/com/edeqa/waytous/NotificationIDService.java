package com.edeqa.waytous;

import com.edeqa.waytous.helpers.Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.edeqa.waytous.Constants.REQUEST_UID;

/**
 * Created 1/8/17.
 */

public class NotificationIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Utils.log(this, "onTokenRefresh:", refreshedToken); //NON-NLS

        State.getInstance().setPreference(REQUEST_UID, refreshedToken);
    }
}