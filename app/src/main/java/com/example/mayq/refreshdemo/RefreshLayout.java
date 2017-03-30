package com.example.mayq.refreshdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * Created by mayq on 2016/12/6.
 * <p>
 * TODO 怎么从不拦截切换到拦截？
 */

public class RefreshLayout extends LinearLayout {
    private RefreshView refreshView;
    private View child;

    private int refreshViewHeightWhenRelease;
    private int firstPointerId;
    private int scrollSlop;
    float yDown = Float.NaN;
    float yNow = Float.NaN;

    private static final float BASE_FRICTION = 1.5f;
    private static final int THRESHOLD_DP = 60;
    private static int THRESHOLD_PX;
    private final int MAX_PULL_DIS_PX;

    private OnRefreshListener listener;

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        MAX_PULL_DIS_PX = Utils.getMaxPullDistancePx(context);
        THRESHOLD_PX = Utils.convertDpToPixel(getContext(), THRESHOLD_DP);
        scrollSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setOrientation(LinearLayout.VERTICAL);

        refreshView = new RefreshView(context);
        addView(refreshView);
        // set layout param after addView()
        setRefreshViewVisibleHeight(0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // I can not get the child in constructor, so I do it here
        child = getChildAt(1);
    }

    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (child instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) child;
                /*
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
                        */
                return absListView.canScrollList(-1);
            } else {
                return child.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(child, -1);
        }
    }

    private void scrollChildToTop() {
        if (child instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) child;
            absListView.setSelection(0);
        } else {
            // TODO
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //boolean result = super.onInterceptTouchEvent(ev);
        //String action = Utils.getActionName(ev);
        //String intercept = result ? "拦住" : "不拦";
        //Log.d("mayq", getClass().getSimpleName()+ " onInterceptTouchEvent " + action + " " + intercept);


        // 默认不拦截
        boolean result = false;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录第一个手指的id和位置
                firstPointerId = ev.getPointerId(ev.getActionIndex());
                yDown = yNow = ev.getY();
                // 不拦截
                result = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // 只关注第一个手指
                if (ev.findPointerIndex(firstPointerId) >= 0) {
                    float y = ev.getY(0); //注意getY(index)

                    if (getRefreshViewVisibleHeight() > 0) {
                        // RefreshView可见的时候，肯定要拦截的
                        result = true;
                    } else {
                        // RefreshView不可见的时候
                        if (Math.abs(y - yDown) > scrollSlop /* 移动足够距离，消除手抖 */
                                && !canChildScrollUp() /* child已经触顶 */
                                && y > yDown /* 是下拉 */) {
                            result = true; //拦截
                        }
                    }
                }
                break;
            default:
                // 其他，都不拦截
                result = false;
                break;
        }

        //Log.d("mayq", "intercept, " + result);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
        boolean result = super.onTouchEvent(event);
        String action = Utils.getActionName(event);
        String touch = result ? "处理" : "不处理";
        Log.d("mayq", getClass().getSimpleName() + " onTouchEvent " + action + " " + touch);
        */

        // 默认，处理
        boolean result = true;
        switch (event.getAction()) {
            /* onInterceptTouchEvent()已经记录按下信息，这儿就不重复了
            case MotionEvent.ACTION_DOWN:
                firstPointerId = event.getPointerId(event.getActionIndex());
                yDown = event.getY();
                result = true;
                break;
                */
            case MotionEvent.ACTION_MOVE:
                if (event.findPointerIndex(firstPointerId) >= 0) {
                    /**
                     * 只关心第一个手指；
                     * 更新yNow，其他方法要用到这个值；
                     * 注意用getY(index)；
                     */
                    yNow = event.getY(0);

                    // onInterceptTouchEvent()已经处理手指抖动问题，所以这儿直接移动就行了

                    moveRefreshView();

                    // 处理
                    result = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 最后一次ACTION_MOVE的位置可能已经不是最新的位置了，更新yNow，
                yNow = event.getY();

                // 保护代码
                if (getRefreshViewVisibleHeight() > 0) {
                    if (pulledEnough()) {
                        // 下拉足够，触发刷新
                        reboundToHalfwayAndStartRefresh();
                    } else {
                        // 下拉不够，回弹
                        rebound();
                    }
                }

                // 处理
                result = true;
                break;
            default:
                // 其他，不处理
                result = false;
                break;
        }

        //Log.d("mayq", "touch, " + result);
        return result;
    }

    private boolean pulledEnough() {
        // 用真实的高度，不要用手指移动距离，因为要考虑手指“打滑”情况
        //return Math.abs(yNow - yDown) > THRESHOLD_PX
        return getRefreshViewVisibleHeight() > THRESHOLD_PX;
    }

    private void reboundToHalfwayAndStartRefresh() {
        //Log.d("mayq", "reboundToHalfwayAndStartRefresh()");
        refreshViewHeightWhenRelease = getRefreshViewVisibleHeight();

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "margin2", 0f, 1f);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                refreshView.startRefreshAnimation();
                if (null != listener) {
                    listener.onRefresh();
                }
            }
        });
        animator.setInterpolator(new DecelerateInterpolator());
        // 回弹时间和拉伸距离有关
        animator.setDuration(Math.min(refreshViewHeightWhenRelease - THRESHOLD_PX, 100));
        animator.start();
    }

    private void rebound() {
        refreshViewHeightWhenRelease = getRefreshViewVisibleHeight();

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "margin", 0f, 1f);
        animator.setInterpolator(new DecelerateInterpolator());
        // 回弹时间和拉伸距离有关
        animator.setDuration(Math.min(refreshViewHeightWhenRelease, 100));
        animator.start();
    }


    // 动画回调，回弹到顶部
    public void setMargin(float percent) {
        int newHeight = (int) (refreshViewHeightWhenRelease * (1 - percent));
        setRefreshViewVisibleHeight(newHeight);
        //invalidate(); why invalidate has no effect?
        requestLayout();
    }

    // 动画回调，回弹到半路并保持
    public void setMargin2(float percent) {
        int newHeight = (int) (THRESHOLD_PX +
                (refreshViewHeightWhenRelease - THRESHOLD_PX) * (1 - percent));
        setRefreshViewVisibleHeight(newHeight);
        //invalidate(); why invalidate has no effect?
        requestLayout();
    }

    private void setRefreshViewVisibleHeight(int height) {
        height = Math.min(height, MAX_PULL_DIS_PX);
        height = Math.max(height, 0);
        int topMargin = height - MAX_PULL_DIS_PX;
        ((LayoutParams) refreshView.getLayoutParams()).topMargin = topMargin;
        //Log.d("mayq", "newMargin=" + topMargin);
    }

    private int getRefreshViewVisibleHeight() {
        int topMargin = ((LayoutParams) refreshView.getLayoutParams()).topMargin;
        return MAX_PULL_DIS_PX + topMargin;
    }

    // use yDown and yNow
    private void moveRefreshView() {
        // 模拟越拉越费力效果
        int oldHeight = getRefreshViewVisibleHeight();
        //转成float再做除法，如果用int，结果总是0
        float percent = (float) oldHeight / MAX_PULL_DIS_PX;
        float friction = BASE_FRICTION * (1 + percent);

        int offset = (int) ((yNow - yDown) / friction);
        int newHeight = 0 + offset;
        setRefreshViewVisibleHeight(newHeight);
        //invalidate(); why invalidate has no effect?
        requestLayout();

        refreshView.setPullDistance(newHeight);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        //Log.d("mayq", "aha");
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public void setOnRefreshListener(OnRefreshListener l) {
        this.listener = l;
    }

    public void finishRefresh() {
        rebound();
        refreshView.stopRefreshAnimation();
    }

    public void performRefresh() {
        // 1和2如果一起调用，就会导致1无效，所以我把2放到了Runnable里

        // 1
        scrollChildToTop();

        post(new Runnable() {
            @Override
            public void run() {
                // 2
                setRefreshViewVisibleHeight(THRESHOLD_PX);
                requestLayout();

                refreshView.startRefreshAnimation();
                if (null != listener) {
                    listener.onRefresh();
                }
            }
        });
    }

}
