package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.Account;
import com.edeqa.waytous.helpers.CustomDialog;
import com.edeqa.waytous.helpers.CustomListDialog;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;
import com.edeqa.waytous.helpers.Utils;
import com.facebook.internal.CallbackManagerImpl;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESULT;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.MAP_READY;
import static com.edeqa.waytous.helpers.Events.SYNC_PROFILE;

/**
 * Created 10/21/17.
 */

@SuppressWarnings("WeakerAccess")
public class UserProfileViewHolder extends AbstractViewHolder {

    public static final String SHOW_USER_PROFILE = "show_user_profile"; //NON-NLS

    private CustomDialog dialog;
    private OnSignGoogleClickListener onSignGoogleClickListener;
    GoogleApiClient mGoogleApiClient;

    public UserProfileViewHolder(MainActivity context) {
        super(context);
        SettingItem.setSharedPreferences(State.getInstance().getSharedPreferences());
        SettingItem.setContext(context);

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Utils.log("onAuthStateChanged:"+firebaseAuth);
            }
        });
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case CREATE_DRAWER:
                DrawerViewHolder.ItemsHolder adder = (DrawerViewHolder.ItemsHolder) object;
                adder.add(R.id.drawer_section_miscellaneous, R.string.user_profile, R.string.user_profile, R.drawable.ic_person_black_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().fire(SHOW_USER_PROFILE);
                        return false;
                    }
                });
                break;
            case SHOW_USER_PROFILE:
                showAccountDialog();
                break;
            case MAP_READY:
                doGlobalSync();
                break;
            case ACTIVITY_RESULT:
                Bundle m = (Bundle) object;
                if(m != null){
                    int requestCode = m.getInt("requestCode");
                    int resultCode = m.getInt("resultCode");
                    Intent data = m.getParcelable("data");
                    if (requestCode == 117) {
//                        if(CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode() == requestCode) {
                            Utils.log("ACTIVITY_RESULT:"+requestCode+":"+resultCode+":"+data);

                            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

                            Utils.log("handleSignInResult:" + result.isSuccess());
                            if (result.isSuccess()) {
                                // Signed in successfully, show authenticated UI.
                                GoogleSignInAccount acct = result.getSignInAccount();

                                Log.i("SIGNEDINAS:",acct.getId()+":"+acct.getIdToken());

                                final FirebaseAuth auth = FirebaseAuth.getInstance();
                                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                                auth.signInWithCredential(credential)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    FirebaseUser user = auth.getCurrentUser();
                                                    Utils.log("signInWithCredential:success:"+user);
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Utils.log("signInWithCredential:failure", task.getException());
                                                }
                                            }
                                        });

                            } else {
                                // Signed out, show unauthenticated UI.
                                Utils.log("SIGNEDOUT");
                            }
//                        }
                    }
                }
                break;

        }
        return true;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    public void showAccountDialog() {

//        if(dialog == null) {
        dialog = new CustomDialog(context);
        dialog.setLayout(R.layout.dialog_user_profile);

        dialog.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                System.out.println(item);
                switch(item.getItemId()) {
                    case R.id.sign_out:

                        FirebaseAuth.getInstance().signOut();
                        break;
                }
                return false;
            }
        });
//        }

        Account account = fetchAccount();
        if (account != null) {
            dialog.setMenu(R.menu.dialog_user_profile_menu);
        } else {
            ViewGroup layout = (ViewGroup) dialog.getLayout().findViewById(R.id.layoutSignButtons);
            layout.setVisibility(View.VISIBLE);

            dialog.hideMenu();

            layout.findViewById(R.id.button_sign_with_facebook).setOnClickListener(onSignFacebookClickListener);
            layout.findViewById(R.id.button_sign_with_google).setOnClickListener(new OnSignGoogleClickListener());
            layout.findViewById(R.id.button_sign_with_twitter).setOnClickListener(onSignTwitterClickListener);
            layout.findViewById(R.id.button_sign_with_email).setOnClickListener(onSignEmailClickListener);

        }

        dialog.setTitle(R.string.user_profile);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.close), null);
        dialog.setOnCancelListener(null);

        if(!dialog.isShowing()) {
            dialog.show();
        }

    }

    public Account fetchAccount() {
        Account account = null;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth != null) {
            FirebaseUser currentUser = auth.getCurrentUser();
            Log.i("USER", String.valueOf(currentUser));
            if(currentUser != null) {
                account = new Account();
                account.setName(currentUser.getDisplayName());
                account.setEmail(currentUser.getEmail());
                account.setSignProvider(currentUser.getProviders().get(0));
                account.setPhotoUrl(currentUser.getPhotoUrl());
            }
            Log.i("ACCOUNT", String.valueOf(account));
        }
//        var data = firebase.auth().currentUser;
//        if(data) {
//            user = {};
//            data.providerData.forEach(function(item){
//                user = u.cloneAsObject(item);
//                return false;
//            });
//            user.uid = data.uid;
//        }
        return account;
    }

    public void doGlobalSync() {
        if(fetchAccount() != null) {
            State.getInstance().fire(SYNC_PROFILE);
        }
    }

    OnClickListener onSignFacebookClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    class OnSignGoogleClickListener implements OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onClick(View v) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addConnectionCallbacks(OnSignGoogleClickListener.this)
                    .addOnConnectionFailedListener(OnSignGoogleClickListener.this)
                    .build();

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.clearDefaultAccountAndReconnect();
            }
            context.startActivityForResult(signInIntent, 117);

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Utils.log("onConnectionFailed:"+connectionResult.getErrorMessage());
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            System.out.println("onConnected:"+bundle);
            mGoogleApiClient.clearDefaultAccountAndReconnect();
        }

        @Override
        public void onConnectionSuspended(int i) {
            System.out.println("onConnectionSuspended:"+i);

        }
    };

    OnClickListener onSignTwitterClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    OnClickListener onSignEmailClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            System.out.println("sign with email");
        }
    };

}
