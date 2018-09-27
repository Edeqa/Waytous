package com.edeqa.waytous.helpers;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar.SnackbarLayout;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created 7/15/16.
 */

@SuppressWarnings("unused")
public class LinearLayoutCompatBehavior extends CoordinatorLayout.Behavior<LinearLayoutCompat> {

    public LinearLayoutCompatBehavior(Context context, AttributeSet attrs) {
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayoutCompat child, View dependency) {
        return dependency instanceof SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayoutCompat child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, LinearLayoutCompat child, View dependency) {
        float translationY = Math.min(0, parent.getBottom() - child.getBottom());
        child.setTranslationY(translationY);
    }
}