package com.edeqa.waytous.helpers;

import android.content.SharedPreferences;

/**
 * Created 7/28/17.
 */

public class SettingItem {

    private static SharedPreferences sharedPreferences;

    private String id;
    private String title;
    private String summary;
    private boolean screen;
    private int type;
    private boolean checked;
    private String[] depended;

    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        SettingItem.sharedPreferences = sharedPreferences;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isScreen() {
        return screen;
    }

    public void setScreen(boolean screen) {
        this.screen = screen;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getDepended() {
        return depended;
    }

    public void setDepended(String[] depended) {
        this.depended = depended;
    }
}
