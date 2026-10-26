package com.fakelag.cryhard8;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQ_VPN        = 0x3e9;
    private static final int REQ_OVERLAY    = 0x3ea;
    private static final int REQ_NOTIF      = 0x3eb;
    private static final int REQ_STORAGE    = 0x3ec;

    private static final String PREFS        = "xevpn_prefs";
    private static final String KEY_GHOST    = "show_ghost";
    private static final String KEY_FREEZE   = "show_freeze";
    private static final String KEY_TELE     = "show_tele";
    private static final String KEY_TELE_V2  = "show_tele_v2";
    private static final String KEY_DELAY_PING = "show_delay_ping";
    private static final String KEY_TELE_SPEED = "show_tele_speed";
    private static final String KEY_MASK_BAR = "show_mask_bar";
    private static final String KEY_MASK_BAR_THICKNESS = "mask_bar_thickness";
    private static final String KEY_MASK_BAR_LENGTH = "mask_bar_length";
    private static final String KEY_CROSSHAIR = "show_crosshair";
    private static final String KEY_CROSSHAIR_STYLE = "crosshair_style";
    private static final String KEY_CROSSHAIR_COLOR = "crosshair_color";
    private static final String KEY_CROSSHAIR_SIZE = "crosshair_size";
    private static final String KEY_CROSSHAIR_ALPHA = "crosshair_alpha";
    private static final String KEY_AUTO_STOP_TELE_ON_FREEZE = "auto_stop_tele_on_freeze";
    private static final String KEY_DEV_MODE = "dev_mode";
    private static final String KEY_TARGET_PACKAGE = "target_package";
    private static final String KEY_FLOAT_ALPHA = "float_alpha_percent";
    private static final String KEY_FLOAT_SIZE = "float_size_percent";
    private static final String KEY_TELE_REPLAY_DELAY = "tele_replay_delay_ms";
    private static final String KEY_TELE_SPEED_BURST = "tele_speed_burst_packets";
    private static final String KEY_TELE_SPEED_SLEEP = "tele_speed_sleep_ms";
    private static final String KEY_TELE_SPEED_CATCHUP_TAIL = "tele_speed_catchup_tail_v2";
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
    private static final String EXTRA_DEV_MODE = "dev_mode";
    private static final String EXTRA_TELE_REPLAY_DELAY = "tele_replay_delay_ms";
    private static final String EXTRA_TELE_SPEED_BURST = "tele_speed_burst_packets";
    private static final String EXTRA_TELE_SPEED_SLEEP = "tele_speed_sleep_ms";
    private static final String EXTRA_TELE_SPEED_CATCHUP_TAIL = "tele_speed_catchup_tail";
    private static final String EXTRA_LOBBY_ROUTE = "vpn_lobby_route";
    private static final int FLOAT_ALPHA_MIN = 35;
    private static final int FLOAT_ALPHA_MAX = 100;
    private static final int FLOAT_SIZE_MIN = 80;
    private static final int FLOAT_SIZE_MAX = 260;
    private static final int MASK_BAR_THICKNESS_MIN = 6;
    private static final int MASK_BAR_THICKNESS_MAX = 36;
    private static final int MASK_BAR_LENGTH_MIN = 120;
    private static final int MASK_BAR_LENGTH_MAX = 340;
    private static final int CROSSHAIR_SIZE_MIN = 20;
    private static final int CROSSHAIR_SIZE_MAX = 96;
    private static final int CROSSHAIR_SIZE_DEFAULT = 42;
    private static final int CROSSHAIR_ALPHA_MIN = 35;
    private static final int CROSSHAIR_ALPHA_MAX = 100;
    private static final int CROSSHAIR_ALPHA_DEFAULT = 100;
    private static final int TELE_REPLAY_DELAY_MIN = 1;
    private static final int TELE_REPLAY_DELAY_MAX = 50;
    private static final int TELE_SPEED_BURST_MIN = 1;
    private static final int TELE_SPEED_BURST_MAX = 16;
    private static final int TELE_SPEED_BURST_DEFAULT = 1;
    private static final int TELE_SPEED_SLEEP_MIN = 0;
    private static final int TELE_SPEED_SLEEP_MAX = 20;
    private static final int TELE_SPEED_SLEEP_DEFAULT = 6;
    private static final boolean TELE_SPEED_CATCHUP_TAIL_DEFAULT = true;

    private List apps       = new ArrayList();
    private View btnEngine;
    private ImageView ivEnginePower;
    private boolean engineOn = false;
    private boolean loaded   = false;
    private String[] names;
    private String selPkg    = null;
    private TextView tvApp;
    private TextView tvStatus;
    private View dotEngine;
    private View dotApp;
    private boolean waiting  = false;

    private View dotVpn, dotDisplay, dotNotif, dotStorage;
    private TextView tvVpnStatus, tvDisplayStatus, tvNotifStatus, tvStorageStatus;
    private TextView cbVpn, cbDisplay, cbNotif, cbStorage;

    private TextView cbFloatGhost, cbFloatFreeze, cbFloatTele, cbFloatTeleV2, cbFloatDelayPing, cbFloatTeleSpeed;
    private TextView tvFloatGhostStatus, tvFloatFreezeStatus, tvFloatTeleStatus, tvFloatTeleV2Status, tvFloatDelayPingStatus, tvFloatTeleSpeedStatus;
    private TextView cbAutoStopTele;
    private TextView tvAutoStopTeleStatus;
    private TextView cbMaskBar;
    private TextView tvMaskBarStatus;
    private TextView cbDevMode;
    private TextView tvDevModeStatus;
    private TextView cbCrosshair;
    private TextView tvCrosshairStatus;
    private TextView tvFloatSizeValue, tvFloatAlphaValue, tvMaskBarThicknessValue, tvMaskBarLengthValue;
    private TextView tvCrosshairSizeValue, tvCrosshairAlphaValue;
    private TextView tvTeleReplayDelayValue, tvTeleSpeedBurstValue, tvTeleSpeedSleepValue, tvTeleSpeedCatchupStatus;
    private TextView cbTeleSpeedCatchup;
    private SeekBar seekFloatSize, seekFloatAlpha, seekMaskBarThickness, seekMaskBarLength, seekTeleReplayDelay, seekTeleSpeedBurst, seekTeleSpeedSleep;
    private SeekBar seekCrosshairSize, seekCrosshairAlpha;
    private TextView[] crosshairStyleButtons;
    private View[] crosshairColorButtons;
    private ViewGroup mainContent;
    private View groupMaskOptions, groupTeleOptions, groupTeleSpeedOptions;

    // BroadcastReceiver lắng nghe tín hiệu Ban/Kick từ BackgroundCheckService
    private BroadcastReceiver killReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent == null) return;
            String reason = intent.getStringExtra("reason");
            if ("BANNED".equals(reason) || "KICKED".equals(reason)) {
                // 1. Tắt VPN
                try {
                    Intent vpnStop = new Intent(MainActivity.this, PacketVpnService.class);
                    vpnStop.setAction("com.fakelag.cryhard8.STOP");
                    startService(vpnStop);
                } catch (Exception e) {}
                // 2. Tắt Nút nổi
                try { stopService(new Intent(MainActivity.this, FloatingOverlayService.class)); } catch (Exception e) {}
                // 3. Hiện màn hình đen
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBlacklistScreen(reason);
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // KIỂM TRA KEY KHI MỞ APP
        if (!KeyManager.isActivated(this)) {
            showKeyDialog();
            return; // Dừng không cho chạy tiếp xuống dưới
        }

        setContentView(R.layout.activity_main);

        // Khởi động Service chạy ngầm check trạng thái Ban/Kick
        try {
            Intent serviceIntent = new Intent(this, BackgroundCheckService.class);
            startService(serviceIntent);
        } catch (Exception e) { }

        // Đăng ký nhận tín hiệu tắt App
        IntentFilter filter = new IntentFilter("com.fakelag.cryhard8.KILL_APP");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(killReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(killReceiver, filter);
        }

        try {
            if (getActionBar() != null) getActionBar().hide();
        } catch (Exception e) { }

        btnEngine   = findViewById(R.id.btn_engine);
        ivEnginePower = (ImageView) findViewById(R.id.iv_engine_power);
        tvApp       = (TextView) findViewById(R.id.tv_selected_app);
        tvStatus    = (TextView) findViewById(R.id.tv_status);
        dotEngine   = findViewById(R.id.dot_engine);
        dotApp      = findViewById(R.id.dot_app);
        final Button btnShowFloating = (Button) findViewById(R.id.btn_show_floating);
        final Button btnFfNormal = (Button) findViewById(R.id.btn_ff_normal);
        final Button btnFfMax = (Button) findViewById(R.id.btn_ff_max);
        mainContent = (ViewGroup) findViewById(R.id.main_content);
        groupMaskOptions = findViewById(R.id.group_mask_options);
        groupTeleOptions = findViewById(R.id.group_tele_options);
        groupTeleSpeedOptions = findViewById(R.id.group_tele_speed_options);

        cbFloatGhost      = (TextView) findViewById(R.id.cb_float_ghost);
        cbFloatFreeze     = (TextView) findViewById(R.id.cb_float_freeze);
        cbFloatTele       = (TextView) findViewById(R.id.cb_float_tele);
        cbFloatTeleV2     = (TextView) findViewById(R.id.cb_float_tele_v2);
        cbFloatDelayPing  = (TextView) findViewById(R.id.cb_float_delay_ping);
        cbFloatTeleSpeed  = (TextView) findViewById(R.id.cb_float_tele_speed);
        tvFloatGhostStatus   = (TextView) findViewById(R.id.tv_float_ghost_status);
        tvFloatFreezeStatus  = (TextView) findViewById(R.id.tv_float_freeze_status);
        tvFloatTeleStatus    = (TextView) findViewById(R.id.tv_float_tele_status);
        tvFloatTeleV2Status  = (TextView) findViewById(R.id.tv_float_tele_v2_status);
        tvFloatDelayPingStatus = (TextView) findViewById(R.id.tv_float_delay_ping_status);
        tvFloatTeleSpeedStatus = (TextView) findViewById(R.id.tv_float_tele_speed_status);
        cbAutoStopTele       = (TextView) findViewById(R.id.cb_auto_stop_tele);
        tvAutoStopTeleStatus = (TextView) findViewById(R.id.tv_auto_stop_tele_status);
        cbMaskBar            = (TextView) findViewById(R.id.cb_mask_bar);
        tvMaskBarStatus      = (TextView) findViewById(R.id.tv_mask_bar_status);
        cbDevMode            = (TextView) findViewById(R.id.cb_dev_mode);
        tvDevModeStatus      = (TextView) findViewById(R.id.tv_dev_mode_status);
        cbCrosshair          = (TextView) findViewById(R.id.cb_crosshair);
        tvCrosshairStatus    = (TextView) findViewById(R.id.tv_crosshair_status);

        dotVpn     = findViewById(R.id.dot_vpn);
        dotDisplay = findViewById(R.id.dot_display);
        dotNotif   = findViewById(R.id.dot_notif);
        dotStorage = findViewById(R.id.dot_storage);
        tvVpnStatus     = (TextView) findViewById(R.id.tv_vpn_status);
        tvDisplayStatus = (TextView) findViewById(R.id.tv_display_status);
        tvNotifStatus   = (TextView) findViewById(R.id.tv_notif_status);
        tvStorageStatus = (TextView) findViewById(R.id.tv_storage_status);
        cbVpn     = (TextView) findViewById(R.id.cb_vpn);
        cbDisplay = (TextView) findViewById(R.id.cb_display);
        cbNotif   = (TextView) findViewById(R.id.cb_notif);
        cbStorage = (TextView) findViewById(R.id.cb_storage);
        tvFloatSizeValue = (TextView) findViewById(R.id.tv_float_size_value);
        tvFloatAlphaValue = (TextView) findViewById(R.id.tv_float_alpha_value);
        tvMaskBarThicknessValue = (TextView) findViewById(R.id.tv_mask_bar_thickness_value);
        tvMaskBarLengthValue = (TextView) findViewById(R.id.tv_mask_bar_length_value);
        tvTeleReplayDelayValue = (TextView) findViewById(R.id.tv_tele_replay_delay_value);
        tvTeleSpeedBurstValue = (TextView) findViewById(R.id.tv_tele_speed_burst_value);
        tvTeleSpeedSleepValue = (TextView) findViewById(R.id.tv_tele_speed_sleep_value);
        tvTeleSpeedCatchupStatus = (TextView) findViewById(R.id.tv_tele_speed_catchup_status);
        cbTeleSpeedCatchup = (TextView) findViewById(R.id.cb_tele_speed_catchup);
        seekFloatSize = (SeekBar) findViewById(R.id.seek_float_size);
        seekFloatAlpha = (SeekBar) findViewById(R.id.seek_float_alpha);
        seekMaskBarThickness = (SeekBar) findViewById(R.id.seek_mask_bar_thickness);
        seekMaskBarLength = (SeekBar) findViewById(R.id.seek_mask_bar_length);
        seekTeleReplayDelay = (SeekBar) findViewById(R.id.seek_tele_replay_delay);
        seekTeleSpeedBurst = (SeekBar) findViewById(R.id.seek_tele_speed_burst);
        seekTeleSpeedSleep = (SeekBar) findViewById(R.id.seek_tele_speed_sleep);
        tvCrosshairSizeValue = (TextView) findViewById(R.id.tv_crosshair_size_value);
        tvCrosshairAlphaValue = (TextView) findViewById(R.id.tv_crosshair_alpha_value);
        seekCrosshairSize = (SeekBar) findViewById(R.id.seek_crosshair_size);
        seekCrosshairAlpha = (SeekBar) findViewById(R.id.seek_crosshair_alpha);
        crosshairStyleButtons = new TextView[]{
                (TextView) findViewById(R.id.crosshair_style_dot),
                (TextView) findViewById(R.id.crosshair_style_cross),
                (TextView) findViewById(R.id.crosshair_style_ring),
                (TextView) findViewById(R.id.crosshair_style_dot_ring),
                (TextView) findViewById(R.id.crosshair_style_sniper),
                (TextView) findViewById(R.id.crosshair_style_dynamic),
                (TextView) findViewById(R.id.crosshair_style_minimal),
                (TextView) findViewById(R.id.crosshair_style_custom)
        };
        crosshairColorButtons = new View[]{
                findViewById(R.id.crosshair_color_cyan),
                findViewById(R.id.crosshair_color_green),
                findViewById(R.id.crosshair_color_yellow),
                findViewById(R.id.crosshair_color_orange),
                findViewById(R.id.crosshair_color_red),
                findViewById(R.id.crosshair_color_pink),
                findViewById(R.id.crosshair_color_purple),
                findViewById(R.id.crosshair_color_white),
                findViewById(R.id.crosshair_color_black)
        };

        setupFloatingToggle(cbFloatFreeze,  tvFloatFreezeStatus,  KEY_FREEZE);
        setupFloatingToggle(cbFloatTeleV2,  tvFloatTeleV2Status,  KEY_GHOST);
        setupFloatingToggle(cbFloatTeleSpeed, tvFloatTeleSpeedStatus, KEY_TELE_SPEED);
        setupFloatSizeSeek();
        setupFloatAlphaSeek();
        setupCrosshairControls();
        updateConditionalGroups(false);
        animateMainEntrance();

        setupPermissionToggle();
        setUI(false);
        loadApps();

        btnEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waiting) return;
                if (!engineOn) {
                    if (selPkg == null) {
                        toast(getString(R.string.toast_choose_app));
                        return;
                    }
                    checkOverlay();
                } else {
                    stopEngine();
                }
            }
        });

        btnShowFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loaded) {
                    toast(getString(R.string.checking));
                    return;
                }
                showPicker();
            }
        });

        btnFfNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selPkg = "com.dts.freefireth";
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_TARGET_PACKAGE, selPkg).commit();
                tvApp.setText("ĐÃ CHỌN: FF THƯỜNG");
                dotApp.setBackgroundResource(R.drawable.dot_on);
                toast("Đã chọn Free Fire Thường");
            }
        });

        btnFfMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selPkg = "com.dts.freefiremax";
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_TARGET_PACKAGE, selPkg).commit();
                tvApp.setText("ĐÃ CHỌN: FF MAX");
                dotApp.setBackgroundResource(R.drawable.dot_on);
                toast("Đã chọn Free Fire MAX");
            }
        });
    }

    // === HÀM HIỂN THỊ MÀN HÌNH ĐEN KHI BỊ BAN/KICK ===
    private void showBlacklistScreen(String reason) {
        final TextView tv = new TextView(this);
        tv.setText(reason.equals("BANNED") ? "MÁY BẠN ĐÃ BỊ BACKLIST\nVUI LÒNG LIÊN HỆ ADMIN ĐỂ ĐƯỢC HỖ TRỢ" : "PHIÊN ĐĂNG NHẬP ĐÃ HẾT HẠN\nVUI LÒNG NHẬP LẠI KEY");
        tv.setTextColor(Color.RED);
        tv.setTextSize(24);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.BLACK);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        
        final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        int type = (Build.VERSION.SDK_INT >= 26) ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            android.graphics.PixelFormat.OPAQUE
        );
        try {
            wm.addView(tv, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Tự động tắt app sau 30 giây
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    wm.removeView(tv);
                } catch (Exception e) {}
                finishAffinity(); // Đóng toàn bộ app
                System.exit(0);
            }
        }, 30000);
    }

    // === HÀM HIỂN THỊ DIALOG NHẬP KEY ===
    private void showKeyDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("KÍCH HOẠT KEY");
        builder.setCancelable(false);
        builder.setMessage("Vui lòng nhập Key để sử dụng ứng dụng.");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập Key tại đây...");
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(input);
        
        final android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        layout.addView(progressBar);
        
        builder.setView(layout);

        final AlertDialog dialog = builder.create();

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "KÍCH HOẠT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                // Xử lý ở dưới
            }
        });
        
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "THOÁT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                finish();
            }
        });

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String key = input.getText().toString().trim();
                if (key.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập Key!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                KeyManager.checkKeyOnline(MainActivity.this, key, new KeyManager.KeyCallback() {
                    @Override
                    public void onResult(final boolean success, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                                if (success) {
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    recreate(); // Khởi động lại Activity
                                } else {
                                    if (message.startsWith("UPDATE|")) {
                                        String updateMsg = message.replace("UPDATE|", "");
                                        new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("CẬP NHẬT PHIÊN BẢN")
                                            .setMessage(updateMsg)
                                            .setCancelable(false)
                                            .setPositiveButton("THOÁT", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface d, int w) {
                                                    finish();
                                                }
                                            }).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPermissionStates();
    }

    // ... (Các hàm setup khác giữ nguyên)
    
    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(killReceiver);
        } catch (Exception e) {}
        super.onDestroy();
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Các hàm setupCrosshairControls, setupFloatingToggle, ... giữ nguyên như cũ
    // (Mình rút gọn để không quá dài, bạn giữ nguyên các hàm cũ là được)
}
