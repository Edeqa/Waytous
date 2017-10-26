package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.edeqa.helpers.Misc;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.Account;
import com.edeqa.waytous.helpers.CustomDialog;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESULT;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.MAP_READY;
import static com.edeqa.waytous.helpers.Events.SYNC_PROFILE;

/**
 * Created 10/21/17.
 */

@SuppressWarnings("WeakerAccess")
public class UserProfileViewHolder extends AbstractViewHolder implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String SHOW_USER_PROFILE = "show_user_profile"; //NON-NLS

    public static final String TYPE = "user_profile";

    private CustomDialog dialog;
    private OnSignGoogleClickListener onSignGoogleClickListener;
    GoogleApiClient mGoogleApiClient;
    private Account account;

    public UserProfileViewHolder(final MainActivity context) {
        super(context);

        Twitter.initialize(context);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


        new Thread(new Runnable() {
            @Override
            public void run() {

                Object object = State.getInstance().getPropertiesHolder().loadFor(TYPE);
                if(object != null) {
                    setAccount((Account) object);
                }

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                System.out.println("CURRENTUSER:"+currentUser);

                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if(dialog != null && dialog.isShowing()) showAccountDialog();
//                        Utils.log("onAuthStateChanged:"+firebaseAuth);
//                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//                        System.out.println("CURRENTUSER2:"+currentUser);
                    }
                });
            }
        }).start();
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
            case ACTIVITY_RESULT:
                Bundle m = (Bundle) object;
                if(m != null){
                    int requestCode = m.getInt("requestCode");
                    int resultCode = m.getInt("resultCode");
                    Intent data = m.getParcelable("data");
                    Utils.log("ACTIVITY_RESULT:"+requestCode+":"+resultCode+":"+data);
                    if (requestCode == 117) {
//                        if(CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode() == requestCode) {

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

                                                    showDialogLayout(DialogMode.PROFILE);
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Utils.log("signInWithCredential:failure", task.getException());

                                                    showDialogLayout(DialogMode.SIGN_BUTTONS);
                                                }
                                            }
                                        });

                            } else {
                                // Signed out, show unauthenticated UI.
                                showDialogLayout(DialogMode.SIGN_BUTTONS);
                                Utils.log("SIGNEDOUT");
                            }
//                        }
                    }
                }
                break;
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
            case MAP_READY:
                doGlobalSync();
                break;
            case SHOW_USER_PROFILE:
                showAccountDialog();
                break;
            case SYNC_PROFILE:
                break;

        }
        return true;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    public void showAccountDialog() {

        if(dialog != null && dialog.isShowing()) dialog.dismiss();
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
                        showAccountDialog();
                        break;
                }
                return false;
            }
        });
//        }

        final Account account = fetchAccount();
        if (account != null) {
            dialog.setMenu(R.menu.dialog_user_profile_menu);

            showDialogLayout(DialogMode.PROFILE);

            final ViewGroup layout = (ViewGroup) dialog.getLayout().findViewById(R.id.layout_profile);
            if(account.getName() != null) {
                ((TextView) layout.findViewById(R.id.tv_name)).setText(account.getName());
            }
            if(account.getEmail() != null) {
                ((TextView) layout.findViewById(R.id.tv_email)).setText(account.getEmail());
            }
            final ImageView ivAvatar = (ImageView) layout.findViewById(R.id.iv_avatar);

            if(account.getPhotoUrl() != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            avatarMode(AvatarMode.LOADING);
                            final Bitmap bitmap = Utils.getImageBitmap(account.getPhotoUrl().toString());
                            if (bitmap != null) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ivAvatar.setImageBitmap(bitmap);
                                        ivAvatar.invalidate();

                                        avatarMode(AvatarMode.AVATAR);
                                    }
                                });
                            } else {
                                avatarMode(AvatarMode.PLACEHOLDER);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            avatarMode(AvatarMode.PLACEHOLDER);
                        }
                    }
                }).start();
            }
        } else {
            showDialogLayout(DialogMode.SIGN_BUTTONS);

            ViewGroup layout = (ViewGroup) dialog.getLayout().findViewById(R.id.layout_sign_buttons);
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
            Log.i("USER", Misc.toStringDeep(currentUser));
            if(currentUser != null) {
                account = new Account();
                account.setName(currentUser.getDisplayName());
                account.setEmail(currentUser.getEmail());
                account.setSignProvider(currentUser.getProviders().get(0));
                account.setPhotoUrl(currentUser.getPhotoUrl());
                account.setUid(currentUser.getUid());
                account.setAnonymous(currentUser.isAnonymous());
                account.setEmailVerified(currentUser.isEmailVerified());
            }
            Log.i("ACCOUNT", Misc.toStringDeep(account));
            Log.i("ID", FirebaseInstanceId.getInstance().getId());
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Utils.log("onConnectionFailed:"+connectionResult.getErrorMessage());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Utils.log("onConnected:"+bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Utils.log("onConnectionSuspended:"+i);

    }


    class OnSignGoogleClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            showDialogLayout(DialogMode.SIGNING);
            if(dialog != null) {
                ((TextView) dialog.getLayout().findViewById(R.id.tv_signing)).setText(R.string.signing_in_with_google);
            }

            if(mGoogleApiClient.isConnected()) {
                mGoogleApiClient.clearDefaultAccountAndReconnect();
            } else {
                mGoogleApiClient.connect();
            }

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//            context.startActivity(signInIntent);
            context.startActivityForResult(signInIntent, 117);

        }

    };

    OnClickListener onSignTwitterClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            TwitterLoginButton mLoginButton = (TwitterLoginButton) dialog.getLayout().findViewById(R.id.twitter_button);
            mLoginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    Log.d("tw", "twitterLogin:success" + result);
//                    handleTwitterSession(result.data);
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.w("tw", "twitterLogin:failure", exception);
//                    updateUI(null);
                }
            });
            mLoginButton.callOnClick();
        }
    };

    OnClickListener onSignEmailClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            System.out.println("sign with email");
        }
    };

    private enum DialogMode {
        SIGN_BUTTONS, PROFILE, SIGNING
    }
    private void showDialogLayout(final DialogMode mode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(dialog == null) return;
                switch(mode) {
                    case PROFILE:
                        dialog.getLayout().findViewById(R.id.layout_sign_buttons).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_signing).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_profile).setVisibility(View.VISIBLE);
                        break;
                    case SIGN_BUTTONS:
                        dialog.getLayout().findViewById(R.id.layout_profile).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_signing).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_sign_buttons).setVisibility(View.VISIBLE);
                        break;
                    case SIGNING:
                        dialog.getLayout().findViewById(R.id.layout_profile).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_sign_buttons).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_signing).setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    private enum AvatarMode {
        AVATAR, PLACEHOLDER, LOADING
    }
    private void avatarMode(final AvatarMode mode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(dialog == null) return;
                switch(mode) {
                    case AVATAR:
                        dialog.getLayout().findViewById(R.id.iv_avatar_placeholder).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.pb_avatar_loading).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_avatar).setVisibility(View.VISIBLE);
                        break;
                    case LOADING:
                        dialog.getLayout().findViewById(R.id.iv_avatar_placeholder).setVisibility(View.INVISIBLE);
                        dialog.getLayout().findViewById(R.id.pb_avatar_loading).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.iv_avatar).setVisibility(View.GONE);
                        break;
                    case PLACEHOLDER:
                        dialog.getLayout().findViewById(R.id.iv_avatar_placeholder).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.pb_avatar_loading).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_avatar).setVisibility(View.GONE);
                        break;
                }
            }
        });
    }


}
