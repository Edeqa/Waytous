package com.edeqa.waytous.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import static com.edeqa.waytous.helpers.SettingItem.GROUP;
import static com.edeqa.waytous.helpers.SettingItem.PAGE;

/**
 * Created 8/7/2017.
 */

@SuppressWarnings("WeakerAccess")
public class PreferenceDividerDecoration extends RecyclerView.ItemDecoration {

    private final Paint mPaint;
    private int heightGroup;
    private int heightOther;

    public PreferenceDividerDecoration(Context context) {
        this(context, Color.argb((int) (255 * 0.2), 0, 0, 0));
    }

    public PreferenceDividerDecoration(Context context, int color) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        heightGroup = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
        heightOther = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        if(heightOther < 1) heightOther = 1;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        int position = parent.getChildAdapterPosition(view);

        switch (Integer.valueOf(""+view.getTag())) {
            case PAGE:
                outRect.set(0, heightGroup, 0, heightOther);
                break;
            case GROUP:
                outRect.set(0, heightGroup, 0, -heightGroup);
                break;
            default:
                outRect.set(0, 0, 0, heightOther);
                break;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
//            int position = parent.getChildAdapterPosition(view);
            switch (Integer.valueOf(""+view.getTag())) {
                case PAGE:
                    c.drawRect(view.getLeft(), view.getTop() - heightGroup, view.getRight(), view.getTop(), mPaint);
                    c.drawRect(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom() + heightOther, mPaint);
                    break;
                case GROUP:
                    c.drawRect(view.getLeft(), view.getTop() - heightGroup, view.getRight(), view.getTop(), mPaint);
                    break;
                default:
                    c.drawRect(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom() + heightOther, mPaint);
                    break;
            }
        }
    }
}