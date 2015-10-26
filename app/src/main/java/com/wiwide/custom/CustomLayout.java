package com.wiwide.custom;

/**
 * Created by DC-ADMIN on 15-9-13.
 */
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CustomLayout extends LinearLayout {

    int downX;
    int firstX;
    int lastX;
    int downY;
    int lastY;
    int screenWidth;
    OnFinishActivity finishActivity;
    Context ctx;
    Activity myActivity;
    View childView;

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        myActivity = (Activity) context;
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        childView = getChildAt(0);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        // TODO Auto-generated method stub
        super.onLayout(changed, left, top, right, bottom);
        screenWidth = right;
        childView.layout(0, 0, right, bottom);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) e.getX();
                downY = (int) e.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                lastX = (int) e.getX();
                lastY = (int) e.getY();
                int tempX = lastX - downX;
                int tempY = lastY - downY;
                if (Math.abs(tempY) > 8)
                    return false;
                if (tempX > 50)
                    return true;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) e.getX();
                firstX = downX;
                break;

            case MotionEvent.ACTION_MOVE:
                lastX = (int) e.getX();
                int deltaX = lastX - downX;
                int newScrollX = getScrollX() - deltaX;
                if (newScrollX <= 0 && newScrollX > -screenWidth)
                    scrollTo(newScrollX, 0);
                downX = lastX;
                break;
            case MotionEvent.ACTION_UP:
                int upX = (int) e.getX();
                int dis = upX - firstX;
                ScrollActivity anim = null;
                if (dis > 100) {
                    if (upX > screenWidth / 2) {
                        // 滑动距离超过100，而且手指达到屏幕1/3处
                        // 动画形式关闭当前的activity
                        // 具体方法留给用户写
                        // finishActivity.finishActivity();
                        anim = new ScrollActivity(this, getScrollX(), -screenWidth);
                        anim.setAnimationListener(new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation arg0) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {
                                // 动画结束，结束activity
                                myActivity.finish();
                            }
                        });
                    } else {
                        // activity重新运动到铺满屏幕
                        anim = new ScrollActivity(this, getScrollX(), 0);
                    }

                } else {
                    // activity重新运动到铺满屏幕
                    Log.e("----------", getScrollX() + "   --------");
                    anim = new ScrollActivity(this, getScrollX(), 0);
                }
                if (anim != null)
                    startAnimation(anim);
                break;
        }
        return true;
    }

    private class ScrollActivity extends Animation {
        // //动画结束时候是否结束该activity
        // boolean ifFinish = false;
        private View activity;
        private int startX, endX;
        private int time;

        public ScrollActivity(View activity, int startX, int endX) {
            this.activity = activity;
            this.startX = startX;
            this.endX = endX;
            time = Math.abs((endX - startX));
            setDuration(time);
        }

        @Override
        protected void applyTransformation(float interpolatedTime,
                                           Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            int curX = (int) (startX + (endX - startX) * interpolatedTime);
            activity.scrollTo(curX, 0);
        }
    }

    public void finishActivity(OnFinishActivity finishActivity) {
        this.finishActivity = finishActivity;
    }

    public interface OnFinishActivity {
        public void finishActivity();
    }
}