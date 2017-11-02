package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.Account;
import com.edeqa.waytous.helpers.CustomDialog;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.net.NetPermission;
import java.util.HashMap;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
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

    public static final String TYPE = "user_profile";
    private final Signer signer;

    private CustomDialog dialog;

    private Account account;

    public UserProfileViewHolder(final MainActivity context) {
        super(context);

        System.out.println("INITIALIZUSERPRO");
        Twitter.initialize(context);
        signer = new Signer();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Object object = State.getInstance().getPropertiesHolder().loadFor(TYPE);
                if(object != null) {
                    setAccount((Account) object);
                }

                /*FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                System.out.println("CURRENTUSER:"+currentUser);

                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if(dialog != null && dialog.isShowing()) showAccountDialog();
//                        Utils.log("onAuthStateChanged:"+firebaseAuth);
//                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//                        System.out.println("CURRENTUSER2:"+currentUser);
                    }
                });*/
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
                    signer.onActivityResult(requestCode, resultCode, data);
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

            switchDialogLayout(DialogMode.PROFILE, null);
            signProviderMode(account.getSignProvider());

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
            switchDialogLayout(DialogMode.SIGN_BUTTONS, null);

            ViewGroup layout = (ViewGroup) dialog.getLayout().findViewById(R.id.layout_sign_buttons);
            dialog.hideMenu();

            layout.findViewById(R.id.button_sign_with_email).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    signer.setMode(SignProviderMode.EMAIL);

                    switchDialogLayout(DialogMode.SIGN_IN_EMAIL, null);

                    final TextInputLayout etEmail = (TextInputLayout) dialog.getLayout().findViewById(R.id.et_email);
                    final TextInputLayout etPassword = (TextInputLayout) dialog.getLayout().findViewById(R.id.et_password);
                    final TextInputLayout etConfirmPassword = (TextInputLayout) dialog.getLayout().findViewById(R.id.et_confirm_password);

                    class Listeners {
                        OnClickListener onClickListenerSignIn = new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                etEmail.setErrorEnabled(false);
                                etPassword.setErrorEnabled(false);
                                if (etEmail.getEditText().getText().toString().length() == 0) {
                                    etEmail.setError("Enter e-mail.");
                                    etEmail.requestFocus();
                                } else if (etPassword.getEditText().getText().toString().length() == 0) {
                                    etPassword.setError("Enter password.");
                                    etPassword.requestFocus();
                                } else {
                                    switchDialogLayout(DialogMode.SIGNING, context.getString(R.string.signing_in_with_email));
                                    signer.setMode(SignProviderMode.EMAIL);
                                    signer.setCallbackElement(new HashMap<String, String>() {{
                                        put("login", etEmail.getEditText().getText().toString());
                                        put("password", etPassword.getEditText().getText().toString());
                                    }});
                                    signer.sign();
                                }
                            }
                        };
                        OnClickListener onClickListenerSignUpTry = new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                etEmail.setErrorEnabled(false);
                                etPassword.setErrorEnabled(false);
                                etConfirmPassword.setErrorEnabled(false);
                                if (etEmail.getEditText().getText().toString().length() == 0) {
                                    etEmail.setError("Enter e-mail.");
                                    etEmail.requestFocus();
                                } else if (etPassword.getEditText().getText().toString().length() == 0) {
                                    etPassword.setError("Enter password.");
                                    etPassword.requestFocus();
                                } else if (etConfirmPassword.getEditText().getText().toString().length() == 0) {
                                    etConfirmPassword.setError("Confirm password.");
                                    etConfirmPassword.requestFocus();
                                } else if (!etPassword.getEditText().getText().toString().equals(etConfirmPassword.getEditText().getText().toString())) {
                                    etConfirmPassword.setError("Password not confirmed.");
                                    etConfirmPassword.requestFocus();
//                                    switchDialogLayout(DialogMode.ERROR, "Password not confirmed.");
                                } else {
                                    switchDialogLayout(DialogMode.SIGNING, context.getString(R.string.signing_up_with_email));
                                    FirebaseAuth.getInstance()
                                            .createUserWithEmailAndPassword(etEmail.getEditText().getText().toString(), etPassword.getEditText().getText().toString())
                                            .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        signer.onSuccessListener.run();
                                                    } else {
                                                        onClickListenerBack.onClick(null);
                                                        switchDialogLayout(DialogMode.ERROR, task.getException().getMessage());
                                                    }
                                                }
                                            });
                                }
                            }
                        };
                        OnClickListener onClickListenerResetTry = new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                etEmail.setErrorEnabled(false);
                                if (etEmail.getEditText().getText().toString().length() == 0) {
                                    etEmail.setError("Enter e-mail.");
                                    etEmail.requestFocus();
                                } else {
                                    switchDialogLayout(DialogMode.SIGNING, context.getString(R.string.resetting_password));
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(etEmail.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, "E-mail with instructions has sent.", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            } else {
                                                onClickListenerBack.onClick(null);
                                                switchDialogLayout(DialogMode.ERROR, task.getException().getMessage());
                                            }
                                        }
                                    });
                                }
                            }
                        };
                        OnClickListener onClickListenerBack = new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                etEmail.setErrorEnabled(false);
                                etPassword.setErrorEnabled(false);
                                etConfirmPassword.setErrorEnabled(false);
                                switchDialogLayout(DialogMode.SIGN_IN_EMAIL, null);
                                etEmail.requestFocus();
                                dialog.getButton(BUTTON_POSITIVE).setOnClickListener(onClickListenerSignIn);
                                dialog.getButton(BUTTON_NEGATIVE).setOnClickListener(onClickListenerSignUp);
                            }
                        };
                        OnClickListener onClickListenerSignUp = new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                etEmail.setErrorEnabled(false);
                                etPassword.setErrorEnabled(false);
                                etConfirmPassword.setErrorEnabled(false);
                                switchDialogLayout(DialogMode.SIGN_UP_EMAIL, null);
                                dialog.getButton(BUTTON_POSITIVE).setOnClickListener(onClickListenerSignUpTry);
                                dialog.getButton(BUTTON_NEGATIVE).setOnClickListener(onClickListenerBack);
                            }
                        };
                    }

                    final Listeners listeners = new Listeners();

                    listeners.onClickListenerBack.onClick(null);

                    dialog.getLayout().findViewById(R.id.button_forgot_password).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchDialogLayout(DialogMode.RESET_EMAIL, null);
                            dialog.getButton(BUTTON_POSITIVE).setOnClickListener(listeners.onClickListenerResetTry);
                            dialog.getButton(BUTTON_NEGATIVE).setOnClickListener(listeners.onClickListenerBack);
                        }
                    });


                }
            });
            layout.findViewById(R.id.button_sign_with_facebook).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchDialogLayout(DialogMode.SIGNING, context.getString(R.string.signing_in_with_facebook));
                    signer.setMode(SignProviderMode.FACEBOOK);
                    signer.setCallbackElement(dialog.getLayout().findViewById(R.id.button_sign_with_facebook_origin));
                    signer.sign();
                }
            });
            layout.findViewById(R.id.button_sign_with_google).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchDialogLayout(DialogMode.SIGNING, context.getString(R.string.signing_in_with_google));
                    signer.setMode(SignProviderMode.GOOGLE);
                    signer.sign();
                }
            });
            layout.findViewById(R.id.button_sign_with_twitter).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchDialogLayout(DialogMode.SIGNING, context.getString(R.string.signing_in_with_twitter));
                    signer.setMode(SignProviderMode.TWITTER);
                    signer.setCallbackElement(dialog.getLayout().findViewById(R.id.button_sign_with_twitter_origin));
                    signer.sign();
                }
            });

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
        return account;
    }

    public void doGlobalSync() {
        if(fetchAccount() != null) {
            State.getInstance().fire(SYNC_PROFILE);
        }
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }


    private class Signer implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private AuthCredential credential;
        private SignProviderMode mode;
        private TwitterLoginButton twitterLoginButton;
        private CallbackManager facebookCallbackManager;
        private GoogleApiClient googleApiClient;
        private LoginButton facebookLoginButton;
        private HashMap<String, String> credentials;

        Signer() {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();
        }

        void sign() {
//            switchDialogLayout(DialogMode.SIGNING, null);
            switch (mode) {
                case EMAIL:
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(credentials.get("login"), credentials.get("password")).addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        onSuccessListener.run();
                                    } else {
                                        onFailureListener.call(task.getException());
                                    }
                                }
                            });
                    break;
                case FACEBOOK:
                    facebookCallbackManager = CallbackManager.Factory.create();
                    facebookLoginButton.setReadPermissions("email", "public_profile");
                    facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                            signToFirebase();
                        }

                        @Override
                        public void onCancel() {
                            onFailureListener.call(new Exception("Cancelled"));
                        }

                        @Override
                        public void onError(FacebookException error) {
                            onFailureListener.call(error);
                        }
                    });
                    facebookLoginButton.callOnClick();
                    break;
                case GOOGLE:
                    if(googleApiClient.isConnected()) {
                        googleApiClient.clearDefaultAccountAndReconnect();
                    } else {
                        googleApiClient.connect();
                    }
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    context.startActivityForResult(signInIntent, 117);

                    break;
                case NONE:
                    break;
                case TWITTER:
                    twitterLoginButton.setCallback(new Callback<TwitterSession>() {
                        @Override
                        public void success(Result<TwitterSession> result) {
                            credential = TwitterAuthProvider.getCredential(result.data.getAuthToken().token, result.data.getAuthToken().secret);
                            signToFirebase();
                        }
                        @Override
                        public void failure(TwitterException exception) {
                            onFailureListener.call(exception);
                        }
                    });
                    twitterLoginButton.callOnClick();
                    break;
            }

        }

        void signToFirebase() {
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
//                                FirebaseUser user = auth.getCurrentUser();
//                                Utils.log("signInWithCredential:success:"+user);
//
                        onSuccessListener.run();
                    } else {
                        onFailureListener.call(task.getException());
                    }
                }
            });

        }

        public SignProviderMode getMode() {
            return mode;
        }

        public void setMode(SignProviderMode mode) {
            this.mode = mode;
        }

        void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (mode) {
                case EMAIL:
                    break;
                case FACEBOOK:
                    if(facebookCallbackManager != null) {
                        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
                    }
                    break;
                case GOOGLE:
                    if (requestCode == 117) {
                        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                        if (result.isSuccess()) {
                            GoogleSignInAccount acct = result.getSignInAccount();
                            credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                            signToFirebase();
                        } else {
                            onFailureListener.call(new Exception("SIGNEDOUT"));
                        }
                    }
                    break;
                case NONE:
                    break;
                case TWITTER:
                    if(twitterLoginButton != null) {
                        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
                    }
                    break;
            }
        }

        public void setCallbackElement(Object element) {
            if(element == null) return;
            if(element instanceof TwitterLoginButton) {
                twitterLoginButton = (TwitterLoginButton) element;
            } else if(element instanceof LoginButton) {
                facebookLoginButton = (LoginButton) element;
            } else if(element instanceof HashMap) {
                credentials = (HashMap<String,String>) element;
            }
        }

        Runnable1<Throwable> onFailureListener = new Runnable1<Throwable>(){
            @Override
            public void call(Throwable arg) {
                Utils.err(arg);
                if(SignProviderMode.EMAIL.equals(mode)) {
                    switchDialogLayout(DialogMode.SIGN_IN_EMAIL, null);
                } else {
                    switchDialogLayout(DialogMode.SIGN_BUTTONS, null);
                }
                switchDialogLayout(DialogMode.ERROR, arg.getMessage());
                arg.printStackTrace();
            }
        };

        Runnable onSuccessListener = new Runnable(){
            @Override
            public void run() {
                if(dialog != null && dialog.isShowing()) {
                    showAccountDialog();
//                    switchDialogLayout(DialogMode.SIGN_BUTTONS);
                }

//                FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
//                    @Override
//                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
////                        Utils.log("onAuthStateChanged:"+firebaseAuth);
////                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
////                        System.out.println("CURRENTUSER2:"+currentUser);
//                    }
//                });
            }
        };

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

    }

    private enum DialogMode {
        SIGN_BUTTONS, PROFILE, SIGNING, ERROR, SIGN_IN_EMAIL, SIGN_UP_EMAIL, RESET_EMAIL
    }
    private void switchDialogLayout(final DialogMode mode, final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(dialog == null) return;

                if(mode.equals(DialogMode.ERROR)) {
                    ((TextView) dialog.getLayout().findViewById(R.id.tv_error)).setText(message);
                    dialog.getLayout().findViewById(R.id.layout_error).setVisibility(View.VISIBLE);
                    return;
                }

                dialog.getLayout().findViewById(R.id.layout_error).setVisibility(View.GONE);
                dialog.getLayout().findViewById(R.id.layout_email).setVisibility(View.GONE);
                dialog.getLayout().findViewById(R.id.layout_profile).setVisibility(View.GONE);
                dialog.getLayout().findViewById(R.id.layout_sign_buttons).setVisibility(View.GONE);
                dialog.getLayout().findViewById(R.id.layout_signing).setVisibility(View.GONE);

                dialog.getButton(BUTTON_POSITIVE).setVisibility(View.GONE);
                dialog.getButton(BUTTON_NEGATIVE).setVisibility(View.GONE);
                dialog.getButton(BUTTON_NEUTRAL).setVisibility(View.GONE);

                switch(mode) {
                    case PROFILE:
                        dialog.getLayout().findViewById(R.id.layout_profile).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setText("Close");

                        break;
                    case SIGN_BUTTONS:
                        dialog.getLayout().findViewById(R.id.layout_sign_buttons).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setText("Close");
                        break;
                    case RESET_EMAIL:
                        dialog.getLayout().findViewById(R.id.layout_email).setVisibility(View.VISIBLE);

                        dialog.getLayout().findViewById(R.id.et_email).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.et_password).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.et_confirm_password).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.button_forgot_password).setVisibility(View.GONE);

                        dialog.getLayout().findViewById(R.id.et_email).requestFocus();

                        dialog.getLayout().findViewById(R.id.et_email).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setText("Reset");

                        dialog.getButton(BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_NEGATIVE).setText("Back");

                        dialog.getButton(BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_NEUTRAL).setText("Close");
                        break;
                    case SIGN_IN_EMAIL:
                        dialog.getLayout().findViewById(R.id.layout_email).setVisibility(View.VISIBLE);

                        dialog.getLayout().findViewById(R.id.et_email).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.et_password).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.et_confirm_password).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.button_forgot_password).setVisibility(View.VISIBLE);

                        dialog.getLayout().findViewById(R.id.et_email).requestFocus();

                        dialog.getButton(BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setText("Sign in");

                        dialog.getButton(BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_NEGATIVE).setText("Sign up");

                        dialog.getButton(BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_NEUTRAL).setText("Close");
                        break;
                    case SIGN_UP_EMAIL:
                        dialog.getLayout().findViewById(R.id.layout_email).setVisibility(View.VISIBLE);

                        dialog.getLayout().findViewById(R.id.et_email).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.et_password).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.et_confirm_password).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.button_forgot_password).setVisibility(View.GONE);

                        dialog.getLayout().findViewById(R.id.et_email).requestFocus();

                        dialog.getButton(BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setText("Sign up");

                        dialog.getButton(BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_NEGATIVE).setText("Back");

                        dialog.getButton(BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_NEUTRAL).setText("Close");
                        break;
                    case SIGNING:
                        dialog.getLayout().findViewById(R.id.layout_signing).setVisibility(View.VISIBLE);
                        ((TextView) dialog.getLayout().findViewById(R.id.tv_signing)).setText(message);

                        dialog.getButton(BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                        dialog.getButton(BUTTON_POSITIVE).setText("Close");
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
                        dialog.getLayout().findViewById(R.id.iv_avatar_placeholder).setVisibility(View.INVISIBLE);
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

    private enum SignProviderMode {
        GOOGLE, FACEBOOK, TWITTER, NONE, EMAIL
    }
    private void signProviderMode(String mode) {
        switch(mode) {
            case "email":
                signProviderMode(SignProviderMode.EMAIL);
                break;
            case "facebook.com":
                signProviderMode(SignProviderMode.FACEBOOK);
                break;
            case "google.com":
                signProviderMode(SignProviderMode.GOOGLE);
                break;
            case "twitter.com":
                signProviderMode(SignProviderMode.TWITTER);
                break;
            default:
                signProviderMode(SignProviderMode.NONE);
        }
    }
    private void signProviderMode(final SignProviderMode mode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(dialog == null) return;
                switch(mode) {
                    case EMAIL:
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_email).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_facebook).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_google).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_twitter).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_provider_icon).setVisibility(View.VISIBLE);
                        break;
                    case FACEBOOK:
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_email).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_facebook).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_google).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_twitter).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_provider_icon).setVisibility(View.VISIBLE);
                        break;
                    case GOOGLE:
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_email).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_facebook).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_google).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_twitter).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_provider_icon).setVisibility(View.VISIBLE);
                        break;
                    case NONE:
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_email).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_facebook).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_google).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_twitter).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.layout_provider_icon).setVisibility(View.GONE);
                        break;
                    case TWITTER:
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_email).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_facebook).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_google).setVisibility(View.GONE);
                        dialog.getLayout().findViewById(R.id.iv_sign_provider_twitter).setVisibility(View.VISIBLE);
                        dialog.getLayout().findViewById(R.id.layout_provider_icon).setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }


}
