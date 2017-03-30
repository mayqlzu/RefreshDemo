package com.example.mayq.refreshdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;

/**
 * Created by mayq on 2016/12/8.
 * <p>
 * generic refresh view, you can customize it as you want
 */

public class RefreshView extends View {
    private final int HEIGHT;

    private Movie movie;
    private long moveStartTime = 0;
    private boolean playing = false;


    public RefreshView(Context context) {
        super(context);

        // 关闭硬件加速，否则gif不会显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        HEIGHT = Utils.getMaxPullDistancePx(context);

        movie = Movie.decodeStream(getResources().openRawResource(R.raw.jiazai));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, HEIGHT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.d("mayq", "onDraw()");

        int height = getMeasuredHeight();

        final long now = SystemClock.uptimeMillis();
        if (moveStartTime == 0) {
            moveStartTime = now;
        }

        final int relTime = playing ? (int) ((now - moveStartTime) % movie.duration()) : 0;
        movie.setTime(relTime);
        movie.draw(canvas, 0, height - movie.height());

        if (playing) {
            invalidate();
        }
    }

    /**
     * @param distance 当前下拉距离
     */
    public void setPullDistance(int distance) {
        // TODO
    }

    public void startRefreshAnimation() {
        moveStartTime = 0;
        playing = true;
        invalidate();
    }

    public void stopRefreshAnimation() {
        moveStartTime = 0;
        playing = false;
    }

}
