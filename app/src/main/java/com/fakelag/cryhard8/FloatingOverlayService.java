package com.fakelag.cryhard8;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.SweepGradient;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class FloatingOverlayService extends Service {

    private static final String CH_ID = "gbf";
    private static final int NOTIF_ID = 2;
    private static final String PREFS = "xevpn_prefs";
    private static final String ACTION_APPLY_FLOAT_APPEARANCE = "com.fakelag.cryhard8.APPLY_FLOAT_APPEARANCE";
    private static final String EXTRA_FLOAT_SIZE = "float_size_percent";
    private static final String EXTRA_FLOAT_ALPHA = "float_alpha_percent";
    private static final String EXTRA_MASK_BAR = "show_mask_bar";
    private static final String EXTRA_MASK_BAR_THICKNESS = "mask_bar_thickness";
    private static final String EXTRA_MASK_BAR_LENGTH = "mask_bar_length";
    private static final String EXTRA_CROSSHAIR = "show_crosshair";
    private static final String EXTRA_CROSSHAIR_STYLE = "crosshair_style";
    private static final String EXTRA_CROSSHAIR_COLOR = "crosshair_color";
    private static final String EXTRA_CROSSHAIR_SIZE = "crosshair_size";
    private static final String EXTRA_CROSSHAIR_ALPHA = "crosshair_alpha";
    private static final String EXTRA_AUTO_STOP_TELE_ON_FREEZE = "auto_stop_tele_on_freeze";
    private static final String KEY_FLOAT_ALPHA = "float_alpha_percent";
    private static final String KEY_FLOAT_SIZE = "float_size_percent";
    private static final String KEY_MASK_BAR = "show_mask_bar";
    private static final String KEY_MASK_BAR_THICKNESS = "mask_bar_thickness";
    private static final String KEY_MASK_BAR_LENGTH = "mask_bar_length";
    private static final String KEY_CROSSHAIR = "show_crosshair";
    private static final String KEY_CROSSHAIR_STYLE = "crosshair_style";
    private static final String KEY_CROSSHAIR_COLOR = "crosshair_color";
    private static final String KEY_CROSSHAIR_SIZE = "crosshair_size";
    private static final String KEY_CROSSHAIR_ALPHA = "crosshair_alpha";
    private static final String KEY_AUTO_STOP_TELE_ON_FREEZE = "auto_stop_tele_on_freeze";
    private static final String EXTRA_DEV_MODE = "dev_mode";
    private static final String KEY_DEV_MODE = "dev_mode";

    private boolean onStandby = true;
    private boolean onFreeze = false;
    private boolean onGhost = false;
    private boolean onJitter = false;
    private boolean onJitterV2 = false;
    private boolean onDelayPing = false;
    private boolean onTeleSpeed = false;
    private boolean addedS = false;
    private boolean addedF = false;
    private boolean addedG = false;
    private boolean addedT = false;
    private boolean addedJ = false;
    private boolean addedJ2 = false;
    private boolean addedDP = false;
    private boolean addedTS = false;
    private boolean addedBanner = false;
    private boolean addedMaskBar = false;
    private boolean addedCrosshair = false;
    private boolean addedDevPopup = false;

    private TextView btnFreeze;
    private TextView btnGhost;
    private TextView btnJitter;
    private TextView btnJitterV2;
    private TextView btnDelayPing;
    private TextView btnTeleSpeed;
    private TextView topBanner;
    private TextView txtStats;
    private TextView devPopup;
    private View maskBar;
    private CrosshairView crosshairView;
    private WindowManager wm;
    private WindowManager.LayoutParams freezeParams;
    private WindowManager.LayoutParams ghostParams;
    private WindowManager.LayoutParams teleParams;
    private WindowManager.LayoutParams teleV2Params;
    private WindowManager.LayoutParams delayPingParams;
    private WindowManager.LayoutParams teleSpeedParams;
    private WindowManager.LayoutParams maskBarParams;
    private WindowManager.LayoutParams crosshairParams;
    private WindowManager.LayoutParams devPopupParams;
    private BroadcastReceiver rcv;
    private ValueAnimator shineAnimator;
    private ValueAnimator pulseAnimator;
    private ValueAnimator bannerAnimator;
    private ValueAnimator maskBarAnimator;
    private ToneGenerator toneGenerator;
    private Typeface floatingButtonFont;
    private GradientDrawable maskBarDrawable;
    private final ArrayList<AnimatedCircleDrawable> ringDrawables = new ArrayList<AnimatedCircleDrawable>();
    private int floatAlphaPercent = 92;
    private int floatSizePercent = 100;
    private int maskBarThickness = 16;
    private int maskBarLength = 220;
    private int crosshairStyle = 3;
    private int crosshairColor = Color.parseColor("#22D3EE");
    private int crosshairSize = 42;
    private int crosshairAlphaPercent = 100;

    private boolean showGhost = true;
    private boolean showFreeze = true;
    private boolean showTele = true;
    private boolean showTeleV2 = true;
    private boolean showDelayPing = true;
    private boolean showTeleSpeed = true;
    private boolean showMaskBar = false;
    private boolean showCrosshair = true;
    private boolean showDevMode = false;
    private boolean autoStopTeleOnFreeze = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        doForeground();
        wm = (WindowManager) getSystemService("window");
        initTone();
        try {
            floatingButtonFont = Typeface.createFromAsset(getAssets(), "fonts/chudep.ttf");
        } catch (Exception e) {
            floatingButtonFont = Typeface.DEFAULT_BOLD;
        }

        android.content.SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        showGhost = prefs.getBoolean("show_ghost", true);
        showFreeze = prefs.getBoolean("show_freeze", true);
        showTele = false;
        showTeleV2 = false;
        showDelayPing = false;
        showTeleSpeed = prefs.getBoolean("show_tele_speed", true);
        showMaskBar = true;
        showCrosshair = prefs.getBoolean(KEY_CROSSHAIR, true);
        crosshairStyle = clampCrosshairStyle(prefs.getInt(KEY_CROSSHAIR_STYLE, 3));
        crosshairColor = prefs.getInt(KEY_CROSSHAIR_COLOR, Color.parseColor("#22D3EE"));
        crosshairSize = clampCrosshairSize(prefs.getInt(KEY_CROSSHAIR_SIZE, 42));
        crosshairAlphaPercent = clampCrosshairAlpha(prefs.getInt(KEY_CROSSHAIR_ALPHA, 100));
        showDevMode = prefs.getBoolean(KEY_DEV_MODE, false);
        autoStopTeleOnFreeze = prefs.getBoolean(KEY_AUTO_STOP_TELE_ON_FREEZE, true);
        floatAlphaPercent = prefs.getInt(KEY_FLOAT_ALPHA, 92);
        floatSizePercent = prefs.getInt(KEY_FLOAT_SIZE, 100);
        maskBarThickness = prefs.getInt(KEY_MASK_BAR_THICKNESS, 16);
        maskBarLength = prefs.getInt(KEY_MASK_BAR_LENGTH, 220);
        if (floatAlphaPercent < 35) floatAlphaPercent = 35;
        if (floatAlphaPercent > 100) floatAlphaPercent = 100;
        if (floatSizePercent < 80) floatSizePercent = 80;
        if (floatSizePercent > 260) floatSizePercent = 260;
        if (maskBarThickness < 6) maskBarThickness = 6;
        if (maskBarThickness > 36) maskBarThickness = 36;
        if (maskBarLength < 120) maskBarLength = 120;
        if (maskBarLength > 340) maskBarLength = 340;

        topBanner = makeTopBanner();
        if (showMaskBar) maskBar = makeMaskBar();
        if (showCrosshair) crosshairView = (CrosshairView) makeCrosshairView();
        if (showDevMode) devPopup = makeDevPopup();
        if (showFreeze) btnFreeze = makeBtn(getString(R.string.freeze), 18, 110, 2);
        if (showGhost) btnGhost = makeBtn(getString(R.string.ghost), 156, 110, 3);
        if (showTele) btnJitter = makeBtn(getString(R.string.tele), 18, 248, 4);
        if (showTeleV2) btnJitterV2 = makeBtn(getString(R.string.tele_v2), 156, 248, 5);
        if (showDelayPing) btnDelayPing = makeBtn(getString(R.string.delay_ping), 18, 386, 6);
        if (showTeleSpeed) btnTeleSpeed = makeBtn(getString(R.string.tele_speed), 156, 386, 7);

        doRegister();

        if (btnFreeze != null) paintBtn(btnFreeze, getString(R.string.freeze), false);
        if (btnGhost != null) paintBtn(btnGhost, getString(R.string.ghost), false);
        if (btnJitter != null) paintBtn(btnJitter, getString(R.string.tele), false);
        if (btnJitterV2 != null) paintBtn(btnJitterV2, getString(R.string.tele_v2), false);
        if (btnDelayPing != null) paintBtn(btnDelayPing, getString(R.string.delay_ping), false);
        if (btnTeleSpeed != null) paintBtn(btnTeleSpeed, getString(R.string.tele_speed), false);
        startRainbowAnimation();
        startPulseAnimation();
        startBannerAnimation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (rcv != null) unregisterReceiver(rcv);
        } catch (Exception e) { }
        rm(btnFreeze, addedF);
        rm(btnGhost, addedG);
        rm(btnJitter, addedJ);
        rm(btnJitterV2, addedJ2);
        rm(btnDelayPing, addedDP);
        rm(btnTeleSpeed, addedTS);
        rm(topBanner, addedBanner);
        rm(maskBar, addedMaskBar);
        rm(crosshairView, addedCrosshair);
        rm(devPopup, addedDevPopup);
        rm(txtStats, addedT);
        if (shineAnimator != null) shineAnimator.cancel();
        if (pulseAnimator != null) pulseAnimator.cancel();
        if (bannerAnimator != null) bannerAnimator.cancel();
        if (maskBarAnimator != null) maskBarAnimator.cancel();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    private void doForeground() {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationManager nm = (NotificationManager) getSystemService("notification");
                if (nm != null) {
                    NotificationChannel ch = new NotificationChannel(CH_ID, "GB", NotificationManager.IMPORTANCE_MIN);
                    ch.setSound(null, null);
                    nm.createNotificationChannel(ch);
                }
                Notification n = new Notification.Builder(this, CH_ID)
                        .setContentTitle("Chơi Game - Ping Thấp")
                        .setContentText("Máy chủ giảm độ trễ đã kích hoạt")
                        .setSmallIcon(0x1080039)
                        .build();
                startForeground(NOTIF_ID, n);
            } else {
                Notification n = new Notification.Builder(this)
                        .setContentTitle("Chơi Game - Ping Thấp")
                        .setContentText("Máy chủ giảm độ trễ đã kích hoạt")
                        .setSmallIcon(0x1080039)
                        .build();
                startForeground(NOTIF_ID, n);
            }
        } catch (Exception e) {
            try {
                Notification n = new Notification();
                n.icon = 0x1080039;
                startForeground(NOTIF_ID, n);
            } catch (Exception e2) { }
        }
    }

    private void doRegister() {
        rcv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                String action = intent.getAction();
                if (ACTION_APPLY_FLOAT_APPEARANCE.equals(action)) {
                    applyAppearanceIntent(intent);
                    return;
                }
                if (!PacketVpnService.ACTION_STATS.equals(action)) return;

                int ping = intent.getIntExtra("ping", 0);
                int loss = intent.getIntExtra("loss", 0);
                long bin = intent.getLongExtra(PacketVpnService.EXTRA_BYTES_IN, 0L);
                long bout = intent.getLongExtra(PacketVpnService.EXTRA_BYTES_OUT, 0L);
                String mode = intent.getStringExtra("mode");
                if (mode == null) mode = "STANDBY";

                String color;
                if (ping < 80) color = "#00E676";
                else if (ping < 150) color = "#FFD600";
                else color = "#FF1744";

                boolean isFilterEnabled = intent.getBooleanExtra(PacketVpnService.EXTRA_FILTER_ENABLED, "ACTIVE".equals(mode));
                if (isFilterEnabled != onStandby) {
                    onStandby = isFilterEnabled;
                    if (!onStandby) {
                        onFreeze = false;
                        onGhost = false;
                        onJitter = false;
                        onJitterV2 = false;
                        onDelayPing = false;
                        onTeleSpeed = false;
                        if (btnFreeze != null) paintBtn(btnFreeze, getString(R.string.freeze), false);
                        if (btnGhost != null) paintBtn(btnGhost, getString(R.string.ghost), false);
                        if (btnJitter != null) paintBtn(btnJitter, getString(R.string.tele), false);
                        if (btnJitterV2 != null) paintBtn(btnJitterV2, getString(R.string.tele_v2), false);
                        if (btnDelayPing != null) paintBtn(btnDelayPing, getString(R.string.delay_ping), false);
                        if (btnTeleSpeed != null) paintBtn(btnTeleSpeed, getString(R.string.tele_speed), false);
                    }
                }
            }
        };

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.fakelag.cryhard8.STATS");
            filter.addAction(ACTION_APPLY_FLOAT_APPEARANCE);
            registerReceiverCompat(rcv, filter);
        } catch (Exception e) { }
    }

    private void registerReceiverCompat(BroadcastReceiver receiver, IntentFilter filter) {
        if (receiver == null || filter == null) return;
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                java.lang.reflect.Method method = Context.class.getMethod(
                        "registerReceiver",
                        BroadcastReceiver.class,
                        IntentFilter.class,
                        Integer.TYPE);
                method.invoke(this, receiver, filter, Integer.valueOf(4));
                return;
            } catch (Exception ignored) { }
        }
        try {
            registerReceiver(receiver, filter);
        } catch (Exception e) { }
    }

    private TextView makeBtn(String label, int x, int y, int id) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(0xFFFFFFFF);
        tv.setAlpha(floatAlphaPercent / 100f);

        float scale = floatSizePercent / 100f;
        int baseDp = 78;
        int sizePx = dpToPx(Math.max(64, Math.round(baseDp * scale)));
        tv.setTextSize(Math.max(10.0f, Math.min(16.0f, 11.0f * scale)));

        tv.setTypeface(floatingButtonFont != null ? floatingButtonFont : Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setSingleLine(false);
        tv.setMaxLines(3);
        tv.setClickable(true);
        tv.setLongClickable(true);
        tv.setIncludeFontPadding(false);
        int innerPadding = dpToPx(Math.max(4, Math.round(6 * scale)));
        tv.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);

        AnimatedCircleDrawable background = new AnimatedCircleDrawable(id * 34f);
        ringDrawables.add(background);
        tv.setTag(background);
        tv.setBackground(background);

        int type = (Build.VERSION.SDK_INT >= 26)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                sizePx,
                sizePx,
                type,
                flags,
                android.graphics.PixelFormat.TRANSLUCENT
        );

        p.gravity = Gravity.TOP | Gravity.LEFT;
        p.x = dpToPx(x);
        p.y = dpToPx(y);

        try {
            wm.addView(tv, p);
        } catch (Exception e) {
            return tv;
        }

        if (id == 2) addedF = true;
        else if (id == 3) addedG = true;
        else if (id == 4) addedJ = true;
        else if (id == 5) addedJ2 = true;
        else if (id == 6) addedDP = true;
        else if (id == 7) addedTS = true;

        if (id == 2) freezeParams = p;
        else if (id == 3) ghostParams = p;
        else if (id == 4) teleParams = p;
        else if (id == 5) teleV2Params = p;
        else if (id == 6) delayPingParams = p;
        else if (id == 7) teleSpeedParams = p;

        tv.setOnTouchListener(new BtnTouchListener(p, tv, id));
        return tv;
    }

    private TextView makeTopBanner() {
        TextView tv = new TextView(this);
        tv.setText("MhaiMod");
        tv.setTextSize(12f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setSingleLine(true);
        tv.setGravity(Gravity.CENTER);
        tv.setAlpha(0.96f);
        tv.setPadding(scalePx(12), scalePx(6), scalePx(12), scalePx(6));
        tv.setTextColor(Color.parseColor("#FF22D3EE"));
        tv.setShadowLayer(18f, 0f, 0f, Color.argb(110, 104, 255, 242));

        int type = (Build.VERSION.SDK_INT >= 26)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                flags,
                android.graphics.PixelFormat.TRANSLUCENT
        );

        p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        p.x = 0;
        p.y = scalePx(10);

        try {
            wm.addView(tv, p);
            addedBanner = true;
        } catch (Exception e) { }

        return tv;
    }

    private View makeMaskBar() {
        TextView view = new TextView(this);
        view.setText(" Minizo Mod ");
        view.setTextColor(Color.WHITE);
        view.setTextSize(12f);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        view.setIncludeFontPadding(false);
        view.setAlpha(1f);
        view.setPadding(scalePx(26), scalePx(7), scalePx(26), scalePx(7));
        maskBarDrawable = new GradientDrawable();
        maskBarDrawable.setShape(GradientDrawable.RECTANGLE);
        maskBarDrawable.setCornerRadius(scalePx(999));
        maskBarDrawable.setColor(Color.parseColor("#FFFF3B30"));
        maskBarDrawable.setStroke(scalePx(1), Color.parseColor("#FFFFFFFF"));
        view.setBackground(maskBarDrawable);

        int type = (Build.VERSION.SDK_INT >= 26)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                scalePx(maskBarLength + 20),
                scalePx(Math.max(maskBarThickness + 30, 46)),
                type,
                flags,
                android.graphics.PixelFormat.OPAQUE
        );

        p.gravity = Gravity.BOTTOM | Gravity.LEFT;
        p.x = scalePx(10);
        p.y = scalePx(2);

        try {
            wm.addView(view, p);
            addedMaskBar = true;
            maskBarParams = p;
            startMaskBarAnimation();
        } catch (Exception e) { }

        return view;
    }

    private View makeCrosshairView() {
        CrosshairView view = new CrosshairView(this);
        view.setConfig(crosshairStyle, crosshairColor, crosshairSize, crosshairAlphaPercent);
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        int type = (Build.VERSION.SDK_INT >= 26)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                getCrosshairOverlaySizePx(),
                getCrosshairOverlaySizePx(),
                type,
                flags,
                android.graphics.PixelFormat.TRANSLUCENT
        );

        p.gravity = Gravity.CENTER;
        p.x = 0;
        p.y = 0;

        try {
            wm.addView(view, p);
            addedCrosshair = true;
            crosshairParams = p;
        } catch (Exception e) { }

        return view;
    }

    private int getCrosshairOverlaySizePx() {
        int dp = Math.max(72, (crosshairSize * 2) + 26);
        return dpToPx(dp);
    }

    private int clampCrosshairStyle(int value) {
        if (value < 0) return 0;
        if (value > 7) return 7;
        return value;
    }

    private int clampCrosshairSize(int value) {
        if (value < 20) return 20;
        if (value > 96) return 96;
        return value;
    }

    private int clampCrosshairAlpha(int value) {
        if (value < 35) return 35;
        if (value > 100) return 100;
        return value;
    }

    private void updateCrosshair() {
        if (showCrosshair) {
            if (crosshairView == null) {
                crosshairView = (CrosshairView) makeCrosshairView();
            }
            if (crosshairView != null) {
                crosshairView.setConfig(crosshairStyle, crosshairColor, crosshairSize, crosshairAlphaPercent);
                if (crosshairParams != null) {
                    int size = getCrosshairOverlaySizePx();
                    crosshairParams.width = size;
                    crosshairParams.height = size;
                    crosshairParams.gravity = Gravity.CENTER;
                    crosshairParams.x = 0;
                    crosshairParams.y = 0;
                    try {
                        wm.updateViewLayout(crosshairView, crosshairParams);
                    } catch (Exception e) { }
                }
            }
        } else {
            if (crosshairView != null && addedCrosshair) {
                rm(crosshairView, true);
                crosshairView = null;
                crosshairParams = null;
                addedCrosshair = false;
            }
        }
    }

    private Drawable createDevPopupBackground() {
        GradientDrawable fill = new GradientDrawable();
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(dpToPx(18));
        fill.setColor(Color.parseColor("#D9112330"));
        fill.setStroke(dpToPx(2), Color.parseColor("#804AF5FF"));

        GradientDrawable glow = new GradientDrawable();
        glow.setShape(GradientDrawable.RECTANGLE);
        glow.setCornerRadius(dpToPx(18));
        glow.setColor(Color.parseColor("#00000000"));
        glow.setStroke(dpToPx(4), Color.parseColor("#1F2BFFFF"));

        return new LayerDrawable(new Drawable[]{glow, fill});
    }

    private void styleDevPopup(TextView tv) {
        if (tv == null) return;
        tv.setText("✦ DEV LOG");
        tv.setTextSize(12.5f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.parseColor("#FFD8FFF7"));
        tv.setAlpha(0.98f);
        tv.setMinWidth(dpToPx(108));
        tv.setMinHeight(dpToPx(44));
        tv.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
        tv.setBackground(createDevPopupBackground());
        tv.setShadowLayer(10f, 0f, 0f, Color.parseColor("#3300F7FF"));
    }

    private TextView makeDevPopup() {
        TextView tv = new TextView(this);
        styleDevPopup(tv);

        int type = (Build.VERSION.SDK_INT >= 26)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                flags,
                android.graphics.PixelFormat.TRANSLUCENT
        );

        p.gravity = Gravity.TOP | Gravity.LEFT;
        p.x = scalePx(156);
        p.y = scalePx(88);

        try {
            wm.addView(tv, p);
            addedDevPopup = true;
            devPopupParams = p;
            tv.setOnTouchListener(new MaskBarTouchListener(p, tv));
        } catch (Exception e) { }

        return tv;
    }

    private Drawable createStatsBackground(boolean isActive) {
        float[] radii = new float[8];
        for (int i = 0; i < 8; i++) radii[i] = 24f;

        ShapeDrawable bg = new ShapeDrawable(new RoundRectShape(radii, null, null));
        bg.getPaint().setColor(Color.argb(isActive ? 230 : 204, 0, 0, 0));

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(24f);
        border.setColor(Color.TRANSPARENT);
        border.setStroke(
                isActive ? 2 : 1,
                isActive ? Color.argb(153, 0, 230, 118) : Color.argb(51, 0, 230, 118)
        );

        Drawable[] layers = new Drawable[]{bg, border};
        return new LayerDrawable(layers);
    }

    private int scalePx(int basePx) {
        return Math.max(1, Math.round(basePx * (floatSizePercent / 100f)));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(dp * density));
    }

    private void initTone() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 95);
        } catch (Exception e) {
            toneGenerator = null;
        }
    }

    private void playToggleBeep(boolean isOn) {
        if (toneGenerator == null) return;
        try {
            toneGenerator.startTone(
                    isOn ? ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD : ToneGenerator.TONE_PROP_ACK,
                    isOn ? 120 : 105
            );
        } catch (Exception e) { }
    }

    private void startRainbowAnimation() {
        if (ringDrawables.isEmpty()) return;
        if (shineAnimator != null) shineAnimator.cancel();
        shineAnimator = ValueAnimator.ofFloat(0f, 360f);
        shineAnimator.setDuration(7200L);
        shineAnimator.setInterpolator(new LinearInterpolator());
        shineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float rotation = ((Float) animator.getAnimatedValue()).floatValue();
                for (int i = 0; i < ringDrawables.size(); i++) {
                    ringDrawables.get(i).setRotationDegrees(rotation + (i * 22f));
                }
            }
        });
        shineAnimator.start();
    }

    private AnimatedCircleDrawable getCircleDrawable(TextView tv) {
        if (tv == null) return null;
        Object tag = tv.getTag();
        if (tag instanceof AnimatedCircleDrawable) {
            return (AnimatedCircleDrawable) tag;
        }
        return null;
    }

    private void startPulseAnimation() {
        if (ringDrawables.isEmpty()) return;
        if (pulseAnimator != null) pulseAnimator.cancel();
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1800L);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float pulse = ((Float) animator.getAnimatedValue()).floatValue();
                for (int i = 0; i < ringDrawables.size(); i++) {
                    ringDrawables.get(i).setPulse(Math.min(1f, pulse + (i * 0.08f)));
                }
            }
        });
        pulseAnimator.start();
    }

    private void startBannerAnimation() {
        if (topBanner == null) return;
        if (bannerAnimator != null) bannerAnimator.cancel();
        bannerAnimator = ValueAnimator.ofFloat(0f, 1f);
        bannerAnimator.setDuration(2600L);
        bannerAnimator.setInterpolator(new LinearInterpolator());
        bannerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        bannerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        bannerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float progress = ((Float) animator.getAnimatedValue()).floatValue();
                int color = blendColor(
                        Color.parseColor("#FF62FFF1"),
                        Color.parseColor("#FF92FF6F"),
                        progress
                );
                topBanner.setTextColor(color);
                topBanner.setShadowLayer(
                        18f + (6f * progress),
                        0f,
                        0f,
                        blendColor(
                                Color.argb(118, 98, 255, 241),
                                Color.argb(124, 146, 255, 111),
                                progress
                        )
                );
            }
        });
        bannerAnimator.start();
    }

    private void startMaskBarAnimation() {
        if (maskBarDrawable == null) return;
        if (maskBarAnimator != null) maskBarAnimator.cancel();
        maskBarAnimator = ValueAnimator.ofFloat(0f, 1f);
        maskBarAnimator.setDuration(3600L);
        maskBarAnimator.setInterpolator(new LinearInterpolator());
        maskBarAnimator.setRepeatCount(ValueAnimator.INFINITE);
        maskBarAnimator.setRepeatMode(ValueAnimator.RESTART);
        maskBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = ((Float) animation.getAnimatedValue()).floatValue();
                int fill = Color.HSVToColor(255, new float[]{360f * p, 0.85f, 1f});
                int stroke = Color.HSVToColor(255, new float[]{360f * p, 0.45f, 1f});
                maskBarDrawable.setColor(fill);
                maskBarDrawable.setStroke(scalePx(1), stroke);
            }
        });
        maskBarAnimator.start();
    }

    private void applyAppearanceIntent(Intent intent) {
        if (intent == null) return;
        int nextSize = intent.getIntExtra(EXTRA_FLOAT_SIZE, floatSizePercent);
        int nextAlpha = intent.getIntExtra(EXTRA_FLOAT_ALPHA, floatAlphaPercent);
        int nextThickness = intent.getIntExtra(EXTRA_MASK_BAR_THICKNESS, maskBarThickness);
        int nextLength = intent.getIntExtra(EXTRA_MASK_BAR_LENGTH, maskBarLength);
        int nextCrosshairStyle = intent.getIntExtra(EXTRA_CROSSHAIR_STYLE, crosshairStyle);
        int nextCrosshairColor = intent.getIntExtra(EXTRA_CROSSHAIR_COLOR, crosshairColor);
        int nextCrosshairSize = intent.getIntExtra(EXTRA_CROSSHAIR_SIZE, crosshairSize);
        int nextCrosshairAlpha = intent.getIntExtra(EXTRA_CROSSHAIR_ALPHA, crosshairAlphaPercent);
        if (nextSize < 80) nextSize = 80;
        if (nextSize > 260) nextSize = 260;
        if (nextAlpha < 35) nextAlpha = 35;
        if (nextAlpha > 100) nextAlpha = 100;
        if (nextThickness < 6) nextThickness = 6;
        if (nextThickness > 36) nextThickness = 36;
        if (nextLength < 120) nextLength = 120;
        if (nextLength > 340) nextLength = 340;
        nextCrosshairStyle = clampCrosshairStyle(nextCrosshairStyle);
        nextCrosshairSize = clampCrosshairSize(nextCrosshairSize);
        nextCrosshairAlpha = clampCrosshairAlpha(nextCrosshairAlpha);
        floatSizePercent = nextSize;
        floatAlphaPercent = nextAlpha;
        maskBarThickness = nextThickness;
        maskBarLength = nextLength;
        crosshairStyle = nextCrosshairStyle;
        crosshairColor = nextCrosshairColor;
        crosshairSize = nextCrosshairSize;
        crosshairAlphaPercent = nextCrosshairAlpha;
        showMaskBar = true;
        showCrosshair = intent.getBooleanExtra(EXTRA_CROSSHAIR, showCrosshair);
        showDevMode = intent.getBooleanExtra(EXTRA_DEV_MODE, showDevMode);
        autoStopTeleOnFreeze = intent.getBooleanExtra(EXTRA_AUTO_STOP_TELE_ON_FREEZE, autoStopTeleOnFreeze);
        applyRealtimeAppearance();
    }

    private void applyRealtimeAppearance() {
        updateButtonLayout(btnFreeze, freezeParams, 2, onFreeze);
        updateButtonLayout(btnGhost, ghostParams, 3, onGhost);
        updateButtonLayout(btnJitter, teleParams, 4, onJitter);
        updateButtonLayout(btnJitterV2, teleV2Params, 5, onJitterV2);
        updateButtonLayout(btnDelayPing, delayPingParams, 6, onDelayPing);
        updateButtonLayout(btnTeleSpeed, teleSpeedParams, 7, onTeleSpeed);
        updateMaskBar();
        updateDevPopup();
        updateCrosshair();
    }

    private void updateButtonLayout(TextView tv, WindowManager.LayoutParams params, int id, boolean isOn) {
        if (tv == null || params == null) return;

        float scale = floatSizePercent / 100f;
        int baseDp = 78;
        int sizePx = dpToPx(Math.max(64, Math.round(baseDp * scale)));
        params.width = sizePx;
        params.height = sizePx;
        tv.setTextSize(Math.max(10.0f, Math.min(16.0f, 11.0f * scale)));
        int innerPadding = dpToPx(Math.max(4, Math.round(6 * scale)));
        tv.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);
        tv.setAlpha(floatAlphaPercent / 100f);

        AnimatedCircleDrawable drawable = getCircleDrawable(tv);
        if (drawable != null) {
            drawable.setActive(isOn);
        }

        try {
            wm.updateViewLayout(tv, params);
        } catch (Exception e) { }

        if (id == 2) paintBtn(tv, getString(R.string.freeze), isOn);
        else if (id == 3) paintBtn(tv, getString(R.string.ghost), isOn);
        else if (id == 4) paintBtn(tv, getString(R.string.tele), isOn);
        else if (id == 5) paintBtn(tv, getString(R.string.tele_v2), isOn);
        else if (id == 6) paintBtn(tv, getString(R.string.delay_ping), isOn);
        else if (id == 7) paintBtn(tv, getString(R.string.tele_speed), isOn);
    }

    private void updateMaskBar() {
        if (showMaskBar) {
            if (maskBar == null) {
                maskBar = makeMaskBar();
            }
            if (maskBar != null && maskBarParams != null) {
                maskBarParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                maskBarParams.x = scalePx(10);
                maskBarParams.y = scalePx(2);
                maskBarParams.width = scalePx(maskBarLength + 20);
                maskBarParams.height = scalePx(Math.max(maskBarThickness + 30, 46));
                maskBar.setAlpha(1f);
                try {
                    wm.updateViewLayout(maskBar, maskBarParams);
                } catch (Exception e) { }
                startMaskBarAnimation();
            }
        } else {
            if (maskBar != null && addedMaskBar) {
                rm(maskBar, true);
                maskBar = null;
                maskBarParams = null;
                addedMaskBar = false;
            }
        }
    }

    private void updateDevPopup() {
        if (showDevMode) {
            if (devPopup == null) {
                devPopup = makeDevPopup();
            }
            if (devPopup != null && devPopupParams != null) {
                styleDevPopup(devPopup);
                try {
                    wm.updateViewLayout(devPopup, devPopupParams);
                } catch (Exception e) { }
            }
        } else {
            if (devPopup != null && addedDevPopup) {
                rm(devPopup, true);
                devPopup = null;
                devPopupParams = null;
                addedDevPopup = false;
            }
        }
    }

    private void paintBtn(TextView tv, String label, boolean on) {
        if (tv == null) return;
        AnimatedCircleDrawable circleDrawable = getCircleDrawable(tv);
        if (circleDrawable != null) {
            circleDrawable.setActive(on);
        }
        tv.setAlpha(floatAlphaPercent / 100f);
        tv.setTextColor(Color.WHITE);
        tv.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT);

        String title = label;
        if (label.equals(getString(R.string.freeze))) title = "Freeze";
        else if (label.equals(getString(R.string.ghost))) title = "Ghost V2";
        else if (label.equals(getString(R.string.tele))) title = "TeleKill";
        else if (label.equals(getString(R.string.tele_v2))) title = "Ghost V2";
        else if (label.equals(getString(R.string.delay_ping))) title = "Delay";
        else if (label.equals(getString(R.string.tele_speed))) title = "Teleport";

        tv.setText(title);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(floatingButtonFont != null ? floatingButtonFont : Typeface.DEFAULT_BOLD);
    }

    private void click(int id) {
        if (id == 2) {
            onFreeze = !onFreeze;
            if (onFreeze && onJitter && autoStopTeleOnFreeze) {
                onJitter = false;
                paintBtn(btnJitter, getString(R.string.tele), false);
            }
            if (onFreeze && onDelayPing && autoStopTeleOnFreeze) {
                onDelayPing = false;
                paintBtn(btnDelayPing, getString(R.string.delay_ping), false);
            }
            if (onFreeze && onTeleSpeed && autoStopTeleOnFreeze) {
                onTeleSpeed = false;
                paintBtn(btnTeleSpeed, getString(R.string.tele_speed), false);
            }
            paintBtn(btnFreeze, getString(R.string.freeze), onFreeze);
            playToggleBeep(onFreeze);
            send(PacketVpnService.ACTION_FREEZE);
        } else if (id == 3) {
            onGhost = !onGhost;
            if (onGhost && onDelayPing) {
                onDelayPing = false;
                paintBtn(btnDelayPing, getString(R.string.delay_ping), false);
            }
            if (onGhost && onTeleSpeed) {
                onTeleSpeed = false;
                paintBtn(btnTeleSpeed, getString(R.string.tele_speed), false);
            }
            paintBtn(btnGhost, getString(R.string.ghost), onGhost);
            playToggleBeep(onGhost);
            send(PacketVpnService.ACTION_GHOST);
        } else if (id == 4) {
            onJitter = !onJitter;
            if (onJitter && onJitterV2) {
                onJitterV2 = false;
                paintBtn(btnJitterV2, getString(R.string.tele_v2), false);
            }
            if (onJitter && onDelayPing) {
                onDelayPing = false;
                paintBtn(btnDelayPing, getString(R.string.delay_ping), false);
            }
            if (onJitter && onTeleSpeed) {
                onTeleSpeed = false;
                paintBtn(btnTeleSpeed, getString(R.string.tele_speed), false);
            }
            paintBtn(btnJitter, getString(R.string.tele), onJitter);
            playToggleBeep(onJitter);
            send(PacketVpnService.ACTION_JITTER);
        } else if (id == 5) {
            onJitterV2 = !onJitterV2;
            if (onJitterV2 && onJitter) {
                onJitter = false;
                paintBtn(btnJitter, getString(R.string.tele), false);
            }
            if (onJitterV2 && onDelayPing) {
                onDelayPing = false;
                paintBtn(btnDelayPing, getString(R.string.delay_ping), false);
            }
            if (onJitterV2 && onTeleSpeed) {
                onTeleSpeed = false;
                paintBtn(btnTeleSpeed, getString(R.string.tele_speed), false);
            }
            paintBtn(btnJitterV2, getString(R.string.tele_v2), onJitterV2);
            playToggleBeep(onJitterV2);
            send(PacketVpnService.ACTION_JITTER_V2);
        } else if (id == 6) {
            onDelayPing = !onDelayPing;
            if (onDelayPing && onGhost) {
                onGhost = false;
                paintBtn(btnGhost, getString(R.string.ghost), false);
            }
            if (onDelayPing && onJitter) {
                onJitter = false;
                paintBtn(btnJitter, getString(R.string.tele), false);
            }
            if (onDelayPing && onJitterV2) {
                onJitterV2 = false;
                paintBtn(btnJitterV2, getString(R.string.tele_v2), false);
            }
            if (onDelayPing && onTeleSpeed) {
                onTeleSpeed = false;
                paintBtn(btnTeleSpeed, getString(R.string.tele_speed), false);
            }
            paintBtn(btnDelayPing, getString(R.string.delay_ping), onDelayPing);
            playToggleBeep(onDelayPing);
            send(PacketVpnService.ACTION_DELAY_PING);
        } else if (id == 7) {
            onTeleSpeed = !onTeleSpeed;
            if (onTeleSpeed && onGhost) {
                onGhost = false;
                paintBtn(btnGhost, getString(R.string.ghost), false);
            }
            if (onTeleSpeed && onJitter) {
                onJitter = false;
                paintBtn(btnJitter, getString(R.string.tele), false);
            }
            if (onTeleSpeed && onJitterV2) {
                onJitterV2 = false;
                paintBtn(btnJitterV2, getString(R.string.tele_v2), false);
            }
            if (onTeleSpeed && onDelayPing) {
                onDelayPing = false;
                paintBtn(btnDelayPing, getString(R.string.delay_ping), false);
            }
            paintBtn(btnTeleSpeed, getString(R.string.tele_speed), onTeleSpeed);
            playToggleBeep(onTeleSpeed);
            send(PacketVpnService.ACTION_TELE_SPEED);
        }
    }

    private void send(String action) {
        try {
            Intent i = new Intent(this, PacketVpnService.class);
            i.setAction(action);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(i);
            } else {
                startService(i);
            }
        } catch (Exception e) { }
    }

    private void rm(View v, boolean added) {
        if (!added || v == null) return;
        try {
            wm.removeView(v);
        } catch (Exception e) { }
    }

    private String fmt(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1048576) return (bytes / 1024) + "K";
        return (bytes / 1048576) + "M";
    }

    private static int blendColor(int from, int to, float progress) {
        float clamped = Math.max(0f, Math.min(1f, progress));
        int a = Color.alpha(from) + Math.round((Color.alpha(to) - Color.alpha(from)) * clamped);
        int r = Color.red(from) + Math.round((Color.red(to) - Color.red(from)) * clamped);
        int g = Color.green(from) + Math.round((Color.green(to) - Color.green(from)) * clamped);
        int b = Color.blue(from) + Math.round((Color.blue(to) - Color.blue(from)) * clamped);
        return Color.argb(a, r, g, b);
    }

    private static class CrosshairView extends View {
        private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int style = 3;
        private int color = Color.parseColor("#22D3EE");
        private int sizeDp = 42;
        private int alphaPercent = 100;

        CrosshairView(Context context) {
            super(context);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeCap(Paint.Cap.ROUND);
            linePaint.setStrokeJoin(Paint.Join.ROUND);
            glowPaint.setStyle(Paint.Style.STROKE);
            glowPaint.setStrokeCap(Paint.Cap.ROUND);
            glowPaint.setStrokeJoin(Paint.Join.ROUND);
        }

        void setConfig(int style, int color, int sizeDp, int alphaPercent) {
            this.style = style;
            this.color = color;
            this.sizeDp = sizeDp;
            this.alphaPercent = alphaPercent;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            float cx = w / 2f;
            float cy = h / 2f;
            float density = getResources().getDisplayMetrics().density;
            float radius = Math.max(8f, sizeDp * density * 0.5f);
            float stroke = Math.max(2.2f, radius * 0.075f);
            int alpha = Math.max(45, Math.min(255, Math.round(255f * (alphaPercent / 100f))));
            int drawColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));

            glowPaint.setStrokeWidth(stroke * 3.1f);
            glowPaint.setColor(Color.argb(Math.max(24, alpha / 3), Color.red(color), Color.green(color), Color.blue(color)));
            linePaint.setStrokeWidth(stroke);
            linePaint.setColor(drawColor);

            if (style == 0) drawDot(canvas, cx, cy, radius, true);
            else if (style == 1) drawCross(canvas, cx, cy, radius, false);
            else if (style == 2) drawRing(canvas, cx, cy, radius, false);
            else if (style == 3) { drawRing(canvas, cx, cy, radius, false); drawDot(canvas, cx, cy, radius, true); }
            else if (style == 4) { drawRing(canvas, cx, cy, radius, false); drawCross(canvas, cx, cy, radius, true); drawDot(canvas, cx, cy, radius, true); }
            else if (style == 5) drawDynamic(canvas, cx, cy, radius);
            else if (style == 6) drawMinimal(canvas, cx, cy, radius);
            else { drawDynamic(canvas, cx, cy, radius); drawDot(canvas, cx, cy, radius, true); }
        }

        private void drawDot(Canvas canvas, float cx, float cy, float radius, boolean fill) {
            Paint old = linePaint;
            old.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, Math.max(2.6f, radius * 0.12f), old);
            old.setStyle(Paint.Style.STROKE);
        }

        private void drawRing(Canvas canvas, float cx, float cy, float radius, boolean small) {
            float r = small ? radius * 0.32f : radius * 0.54f;
            canvas.drawCircle(cx, cy, r, glowPaint);
            canvas.drawCircle(cx, cy, r, linePaint);
        }

        private void drawCross(Canvas canvas, float cx, float cy, float radius, boolean longLine) {
            float len = longLine ? radius * 0.92f : radius * 0.72f;
            float gap = radius * 0.22f;
            canvas.drawLine(cx - len, cy, cx - gap, cy, glowPaint);
            canvas.drawLine(cx + gap, cy, cx + len, cy, glowPaint);
            canvas.drawLine(cx, cy - len, cx, cy - gap, glowPaint);
            canvas.drawLine(cx, cy + gap, cx, cy + len, glowPaint);
            canvas.drawLine(cx - len, cy, cx - gap, cy, linePaint);
            canvas.drawLine(cx + gap, cy, cx + len, cy, linePaint);
            canvas.drawLine(cx, cy - len, cx, cy - gap, linePaint);
            canvas.drawLine(cx, cy + gap, cx, cy + len, linePaint);
        }

        private void drawDynamic(Canvas canvas, float cx, float cy, float radius) {
            float outer = radius * 0.70f;
            RectF r = new RectF(cx - outer, cy - outer, cx + outer, cy + outer);
            canvas.drawArc(r, 18f, 54f, false, glowPaint);
            canvas.drawArc(r, 108f, 54f, false, glowPaint);
            canvas.drawArc(r, 198f, 54f, false, glowPaint);
            canvas.drawArc(r, 288f, 54f, false, glowPaint);
            canvas.drawArc(r, 18f, 54f, false, linePaint);
            canvas.drawArc(r, 108f, 54f, false, linePaint);
            canvas.drawArc(r, 198f, 54f, false, linePaint);
            canvas.drawArc(r, 288f, 54f, false, linePaint);
        }

        private void drawMinimal(Canvas canvas, float cx, float cy, float radius) {
            float outer = radius * 0.72f;
            float arm = radius * 0.22f;
            drawCorner(canvas, cx - outer, cy - outer, arm, arm);
            drawCorner(canvas, cx + outer, cy - outer, -arm, arm);
            drawCorner(canvas, cx - outer, cy + outer, arm, -arm);
            drawCorner(canvas, cx + outer, cy + outer, -arm, -arm);
        }

        private void drawCorner(Canvas canvas, float x, float y, float dx, float dy) {
            canvas.drawLine(x, y, x + dx, y, glowPaint);
            canvas.drawLine(x, y, x, y + dy, glowPaint);
            canvas.drawLine(x, y, x + dx, y, linePaint);
            canvas.drawLine(x, y, x, y + dy, linePaint);
        }
    }

    private static class AnimatedCircleDrawable extends Drawable {
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final RectF innerRect = new RectF();
        private final float hueOffset;
        private float pulse;
        private float rotationDegrees;
        private boolean active;

        AnimatedCircleDrawable(float hueOffset) {
            this.hueOffset = hueOffset;
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeCap(Paint.Cap.ROUND);
            glowPaint.setStyle(Paint.Style.STROKE);
            glowPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        void setActive(boolean active) {
            this.active = active;
            updatePaints(getBounds());
            invalidateSelf();
        }

        void setPulse(float pulse) {
            this.pulse = pulse;
            updatePaints(getBounds());
            invalidateSelf();
        }

        void setRotationDegrees(float rotationDegrees) {
            this.rotationDegrees = rotationDegrees;
            invalidateSelf();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            updatePaints(bounds);
        }

        private void updatePaints(Rect bounds) {
            if (bounds == null || bounds.isEmpty()) return;
            float diameter = Math.min(bounds.width(), bounds.height());
            float stroke = Math.max(3f, diameter * 0.035f);
            float inset = stroke * 0.5f;
            int fillColor = active ? Color.parseColor("#26bf21") : Color.parseColor("#2A1F3F");
            int borderColor = active ? Color.parseColor("#08ff00") : Color.parseColor("#4B49AC");
            rect.set(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset);
            innerRect.setEmpty();

            fillPaint.setShader(null);
            fillPaint.setColor(fillColor);

            float pulseAlpha = active ? (0.92f + 0.08f * pulse) : 1f;
            borderPaint.setStrokeWidth(stroke);
            borderPaint.setShader(null);
            borderPaint.setColor(Color.argb((int) (255 * pulseAlpha), Color.red(borderColor), Color.green(borderColor), Color.blue(borderColor)));

            glowPaint.setStrokeWidth(stroke * 2.2f);
            glowPaint.setShader(null);
            glowPaint.setColor(Color.TRANSPARENT);

            shinePaint.setShader(null);
            shinePaint.setColor(Color.TRANSPARENT);
        }

        @Override
        public void draw(Canvas canvas) {
            float radius = Math.min(rect.width(), rect.height()) * 0.5f;
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius, glowPaint);
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius, fillPaint);
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius, borderPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            fillPaint.setAlpha(alpha);
            borderPaint.setAlpha(alpha);
            glowPaint.setAlpha(alpha);
            shinePaint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            fillPaint.setColorFilter(colorFilter);
            borderPaint.setColorFilter(colorFilter);
            glowPaint.setColorFilter(colorFilter);
            shinePaint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private class BtnTouchListener implements View.OnTouchListener {
        private final WindowManager.LayoutParams p;
        private final TextView tv;
        private final int id;

        private int ox, oy;
        private float startTouchRawX, startTouchRawY;
        private int activePointerId = -1;
        private boolean drag = false;
        private boolean isLocked = false;
        private long downTime;

        BtnTouchListener(WindowManager.LayoutParams p, TextView tv, int id) {
            this.p = p;
            this.tv = tv;
            this.id = id;
        }

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getActionMasked();
            int actionIndex = e.getActionIndex();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    if (activePointerId != -1) return true;

                    activePointerId = e.getPointerId(actionIndex);
                    ox = p.x;
                    oy = p.y;

                    startTouchRawX = e.getRawX() - e.getX() + e.getX(actionIndex);
                    startTouchRawY = e.getRawY() - e.getY() + e.getY(actionIndex);

                    drag = false;
                    downTime = System.currentTimeMillis();
                    return true;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (activePointerId == -1) return false;

                    int idx = e.findPointerIndex(activePointerId);
                    if (idx < 0) return false;

                    if (isLocked) return true;

                    float currentRawX = e.getRawX() - e.getX() + e.getX(idx);
                    float currentRawY = e.getRawY() - e.getY() + e.getY(idx);

                    float dx = currentRawX - startTouchRawX;
                    float dy = currentRawY - startTouchRawY;

                    if (Math.abs(dx) > 8f || Math.abs(dy) > 8f) {
                        drag = true;
                        p.x = ox + (int) dx;
                        p.y = oy + (int) dy;
                        try {
                            wm.updateViewLayout(tv, p);
                        } catch (Exception ex) { }
                    }
                    return true;
                }

                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP: {
                    int pointerId = e.getPointerId(actionIndex);
                    if (pointerId != activePointerId) return true;

                    long held = System.currentTimeMillis() - downTime;

                    if (held > 2000) {
                        isLocked = !isLocked;
                        Toast.makeText(
                                FloatingOverlayService.this,
                                isLocked ? getString(R.string.pos_locked) : getString(R.string.pos_unlocked),
                                Toast.LENGTH_SHORT
                        ).show();
                    } else if (!drag) {
                        click(id);
                    }

                    activePointerId = -1;
                    return true;
                }

                case MotionEvent.ACTION_CANCEL: {
                    activePointerId = -1;
                    drag = false;
                    return true;
                }
            }

            return false;
        }
    }

    private class MaskBarTouchListener implements View.OnTouchListener {
        private final WindowManager.LayoutParams p;
        private final View view;

        private int ox, oy;
        private float startTouchRawX, startTouchRawY;
        private int activePointerId = -1;
        private boolean drag = false;
        private boolean isLocked = false;
        private long downTime;

        MaskBarTouchListener(WindowManager.LayoutParams p, View view) {
            this.p = p;
            this.view = view;
        }

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getActionMasked();
            int actionIndex = e.getActionIndex();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    if (activePointerId != -1) return true;

                    activePointerId = e.getPointerId(actionIndex);
                    ox = p.x;
                    oy = p.y;
                    startTouchRawX = e.getRawX() - e.getX() + e.getX(actionIndex);
                    startTouchRawY = e.getRawY() - e.getY() + e.getY(actionIndex);
                    drag = false;
                    downTime = System.currentTimeMillis();
                    return true;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (activePointerId == -1) return false;
                    int idx = e.findPointerIndex(activePointerId);
                    if (idx < 0) return false;
                    if (isLocked) return true;

                    float currentRawX = e.getRawX() - e.getX() + e.getX(idx);
                    float currentRawY = e.getRawY() - e.getY() + e.getY(idx);
                    float dx = currentRawX - startTouchRawX;
                    float dy = currentRawY - startTouchRawY;

                    if (Math.abs(dx) > 8f || Math.abs(dy) > 8f) {
                        drag = true;
                        p.x = ox + (int) dx;
                        p.y = oy + (int) dy;
                        try {
                            wm.updateViewLayout(view, p);
                        } catch (Exception ex) { }
                    }
                    return true;
                }

                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP: {
                    int pointerId = e.getPointerId(actionIndex);
                    if (pointerId != activePointerId) return true;

                    long held = System.currentTimeMillis() - downTime;
                    if (held > 2000) {
                        isLocked = !isLocked;
                        Toast.makeText(
                                FloatingOverlayService.this,
                                isLocked ? getString(R.string.pos_locked) : getString(R.string.pos_unlocked),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    activePointerId = -1;
                    drag = false;
                    return true;
                }

                case MotionEvent.ACTION_CANCEL: {
                    activePointerId = -1;
                    drag = false;
                    return true;
                }
            }

            return false;
        }
    }

    private class StatTouchListener implements View.OnTouchListener {
        private final WindowManager.LayoutParams p;

        private int ox, oy;
        private float startTouchRawX, startTouchRawY;
        private int activePointerId = -1;

        StatTouchListener(WindowManager.LayoutParams p) {
            this.p = p;
        }

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int action = e.getActionMasked();
            int actionIndex = e.getActionIndex();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    if (activePointerId != -1) return true;

                    activePointerId = e.getPointerId(actionIndex);
                    ox = p.x;
                    oy = p.y;

                    startTouchRawX = e.getRawX() - e.getX() + e.getX(actionIndex);
                    startTouchRawY = e.getRawY() - e.getY() + e.getY(actionIndex);
                    return true;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (activePointerId == -1) return false;

                    int idx = e.findPointerIndex(activePointerId);
                    if (idx < 0) return false;

                    float currentRawX = e.getRawX() - e.getX() + e.getX(idx);
                    float currentRawY = e.getRawY() - e.getY() + e.getY(idx);

                    p.x = ox + (int) (currentRawX - startTouchRawX);
                    p.y = oy + (int) (currentRawY - startTouchRawY);

                    try {
                        wm.updateViewLayout(txtStats, p);
                    } catch (Exception ex) { }

                    return true;
                }

                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP: {
                    int pointerId = e.getPointerId(actionIndex);
                    if (pointerId == activePointerId) {
                        activePointerId = -1;
                    }
                    return true;
                }

                case MotionEvent.ACTION_CANCEL: {
                    activePointerId = -1;
                    return true;
                }
            }

            return false;
        }
    }
}
