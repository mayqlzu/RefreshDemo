package com.example.mayq.refreshdemo;

import android.content.Context;
import android.view.MotionEvent;

/**
 * Created by mayq on 2016/12/8.
 */

public class Utils {
    /**
     * 调试发现，这个值不能太大，如果换算成px后超过屏幕高度（比如1280px），则不会显示了，
     * 估计和Android的绘图逻辑有关
     */
    public static final int MAX_PULL_DISTANCE_DP = 500;

    public static String getActionName(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return "按下";
            case MotionEvent.ACTION_MOVE:
                return "移动";
            case MotionEvent.ACTION_UP:
                return "抬起";
            default:
                return "其他";
        }
    }

    public static int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static int getMaxPullDistancePx(Context context) {
        return convertDpToPixel(context, MAX_PULL_DISTANCE_DP);
    }

}
