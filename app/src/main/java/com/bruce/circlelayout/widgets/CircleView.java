package com.bruce.circlelayout.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bruce.circlelayout.R;

/**
 * Created by Bruce on 2016/11/28.
 */

public class CircleView extends FrameLayout implements Checkable {

    private final static int DEFAULT_SIZE = 120;
    private final static float DEFAULT_FACTOR = 0.8f;

    private ImageView mBgView;
    private ImageView mCheckView;
    private boolean isChecked = false;

    private int mPosition;
    private float mAngle;

    private Drawable mBgDrawable;
    private Drawable mCheckDrawable;
    private LayoutParams mBgParams, mCheckParams;

    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if(attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CircleView);
            mBgDrawable = array.getDrawable(R.styleable.CircleView_bgIcon);
            mCheckDrawable = array.getDrawable(R.styleable.CircleView_checkIcon);
        }

        View view = View.inflate(getContext(), R.layout.circle_view, null);
        mBgView = (ImageView) view.findViewById(R.id.circle_view_bg_icon);
        mCheckView= (ImageView) view.findViewById(R.id.circle_view_check_vector);

        addView(view);
        if(mBgView != null && mBgDrawable != null) {
            mBgView.setImageDrawable(mBgDrawable);
        }
        if(mCheckView != null && mCheckDrawable != null) {
            mCheckView.setImageDrawable(mCheckDrawable);
        }
        mBgParams = new LayoutParams(DEFAULT_SIZE, DEFAULT_SIZE);
        mCheckParams = new LayoutParams(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = right - left;
        int height = bottom - top;
        mBgParams.width = width;
        mBgParams.height = height;
        mBgView.setLayoutParams(mBgParams);

        mCheckParams.width = (int) (width * DEFAULT_FACTOR);
        mCheckParams.height =  (int) (height * DEFAULT_FACTOR);
        mCheckParams.gravity = Gravity.CENTER;
        mCheckView.setLayoutParams(mCheckParams);
    }

    private void check(){
        mCheckView.setVisibility(VISIBLE);
        Drawable drawable = mCheckView.getDrawable();
        if(drawable != null && drawable instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable)drawable).start();
        }
        isChecked = true;
    }

    private void uncheck() {
        mCheckView.animate().alpha(0).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isChecked = false;
                        mCheckView.setVisibility(INVISIBLE);
                        mCheckView.setAlpha(1.0f);
                    }
                }).start();
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        isChecked = checked;
        if(isChecked) {
            check();
        } else {
            uncheck();
        }
        if(mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, isChecked);
        }
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public ImageView getBgView() {
        return mBgView;
    }

    public void setBgView(Drawable drawable) {
        mBgView.setImageDrawable(drawable);
    }

    public void setBgView(Bitmap bitmap) {
        mBgView.setImageBitmap(bitmap);
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return mOnCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    public static interface OnCheckedChangeListener {
        void onCheckedChanged(CircleView CircleView, boolean isChecked);
    }
}
