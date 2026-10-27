package com.fakelag.cryhard8;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimatedBackgroundView extends View {

    private Paint paint;
    private List<Particle> particles;
    private Random random = new Random();
    private boolean isRunning = true;
    private boolean isEngineOn = false;
    private int width, height;

    // Màu sắc: Hồng khi off, Xanh khi on
    private int colorAccent = Color.parseColor("#ff2d95"); // Hồng
    private int colorAccent2 = Color.parseColor("#a855f7"); // Tím

    static class Particle {
        float x, y, vx, vy, radius;
    }

    public AnimatedBackgroundView(Context context) {
        super(context);
        init();
    }

    public AnimatedBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particles = new ArrayList<>();
    }

    public void setEngineOn(boolean on) {
        this.isEngineOn = on;
        if (on) {
            colorAccent = Color.parseColor("#00f3ff"); // Xanh
            colorAccent2 = Color.parseColor("#00ff9d"); // Xanh lá
        } else {
            colorAccent = Color.parseColor("#ff2d95"); // Hồng
            colorAccent2 = Color.parseColor("#a855f7"); // Tím
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        
        // Tạo hạt
        particles.clear();
        int count = Math.min(100, (w * h) / 16000);
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * w;
            p.y = random.nextFloat() * h;
            p.vx = (random.nextFloat() - 0.5f) * 2f;
            p.vy = (random.nextFloat() - 0.5f) * 2f;
            p.radius = random.nextFloat() * 1.6f + 0.6f;
            particles.add(p);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Nền tối
        canvas.drawColor(Color.parseColor("#04040a"));

        // Vẽ grid mờ (giống bản Web)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(15, 255, 255, 255));
        int gridSize = 60;
        for (int x = 0; x < width; x += gridSize) {
            canvas.drawLine(x, 0, x, height, paint);
        }
        for (int y = 0; y < height; y += gridSize) {
            canvas.drawLine(0, y, width, y, paint);
        }

        // Cập nhật vị trí hạt
        float boost = isEngineOn ? 2.4f : 1f;
        for (Particle p : particles) {
            p.x += p.vx * boost;
            p.y += p.vy * boost;

            // Đập tường thì bật lại
            if (p.x < 0 || p.x > width) p.vx *= -1;
            if (p.y < 0 || p.y > height) p.vy *= -1;

            // Vẽ hạt
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colorAccent);
            paint.setAlpha(200);
            canvas.drawCircle(p.x, p.y, p.radius, paint);
        }

        // Vẽ đường nối giữa các hạt gần nhau
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.7f);
        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                float dx = particles.get(i).x - particles.get(j).x;
                float dy = particles.get(i).y - particles.get(j).y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < 150) {
                    int alpha = (int) ((1 - dist / 150) * 60);
                    paint.setColor(Color.argb(alpha, 
                        Color.red(colorAccent), 
                        Color.green(colorAccent), 
                        Color.blue(colorAccent)));
                    canvas.drawLine(particles.get(i).x, particles.get(i).y,
                            particles.get(j).x, particles.get(j).y, paint);
                }
            }
        }

        // Vẽ lại liên tục
        if (isRunning) {
            invalidate();
        }
    }

    public void stop() {
        isRunning = false;
    }
}
