package com.fakelag.cryhard8;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class CyberAnimations {

    // Hiệu ứng xoay 360 độ (dùng cho nút Engine)
    public static void rotate360(final View view, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setRotation(value);
            }
        });
        animator.start();
    }

    // Hiệu ứng phóng to rồi thu nhỏ (dùng khi bấm nút)
    public static void pulse(final View view) {
        view.animate().cancel();
        view.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(120)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(180)
                                .start();
                    }
                })
                .start();
    }

    // Hiệu ứng lắc (dùng khi nhập sai)
    public static void shake(final View view) {
        view.animate().cancel();
        view.animate()
                .translationX(-12f)
                .setDuration(50)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate().translationX(12f).setDuration(50).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                view.animate().translationX(-8f).setDuration(50).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.animate().translationX(0f).setDuration(100).start();
                                    }
                                }).start();
                            }
                        }).start();
                    }
                })
                .start();
    }

    // Hiệu ứng nhấp nháy màu (dùng khi bật/tắt)
    public static void glow(final View view, final int colorFrom, final int colorTo) {
        ValueAnimator animator = ValueAnimator.ofArgb(colorFrom, colorTo, colorFrom);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();
                view.setBackgroundColor(color);
            }
        });
        animator.start();
    }

    // Hiệu ứng fade in (dùng khi mở App)
    public static void fadeIn(final View view, long delay) {
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(300)
                .start();
    }

    // Hiệu ứng trượt lên (dùng khi hiện card)
    public static void slideUp(final View view, long delay) {
        view.setTranslationY(60f);
        view.setAlpha(0f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}
