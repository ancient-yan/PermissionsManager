package com.gwchina.sdk.base.widget.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewAnimator;

import com.app.base.R;

import timber.log.Timber;

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-12 16:07
 */
public class GwRatingBar extends LinearLayout {

    private int mTotalStar;
    private int mCurrentStar;

    private final boolean mUseHalfStar;

    private Drawable mStarDrawable;
    private Drawable mHalfStarDrawable;
    private Drawable mEmptyStarDrawable;

    private int mStarMargin;
    private int mStarSize;//size of star view

    private TimeInterpolator mTimeInterpolator = new AccelerateDecelerateInterpolator();

    private static final int MAX_STAR = 20;
    private static final int DEFAULT_STAT = 5;

    private FtRatingBarListener mFtRatingBarListener;

    private boolean mCanOperation = true;

    public GwRatingBar(Context context) {
        this(context, null);
    }

    public GwRatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GwRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GwRatingBar);

        mTotalStar = typedArray.getInt(R.styleable.GwRatingBar_rb_star_count, DEFAULT_STAT);
        mCurrentStar = typedArray.getInt(R.styleable.GwRatingBar_rb_init_star, 1);

        if (mTotalStar > MAX_STAR) {
            mTotalStar = MAX_STAR;
        }

        mStarMargin = (int) typedArray.getDimension(R.styleable.GwRatingBar_rb_star_margin, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getContext().getResources().getDisplayMetrics()));

        mCanOperation = typedArray.getBoolean(R.styleable.GwRatingBar_rb_can_operation, true);
        mUseHalfStar = typedArray.getBoolean(R.styleable.GwRatingBar_rb_use_half_star, false);

        mStarDrawable = typedArray.getDrawable(R.styleable.GwRatingBar_rb_icon_star);
        mEmptyStarDrawable = typedArray.getDrawable(R.styleable.GwRatingBar_rb_icon_no_star);
        mHalfStarDrawable = typedArray.getDrawable(R.styleable.GwRatingBar_rb_icon_half_star);

        if (mStarDrawable == null) {
            mStarDrawable = ContextCompat.getDrawable(getContext(), R.drawable.icon_heart_full);
        }
        if (mEmptyStarDrawable == null) {
            mEmptyStarDrawable = ContextCompat.getDrawable(getContext(), R.drawable.icon_heart_empty);
        }
        if (mHalfStarDrawable == null) {
            mHalfStarDrawable = ContextCompat.getDrawable(getContext(), R.drawable.icon_heart_half);
        }

        mStarSize = (int) typedArray.getDimension(R.styleable.GwRatingBar_rb_star_size, 0);

        if (mStarSize == 0) {
            mStarSize = LayoutParams.WRAP_CONTENT;
        }

        typedArray.recycle();
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setClickable(false);
        checkSet(mCurrentStar);
        addStars();
    }

    private void addStars() {
        LayoutParams firstLayoutParams = new LayoutParams(mStarSize, mStarSize);
        LayoutParams layoutParams = new LayoutParams(mStarSize, mStarSize);
        layoutParams.leftMargin = mStarMargin;

        ImageView imageView;

        int totalStar = mTotalStar;
        if (mUseHalfStar) {
            if (mTotalStar % 2 != 0) {
                throw new IllegalArgumentException("if use half star mode, the count of total star must be even");
            }
            totalStar = (mTotalStar / 2);
        }

        Timber.d("totalStar = %d", totalStar);

        for (int i = 0; i < totalStar; i++) {
            imageView = new AppCompatImageView(getContext(), null, R.style.Widget_AppCompat_Button_Borderless_Colored);

            imageView.setTag(i);
            imageView.setImageDrawable(getDrawableForCurrentStar(i));

            if (i == 0) {
                addView(imageView, firstLayoutParams);
            } else {
                addView(imageView, layoutParams);
            }

            if (mCanOperation) {
                imageView.setOnClickListener(v -> changeChildStatus(indexOfChild(v), true, !mUseHalfStar));
            }
        }
    }

    public void setRatingBarListener(FtRatingBarListener ftRatingBarListener) {
        mFtRatingBarListener = ftRatingBarListener;
    }

    private void changeChildStatus(int target, boolean notify, boolean doAnimator) {

        int originStar = mCurrentStar;

        checkSet(target + 1);

        if (notify) {
            notifyChanged();
        }

        if (originStar == mCurrentStar) {
            return;
        }

        if (doAnimator) {
            animateChange(originStar);
        } else {
            normalChange();
        }
    }

    private void animateChange(int originStar) {

        boolean doZoomIn = mCurrentStar > originStar;
        int start = Math.min(mCurrentStar, originStar);
        int end = Math.max(mCurrentStar, originStar) - 1;

        float value = 1.5F;

        PropertyValuesHolder xPropertyValuesHolder = PropertyValuesHolder.ofFloat(ViewAnimator.SCALE_X.getName(), 1F, value, 1);
        PropertyValuesHolder yPropertyValuesHolder = PropertyValuesHolder.ofFloat(ViewAnimator.SCALE_Y.getName(), 1F, value, 1);

        if (doZoomIn) {
            for (int i = start; i <= end; i++) {
                ImageView imageView = (ImageView) getChildAt(i);
                ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(imageView, xPropertyValuesHolder, yPropertyValuesHolder);
                objectAnimator.setDuration(300);
                objectAnimator.setInterpolator(mTimeInterpolator);
                objectAnimator.start();
                objectAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageView.setImageDrawable(mStarDrawable);
                    }
                });
            }
        } else {
            for (int i = end; i >= start; i--) {
                ImageView imageView = (ImageView) getChildAt(i);
                ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(imageView, xPropertyValuesHolder, yPropertyValuesHolder);
                objectAnimator.setDuration(200);
                objectAnimator.setInterpolator(mTimeInterpolator);
                objectAnimator.start();
                objectAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageView.setImageDrawable(mEmptyStarDrawable);
                    }
                });
            }
        }
    }

    private void normalChange() {
        int childCount = getChildCount();
        Timber.d("childCount = %d", childCount);
        ImageView imageView;
        for (int i = 0; i < childCount; i++) {
            imageView = (ImageView) getChildAt(i);
            imageView.setImageDrawable(getDrawableForCurrentStar(i));
        }
    }

    private Drawable getDrawableForCurrentStar(int index) {
        if (mUseHalfStar) {
            int realStar = mCurrentStar / 2 + mCurrentStar % 2;
            if (index == realStar - 1) {
                if (mCurrentStar % 2 != 0) {
                    return mHalfStarDrawable;
                } else {
                    return mStarDrawable;
                }
            } else if (index < realStar - 1) {
                return mStarDrawable;
            } else {
                return mEmptyStarDrawable;
            }
        } else {
            if (index <= mCurrentStar - 1) {
                return mStarDrawable;
            } else {
                return mEmptyStarDrawable;
            }
        }
    }

    public int getStar() {
        return mCurrentStar;
    }

    public void setStar(int star) {
        post(() -> changeChildStatus(star - 1/*to index*/, false, false));
    }

    public void setStarAndNotify(int start) {
        post(() -> changeChildStatus(start - 1/*to index*/, true, false));
    }

    private void checkSet(int currentStar) {
        if (currentStar > mTotalStar) {
            currentStar = mTotalStar;
        }
        if (currentStar < 1) {
            currentStar = 1;
        }
        mCurrentStar = currentStar;
    }

    private void notifyChanged() {
        if (mFtRatingBarListener != null) {
            mFtRatingBarListener.onStarChanged(mCurrentStar);
        }
    }

    public interface FtRatingBarListener {
        void onStarChanged(int start);
    }

}
