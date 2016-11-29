package com.bruce.circlelayout.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.orhanobut.logger.Logger;

/**
 * Created by Bruce on 2016/11/28.
 */
public class CircleLayout extends ViewGroup implements View.OnClickListener{

    /**默认间隔角度 */
    private final static float DEFAULT_INTERVAL_ANGLE = 45f;

    private int mLayoutWidth, mLayoutHeight;
    private int mChildWidth, mChildHeight;
    private int mCenterWidth, mCenterHeight;
    private int mRadius;
    /**两图标间隔角度 */
    private float mIntervalAngle = DEFAULT_INTERVAL_ANGLE;

    private float mAngle = 0;
    private float mTouchStartAngle;

    private long mDuration = 500;
    private State mState = State.HIDE;
    private boolean isRotating = false;
    private boolean isCanRotated = true;

    private int mCheckedId = -1;

    private OnCheckedChangeListener mOnCheckedChangeListener;
    private GestureDetector mGestureDetector;
    private int mTappedViewPosition = -1;
    private View mTappedVeiw;
    private int mSelected = 0;

    public CircleLayout(Context context) {
        this(context, null);
    }

    public CircleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mGestureDetector = new GestureDetector(getContext(), new MGestureListener());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        for(int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if(child.getVisibility() == GONE) {
                continue;
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mLayoutWidth = r - l;
        mLayoutHeight = b - t;

        mChildWidth = getChildAt(0).getMeasuredWidth();
        mChildHeight = getChildAt(0).getMeasuredHeight();
        mRadius = (mLayoutWidth - mChildWidth) / 2;

        View centerChild = getChildAt(getChildCount() - 1);
        centerChild.setOnClickListener(this);
        mCenterWidth = centerChild.getMeasuredWidth();
        mCenterHeight = centerChild.getMeasuredHeight();

        int left, top, right, bottom;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if(child.getVisibility() == GONE) {
                continue;
            }

            left = mLayoutWidth / 2 - child.getMeasuredWidth() / 2;
            top = mLayoutHeight / 2 - child.getMeasuredHeight() / 2;
            right = mLayoutWidth / 2 + child.getMeasuredWidth() / 2;
            bottom = mLayoutHeight / 2 + child.getMeasuredHeight() / 2;

            child.layout(left, top, right, bottom);
            if (i != getChildCount() - 1) {
                CircleView circleView = (CircleView) child;
                circleView.setAngle(i * mIntervalAngle);
                circleView.setPosition(i);
            }
        }

    }

    private int getStartLeft() {
        return mLayoutWidth / 2 - mChildWidth / 2;
    }

    private int getStartTop() {
        return mLayoutHeight / 2 - mChildHeight / 2;
    }

    private int getEndLeft(int position, float angle){
        CircleView child = (CircleView) getChildAt(position);
        return (int) (mLayoutWidth / 2 + mRadius * Math.cos(Math.toRadians(child.getAngle() + angle))) - mChildWidth / 2;
    }

    private int getEndTop(int position, float angle){
        CircleView child = (CircleView) getChildAt(position);
        return (int) (mLayoutWidth / 2 + mRadius * Math.sin(Math.toRadians(child.getAngle() + angle))) - mChildHeight / 2;
    }

    /**
     * 该方法才是实现旋转的本质方法
     */
    private void chanageChildLayout() {
        int left, top;
        final int childCount = getChildCount() - 1;
        for(int i = 0; i < childCount; i++) {
            final CircleView child = (CircleView) getChildAt(i);
            if(child.getVisibility() == GONE) {
                continue;
            }
            left = getEndLeft(i, mAngle);
            top = getEndTop(i, mAngle);
            child.setX(left);
            child.setY(top);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled() || !isCanRotated) {
            return false;
        }
        if(mState != State.SHOW) {
            return false;
        }
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartAngle = getPositionAngle(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                float currentAngle = getPositionAngle(event.getX(), event.getY());
                mAngle += (mTouchStartAngle - currentAngle);
                chanageChildLayout();
                mTouchStartAngle = currentAngle;
                isRotating = true;
                break;

            case MotionEvent.ACTION_UP:
                isRotating = false;
                break;
        }
        return true;
    }

    private int pointToPosition(float x, float y) {
        float left, top;
        for (int i = 0; i < getChildCount(); i++) {
            left = getEndLeft(i, mAngle);
            top = getEndTop(i, mAngle);
            if(i == getChildCount() - 1) {
                if((left < x && (left + mCenterWidth) > x) && (top < y && (top + mCenterHeight) > y)) {
                    return i;
                }
            } else {
                if((left < x && (left + mChildWidth) > x) && (top < y && (top + mChildHeight) > y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private float getPositionAngle(float xTouch, float yTouch) {
        float x = xTouch - mLayoutWidth / 2f;
        float y = mLayoutHeight / 2f - yTouch;

        switch (getPositionQuadrant(x, y)) {
            case 1:
                return (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 2:
            case 3:
                return (float) (180 - (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI));
            case 4:
                return (float) (360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            default:
                return 0;
        }
    }
    /**
     * 根据坐标值获取坐标在几象限
     * @param x
     * @param y
     * @return
     */
    private int getPositionQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    /**
     * 移动动画， 展开与隐藏
     * @param view
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @param isShow 是否展开，标记每块的状态改变
     */
    private void translationAnimation(View view, float startX, float startY, float endX, float endY, final boolean isShow) {
        final AnimatorSet set = new AnimatorSet();
        ObjectAnimator viewXAnimator = ObjectAnimator.ofFloat(view, View.X, startX, endX);
        ObjectAnimator viewYAnimator = ObjectAnimator.ofFloat(view, View.Y, startY, endY);
        set.play(viewXAnimator).with(viewYAnimator);
        set.setDuration(mDuration);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = isShow ? State.SHOW : State.HIDE;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mState = State.SHOWING;
            }
        });
        set.start();
    }

    private void show() {
        for(int i = 0; i < getChildCount() - 1; i++) {
            translationAnimation(getChildAt(i), getStartLeft(), getStartTop(), getEndLeft(i, mAngle), getEndTop(i, mAngle), true);
        }
    }

    private void hide() {
        for(int i = 0; i < getChildCount() - 1; i++) {
            CircleView child = (CircleView) getChildAt(i);
            child.setChecked(false);
            translationAnimation(child, getEndLeft(i, mAngle), getEndTop(i, mAngle), getStartLeft(), getStartTop(),  false);
        }
    }

    public void showAndHide() {
        if(mState == State.SHOWING) {
            return ;
        }
        if(isRotating) {
            return ;
        }
        if(mState == State.SHOW) {
            hide();
        } else if(mState == State.HIDE) {
            show();
        }
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
        chanageChildLayout();
    }

    @Override
    public void onClick(View view) {
        showAndHide();
    }

    public void check(int id) {
        if(id != -1 && (id == mCheckedId)) {
            return ;
        }

        if(mCheckedId != -1) {
            setCheckedState(mCheckedId, false);
        }

        if(id != -1) {
            setCheckedState(id, true);
        }
        setCheckedId(id);
    }

    private void setCheckedId(int id) {
        mCheckedId = id;
        if(mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
        }
    }

    private void setCheckedState(int id, boolean checked) {
        View view = findViewById(id);
        if(view != null && view instanceof CircleView) {
            ((CircleView)view).setChecked(checked);
        }
    }

    public int getCheckedCircleView() {
        return mCheckedId;
    }

    public void clearCheck() {
        check(-1);
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return mOnCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    private class MGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            mTappedViewPosition = pointToPosition(e.getX(), e.getY());
            Logger.i("mTappedViewPosition = %d, x = %f, y = %f", mTappedViewPosition, e.getX(), e.getY());
            if(mTappedViewPosition >= 0) {
                mTappedVeiw = getChildAt(mTappedViewPosition);
                mTappedVeiw.setPressed(true);
            }
            if(mTappedVeiw != null) {
                if(mTappedVeiw instanceof CircleView) {
                    CircleView view = (CircleView) mTappedVeiw;
//                    if(mSelected == mTappedViewPosition) {
//
//                    }
                    view.toggle();
                }
                return true;
            }

            return super.onSingleTapUp(e);
        }
    }

    enum State {
        HIDE, SHOWING, SHOW
    }

    public static interface OnCheckedChangeListener {
        public void onCheckedChanged(CircleLayout circleLayout, int checkedId);
    }
}
