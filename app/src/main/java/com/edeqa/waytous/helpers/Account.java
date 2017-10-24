package com.edeqa.waytous.helpers;

import android.net.Uri;

/**
 * Created 10/21/17.
 */

public class Account {

    private String name;
    private String email;
    private String signProvider;
    private Long created;
    private Long changed;
    private Long synced;
    private Uri photoUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignProvider() {
        return signProvider;
    }

    public void setSignProvider(String signProvider) {
        this.signProvider = signProvider;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getChanged() {
        return changed;
    }

    public void setChanged(Long changed) {
        this.changed = changed;
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

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", signProvider='" + signProvider + '\'' +
                ", created=" + created +
                ", changed=" + changed +
                ", synced=" + synced +
                ", photoUrl=" + photoUrl +
                '}';
    }
}
