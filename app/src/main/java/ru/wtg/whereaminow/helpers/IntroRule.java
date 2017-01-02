package ru.wtg.whereaminow.helpers;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created 12/31/16.
 */

public class IntroRule {

    public static final int LINK_TO_OPTIONS_MENU = 1;
    public static final int LINK_TO_OPTIONS_MENU_ITEM = 2;
    public static final int LINK_TO_CENTER = 3;
    public static final int LINK_TO_CENTER_OF_VIEW = 4;
    public static final int LINK_TO_DRAWER_BUTTON = 5;
    public static final int LINK_TO_CONTEXT_MENU = 6;
    public static final int LINK_TO_DIALOG = 7;

    private String id;
    private String title;
    private String description;
    private int imageToShow;
    private String event;

    private int viewId;
    private String viewTag;
    private View view;
    private int linkTo;
    private LatLng latLng;
    private int order;

    public String getId() {
        return id;
    }

    public IntroRule setId(String id) {
        this.id = id;
        return this;
    }

    public View getView() {
        return view;
    }

    public IntroRule setView(View view) {
        this.view = view;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public IntroRule setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getLinkTo() {
        return linkTo;
    }

    public IntroRule setLinkTo(int linkTo) {
        this.linkTo = linkTo;
        return this;
    }

    public int getImageToShow() {
        return imageToShow;
    }

    public IntroRule setImageToShow(int imageToShow) {
        this.imageToShow = imageToShow;
        return this;
    }

    public String getEvent() {
        return event;
    }

    public IntroRule setEvent(String event) {
        this.event = event;
        return this;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public IntroRule setLatLng(LatLng latLng) {
        this.latLng = latLng;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public IntroRule setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getViewId() {
        return viewId;
    }

    public IntroRule setViewId(int viewId) {
        this.viewId = viewId;
        return this;
    }

    public String getViewTag() {
        return viewTag;
    }

    public IntroRule setViewTag(String viewTag) {
        this.viewTag = viewTag;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
