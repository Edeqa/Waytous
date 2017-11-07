package com.edeqa.waytous.helpers;

import android.net.Uri;

import java.io.Serializable;

import static com.edeqa.waytous.helpers.Account.SignProvider.FACEBOOK;
import static com.edeqa.waytous.helpers.Account.SignProvider.GOOGLE;
import static com.edeqa.waytous.helpers.Account.SignProvider.NONE;
import static com.edeqa.waytous.helpers.Account.SignProvider.PASSWORD;
import static com.edeqa.waytous.helpers.Account.SignProvider.TWITTER;

/**
 * Created 10/21/17.
 */

public class Account implements Serializable{

    private static final long serialVersionUID = 8336921607194270112L;

    private Uri photoUrl;
    private String name;
    private String email;
    private SignProvider signProvider;
    private String uid;
    private Long synced;
    private boolean anonymous;
    private boolean emailVerified;

    public Account() {
        signProvider = SignProvider.NONE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SignProvider getSignProvider() {
        return signProvider;
    }

    public void setSignProvider(SignProvider signProvider) {
        this.signProvider = signProvider;
    }

    public void setSignProvider(String signProvider) {
        if(signProvider != null) {
            switch(signProvider) {
                case "google.com":
                    setSignProvider(GOOGLE);
                    break;
                case "facebook.com":
                    setSignProvider(FACEBOOK);
                    break;
                case "twitter.com":
                    setSignProvider(TWITTER);
                    break;
                case "password":
                    setSignProvider(PASSWORD);
                    break;
                default:
                    setSignProvider(NONE);
            }
        } else {
            setSignProvider(SignProvider.NONE);
        }
    }

    public Long getSynced() {
        return synced;
    }

    public void setSynced(Long synced) {
        this.synced = synced;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public enum SignProvider {
        GOOGLE("google.com"), FACEBOOK("facebook.com"), TWITTER("twitter.com"), NONE("anonymous"), PASSWORD("password");
        private String id;
        SignProvider(String id) {
            this.id = id;
        }
        public String toString() {
            return id;
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        return "Account{" +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", signProvider=" + signProvider +
                ", photoUrl=" + photoUrl +
                ", synced=" + synced +
                ", anonymous=" + anonymous +
                ", emailVerified=" + emailVerified +
                '}';
    }
}
