package com.example.flush_poker_android.Client.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class PlayerCountdownView extends View {
    private Paint foregroundPaint;
    private Path roundedRectPath;
    private RectF rect;
    private PathMeasure pathMeasure;
    private float pathLength;
    private long startTime;
    private long countdownDuration;
    private CountDownTimer countDownTimer;
    private boolean isCountdownFinished = false;

    public PlayerCountdownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setColor(0xFFFFFFFF); // Countdown color
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeWidth(dpToPx(3));

        roundedRectPath = new Path();
        rect = new RectF();
        pathMeasure = new PathMeasure();
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float cornerRadius = dpToPx(20);
        rect.set(dpToPx(3) / 2, dpToPx(3) / 2, w - dpToPx(3) / 2, h - dpToPx(3) / 2);
        roundedRectPath.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        pathMeasure.setPath(roundedRectPath, false);
        pathLength = pathMeasure.getLength();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (startTime == 0 || isCountdownFinished) {
            // Draw the complete path with the new color if countdown is finished
            canvas.drawPath(roundedRectPath, foregroundPaint);
            return;
        }

        float elapsedTime = SystemClock.elapsedRealtime() - startTime;
        float progress = elapsedTime / (float) countdownDuration;
        float distance = progress * pathLength;

        Path dst = new Path();
        pathMeasure.getSegment(0, distance, dst, true);
        canvas.drawPath(dst, foregroundPaint);

        if (progress < 1f) {
            postInvalidateDelayed(16);
        }
    }

    public void startCountdown(long duration) {
        this.countdownDuration = duration;
        this.startTime = SystemClock.elapsedRealtime();
        this.isCountdownFinished = false;
        foregroundPaint.setColor(0xFFFFFFFF); // Reset to original countdown color
        invalidate();

        countDownTimer = new CountDownTimer(duration, duration) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Not used
            }

            @Override
            public void onFinish() {
                isCountdownFinished = true;
                foregroundPaint.setColor(0x052E3E00);
                invalidate();
            }
        }.start();
    }

    public void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
