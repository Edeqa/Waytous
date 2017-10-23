package com.edeqa.waytous.helpers;

/**
 * Created 10/21/17.
 */

public class Account {

    private String name;
    private String signProvider;
    private Long created;
    private Long changed;
    private Long synced;

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
}
