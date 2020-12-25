package com.iitp.iitp_demo.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;

/**
 * Android View utility
 */
public class ViewUtils{
    /**
     * set fading edge
     * @param view target view
     * @param dp   edge length
     */
    public static void setVerticalFadingEdge(View view, int dp) {
        view.setVerticalFadingEdgeEnabled(true);
        view.setFadingEdgeLength((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    /**
     * dp to px
     * @param context android context
     * @param dp      dp
     * @return px
     */
    public static float dp2px(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * sp to px
     * @param context android context
     * @param sp      sp
     * @return px
     */
    public static float sp2px(Context context, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static @ColorInt
    int parseColor(String colorString, @ColorInt int defaultColor) {
        try {
            return Color.parseColor(colorString);
        }
        catch (Exception e) {
            return defaultColor;
        }
    }
}
