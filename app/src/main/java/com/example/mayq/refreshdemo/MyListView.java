package com.example.mayq.refreshdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by mayq on 2016/12/8.
 */

public class MyListView extends ListView {
    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        /*
        String action = Utils.getActionName(ev);
        String intercept = result ? "拦住" : "不拦";
        Log.d("mayq", getClass().getSimpleName() + " onInterceptTouchEvent " + action + " " + intercept);
        */
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        /*
        String action = Utils.getActionName(event);
        String touch = result ? "处理" : "不处理";
        Log.d("mayq", getClass().getSimpleName() + " onTouchEvent " + action + " " + touch);
        */
        return result;
    }

}
