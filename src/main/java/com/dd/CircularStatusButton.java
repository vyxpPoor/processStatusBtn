package com.dd;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.Button;


public class CircularStatusButton extends Button {


    private StrokeGradientDrawable background;
    private CircularAnimatedDrawable mAnimatedDrawable;
    private ColorStateList mIdleColorState;
    private StateListDrawable mIdleStateDrawable;


    private StatusEnum mState;
    private String mIdleText;

    private int mColorProgress;
    private int mColorIndicator;
    private int mColorIndicatorBackground;
    private int mStrokeWidth;
    private int mPaddingProgress;
    private float mCornerRadius;
    private boolean mConfigurationChanged;


    //追踪Path的坐标
    private PathMeasure mPathMeasure;
    //画圆的Path
    private Path mPathCircle;
    //截取PathMeasure中的path
    private Path mPathCircleDst;
    private Path successPath;
    private Path failurePathLeft;
    private Path failurePathRight;

    private Paint mPaint;
    private float circleValue;
    private float successValue;
    private float failValueRight;
    private float failValueLeft;
    private int loadSuccessColor;    //成功的颜色
    private int loadFailureColor;   //失败的颜色

    private ValueAnimator circleAnimator;


    public CircularStatusButton(Context context) {
        super(context);
        init(context, null);
    }

    public CircularStatusButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularStatusButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mStrokeWidth = (int) getContext().getResources().getDimension(R.dimen.cpb_stroke_width);
        initAttributes(context, attributeSet);
        mState = StatusEnum.Normal;
        setText(mIdleText);
        initIdleStateDrawable();
        setBackgroundCompat(mIdleStateDrawable);
        initPaint();
        initPath();
        initAnim();
    }

    private void initAnim() {
        circleAnimator = ValueAnimator.ofFloat(0, 1);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleValue = (float) animation.getAnimatedValue();
                if (circleValue != 1) {
                    invalidate();
                }
            }
        });
    }

    public void loadSuccess() {
        mAnimatedDrawable.stop();
        setStatus(StatusEnum.LoadSuccess);
        startSuccessAnim();
    }

    public void loadFailure() {
        mAnimatedDrawable.stop();
        setStatus(StatusEnum.LoadFailure);
        startFailAnim();
    }

    private void startFailAnim() {
        ValueAnimator failLeft = ValueAnimator.ofFloat(0f, 1.0f);
        failLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                failValueRight = (float) animation.getAnimatedValue();
                if (failValueRight != 1) {
                    invalidate();
                }
            }
        });
        ValueAnimator failRight = ValueAnimator.ofFloat(0f, 1.0f);
        failRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                failValueLeft = (float) animation.getAnimatedValue();
                if (failValueLeft != 1) {
                    invalidate();
                }
            }
        });
        //组合动画,一先一后执行
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(failLeft).after(circleAnimator).before(failRight);
        animatorSet.setDuration(500);
        animatorSet.start();
    }

    private void setStatus(StatusEnum status) {
        mState = status;
        invalidate();
    }

    private void startSuccessAnim() {
        final ValueAnimator success = ValueAnimator.ofFloat(0f, 1.0f);
        success.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                successValue = (float) animation.getAnimatedValue();
                if (successValue != 1) {
                    invalidate();
                }
            }
        });
        //组合动画,一先一后执行
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(success).after(circleAnimator);
        animatorSet.setDuration(500);
        animatorSet.start();
    }


    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth((int) getContext().getResources().getDimension(R.dimen.cpb_stroke_width_circle));
        mPaint.setStrokeCap(Paint.Cap.ROUND);    //设置画笔为圆角笔触
    }

    private void initPath() {
        mPathCircle = new Path();
        mPathMeasure = new PathMeasure();
        mPathCircleDst = new Path();
        successPath = new Path();
        failurePathLeft = new Path();
        failurePathRight = new Path();
    }

    private void initIdleStateDrawable() {
        int colorNormal = getNormalColor(mIdleColorState);
        int colorPressed = getPressedColor(mIdleColorState);
        int colorFocused = getFocusedColor(mIdleColorState);
        int colorDisabled = getDisabledColor(mIdleColorState);
        if (background == null) {
            background = createDrawable(colorNormal);
        }
        StrokeGradientDrawable drawableDisabled = createDrawable(colorDisabled);
        StrokeGradientDrawable drawableFocused = createDrawable(colorFocused);
        StrokeGradientDrawable drawablePressed = createDrawable(colorPressed);
        mIdleStateDrawable = new StateListDrawable();

        mIdleStateDrawable.addState(new int[]{android.R.attr.state_pressed}, drawablePressed.getGradientDrawable());
        mIdleStateDrawable.addState(new int[]{android.R.attr.state_focused}, drawableFocused.getGradientDrawable());
        mIdleStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, drawableDisabled.getGradientDrawable());
        mIdleStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
    }


    private int getNormalColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_enabled}, 0);
    }

    private int getPressedColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_pressed}, 0);
    }

    private int getFocusedColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{android.R.attr.state_focused}, 0);
    }

    private int getDisabledColor(ColorStateList colorStateList) {
        return colorStateList.getColorForState(new int[]{-android.R.attr.state_enabled}, 0);
    }

    private StrokeGradientDrawable createDrawable(int color) {
//        GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.cpb_background).mutate();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(mCornerRadius);
        StrokeGradientDrawable strokeGradientDrawable = new StrokeGradientDrawable(drawable);
        strokeGradientDrawable.setStrokeColor(color);
        strokeGradientDrawable.setStrokeWidth(mStrokeWidth);
        return strokeGradientDrawable;
    }


    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, R.styleable.CircularProgressButton);
        if (attr == null) {
            return;
        }
        try {
            loadSuccessColor = attr.getColor(R.styleable.CircularProgressButton_load_success_color, ContextCompat.getColor(context, R.color.load_success));
            loadFailureColor = attr.getColor(R.styleable.CircularProgressButton_load_failure_color, ContextCompat.getColor(context, R.color.load_failure));
            mIdleText = attr.getString(R.styleable.CircularProgressButton_cpb_textIdle);

            mCornerRadius = attr.getDimension(R.styleable.CircularProgressButton_cpb_cornerRadius, 2);
            mPaddingProgress = attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_paddingProgress, 0);

            int blue = getColor(R.color.cpb_blue);
            int grey = getColor(R.color.cpb_grey);
            int white = getColor(R.color.cpb_white);
            int idleStateSelector = attr.getResourceId(R.styleable.CircularProgressButton_cpb_selectorIdle,
                    R.color.cpb_idle_state_selector);
            mIdleColorState = getResources().getColorStateList(idleStateSelector);
            mColorProgress = attr.getColor(R.styleable.CircularProgressButton_cpb_colorProgress, white);
            mColorIndicator = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicator, blue);
            mColorIndicatorBackground = attr.getColor(R.styleable.CircularProgressButton_cpb_colorIndicatorBackground, grey);
        } finally {
            attr.recycle();
        }
    }

    protected int getColor(int id) {
        return getResources().getColor(id);
    }

    protected TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (mState == StatusEnum.Loading) {
            drawIndeterminateProgress(canvas);
        } else if (mState == StatusEnum.LoadSuccess) {
            mPaint.setColor(loadSuccessColor);
            mPathCircle.addCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2 - mStrokeWidth / 2, Path.Direction.CW);
            mPathMeasure.setPath(mPathCircle, false);
            mPathMeasure.getSegment(0, circleValue * mPathMeasure.getLength(), mPathCircleDst, true);   //截取path并保存到mPathCircleDst中
            canvas.drawPath(mPathCircleDst, mPaint);
            if (circleValue == 1) {
                successPath.moveTo(getWidth() / 2 - 60, getHeight() / 5 * 3);
                successPath.lineTo(getWidth() / 2, getHeight() / 5 * 4);
                successPath.lineTo(getWidth() / 2 + 50, getHeight() / 7 * 2);
                mPathMeasure.nextContour();
                mPathMeasure.setPath(successPath, false);
                mPathMeasure.getSegment(0, successValue * mPathMeasure.getLength(), mPathCircleDst, true);
                canvas.drawPath(mPathCircleDst, mPaint);
            }
        } else if (mState == StatusEnum.LoadFailure) {
            mPaint.setColor(loadFailureColor);
            mPathCircle.addCircle(getWidth() / 2, getHeight() / 2, getHeight() / 2 - mStrokeWidth / 2, Path.Direction.CW);
            mPathMeasure.setPath(mPathCircle, false);
            mPathMeasure.getSegment(0, circleValue * mPathMeasure.getLength(), mPathCircleDst, true);
            canvas.drawPath(mPathCircleDst, mPaint);
            if (circleValue == 1) {  //表示圆画完了,可以画叉叉的右边部分
                failurePathRight.moveTo(getWidth() / 2 + 40, getHeight() / 5 + 20);
                failurePathRight.lineTo(getWidth() / 2 - 40, getHeight() / 5 * 4 - 20);
                mPathMeasure.nextContour();
                mPathMeasure.setPath(failurePathRight, false);
                mPathMeasure.getSegment(0, failValueRight * mPathMeasure.getLength(), mPathCircleDst, true);
                canvas.drawPath(mPathCircleDst, mPaint);
            }
            if (failValueRight == 1) {    //表示叉叉的右边部分画完了,可以画叉叉的左边部分
                failurePathLeft.moveTo(getWidth() / 2 - 40, getHeight() / 5 + 20);
                failurePathLeft.lineTo(getWidth() / 2 + 40, getHeight() / 5 * 4 - 20);
                mPathMeasure.nextContour();
                mPathMeasure.setPath(failurePathLeft, false);
                mPathMeasure.getSegment(0, failValueLeft * mPathMeasure.getLength(), mPathCircleDst, true);
                canvas.drawPath(mPathCircleDst, mPaint);
                delayClear();
            }
        }else if (mState==StatusEnum.DrawNo){
            delayToNormal();
        }
    }

    private void delayClear(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setStatus(StatusEnum.DrawNo);
                mPathCircleDst = new Path();
                circleValue = 0;
                failValueRight= 0;
                failValueLeft = 0;
            }
        }, 500);
    }

    private void delayToNormal() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                morphFaileToNoraml();
            }
        }, 100);
    }

    private void morphFaileToNoraml() {
        MorphingAnimation animation = createProgressMorphing(getHeight(), mCornerRadius, getHeight(), getWidth());
        animation.setFromColor(Color.WHITE);
        animation.setToColor(getNormalColor(mIdleColorState));
        animation.setFromStrokeColor(mColorIndicatorBackground);
        animation.setToStrokeColor(getNormalColor(mIdleColorState));
        animation.setListener(mErrorStateListener);
        animation.start();
    }

    private OnAnimationEndListener mErrorStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            mState = StatusEnum.Normal;
            setClickable(true);
        }
    };

    private void drawIndeterminateProgress(Canvas canvas) {
        if (mAnimatedDrawable == null) {
            int offset = (getWidth() - getHeight()) / 2;
            mAnimatedDrawable = new CircularAnimatedDrawable(mColorIndicator, mStrokeWidth);
            int left = offset + mPaddingProgress;
            int right = getWidth() - offset - mPaddingProgress;
            int bottom = getHeight() - mPaddingProgress;
            int top = mPaddingProgress;
            mAnimatedDrawable.setBounds(left, top, right, bottom);
            mAnimatedDrawable.setCallback(this);
            mAnimatedDrawable.start();
        } else {
            mAnimatedDrawable.draw(canvas);
            mAnimatedDrawable.start();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mAnimatedDrawable || super.verifyDrawable(who);
    }

    private MorphingAnimation createProgressMorphing(float fromCorner, float toCorner, int fromWidth, int toWidth) {
        MorphingAnimation animation = new MorphingAnimation(this, background);
        animation.setFromCornerRadius(fromCorner);
        animation.setToCornerRadius(toCorner);
        animation.setPadding(mPaddingProgress);
        animation.setFromWidth(fromWidth);
        animation.setToWidth(toWidth);
        if (mConfigurationChanged) {
            animation.setDuration(MorphingAnimation.DURATION_INSTANT);
        } else {
            animation.setDuration(MorphingAnimation.DURATION_NORMAL);
        }
        mConfigurationChanged = false;
        return animation;
    }

    private void morphToProgress() {
        setWidth(getWidth());
        setText("");
        MorphingAnimation animation = createProgressMorphing(mCornerRadius, getHeight(), getWidth(), getHeight());
        animation.setFromColor(getNormalColor(mIdleColorState));
        animation.setToColor(mColorProgress);
        animation.setFromStrokeColor(getNormalColor(mIdleColorState));
        animation.setToStrokeColor(mColorIndicatorBackground);
        animation.setListener(mProgressStateListener);
        animation.start();
    }

    private OnAnimationEndListener mProgressStateListener = new OnAnimationEndListener() {
        @Override
        public void onAnimationEnd() {
            setStatus(StatusEnum.Loading);
        }
    };


    /**
     * Set the View's background. Masks the API changes made in Jelly Bean.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void setBackgroundCompat(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    public void loadStart() {
        setClickable(false);
        morphToProgress();
    }


    public void setBackgroundColor(int color) {
        background.getGradientDrawable().setColor(color);
    }


}
