package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Map;

/**
 * Created by tujger on 9/23/16.
 */

public class RelativeLayoutMap extends RelativeLayout {

    OnTouchListener onTouchListener;

    public RelativeLayoutMap(Context context) {
        super(context);
    }

    public RelativeLayoutMap(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        onTouchListener.onTouch(this,ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }
}
