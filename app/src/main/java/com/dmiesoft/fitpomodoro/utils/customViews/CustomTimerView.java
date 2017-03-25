package com.dmiesoft.fitpomodoro.utils.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;

import java.util.Random;

public class CustomTimerView extends View {

//    public static final int CIRCLE_RESUME = 7235;
//    public static final int CIRCLE_STOP = 2864;
//    public static final int CIRCLE_PAUSE = 9898;

    private static int[] DRAWABLES_WORK = {
            R.drawable.ic_hard_working_dude,
            R.drawable.ic_tacka_dude
    };
    private static int[] DRAWABLES_SHORT_BREAK = {
            R.drawable.ic_biceps,
            R.drawable.ic_stretching_dude,
            R.drawable.ic_snatch_dude
    };
    private static int[] DRAWABLES_LONG_BREAK = {
            R.drawable.ic_pull_up_dude,
            R.drawable.ic_weightlifting_dude
    };

    private static final String TAG = "CTV";
    private static final float THICKNESS_SCALE = .05f;
    private static final String TIMER_BASE_COLOR = "#dbdbdb";
    private Paint mPomidorasPaint, mEraserPaint, mUnderPaint;
    private TextPaint mTimerTextPaint, mTimerTapHintPaint;
    private int mColor = Color.RED,
            mTextColor = Color.WHITE,
            mTimerHintTextColor,
            mTimerHintTextColor2,
            yTextPos,
            mStrokeWidth;
    private float mTextSize = getResources().getDimension(R.dimen.custom_timer_text_size);
    private String mTimerText, mTapHintText, mTapHintText2;
    private Bitmap mBitmap, vectorBitmap;
    private Canvas mCanvas;
    private RectF mCircleOuterBounds; //, mCircleInnerBounds;
    private float mCircleSweepAngle;
    private Context context;

    public CustomTimerView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomTimerView,
                0, 0);

        try {
            mColor = typedArray.getColor(R.styleable.CustomTimerView_color, Color.RED);
            mTextSize = typedArray.getDimension(R.styleable.CustomTimerView_textSize, getResources().getDimension(R.dimen.custom_timer_text_size));
            mTextColor = typedArray.getColor(R.styleable.CustomTimerView_textColor, Color.WHITE);
            mTimerText = typedArray.getString(R.styleable.CustomTimerView_text);
            mStrokeWidth = (int) typedArray.getDimension(R.styleable.CustomTimerView_strokeWidth, getResources().getDimension(R.dimen.custom_timer_stroke_width));
        } finally {
            typedArray.recycle();
        }

        init();
    }

    private void init() {
        mPomidorasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPomidorasPaint.setStyle(Paint.Style.STROKE);
        mPomidorasPaint.setStrokeWidth(mStrokeWidth);
        mPomidorasPaint.setColor(mColor);
        mPomidorasPaint.setTextSize(mTextSize);

        mUnderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnderPaint.setStyle(Paint.Style.STROKE);
        mUnderPaint.setStrokeWidth(mStrokeWidth);
        mUnderPaint.setColor(Color.parseColor(TIMER_BASE_COLOR));
        mUnderPaint.setShadowLayer(5, 1.5f, 1.5f, Color.BLACK);
        setLayerType(LAYER_TYPE_SOFTWARE, mUnderPaint);

//        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mEraserPaint.setColor(Color.TRANSPARENT);
//        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mTimerTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTimerTextPaint.setColor(mTextColor);
        if (mTextSize == 0) {
            mTextSize = mTimerTextPaint.getTextSize();
        } else {
            mTimerTextPaint.setTextSize(mTextSize);
        }
        mTimerTextPaint.setTextAlign(Paint.Align.CENTER);

        float mHintTextSize = mTextSize * 0.6f;
        mTimerHintTextColor = mTextColor;
        mTimerTapHintPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTimerTapHintPaint.setColor(mTimerHintTextColor);
        mTimerTapHintPaint.setTextSize(mHintTextSize);
        mTimerTapHintPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //kodel????
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        int x = getWidth();

        mCanvas.drawArc(mCircleOuterBounds, 0, 360, false, mUnderPaint);
        if (mCircleSweepAngle > 0f) {
            mCanvas.drawArc(mCircleOuterBounds, 270, -mCircleSweepAngle, false, mPomidorasPaint);
        }
//        mCanvas.drawOval(mCircleInnerBounds, mEraserPaint);
        canvas.drawBitmap(mBitmap, 0, 0, null);

        int xC = (x / 2);
        yTextPos = (int) ((canvas.getHeight() / 2) - ((mTimerTextPaint.descent() + mTimerTextPaint.ascent()) / 2));
        canvas.drawText(mTimerText, xC, yTextPos, mTimerTextPaint);

        int yHintTextPos = (int) (yTextPos + ((mTimerTapHintPaint.descent() - mTimerTapHintPaint.ascent())));
        int yHintTextPos2 = (int) (yTextPos + 2 * (mTimerTapHintPaint.descent() - mTimerTapHintPaint.ascent()));
        mTimerTapHintPaint.setColor(mTimerHintTextColor);
        canvas.drawText(mTapHintText, xC, yHintTextPos, mTimerTapHintPaint);
        mTimerTapHintPaint.setColor(mTimerHintTextColor2);
        canvas.drawText(mTapHintText2, xC, yHintTextPos2, mTimerTapHintPaint);

        int yBitmapPos = (int) ((yTextPos + (mTimerTextPaint.ascent())) - vectorBitmap.getHeight());
        canvas.drawBitmap(vectorBitmap, (xC - (vectorBitmap.getWidth() / 2)), yBitmapPos, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // kam tas bitmap ir mCanvas??????
        if (w != oldw || h != oldh) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mBitmap.eraseColor(Color.TRANSPARENT);
            mCanvas = new Canvas(mBitmap);
        }
        super.onSizeChanged(w, h, oldw, oldh);
        updateBounds();
    }

    private void updateBounds() {
        float thickness = getWidth() * THICKNESS_SCALE;
        mCircleOuterBounds = new RectF(0 + thickness, 0 + thickness, getWidth() - thickness, getHeight() - thickness);

        //nebereikia nes naudoju fill o ne
//        mCircleInnerBounds = new RectF(
//                mCircleOuterBounds.left + thickness,
//                mCircleOuterBounds.top + thickness,
//                mCircleOuterBounds.right - thickness,
//                mCircleOuterBounds.bottom - thickness
//        );
        invalidate();
    }

    public void drawProgress(float progress) {
        mCircleSweepAngle = 360 * progress;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mUnderPaint.setShadowLayer(15, 1.5f, 1.5f, Color.BLACK);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mUnderPaint.setShadowLayer(5, 1.5f, 1.5f, Color.BLACK);
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = (int) context.getResources().getDimension(R.dimen.timer_size);
        int desiredHeight = desiredWidth;

        int width;
        int height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = widthSize;
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = heightSize;
        } else {
            height = desiredHeight;
        }

        int finalDimen = Math.min(width, height);

        setMeasuredDimension(finalDimen, finalDimen);

    }

    public void setmTimerStateAndType(int timerState, int timerType) {
        Random rand = new Random();
        if (timerType == TimerTaskFragment.TYPE_WORK) {
            int index = rand.nextInt(DRAWABLES_WORK.length);
            setBitmapFromVectorDrawable(DRAWABLES_WORK[index]);
        } else if (timerType == TimerTaskFragment.TYPE_SHORT_BREAK) {
            int index = rand.nextInt(DRAWABLES_SHORT_BREAK.length);
            setBitmapFromVectorDrawable(DRAWABLES_SHORT_BREAK[index]);
        } else {
            int index = rand.nextInt(DRAWABLES_LONG_BREAK.length);
            setBitmapFromVectorDrawable(DRAWABLES_LONG_BREAK[index]);
        }
        mTimerHintTextColor2 = Color.RED;
        if (timerState == TimerTaskFragment.STATE_RUNNING) {
            mTimerHintTextColor = Color.parseColor("#ff9900");
            mTapHintText = "Tap to pause";
            mTapHintText2 = "Long press to stop";
        } else if (timerState == TimerTaskFragment.STATE_PAUSED) {
            mTimerHintTextColor = Color.parseColor("#009900");
            mTapHintText = "Tap to start";
            mTapHintText2 = "Long press to stop";
        } else {
            mTimerHintTextColor = Color.parseColor("#009900");
            mTapHintText = "Tap to start";
            mTapHintText2 = "Long press to stop";
        }
        invalidate();
    }

    public void setmTimerText(String mTimerText) {
        this.mTimerText = mTimerText;
        invalidate();
        requestLayout();
    }

    private void setBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate();
        }
        vectorBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(vectorBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
    }

}
