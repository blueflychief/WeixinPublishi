package com.infinite.weixincircle.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.infinite.weixincircle.R;


public class DotIndicatorView extends View {
    private int mDotNumber = 5;
    private int mDotCurrent = 1;
    private int mDotNormalRadius = 4;
    private int mDotSelectRadius = 6;
    private int mDotMargin = 4;
    private int mDotSelectColor = 0x55555555;
    private int mDotNormolColor = 0x55999999;
    private int mWidth;
    private int mHeight;
    private int mOrientation = 1;
    Paint mPaint;

    public DotIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DotIndicatorView);
        mOrientation = a.getInteger(R.styleable.DotIndicatorView_orientation, mOrientation);
        mDotNumber = a.getInteger(R.styleable.DotIndicatorView_dot_number, mDotNumber);
        mDotCurrent = a.getInteger(R.styleable.DotIndicatorView_dot_current, mDotCurrent);
        mDotMargin = (int) a.getDimension(R.styleable.DotIndicatorView_dot_margin, TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDotMargin,
                        getResources().getDisplayMetrics()));
        mDotNormalRadius = (int) a.getDimension(R.styleable.DotIndicatorView_dot_normal_radius, TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDotNormalRadius,
                        getResources().getDisplayMetrics()));
        mDotSelectRadius = (int) a.getDimension(R.styleable.DotIndicatorView_dot_select_radius, TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDotSelectRadius,
                        getResources().getDisplayMetrics()));
        mDotNormolColor = a.getColor(R.styleable.DotIndicatorView_dot_normol_color, mDotNormolColor);
        mDotSelectColor = a.getColor(R.styleable.DotIndicatorView_dot_select_color, mDotSelectColor);
        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = (mDotNumber - 1) * (mDotNormalRadius + mDotMargin) * 2 + (mDotSelectRadius + mDotMargin) * 2 + getPaddingLeft() + getPaddingRight();
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = (Math.max(mDotNormalRadius, mDotSelectRadius) + mDotMargin) * 2;
        }

        mWidth = mOrientation == 1 ? width : height;
        mHeight = mOrientation == 1 ? height : width;
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float start = mOrientation == 1 ? getPaddingLeft() + mDotMargin : getPaddingTop() + mDotMargin;
        float x;
        float y;
        y = (mOrientation == 1 ? mHeight : mWidth) * 1.0f / 2;
        for (int i = 0; i < mDotNumber; i++) {
            if (mDotCurrent == (i + 1)) {
                mPaint.setColor(mDotSelectColor);
                if (i > 0) {
                    x = start + mDotMargin * 2 + mDotSelectRadius + mDotNormalRadius;
                } else {
                    x = start + mDotSelectRadius;
                }
                canvas.drawCircle((mOrientation == 1 ? x : y), (mOrientation == 1 ? y : x), mDotSelectRadius, mPaint);
                start = x;
            } else {
                mPaint.setColor(mDotNormolColor);
                if (i > 0) {
                    x = start + (mDotNormalRadius + mDotMargin) * 2;
                } else {
                    x = start + mDotNormalRadius;
                }
                canvas.drawCircle(mOrientation == 1 ? x : y, mOrientation == 1 ? y : x, mDotNormalRadius, mPaint);
                start = x;
            }
        }

    }

    public void setCurrentDot(int currentDot) {
        if (currentDot <= mDotNumber && currentDot > 0) {
            mDotCurrent = currentDot;
        } else {
            mDotCurrent = mDotNumber;
        }
        invalidateView();
    }

    public void setDotNumber(int number) {
        mDotNumber = number;
        invalidateView();
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    private static final String INSTANCE_STATE = "instance_state";
    private static final String STATE_CURRENT = "state_dotcurrent";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(STATE_CURRENT, mDotCurrent);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mDotCurrent = bundle.getInt(STATE_CURRENT);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }

    }

}