package com.fakelag.cryhard8;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ShockwaveView extends View {

    private Paint paint;
    private float radius = 0f;
    private float maxRadius = 300f;
    private float alpha = 1f;
    private int color = Color.parseColor("#00f3ff");

    public ShockwaveView(Context context) {
        super(context);
        init();
    }

    public ShockwaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
    }

    public void trigger(int color) {
        this.color = color;
        radius = 0f;
        alpha = 1f;
        
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                radius = maxRadius * v;
                alpha = 1f - v;
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (alpha <= 0) return;
        
        paint.setColor(Color.argb((int)(alpha * 200), 
            Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, paint);
    }
}
