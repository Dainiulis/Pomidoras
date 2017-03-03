package com.dmiesoft.fitpomodoro.utils.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import com.dmiesoft.fitpomodoro.R;

public class CustomTimerView extends View {

//    public static final int CIRCLE_RESUME = 7235;
//    public static final int CIRCLE_STOP = 2864;
//    public static final int CIRCLE_PAUSE = 9898;

    private static final String TAG = "CTV";
    private static final float THICKNESS_SCALE = .05f;
    private static final String TIMER_BASE_COLOR = "#dbdbdb";
    private Paint mPomidorasPaint, mEraserPaint, mUnderPaint;
    private TextPaint mTextPaint;
    private int mColor = Color.RED;
    private int mTextColor = Color.WHITE;
    private float mTextSize = getResources().getDimension(R.dimen.custom_timer_text_size);
    private String mText;
    private int yTextPos;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private RectF mCircleOuterBounds, mCircleInnerBounds;
    private float mCircleSweepAngle;

    public CustomTimerView(Context context) {
        super(context);
        init();
    }

    public CustomTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomTimerView,
                0, 0);

        try {
            mColor = typedArray.getColor(R.styleable.CustomTimerView_color, Color.RED);
            mTextSize = typedArray.getDimension(R.styleable.CustomTimerView_textSize, getResources().getDimension(R.dimen.custom_timer_text_size));
            mTextColor = typedArray.getColor(R.styleable.CustomTimerView_textColor, Color.WHITE);
            mText = typedArray.getString(R.styleable.CustomTimerView_text);
        } finally {
            typedArray.recycle();
        }

        init();
    }

    private void init() {
        mPomidorasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPomidorasPaint.setStyle(Paint.Style.FILL);
        mPomidorasPaint.setColor(mColor);
        mPomidorasPaint.setTextSize(mTextSize);

        mUnderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnderPaint.setStyle(Paint.Style.FILL);
        mUnderPaint.setColor(Color.parseColor(TIMER_BASE_COLOR));
        mUnderPaint.setShadowLayer(12, 2, 2, Color.BLACK);
        setLayerType(LAYER_TYPE_SOFTWARE, mUnderPaint);

        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEraserPaint.setColor(Color.TRANSPARENT);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        if (mTextSize == 0) {
            mTextSize = mTextPaint.getTextSize();
        } else {
            mTextPaint.setTextSize(mTextSize);
        }
        mTextPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //kodel????
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        int x = getWidth();
        int y = getHeight();

        mCanvas.drawArc(mCircleOuterBounds, 0, 360, true, mUnderPaint);
        if (mCircleSweepAngle > 0f) {
            mCanvas.drawArc(mCircleOuterBounds, 270, -mCircleSweepAngle, true, mPomidorasPaint);
        }
        mCanvas.drawOval(mCircleInnerBounds, mEraserPaint);

        canvas.drawBitmap(mBitmap, 0, 0, null);

        int xC = (x / 2) - 10;
        int yC = (y / 2) - 10;
        yTextPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

        canvas.drawText(mText, xC, yTextPos, mTextPaint);

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
        mCircleInnerBounds = new RectF(
                mCircleOuterBounds.left + thickness,
                mCircleOuterBounds.top + thickness,
                mCircleOuterBounds.right - thickness,
                mCircleOuterBounds.bottom - thickness
        );
        invalidate();
    }

    public void drawProgress(float progress) {
        mCircleSweepAngle = 360 * progress;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = 300;
        int desiredHeight = 300;

        int width;
        int height;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);

    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
        invalidate();
        requestLayout();
    }

}
