package com.edeqa.waytous;

import com.edeqa.waytous.helpers.Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created 1/8/17.
 */

public class NotificationIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Utils.log(this, "onTokenRefresh:", refreshedToken);

        State.getInstance().setPreference("device_id", refreshedToken);
    }
}