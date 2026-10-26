package com.fakelag.cryhard8;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PacketVpnService extends VpnService implements Runnable {

    public static final String ACTION_ACTIVATE   = "com.fakelag.cryhard8.ACTIVATE";
    public static final String ACTION_DEACTIVATE = "com.fakelag.cryhard8.DEACTIVATE";
    public static final String ACTION_FREEZE     = "com.fakelag.cryhard8.FREEZE";
    public static final String ACTION_GHOST      = "com.fakelag.cryhard8.GHOST";
    public static final String ACTION_JITTER     = "com.fakelag.cryhard8.JITTER";
    public static final String ACTION_JITTER_V2  = "com.fakelag.cryhard8.JITTER_V2";
    public static final String ACTION_DELAY_PING = "com.fakelag.cryhard8.DELAY_PING";
    public static final String ACTION_TELE_SPEED = "com.fakelag.cryhard8.TELE_SPEED";
    public static final String ACTION_SET_APP    = "com.fakelag.cryhard8.SET_APP";
    public static final String ACTION_STATS      = "com.fakelag.cryhard8.STATS";
    public static final String ACTION_STOP       = "com.fakelag.cryhard8.STOP";
    public static final String EXTRA_BYTES_IN    = "bytes_in";
    public static final String EXTRA_BYTES_OUT   = "bytes_out";
    public static final String EXTRA_FILTER_LOG  = "filter_log";
    public static final String EXTRA_FILTER_ENABLED = "filter_enabled";
    public static final String EXTRA_LOSS        = "loss";
    public static final String EXTRA_MODE        = "mode";
    public static final String EXTRA_PING        = "ping";
    private static final String PREFS            = "xevpn_prefs";
    private static final String KEY_TARGET_PACKAGE = "target_package";
    private static final String KEY_TUN_IP_CURSOR = "tun_ip_cursor";
    private static final String KEY_DEV_MODE = "dev_mode";
    private static final String KEY_TELE_REPLAY_DELAY = "tele_replay_delay_ms";
    private static final String KEY_TELE_SPEED_BURST = "tele_speed_burst_packets";
    private static final String KEY_TELE_SPEED_SLEEP = "tele_speed_sleep_ms";
    private static final String KEY_TELE_SPEED_CATCHUP_TAIL = "tele_speed_catchup_tail_v2";
    private static final String KEY_AUTO_STOP_TELE_ON_FREEZE = "auto_stop_tele_on_freeze";
    private static final String ACTION_APPLY_FLOAT_APPEARANCE = "com.fakelag.cryhard8.APPLY_FLOAT_APPEARANCE";
    private static final String EXTRA_DEV_MODE = "dev_mode";
    private static final String EXTRA_TELE_REPLAY_DELAY = "tele_replay_delay_ms";
    private static final String EXTRA_TELE_SPEED_BURST = "tele_speed_burst_packets";
    private static final String EXTRA_TELE_SPEED_SLEEP = "tele_speed_sleep_ms";
    private static final String EXTRA_TELE_SPEED_CATCHUP_TAIL = "tele_speed_catchup_tail";
    private static final String EXTRA_LOBBY_ROUTE = "vpn_lobby_route";

    private static final int  DROP_MIN     = 0x15;
    private static final int  DROP_MAX     = 0x1c2;
    private static final long IDLE         = 0xafc8L;
    private static final int  SO_TO        = 0x7530;
    private static final int  TELE_BUF_MAX = 10000;
    private static final int  TELE_PACKET_NONE = 0;
    private static final int  TELE_PACKET_MOVE = 1;
    private static final int  TELE_PACKET_FIRE = 2;
    private static final int  TELE_MOVE_MIN = 0x37;
    private static final int  TELE_MOVE_MAX = 0xa0;
    private static final int  TELE_FIRE_MIN = DROP_MIN;
    private static final int  TELE_FIRE_MAX = DROP_MAX;
    private static final int  TELE_BROAD_FIRE_MAX = 0x36;
    private static final int  TELE_V2_MOVE_TAIL_MAX = 64;
    private static final int  TELE_V2_BUF_MAX = TELE_V2_MOVE_TAIL_MAX;
    private static final int  TELE_V2_REPLAY_DELAY_MAX_MS = 3;
    private static final long DELAY_PING_WINDOW_MS = 500L;
    private static final int  TELE_REPLAY_DELAY_DEFAULT_MS = 1;
    private static final int  TELE_REPLAY_DELAY_MIN_MS = 1;
    private static final int  TELE_REPLAY_DELAY_MAX_MS = 50;
    private static final int  TELE_SPEED_BURST_DEFAULT = 1;
    private static final int  TELE_SPEED_BURST_MIN = 1;
    private static final int  TELE_SPEED_BURST_MAX = 16;
    private static final int  TELE_SPEED_SLEEP_DEFAULT_MS = 6;
    private static final int  TELE_SPEED_SLEEP_MIN_MS = 0;
    private static final int  TELE_SPEED_SLEEP_MAX_MS = 20;
    private static final int  TELE_SPEED_CATCHUP_TAIL_MAX = 32;
    private static final boolean TELE_SPEED_CATCHUP_TAIL_DEFAULT = false;
    private static final boolean ICMP_ECHO_REPLY_ENABLED = true;
    private static final int  TUN_IP_B_BASE = 12;
    private static final int  TUN_IP_B_COUNT = 50;
    private static final int  TUN_IP_C_COUNT = 254;
    private static final int  TUN_IP_POOL_SIZE = TUN_IP_B_COUNT * TUN_IP_C_COUNT;
    private static final int  DEV_MAX_EVENTS = 20000;
    private static final int  DEV_MAX_PAYLOAD_BYTES = 512;
    private static final int  DEV_UNKNOWN_ACTIVE_MAX = 512;
    private static final int  DEV_PROTOCOL_DROP_MAX = 1024;
    private static final String CH_ID      = "vpn_svc";

    static final class SE {
        final DatagramSocket s;
        volatile long t;
        SE(DatagramSocket socket) { s = socket; t = System.currentTimeMillis(); }
        void touch() { t = System.currentTimeMillis(); }
    }

    static final class _TP {
        final byte[] data; final int dip, dp, sp, type;
        _TP(byte[] data, int dip, int dp, int sp, int type) {
            this.data=data; this.dip=dip; this.dp=dp; this.sp=sp; this.type=type;
        }
    }

    static final class TunAddress {
        final String ip;
        final int ipInt;
        TunAddress(String ip, int ipInt) {
            this.ip = ip;
            this.ipInt = ipInt;
        }
    }

    private volatile boolean isRun    = false;
    private volatile boolean isActive = false;
    private volatile boolean isFilterEnabled = false;
    private volatile boolean isFreeze = false;
    private volatile boolean isGhost  = false;
    private volatile boolean isJitter = false;
    private volatile boolean isJitterV2 = false;
    private volatile boolean isDelayPing = false;
    private volatile boolean isTeleSpeed = false;

    private final ConcurrentLinkedQueue<_TP> _teleBuf = new ConcurrentLinkedQueue<>();
    private final Object _teleCatchupLock = new Object();
    private final ArrayList<_TP> _teleCatchupBuf = new ArrayList<_TP>();
    private final Object _teleV2Lock = new Object();
    private final ArrayList<_TP> _teleV2Buf = new ArrayList<_TP>();
    private final Object _delayPingLock = new Object();
    private final ArrayList<_TP> _delayPingBuf = new ArrayList<_TP>();
    private final ConcurrentLinkedQueue<List<_TP>> _delayPingReplayQueue = new ConcurrentLinkedQueue<List<_TP>>();
    private final Object _teleSpeedLock = new Object();
    private final ArrayList<_TP> _teleSpeedBuf = new ArrayList<_TP>();
    private final ArrayList<_TP> _teleSpeedCatchupBuf = new ArrayList<_TP>();
    private volatile boolean _teleReplaying = false;
    private volatile boolean _teleV2Replaying = false;
    private volatile boolean _delayPingReplaying = false;
    private volatile boolean _teleSpeedReplaying = false;
    private volatile boolean _delayPingLoopRunning = false;
    private volatile long    _teleStartTime = 0L;
    private int _teleV2MoveCount = 0;
    private int _delayPingGeneration = 0;

    private String pkg = "";
    private final ConcurrentHashMap<Long, SE> udpMap = new ConcurrentHashMap<>();
    private final AtomicLong bIn   = new AtomicLong();
    private final AtomicLong bOut  = new AtomicLong();
    private final AtomicLong pSent = new AtomicLong();
    private final AtomicLong pDrop = new AtomicLong();
    private final AtomicLong outUdpSeen = new AtomicLong();
    private volatile long rtt  = 40L;
    private final Random rng   = new Random();
    private volatile int tunIP = 0xC0A8010A;
    private volatile ParcelFileDescriptor tun;
    private volatile Thread vpnT;
    private ExecutorService threadPool;
    private BroadcastReceiver devReceiver;
    private final Object devLock = new Object();
    private volatile boolean devMode = false;
    private volatile int teleReplayDelayMs = TELE_REPLAY_DELAY_DEFAULT_MS;
    private volatile int teleSpeedBurstPackets = TELE_SPEED_BURST_DEFAULT;
    private volatile int teleSpeedSleepMs = TELE_SPEED_SLEEP_DEFAULT_MS;
    private volatile boolean teleSpeedCatchupTail = TELE_SPEED_CATCHUP_TAIL_DEFAULT;
    private volatile boolean lobbyRouteEnabled = true;
    private volatile boolean lobbyRouteFallbackFull = false;
    private boolean devSessionActive = false;
    private long devSessionStartMs = 0L;
    private long devSessionEndMs = 0L;
    private int devEventSeq = 0;
    private int devDroppedEvents = 0;
    private int devUnknownActiveEvents = 0;
    private int devProtocolDropEvents = 0;
    private int devNonIpv4Drops = 0;
    private int devNonUdpDrops = 0;
    private final ArrayList<String> devEvents = new ArrayList<String>();
    private final HashMap<String, Integer> devKindCounts = new HashMap<String, Integer>();
    private final HashMap<Integer, Integer> devSizeCounts = new HashMap<Integer, Integer>();

    @Override
    public void onCreate() {
        super.onCreate();
        threadPool = Executors.newCachedThreadPool();
        setupForeground();
        setupDevReceiver();
        setDevMode(false);
        teleReplayDelayMs = getSavedTeleReplayDelayMs();
        loadTeleSpeedSettings();
        loadLobbyRouteSetting(false);
    }

    private void setupForeground() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(CH_ID, "VPN Service", NotificationManager.IMPORTANCE_LOW);
            if (nm != null) nm.createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, CH_ID)
                .setContentTitle("SunnyMod")
                .setContentText("VPN đang hoạt động...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(1, n);
            }
        }
    }

    private void setupDevReceiver() {
        devReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) return;
                if (!ACTION_APPLY_FLOAT_APPEARANCE.equals(intent.getAction())) return;
                boolean enabled = intent.getBooleanExtra(EXTRA_DEV_MODE, devMode);
                setDevMode(enabled);
                setTeleReplayDelayMs(intent.getIntExtra(EXTRA_TELE_REPLAY_DELAY, teleReplayDelayMs));
                setTeleSpeedSettings(
                        intent.getIntExtra(EXTRA_TELE_SPEED_BURST, teleSpeedBurstPackets),
                        intent.getIntExtra(EXTRA_TELE_SPEED_SLEEP, teleSpeedSleepMs),
                        intent.getBooleanExtra(EXTRA_TELE_SPEED_CATCHUP_TAIL, teleSpeedCatchupTail));
                setLobbyRouteEnabled(intent.getBooleanExtra(EXTRA_LOBBY_ROUTE, true), true);
            }
        };

        try {
            IntentFilter filter = new IntentFilter(ACTION_APPLY_FLOAT_APPEARANCE);
            registerReceiverCompat(devReceiver, filter);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_STICKY;
        String action = intent.getAction();
        if (pkg == null || pkg.isEmpty()) {
            pkg = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_TARGET_PACKAGE, "");
        }
        setDevMode(false);
        teleReplayDelayMs = getSavedTeleReplayDelayMs();
        loadTeleSpeedSettings();
        loadLobbyRouteSetting(false);

        if (ACTION_SET_APP.equals(action)) {
            pkg = intent.getStringExtra("PACKAGE");
            if (pkg == null) pkg = intent.getStringExtra("PKG");
            if (pkg == null) pkg = "";
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_TARGET_PACKAGE, pkg).commit();
            disableFilters();
            isActive = true;
            isFilterEnabled = true;
            if (isRun) {
                restartTunnel(true);
            }
        } else if (ACTION_STOP.equals(action)) {
            stop(); return START_NOT_STICKY;
        } else if (ACTION_FREEZE.equals(action)) {
            if (!isRun || !isFilterEnabled) return START_NOT_STICKY;
            isFreeze = !isFreeze;
            if (isFreeze && shouldAutoStopTeleOnFreeze()) {
                if (isJitter) stopJitterAndReplay();
                if (isTeleSpeed) stopTeleSpeedAndReplay();
                if (isDelayPing) stopDelayPingAndReplay();
            }
            if (isFreeze) scheduleLobbyRouteFallbackIfNoUdp("freeze");
        } else if (ACTION_GHOST.equals(action)) {
            if (!isRun || !isFilterEnabled) return START_NOT_STICKY;
            if (!isGhost && isDelayPing) stopDelayPingAndReplay();
            if (!isGhost && isTeleSpeed) stopTeleSpeedAndReplay();
            isGhost = !isGhost;
            if (isGhost) scheduleLobbyRouteFallbackIfNoUdp("ghost");
        } else if (ACTION_JITTER.equals(action)) {
            if (!isRun || !isFilterEnabled) return START_NOT_STICKY;
            if (isJitter) {
                stopJitterAndReplay();
            } else {
                if (isJitterV2) stopJitterV2AndReplay();
                if (isDelayPing) stopDelayPingAndReplay();
                if (isTeleSpeed) stopTeleSpeedAndReplay();
                _teleBuf.clear();
                clearTeleCatchupBuffer();
                _teleReplaying = false;
                _teleStartTime = System.currentTimeMillis();
                isJitter = true;
                logDevControl("MODE_TELE_ON", "mode=tele");
                scheduleLobbyRouteFallbackIfNoUdp("tele");
            }
        } else if (ACTION_JITTER_V2.equals(action)) {
            if (!isRun || !isFilterEnabled) return START_NOT_STICKY;
            if (isJitterV2) {
                stopJitterV2AndReplay();
            } else {
                if (isJitter) stopJitterAndReplay();
                if (isDelayPing) stopDelayPingAndReplay();
                if (isTeleSpeed) stopTeleSpeedAndReplay();
                clearTeleV2Buffer();
                _teleV2Replaying = false;
                _teleStartTime = System.currentTimeMillis();
                isJitterV2 = true;
                scheduleLobbyRouteFallbackIfNoUdp("tele_v2");
            }
        } else if (ACTION_DELAY_PING.equals(action)) {
            if (!isRun || !isFilterEnabled) return START_NOT_STICKY;
            if (isDelayPing) {
                stopDelayPingAndReplay();
            } else {
                if (isGhost) isGhost = false;
                if (isJitter) stopJitterAndReplay();
                if (isJitterV2) stopJitterV2AndReplay();
                if (isTeleSpeed) stopTeleSpeedAndReplay();
                startDelayPing();
                scheduleLobbyRouteFallbackIfNoUdp("delay_ping");
            }
        } else if (ACTION_TELE_SPEED.equals(action)) {
            if (!isRun || !isFilterEnabled) return START_NOT_STICKY;
            if (isTeleSpeed) {
                stopTeleSpeedAndReplay();
            } else {
                if (isGhost) isGhost = false;
                if (isJitter) stopJitterAndReplay();
                if (isJitterV2) stopJitterV2AndReplay();
                if (isDelayPing) stopDelayPingAndReplay();
                startTeleSpeed();
                scheduleLobbyRouteFallbackIfNoUdp("tele_speed");
            }
        } else if (ACTION_ACTIVATE.equals(action)) {
            isFilterEnabled = true;
            if (!isRun) {
                isActive = true;
            } else if (!isActive) {
                toggle(true);
            } else {
                isActive = true;
            }
        } else if (ACTION_DEACTIVATE.equals(action)) {
            disableFilters();
            lobbyRouteFallbackFull = false;
            if (isActive) toggle(false);
            return START_NOT_STICKY;
        }

        if (!isRun && (ACTION_SET_APP.equals(action) || ACTION_ACTIVATE.equals(action))) {
            isRun = true;
            vpnT = new Thread(this, "VPN");
            vpnT.start();
            startStats();
        }
        return START_STICKY;
    }

    @Override public android.os.IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        try {
            if (devReceiver != null) unregisterReceiver(devReceiver);
        } catch (Exception e) { }
        stop();
        super.onDestroy();
    }

    @Override
    public void run() {
        try {
            int mtu = 1280;
            TunAddress tunAddress = nextTunAddress();
            String ip = tunAddress.ip;
            String ipsix = "fd64:2534:1111::2";
            tunIP = tunAddress.ipInt;

            Builder builder = new Builder()
                .setSession("SunnyMod Tunnel")
                .addAddress(ip, 24)
                .addAddress(ipsix, 64)
                .setMtu(mtu)
                .setBlocking(true);

            if (pkg != null && !pkg.isEmpty()) {
                try { builder.addAllowedApplication(pkg); } catch (Exception e) { }
            }

            if (isActive) addActiveRoutes(builder);

            tun = builder.establish();
            if (tun == null) { stop(); return; }
            logDevControl("MODE_VPN_TUN_READY",
                    "active=" + isActive
                            + " route=" + activeRouteModeName()
                            + " pkg=" + pkg
                            + " tun_ip=" + ip);

            if (isActive) {
                try { loop(); } catch (Exception e) { e.printStackTrace(); }
            } else {
                while (isRun && !isActive) {
                    try { Thread.sleep(200); } catch (InterruptedException e) { break; }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isRun) stop();
        }
    }

    private void toggle(final boolean on) {
        if (on == isActive) return;
        restartTunnel(on);
    }

    private void restartTunnel(final boolean nextActive) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                isRun = false;
                try { if (tun != null) tun.close(); } catch (Exception e) { }
                tun = null;
                try { if (vpnT != null) vpnT.join(1000); } catch (Exception e) { }

                for (SE se : udpMap.values()) {
                    try { se.s.close(); } catch (Exception e) { }
                }
                udpMap.clear();
                isActive = nextActive;
                isRun = true;
                vpnT = new Thread(PacketVpnService.this, nextActive ? "VPN-A" : "VPN-S");
                vpnT.start();
            }
        });
    }

    private void addActiveRoutes(Builder builder) {
        if (lobbyRouteEnabled && !lobbyRouteFallbackFull) {
            builder.addRoute("148.222.80.0", 23);
            builder.addRoute("148.222.83.0", 24);
            builder.addRoute("148.222.84.0", 22);
            builder.addRoute("148.222.88.0", 21);
            builder.addRoute("103.78.86.128", 25);
        } else {
            builder.addRoute("0.0.0.0", 0);
        }
    }

    private String activeRouteModeName() {
        if (!lobbyRouteEnabled) return "full_ipv4";
        if (lobbyRouteFallbackFull) return "lobby_auto_fallback_full_ipv4";
        return "lobby_match_wide_148_222_80_23_83_24_84_22_88_21_103_78_86_128_25_icmp_reply";
    }

    private void scheduleLobbyRouteFallbackIfNoUdp(final String trigger) {
        if (!lobbyRouteEnabled || lobbyRouteFallbackFull || !isRun || !isActive || !isFilterEnabled) return;
        final long startUdp = outUdpSeen.get();
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try { Thread.sleep(1200L); } catch (InterruptedException e) { return; }
                if (!lobbyRouteEnabled || lobbyRouteFallbackFull || !isRun || !isActive || !isFilterEnabled) return;
                if (!hasAnyFilterActive()) return;
                if (outUdpSeen.get() != startUdp) return;
                lobbyRouteFallbackFull = true;
                logDevControl("MODE_VPN_ROUTE_NO_UDP",
                        "trigger=" + trigger
                                + " udp_seen=" + startUdp
                                + " route=" + activeRouteModeName()
                                + " full_fallback=true");
                restartTunnel(isActive);
            }
        });
    }

    private boolean hasAnyFilterActive() {
        return isFreeze || isGhost || isJitter || isJitterV2 || isDelayPing || isTeleSpeed;
    }

    private void loop() throws Exception {
        FileInputStream  in  = new FileInputStream(tun.getFileDescriptor());
        FileOutputStream out = new FileOutputStream(tun.getFileDescriptor());
        byte[] buf = new byte[0xffff];

        while (isRun && isActive) {
            int len = in.read(buf);
            if (len < 20) continue;
            bIn.addAndGet(len);

            int version = (buf[0] >> 4) & 0xf;
            if (version != 4) {
                logProtocolDrop("DROP_NON_IPV4", buf, 0, len, 0, 0, 0,
                        "version=" + version + " ip_len=" + len + " route=" + activeRouteModeName());
                continue;
            }
            int proto = buf[9] & 0xff;
            int ihl   = (buf[0] & 0xf) * 4;

            if (len < ihl + 8) continue;

            int dip  = ri(buf, 16);
            int sp   = rs(buf, ihl);
            int dp   = rs(buf, ihl + 2);

            if (proto == 1 && ICMP_ECHO_REPLY_ENABLED
                    && replyIcmpEchoRequest(buf, len, ihl, dip, sp, dp, out)) {
                continue;
            }

            if (proto != 17) {
                logProtocolDrop("DROP_NON_UDP", buf, ihl, len - ihl, sp, dp, dip,
                        "proto=" + protoName(proto)
                                + " ip_len=" + len
                                + " route=" + activeRouteModeName());
                continue;
            }

            int plen = rs(buf, ihl + 4) - 8;
            int off  = ihl + 8;

            if (plen < 0 || off + plen > len) continue;
            outUdpSeen.incrementAndGet();

            int telePacketType = classifyTelePacket(buf, off, plen);
            if (telePacketType == TELE_PACKET_NONE
                    && isFilterEnabled
                    && isTeleFireCaptureActive()
                    && (isTeleFireAuxPayload(buf, off, plen) || isBroadTeleFirePayload(plen))) {
                telePacketType = TELE_PACKET_FIRE;
            }

            if (isFilterEnabled && isGhost && telePacketType == TELE_PACKET_MOVE) {
                logDevPacket("BLOCK_GHOST", "OUT", buf, off, plen, sp, dp, dip, "ghost=true");
                pDrop.incrementAndGet();
                continue;
            }

            if (isFilterEnabled && isJitter && telePacketType != TELE_PACKET_NONE) {
                if (_teleBuf.size() < TELE_BUF_MAX) {
                    byte[] copy = new byte[plen];
                    System.arraycopy(buf, off, copy, 0, plen);
                    _teleBuf.offer(new _TP(copy, dip, dp, sp, telePacketType));
                    logDevPacket("DELAY_TELE_" + teleTypeName(telePacketType), "OUT",
                            buf, off, plen, sp, dp, dip, teleNote(telePacketType, _teleBuf.size()));
                } else {
                    logDevPacket("DROP_TELE_BUFFER_FULL_" + teleTypeName(telePacketType), "OUT",
                            buf, off, plen, sp, dp, dip, teleNote(telePacketType, _teleBuf.size()));
                }
                continue;
            }

            if (isFilterEnabled && isJitterV2 && telePacketType == TELE_PACKET_MOVE) {
                int queueSize = offerTeleV2(new _TP(copyPayload(buf, off, plen), dip, dp, sp, telePacketType));
                logDevPacket("DELAY_TELE_V2_" + teleTypeName(telePacketType), "OUT",
                        buf, off, plen, sp, dp, dip, teleNote(telePacketType, queueSize));
                continue;
            }

            if (isFilterEnabled && isDelayPing && telePacketType == TELE_PACKET_MOVE) {
                int queueSize = offerDelayPing(new _TP(copyPayload(buf, off, plen), dip, dp, sp, telePacketType));
                logDevPacket("DELAY_PING_HOLD_" + teleTypeName(telePacketType), "OUT",
                        buf, off, plen, sp, dp, dip, teleNote(telePacketType, queueSize));
                continue;
            }

            if (isFilterEnabled && isTeleSpeed && telePacketType == TELE_PACKET_MOVE) {
                int queueSize = offerTeleSpeed(new _TP(copyPayload(buf, off, plen), dip, dp, sp, telePacketType));
                logDevPacket("DELAY_TELE_SPEED_" + teleTypeName(telePacketType), "OUT",
                        buf, off, plen, sp, dp, dip, teleNote(telePacketType, queueSize));
                continue;
            }

            if (isFilterEnabled && _teleReplaying && telePacketType != TELE_PACKET_NONE) {
                if (telePacketType == TELE_PACKET_MOVE) {
                    int queueSize = offerTeleCatchup(new _TP(copyPayload(buf, off, plen), dip, dp, sp, telePacketType));
                    logDevPacket("CATCHUP_TELE_" + teleTypeName(telePacketType), "OUT",
                            buf, off, plen, sp, dp, dip, teleNote(telePacketType, queueSize));
                } else {
                    logDevPacket("DROP_LIVE_DURING_REPLAY_TELE_" + teleTypeName(telePacketType), "OUT",
                            buf, off, plen, sp, dp, dip,
                            "type=" + teleTypeName(telePacketType) + " replay=tele");
                }
                continue;
            }

            if (isFilterEnabled && _teleSpeedReplaying && telePacketType == TELE_PACKET_MOVE) {
                int queueSize = offerTeleSpeedCatchup(new _TP(copyPayload(buf, off, plen), dip, dp, sp, telePacketType));
                logDevPacket("CATCHUP_TELE_SPEED_" + teleTypeName(telePacketType), "OUT",
                        buf, off, plen, sp, dp, dip, teleNote(telePacketType, queueSize));
                continue;
            }

            if (isFilterEnabled && (_teleV2Replaying || _delayPingReplaying)
                    && telePacketType == TELE_PACKET_MOVE) {
                String replayMode = _delayPingReplaying ? "delay_ping" : "v2";
                logDevPacket("DROP_LIVE_DURING_REPLAY_" + replayMode + "_" + teleTypeName(telePacketType), "OUT",
                        buf, off, plen, sp, dp, dip,
                        "type=" + teleTypeName(telePacketType) + " replay=" + replayMode);
                continue;
            }

            if (isFilterEnabled && telePacketType == TELE_PACKET_NONE
                    && isTeleUnknownCaptureActive() && shouldLogUnknownActive(plen, dp)) {
                logDevPacket("PASS_TELE_UNKNOWN_ACTIVE", "OUT",
                        buf, off, plen, sp, dp, dip,
                        "mode=" + activeTeleLogMode() + " live=true");
            }

            long key = ((long)(sp & 0xffff) << 48) | ((long)(dp & 0xffff) << 32) | (dip & 0xFFFFFFFFL);
            SE se = getOrCreate(key, sp, dip, dp, out);
            if (se == null) continue;

            try {
                if (telePacketType != TELE_PACKET_NONE) {
                    logDevPacket("PASS_TELE_" + teleTypeName(telePacketType), "OUT",
                            buf, off, plen, sp, dp, dip, "live=true");
                }
                DatagramPacket pkt = new DatagramPacket(buf, off, plen, InetAddress.getByAddress(tb(dip)), dp);
                se.s.send(pkt);
                se.touch();
                pSent.incrementAndGet();
                bOut.addAndGet(plen);
            } catch (Exception e) { }
        }
    }

    private SE getOrCreate(long key, int sp, int dip, int dp, FileOutputStream out) {
        SE existing = udpMap.get(key);
        if (existing != null) return existing;
        try {
            DatagramSocket sock = new DatagramSocket();
            protect(sock);
            try { sock.setSoTimeout(SO_TO); } catch (Exception e) { }
            SE se = new SE(sock);
            SE prev = udpMap.putIfAbsent(key, se);
            if (prev != null) { sock.close(); return prev; }
            threadPool.execute(new RecvRunnable(se, sp, dip, dp, out, key));
            return se;
        } catch (Exception e) {
            return null;
        }
    }

    private class RecvRunnable implements Runnable {
        private final SE fe;
        private final int sip, dp, sp;
        private final FileOutputStream out;
        private final long key;

        RecvRunnable(SE fe, int origSip, int origDip, int origDp, FileOutputStream out, long key) {
            this.fe  = fe;
            this.sip = origDip; 
            this.dp  = origDp;  
            this.sp  = origSip; 
            this.out = out;
            this.key = key;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            byte[] rbuf = new byte[0x8000];
            while (isRun && isActive) {
                try {
                    DatagramPacket pkt = new DatagramPacket(rbuf, rbuf.length);
                    long t0 = System.nanoTime();
                    fe.s.receive(pkt);
                    rtt = (rtt * 3 + (System.nanoTime() - t0) / 1000000L) / 4;
                    int rlen = pkt.getLength();
                    bIn.addAndGet(rlen);
                    fe.touch();

                    if (isFilterEnabled && isFreeze && rlen >= DROP_MIN && rlen <= DROP_MAX) {
                        logDevPacket("BLOCK_FREEZE", "IN", pkt.getData(), pkt.getOffset(), rlen, dp, sp, sip, "freeze=true");
                        pDrop.incrementAndGet();
                        continue;
                    }

                    byte[] ip = buildPkt(pkt, sip, dp, sp);
                    synchronized (out) {
                        out.write(ip);
                    }
                } catch (java.net.SocketTimeoutException e) {
                    if (!isRun || !isActive) break;
                    if (System.currentTimeMillis() - fe.t > IDLE) break;
                } catch (Exception e) {
                    break;
                }
            }
            udpMap.remove(key);
            try { fe.s.close(); } catch (Exception e) { }
        }
    }

    private byte[] buildPkt(DatagramPacket pkt, int sip, int dp, int sp) {
        int plen  = pkt.getLength();
        int total = plen + 0x1c;
        byte[] buf = new byte[total];
        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.put((byte) 0x45);
        bb.put((byte) 0x00);
        bb.putShort((short) total);
        bb.putShort((short) rng.nextInt(0xffff));
        bb.putShort((short) 0x4000);
        bb.put((byte) 0x40);
        bb.put((byte) 0x11);
        bb.putShort((short) 0);
        bb.putInt(sip);
        bb.putInt(tunIP);
        short ck = cksum(buf, 0x14);
        buf[10] = (byte)(ck >> 8);
        buf[11] = (byte)(ck & 0xff);
        bb.putShort((short) dp);
        bb.putShort((short) sp);
        bb.putShort((short)(plen + 8));
        bb.putShort((short) 0);
        System.arraycopy(pkt.getData(), pkt.getOffset(), buf, 0x1c, plen);
        return buf;
    }

    private boolean replyIcmpEchoRequest(byte[] packet, int len, int ihl, int dip,
                                         int sourcePort, int destPort, FileOutputStream out) {
        int totalLen = rs(packet, 2);
        if (totalLen <= 0 || totalLen > len) totalLen = len;
        int icmpOff = ihl;
        int icmpLen = totalLen - icmpOff;
        if (icmpLen < 8 || ub(packet, icmpOff) != 8 || ub(packet, icmpOff + 1) != 0) {
            return false;
        }

        byte[] reply = new byte[totalLen];
        System.arraycopy(packet, 0, reply, 0, totalLen);

        for (int i = 0; i < 4; i++) {
            byte src = reply[12 + i];
            reply[12 + i] = reply[16 + i];
            reply[16 + i] = src;
        }

        reply[8] = 64;
        reply[10] = 0;
        reply[11] = 0;
        short ipChecksum = cksum(reply, 0, ihl);
        reply[10] = (byte)(ipChecksum >> 8);
        reply[11] = (byte)(ipChecksum & 0xff);

        reply[icmpOff] = 0;
        reply[icmpOff + 1] = 0;
        reply[icmpOff + 2] = 0;
        reply[icmpOff + 3] = 0;
        short icmpChecksum = cksum(reply, icmpOff, icmpLen);
        reply[icmpOff + 2] = (byte)(icmpChecksum >> 8);
        reply[icmpOff + 3] = (byte)(icmpChecksum & 0xff);

        try {
            synchronized (out) {
                out.write(reply);
            }
            logDevPacket("REPLY_ICMP_ECHO", "IN", packet, icmpOff, icmpLen,
                    sourcePort, destPort, dip,
                    "icmp=echo route=" + activeRouteModeName());
            return true;
        } catch (Exception e) {
            logProtocolDrop("DROP_ICMP_ECHO_REPLY_FAIL", packet, icmpOff, icmpLen,
                    sourcePort, destPort, dip,
                    "icmp=echo_reply_error route=" + activeRouteModeName());
            return true;
        }
    }

    private static short cksum(byte[] buf, int len) {
        return cksum(buf, 0, len);
    }

    private static short cksum(byte[] buf, int off, int len) {
        int sum = 0;
        int end = off + len;
        int i = off;
        while (i + 1 < end) {
            sum += ((buf[i] & 0xff) << 8) | (buf[i + 1] & 0xff);
            i += 2;
        }
        if (i < end) {
            sum += (buf[i] & 0xff) << 8;
        }
        while ((sum >> 16) != 0)
            sum = (sum & 0xffff) + (sum >> 16);
        return (short)(~sum);
    }

    private static int ri(byte[] b, int o) {
        return ((b[o]&0xff)<<24)|((b[o+1]&0xff)<<16)|((b[o+2]&0xff)<<8)|(b[o+3]&0xff);
    }
    private static int rs(byte[] b, int o) {
        return ((b[o]&0xff)<<8)|(b[o+1]&0xff);
    }
    private static byte[] tb(int ip) {
        return new byte[]{(byte)(ip>>24),(byte)(ip>>16),(byte)(ip>>8),(byte)ip};
    }

    private static byte[] copyPayload(byte[] src, int off, int len) {
        byte[] copy = new byte[len];
        System.arraycopy(src, off, copy, 0, len);
        return copy;
    }

    private static int classifyTelePacket(byte[] data, int off, int payloadLen) {
        if (isTeleMovePayload(payloadLen)) {
            return TELE_PACKET_MOVE;
        }
        if (isTeleFirePayload(data, off, payloadLen)) {
            return TELE_PACKET_FIRE;
        }
        return TELE_PACKET_NONE;
    }

    private static boolean isTeleMovePayload(int payloadLen) {
        return payloadLen >= TELE_MOVE_MIN && payloadLen <= TELE_MOVE_MAX;
    }

    private static boolean isBroadTeleFirePayload(int payloadLen) {
        return !isTeleMovePayload(payloadLen)
                && payloadLen >= TELE_FIRE_MIN
                && payloadLen <= TELE_BROAD_FIRE_MAX;
    }

    private static boolean isTeleFirePayload(byte[] data, int off, int payloadLen) {
        if (isMaskedTeleFirePayload(data, off, payloadLen)) return true;
        if (isCurrentTeleFirePayload(data, off, payloadLen)) return true;
        return isLegacyTeleFirePayload(data, off, payloadLen);
    }

    private static boolean isLegacyTeleFirePayload(byte[] data, int off, int payloadLen) {
        if (payloadLen != 26 && payloadLen != 34) return false;
        if (!hasBytes(data, off, payloadLen, 4, 0xBB, 0xBA)) return false;
        int b2 = ub(data, off + 2);
        int b3 = ub(data, off + 3);
        return b2 == 0xB9 && (b3 == 0xD2 || b3 == 0xD7);
    }

    private static boolean isCurrentTeleFirePayload(byte[] data, int off, int payloadLen) {
        if (off < 0 || off + payloadLen > data.length || payloadLen < 10) return false;
        if (payloadLen == 34) {
            if (!hasBytes(data, off, payloadLen, 2, 0x81, 0x98, 0x80)) return false;
            if (ub(data, off + 5) != 0x82 || ub(data, off + 7) != 0x80 || ub(data, off + 9) != 0x80) {
                return false;
            }
            int marker = ub(data, off + 8);
            return marker == 0xE8 || marker == 0xE9 || marker == 0x06;
        }
        if (payloadLen == 26) {
            return hasBytes(data, off, payloadLen, 2, 0x81, 0x90, 0x80)
                    && ub(data, off + 5) == 0x82
                    && ub(data, off + 7) == 0x80
                    && ub(data, off + 8) == 0xEC
                    && ub(data, off + 9) == 0x80;
        }
        return false;
    }

    private static boolean isMaskedTeleFirePayload(byte[] data, int off, int payloadLen) {
        if (off < 0 || off + payloadLen > data.length || payloadLen < 10) return false;
        if (payloadLen == 34) {
            return ub(data, off + 2) == 0x58
                    && ub(data, off + 4) == 0x5A
                    && ub(data, off + 6) == 0x58
                    && ub(data, off + 7) == 0x5B
                    && ub(data, off + 8) == 0x42
                    && ub(data, off + 9) == 0x5A;
        }
        if (payloadLen == 26) {
            return ub(data, off + 2) == 0x58
                    && ub(data, off + 4) == 0x5A
                    && ub(data, off + 6) == 0x58
                    && ub(data, off + 7) == 0x5B
                    && ub(data, off + 8) == 0x4A
                    && ub(data, off + 9) == 0x5A;
        }
        return false;
    }

    private static boolean isTeleFireAuxPayload(byte[] data, int off, int payloadLen) {
        return isMaskedTeleFireAuxPayload(data, off, payloadLen)
                || isCurrentTeleFireAuxPayload(data, off, payloadLen)
                || isLegacyTeleFireAuxPayload(data, off, payloadLen);
    }

    private static boolean isCurrentTeleFireAuxPayload(byte[] data, int off, int payloadLen) {
        return payloadLen == 24
                && hasBytes(data, off, payloadLen, 2, 0x81, 0x90, 0x80)
                && ub(data, off + 5) == 0x80
                && ub(data, off + 6) == 0x82
                && ub(data, off + 7) == 0x80;
    }

    private static boolean isLegacyTeleFireAuxPayload(byte[] data, int off, int payloadLen) {
        return payloadLen == 24
                && hasBytes(data, off, payloadLen, 2, 0xBB, 0xB9, 0xBB)
                && hasBytes(data, off, payloadLen, 5, 0xBA, 0xAB, 0xBB);
    }

    private static boolean isMaskedTeleFireAuxPayload(byte[] data, int off, int payloadLen) {
        return off >= 0
                && off + payloadLen <= data.length
                && payloadLen == 24
                && hasBytes(data, off, payloadLen, 2, 0x5A, 0x58, 0x5A)
                && ub(data, off + 5) == 0x5B
                && ub(data, off + 6) == 0x4A
                && ub(data, off + 7) == 0x5A;
    }

    private static boolean hasBytes(byte[] data, int off, int len, int rel, int b0, int b1) {
        return off >= 0
                && len >= rel + 2
                && off + rel + 1 < data.length
                && ub(data, off + rel) == b0
                && ub(data, off + rel + 1) == b1;
    }

    private static boolean hasBytes(byte[] data, int off, int len, int rel, int b0, int b1, int b2) {
        return off >= 0
                && len >= rel + 3
                && off + rel + 2 < data.length
                && ub(data, off + rel) == b0
                && ub(data, off + rel + 1) == b1
                && ub(data, off + rel + 2) == b2;
    }

    private static int ub(byte[] data, int index) {
        return data[index] & 0xff;
    }

    private static String teleTypeName(int type) {
        if (type == TELE_PACKET_MOVE) return "MOVE";
        return "FIRE";
    }

    private static String teleNote(int type, int queueSize) {
        return "type=" + teleTypeName(type) + " queue=" + queueSize;
    }

    private boolean isTeleUnknownCaptureActive() {
        return isJitter || isTeleSpeed || _teleReplaying || _teleSpeedReplaying;
    }

    private boolean isTeleFireCaptureActive() {
        return isJitter || _teleReplaying;
    }

    private String activeTeleLogMode() {
        if (isTeleSpeed || _teleSpeedReplaying) return "tele_speed";
        return "tele";
    }

    private boolean shouldLogUnknownActive(int payloadLen, int destPort) {
        if (!devMode) return false;
        if (destPort < 10000) return false;
        if (payloadLen < TELE_FIRE_MIN || payloadLen > TELE_FIRE_MAX) return false;
        synchronized (devLock) {
            if (devUnknownActiveEvents >= DEV_UNKNOWN_ACTIVE_MAX) return false;
            devUnknownActiveEvents++;
            return true;
        }
    }

    private void _startTeleReplay() {
        if (_teleBuf.isEmpty()) return;
        _teleReplaying = true;
        final List<_TP> all = new ArrayList<>();
        _TP tp;
        while ((tp = _teleBuf.poll()) != null) all.add(tp);
        threadPool.execute(new TeleReplayRunnable(all, false));
    }

    private void startTeleV2Replay() {
        final List<_TP> all;
        synchronized (_teleV2Lock) {
            if (_teleV2Buf.isEmpty()) return;
            all = drainTeleV2BufferLocked();
        }
        _teleV2Replaying = true;
        threadPool.execute(new TeleReplayRunnable(all, true));
    }

    private void replayTeleV2Blocking() {
        final List<_TP> all;
        synchronized (_teleV2Lock) {
            if (_teleV2Buf.isEmpty()) return;
            all = drainTeleV2BufferLocked();
        }
        _teleV2Replaying = true;
        try {
            replayTelePackets(all, true);
        } finally {
            _teleV2Replaying = false;
            clearTeleV2Buffer();
        }
    }

    private List<_TP> drainTeleV2BufferLocked() {
        List<_TP> all = new ArrayList<_TP>(_teleV2Buf);
        _teleV2Buf.clear();
        _teleV2MoveCount = 0;
        return all;
    }

    private void stopJitterAndReplay() {
        isJitter = false;
        if (_teleBuf.isEmpty()) {
            _teleReplaying = false;
            clearTeleCatchupBuffer();
            logDevControl("MODE_TELE_OFF_EMPTY", "mode=tele queue=0");
            return;
        }
        logDevControl("MODE_TELE_OFF_REPLAY", "mode=tele queue=" + _teleBuf.size());
        _teleReplaying = true;
        _startTeleReplay();
    }

    private void stopJitterV2AndReplay() {
        isJitterV2 = false;
        if (isTeleV2BufferEmpty()) {
            _teleV2Replaying = false;
            return;
        }
        _teleV2Replaying = true;
        startTeleV2Replay();
    }

    private void disableFilters() {
        isFilterEnabled = false;
        isFreeze = false;
        isGhost = false;
        isJitter = false;
        isJitterV2 = false;
        isDelayPing = false;
        isTeleSpeed = false;
        lobbyRouteFallbackFull = false;
        _teleReplaying = false;
        _teleV2Replaying = false;
        _delayPingReplaying = false;
        _teleSpeedReplaying = false;
        _teleBuf.clear();
        clearTeleCatchupBuffer();
        clearTeleV2Buffer();
        clearTeleSpeedBuffers();
        synchronized (_delayPingLock) {
            _delayPingGeneration++;
            _delayPingBuf.clear();
            _delayPingReplayQueue.clear();
            _delayPingLoopRunning = false;
        }
    }

    private int offerTeleCatchup(_TP tp) {
        synchronized (_teleCatchupLock) {
            while (_teleCatchupBuf.size() >= TELE_BUF_MAX) {
                _teleCatchupBuf.remove(0);
            }
            _teleCatchupBuf.add(tp);
            return _teleCatchupBuf.size();
        }
    }

    private List<_TP> drainTeleCatchupBuffer() {
        synchronized (_teleCatchupLock) {
            if (_teleCatchupBuf.isEmpty()) return new ArrayList<_TP>();
            List<_TP> all = new ArrayList<_TP>(_teleCatchupBuf);
            _teleCatchupBuf.clear();
            return all;
        }
    }

    private void clearTeleCatchupBuffer() {
        synchronized (_teleCatchupLock) {
            _teleCatchupBuf.clear();
        }
    }

    private void startTeleSpeed() {
        synchronized (_teleSpeedLock) {
            _teleSpeedBuf.clear();
            _teleSpeedCatchupBuf.clear();
            _teleSpeedReplaying = false;
        }
        _teleStartTime = System.currentTimeMillis();
        isTeleSpeed = true;
        logDevControl("MODE_TELE_SPEED_ON",
                "mode=tele_speed burst=" + teleSpeedBurstPackets
                        + " sleep_ms=" + teleSpeedSleepMs
                        + " catchup_tail=" + teleSpeedCatchupTail
                        + " hold_fire=false");
    }

    private int offerTeleSpeed(_TP tp) {
        synchronized (_teleSpeedLock) {
            if (tp == null || tp.type != TELE_PACKET_MOVE) {
                return _teleSpeedBuf.size();
            }
            while (_teleSpeedBuf.size() >= TELE_BUF_MAX) {
                _teleSpeedBuf.remove(0);
            }
            _teleSpeedBuf.add(tp);
            return _teleSpeedBuf.size();
        }
    }

    private int offerTeleSpeedCatchup(_TP tp) {
        synchronized (_teleSpeedLock) {
            if (tp == null || tp.type != TELE_PACKET_MOVE) {
                return _teleSpeedCatchupBuf.size();
            }
            if (teleSpeedCatchupTail) {
                while (_teleSpeedCatchupBuf.size() >= TELE_SPEED_CATCHUP_TAIL_MAX) {
                    _teleSpeedCatchupBuf.remove(0);
                }
            } else {
                while (_teleSpeedCatchupBuf.size() >= TELE_BUF_MAX) {
                    _teleSpeedCatchupBuf.remove(0);
                }
            }
            _teleSpeedCatchupBuf.add(tp);
            return _teleSpeedCatchupBuf.size();
        }
    }

    private List<_TP> drainTeleSpeedBuffer() {
        synchronized (_teleSpeedLock) {
            if (_teleSpeedBuf.isEmpty()) return new ArrayList<_TP>();
            List<_TP> all = new ArrayList<_TP>(_teleSpeedBuf);
            _teleSpeedBuf.clear();
            return all;
        }
    }

    private List<_TP> drainTeleSpeedCatchupBuffer() {
        synchronized (_teleSpeedLock) {
            if (_teleSpeedCatchupBuf.isEmpty()) return new ArrayList<_TP>();
            List<_TP> all = new ArrayList<_TP>(_teleSpeedCatchupBuf);
            _teleSpeedCatchupBuf.clear();
            return all;
        }
    }

    private void clearTeleSpeedBuffers() {
        synchronized (_teleSpeedLock) {
            _teleSpeedBuf.clear();
            _teleSpeedCatchupBuf.clear();
        }
    }

    private void stopTeleSpeedAndReplay() {
        isTeleSpeed = false;
        final List<_TP> all = drainTeleSpeedBuffer();
        if (all.isEmpty()) {
            _teleSpeedReplaying = false;
            clearTeleSpeedBuffers();
            logDevControl("MODE_TELE_SPEED_OFF_EMPTY", "mode=tele_speed queue=0");
            return;
        }
        logDevControl("MODE_TELE_SPEED_OFF_REPLAY",
                "mode=tele_speed queue=" + all.size()
                        + " burst=" + teleSpeedBurstPackets
                        + " sleep_ms=" + teleSpeedSleepMs
                        + " catchup_tail=" + teleSpeedCatchupTail
                        + " hold_fire=false");
        _teleSpeedReplaying = true;
        threadPool.execute(new TeleSpeedReplayRunnable(all));
    }

    private void startDelayPing() {
        synchronized (_delayPingLock) {
            _delayPingBuf.clear();
            _delayPingReplayQueue.clear();
            _delayPingReplaying = false;
            _delayPingGeneration++;
        }
        _teleStartTime = System.currentTimeMillis();
        isDelayPing = true;
        startDelayPingLoop();
    }

    private void startDelayPingLoop() {
        final int generation;
        synchronized (_delayPingLock) {
            if (_delayPingLoopRunning) return;
            _delayPingLoopRunning = true;
            generation = _delayPingGeneration;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRun && isFilterEnabled && isDelayPing) {
                        synchronized (_delayPingLock) {
                            if (generation != _delayPingGeneration) break;
                        }
                        try { Thread.sleep(DELAY_PING_WINDOW_MS); } catch (InterruptedException e) { break; }
                        synchronized (_delayPingLock) {
                            if (generation != _delayPingGeneration) break;
                        }
                        flushDelayPingWindow();
                    }
                } finally {
                    synchronized (_delayPingLock) {
                        if (generation == _delayPingGeneration) {
                            _delayPingLoopRunning = false;
                        }
                    }
                }
            }
        });
    }

    private int offerDelayPing(_TP tp) {
        synchronized (_delayPingLock) {
            while (_delayPingBuf.size() >= TELE_BUF_MAX) {
                _delayPingBuf.remove(0);
            }
            _delayPingBuf.add(tp);
            return _delayPingBuf.size();
        }
    }

    private void flushDelayPingWindow() {
        List<_TP> all = null;
        synchronized (_delayPingLock) {
            if (!_delayPingBuf.isEmpty()) {
                all = new ArrayList<_TP>(_delayPingBuf);
                _delayPingBuf.clear();
            }
        }
        if (all != null && !all.isEmpty()) {
            _delayPingReplayQueue.offer(all);
            ensureDelayPingReplay();
        }
    }

    private void ensureDelayPingReplay() {
        synchronized (_delayPingLock) {
            if (_delayPingReplaying || _delayPingReplayQueue.isEmpty()) return;
            _delayPingReplaying = true;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    List<_TP> all = _delayPingReplayQueue.poll();
                    if (all == null) {
                        synchronized (_delayPingLock) {
                            if (_delayPingReplayQueue.isEmpty()) {
                                _delayPingReplaying = false;
                                return;
                            }
                        }
                        continue;
                    }
                    replayDelayPingPackets(all);
                }
            }
        });
    }

    private void stopDelayPingAndReplay() {
        isDelayPing = false;
        synchronized (_delayPingLock) {
            _delayPingGeneration++;
            _delayPingLoopRunning = false;
        }
        flushDelayPingWindow();
        ensureDelayPingReplay();
    }

    private int offerTeleV2(_TP tp) {
        synchronized (_teleV2Lock) {
            while (_teleV2Buf.size() >= TELE_V2_BUF_MAX) {
                removeOldestTeleV2Locked();
            }
            while (_teleV2MoveCount >= TELE_V2_MOVE_TAIL_MAX) {
                if (!removeOldestTeleV2MoveLocked()) break;
            }
            _teleV2Buf.add(tp);
            _teleV2MoveCount++;
            return _teleV2Buf.size();
        }
    }

    private void removeOldestTeleV2Locked() {
        if (_teleV2Buf.isEmpty()) return;
        _TP removed = _teleV2Buf.remove(0);
        if (removed.type == TELE_PACKET_MOVE && _teleV2MoveCount > 0) {
            _teleV2MoveCount--;
        }
    }

    private boolean removeOldestTeleV2MoveLocked() {
        for (int i = 0; i < _teleV2Buf.size(); i++) {
            if (_teleV2Buf.get(i).type == TELE_PACKET_MOVE) {
                _teleV2Buf.remove(i);
                _teleV2MoveCount--;
                return true;
            }
        }
        _teleV2MoveCount = 0;
        return false;
    }

    private boolean isTeleV2BufferEmpty() {
        synchronized (_teleV2Lock) {
            return _teleV2Buf.isEmpty();
        }
    }

    private void clearTeleV2Buffer() {
        synchronized (_teleV2Lock) {
            _teleV2Buf.clear();
            _teleV2MoveCount = 0;
        }
    }

    private class TeleReplayRunnable implements Runnable {
        private final List<_TP> all;
        private final boolean v2;
        TeleReplayRunnable(List<_TP> all, boolean v2) {
            this.all = all;
            this.v2 = v2;
        }
        @Override
        public void run() {
            try {
                replayTelePackets(all, v2);
                if (!v2) {
                    while (true) {
                        List<_TP> catchup = drainTeleCatchupBuffer();
                        if (catchup.isEmpty()) break;
                        replayPacketList(catchup, "REPLAY_TELE_CATCHUP_", "REPLAY_CATCHUP_SEND_FAIL_", teleReplayDelayMs);
                    }
                }
            } finally {
                if (v2) {
                    _teleV2Replaying = false;
                    clearTeleV2Buffer();
                } else {
                    _teleReplaying = false;
                    _teleBuf.clear();
                    clearTeleCatchupBuffer();
                }
            }
        }
    }

    private class TeleSpeedReplayRunnable implements Runnable {
        private final List<_TP> all;
        TeleSpeedReplayRunnable(List<_TP> all) {
            this.all = all;
        }
        @Override
        public void run() {
            try {
                replayTeleSpeedPackets(all);
                while (true) {
                    List<_TP> catchup = drainTeleSpeedCatchupBuffer();
                    if (catchup.isEmpty()) break;
                    replayTeleSpeedCatchupPackets(catchup);
                }
            } finally {
                _teleSpeedReplaying = false;
                clearTeleSpeedBuffers();
            }
        }
    }

    private void replayTelePackets(List<_TP> all, boolean v2) {
        int replayDelay = v2
                ? Math.min(teleReplayDelayMs, TELE_V2_REPLAY_DELAY_MAX_MS)
                : teleReplayDelayMs;
        replayPacketList(all, v2 ? "REPLAY_TELE_V2_" : "REPLAY_TELE_",
                v2 ? "REPLAY_V2_SEND_FAIL_" : "REPLAY_SEND_FAIL_", replayDelay);
    }

    private void replayDelayPingPackets(List<_TP> all) {
        replayPacketList(all, "REPLAY_DELAY_PING_", "REPLAY_DELAY_PING_SEND_FAIL_", teleReplayDelayMs);
    }

    private void replayTeleSpeedPackets(List<_TP> all) {
        logDevControl("MODE_TELE_SPEED_REPLAY_ORIGINAL",
                "mode=tele_speed queue=" + all.size()
                        + " burst=" + teleSpeedBurstPackets
                        + " sleep_ms=" + teleSpeedSleepMs
                        + " order=original");
        replayPacketList(all, "REPLAY_TELE_SPEED_", "REPLAY_TELE_SPEED_SEND_FAIL_",
                teleSpeedSleepMs, teleSpeedBurstPackets, teleSpeedSleepMs);
    }

    private void replayTeleSpeedCatchupPackets(List<_TP> all) {
        replayPacketList(all, "REPLAY_TELE_SPEED_CATCHUP_", "REPLAY_TELE_SPEED_CATCHUP_SEND_FAIL_",
                teleSpeedSleepMs, teleSpeedBurstPackets, teleSpeedSleepMs);
    }

    private void replayPacketList(List<_TP> all, String replayPrefix, String failPrefix, int replayDelay) {
        replayPacketList(all, replayPrefix, failPrefix, replayDelay, 1, replayDelay);
    }

    private void replayPacketList(List<_TP> all, String replayPrefix, String failPrefix,
                                  int replayDelay, int packetsPerBurst, int burstSleepMs) {
        int safeBurst = packetsPerBurst < 1 ? 1 : packetsPerBurst;
        int safeSleep = burstSleepMs < 0 ? 0 : burstSleepMs;
        for (int index = 0; index < all.size(); index++) {
            _TP tp = all.get(index);
            if (Thread.currentThread().isInterrupted() || !isRun || !isActive || !isFilterEnabled) {
                logDevPacket(replayPrefix + "ABORT_" + teleTypeName(tp.type), "OUT",
                        tp.data, 0, tp.data.length, tp.sp, tp.dp, tp.dip,
                        "type=" + teleTypeName(tp.type) + " replay_aborted");
                break;
            }
            long key = ((long)(tp.sp&0xffff)<<48) | ((long)(tp.dp&0xffff)<<32) | (tp.dip & 0xFFFFFFFFL);
            SE se = udpMap.get(key);
            boolean sent = false;
            if (se != null) {
                try {
                    DatagramPacket p = new DatagramPacket(tp.data, 0, tp.data.length, InetAddress.getByAddress(tb(tp.dip)), tp.dp);
                    se.s.send(p);
                    se.touch();
                    sent = true;
                    logDevPacket(replayPrefix + teleTypeName(tp.type), "OUT",
                            tp.data, 0, tp.data.length, tp.sp, tp.dp, tp.dip,
                            "type=" + teleTypeName(tp.type)
                                    + " delay_ms=" + replayDelay
                                    + " burst=" + safeBurst
                                    + " sleep_ms=" + safeSleep);
                } catch (Exception e) { }
            }
            if (!sent) {
                DatagramSocket fallback = null;
                try {
                    fallback = new DatagramSocket();
                    protect(fallback);
                    DatagramPacket p = new DatagramPacket(tp.data, 0, tp.data.length, InetAddress.getByAddress(tb(tp.dip)), tp.dp);
                    fallback.send(p);
                    sent = true;
                    logDevPacket(replayPrefix + "FALLBACK_" + teleTypeName(tp.type), "OUT",
                            tp.data, 0, tp.data.length, tp.sp, tp.dp, tp.dip,
                            "type=" + teleTypeName(tp.type)
                                    + " delay_ms=" + replayDelay
                                    + " burst=" + safeBurst
                                    + " sleep_ms=" + safeSleep);
                } catch (Exception e) {
                    logDevPacket(failPrefix + teleTypeName(tp.type), "OUT",
                            tp.data, 0, tp.data.length, tp.sp, tp.dp, tp.dip,
                            "type=" + teleTypeName(tp.type) + " fallback_error");
                } finally {
                    if (fallback != null) {
                        try { fallback.close(); } catch (Exception e) { }
                    }
                }
            }
            if (safeSleep > 0 && ((index + 1) % safeBurst == 0 || index == all.size() - 1)) {
                try { Thread.sleep(safeSleep); } catch (Exception e) { }
            }
        }
    }

    private TunAddress nextTunAddress() {
        synchronized (PacketVpnService.class) {
            android.content.SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            int last = prefs.getInt(KEY_TUN_IP_CURSOR, Integer.MIN_VALUE);
            int next;
            if (last < 0 || last >= TUN_IP_POOL_SIZE) {
                next = rng.nextInt(TUN_IP_POOL_SIZE);
            } else {
                next = (last + 1) % TUN_IP_POOL_SIZE;
            }
            prefs.edit().putInt(KEY_TUN_IP_CURSOR, next).commit();

            int b = TUN_IP_B_BASE + (next / TUN_IP_C_COUNT);
            int c = next % TUN_IP_C_COUNT;
            String ip = "26." + b + "." + c + ".2";
            int ipInt = 0x1a000000 | (b << 16) | (c << 8) | 0x02;
            return new TunAddress(ip, ipInt);
        }
    }

    private void setDevMode(boolean enabled) {
        synchronized (devLock) {
            devMode = enabled;
            if (enabled) {
                if (!devSessionActive) {
                    devSessionActive = true;
                    devSessionStartMs = System.currentTimeMillis();
                    devSessionEndMs = 0L;
                    devEventSeq = 0;
                    devDroppedEvents = 0;
                    devUnknownActiveEvents = 0;
                    devProtocolDropEvents = 0;
                    devNonIpv4Drops = 0;
                    devNonUdpDrops = 0;
                    devEvents.clear();
                    devKindCounts.clear();
                    devSizeCounts.clear();
                }
            } else if (devSessionActive) {
                dumpDevReportLocked();
            }
        }
    }

    private int getSavedTeleReplayDelayMs() {
        int saved = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getInt(KEY_TELE_REPLAY_DELAY, TELE_REPLAY_DELAY_DEFAULT_MS);
        return clampTeleReplayDelayMs(saved);
    }

    private void loadTeleSpeedSettings() {
        setTeleSpeedSettings(
                TELE_SPEED_BURST_DEFAULT,
                TELE_SPEED_SLEEP_DEFAULT_MS,
                false);
    }

    private void loadLobbyRouteSetting(boolean rebuildIfChanged) {
        setLobbyRouteEnabled(true, rebuildIfChanged);
    }

    private void setLobbyRouteEnabled(boolean enabled, boolean rebuildIfChanged) {
        boolean changed = lobbyRouteEnabled != enabled;
        lobbyRouteEnabled = enabled;
        if (changed) {
            lobbyRouteFallbackFull = false;
            logDevControl("MODE_VPN_ROUTE_CHANGED",
                    "route=" + activeRouteModeName()
                            + " active=" + isActive
                            + " run=" + isRun);
            if (rebuildIfChanged && isRun) {
                restartTunnel(isActive);
            }
        }
    }

    private boolean shouldAutoStopTeleOnFreeze() {
        return getSharedPreferences(PREFS, MODE_PRIVATE)
                .getBoolean(KEY_AUTO_STOP_TELE_ON_FREEZE, true);
    }

    private void setTeleReplayDelayMs(int value) {
        teleReplayDelayMs = clampTeleReplayDelayMs(value);
    }

    private void setTeleSpeedSettings(int burstPackets, int sleepMs, boolean catchupTail) {
        teleSpeedBurstPackets = TELE_SPEED_BURST_DEFAULT;
        teleSpeedSleepMs = TELE_SPEED_SLEEP_DEFAULT_MS;
        teleSpeedCatchupTail = false;
    }

    private static int clampTeleReplayDelayMs(int value) {
        if (value < TELE_REPLAY_DELAY_MIN_MS) return TELE_REPLAY_DELAY_MIN_MS;
        if (value > TELE_REPLAY_DELAY_MAX_MS) return TELE_REPLAY_DELAY_MAX_MS;
        return value;
    }

    private static int clampTeleSpeedBurstPackets(int value) {
        if (value < TELE_SPEED_BURST_MIN) return TELE_SPEED_BURST_MIN;
        if (value > TELE_SPEED_BURST_MAX) return TELE_SPEED_BURST_MAX;
        return value;
    }

    private static int clampTeleSpeedSleepMs(int value) {
        if (value < TELE_SPEED_SLEEP_MIN_MS) return TELE_SPEED_SLEEP_MIN_MS;
        if (value > TELE_SPEED_SLEEP_MAX_MS) return TELE_SPEED_SLEEP_MAX_MS;
        return value;
    }

    private void logProtocolDrop(String kind, byte[] data, int off, int len,
                                 int sourcePort, int destPort, int peerIp, String note) {
        if (!markProtocolDrop(kind)) return;
        logDevPacket(kind, "OUT", data, off, len, sourcePort, destPort, peerIp, note);
    }

    private boolean markProtocolDrop(String kind) {
        if (!devMode) return false;
        synchronized (devLock) {
            if ("DROP_NON_IPV4".equals(kind)) {
                devNonIpv4Drops++;
            } else if ("DROP_NON_UDP".equals(kind)) {
                devNonUdpDrops++;
            }
            if (devProtocolDropEvents >= DEV_PROTOCOL_DROP_MAX) return false;
            devProtocolDropEvents++;
            return true;
        }
    }

    private static String protoName(int proto) {
        if (proto == 1) return "ICMP(1)";
        if (proto == 6) return "TCP(6)";
        if (proto == 17) return "UDP(17)";
        if (proto == 58) return "ICMPV6(58)";
        return "PROTO(" + proto + ")";
    }

    private void logDevPacket(String kind, String direction, byte[] data, int off, int len,
                              int sourcePort, int destPort, int peerIp, String note) {
        if (!devMode) return;
        long now = System.currentTimeMillis();
        String line;
        synchronized (devLock) {
            if (!devSessionActive) {
                devSessionActive = true;
                devSessionStartMs = now;
                devSessionEndMs = 0L;
                devUnknownActiveEvents = 0;
            }

            incCount(devKindCounts, kind);
            incCount(devSizeCounts, Integer.valueOf(len));

            devEventSeq++;
            line = "#" + devEventSeq
                    + " +" + (now - devSessionStartMs) + "ms"
                    + " kind=" + kind
                    + " dir=" + direction
                    + " peer=" + ipToString(peerIp)
                    + " sp=" + sourcePort
                    + " dp=" + destPort
                    + " size=" + len
                    + " active=" + isActive
                    + " filter=" + isFilterEnabled
                    + " freeze=" + isFreeze
                    + " ghost=" + isGhost
                    + " tele=" + isJitter
                    + " tele_v2=" + isJitterV2
                    + " delay_ping=" + isDelayPing
                    + " tele_speed=" + isTeleSpeed
                    + " replay=" + _teleReplaying
                    + " replay_v2=" + _teleV2Replaying
                    + " replay_delay_ping=" + _delayPingReplaying
                    + " replay_tele_speed=" + _teleSpeedReplaying
                    + " note=" + note
                    + " payload_hex=" + hex(data, off, len);

            if (devEvents.size() < DEV_MAX_EVENTS) {
                devEvents.add(line);
            } else {
                devDroppedEvents++;
            }
        }
    }

    private void logDevControl(String kind, String note) {
        logDevPacket(kind, "CTRL", new byte[0], 0, 0, 0, 0, 0, note);
    }

    private void dumpDevReportLocked() {
        devSessionEndMs = System.currentTimeMillis();
        File dir = getExternalFilesDir("dev_logs");
        if (dir == null) dir = new File(getFilesDir(), "dev_logs");
        try {
            if (!dir.exists()) dir.mkdirs();
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(devSessionEndMs));
            File out = new File(dir, "dev_packet_log_" + stamp + ".txt");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
            try {
                writer.write("DEV MODE PACKET REPORT\n");
                writer.write("package=" + pkg + "\n");
                writer.write("started=" + formatTime(devSessionStartMs) + "\n");
                writer.write("ended=" + formatTime(devSessionEndMs) + "\n");
                writer.write("duration_ms=" + (devSessionEndMs - devSessionStartMs) + "\n");
                writer.write("events_saved=" + devEvents.size() + "\n");
                writer.write("events_dropped_by_log_cap=" + devDroppedEvents + "\n");
                writer.write("unknown_active_log_cap=" + DEV_UNKNOWN_ACTIVE_MAX + "\n");
                writer.write("unknown_active_logged=" + devUnknownActiveEvents + "\n");
                writer.write("protocol_drop_log_cap=" + DEV_PROTOCOL_DROP_MAX + "\n");
                writer.write("protocol_drop_logged=" + devProtocolDropEvents + "\n");
                writer.write("non_ipv4_drops_seen=" + devNonIpv4Drops + "\n");
                writer.write("non_udp_drops_seen=" + devNonUdpDrops + "\n");
                writer.write("vpn_route_mode=" + activeRouteModeName() + "\n");
                writer.write("vpn_route_fallback_full=" + lobbyRouteFallbackFull + "\n");
                writer.write("out_udp_seen=" + outUdpSeen.get() + "\n");
                writer.write("icmp_echo_reply_enabled=" + ICMP_ECHO_REPLY_ENABLED + "\n");
                writer.write("tele_buf_max=" + TELE_BUF_MAX + "\n");
                writer.write("tele_v2_move_tail_max=" + TELE_V2_MOVE_TAIL_MAX + "\n");
                writer.write("delay_ping_window_ms=" + DELAY_PING_WINDOW_MS + "\n");
                writer.write("tele_replay_delay_ms=" + teleReplayDelayMs + "\n\n");
                writer.write("tele_speed_burst_packets=" + teleSpeedBurstPackets + "\n");
                writer.write("tele_speed_sleep_ms=" + teleSpeedSleepMs + "\n");
                writer.write("tele_speed_catchup_tail=" + teleSpeedCatchupTail + "\n");
                writer.write("tele_speed_catchup_tail_default=" + TELE_SPEED_CATCHUP_TAIL_DEFAULT + "\n");
                writer.write("tele_speed_hold_fire=false\n");
                writer.write("tele_speed_replay_order=original\n\n");

                writer.write("SUMMARY_BY_KIND\n");
                for (Map.Entry<String, Integer> entry : devKindCounts.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
                }

                writer.write("\nSUMMARY_BY_PAYLOAD_SIZE\n");
                for (Map.Entry<Integer, Integer> entry : devSizeCounts.entrySet()) {
                    writer.write(entry.getKey() + " bytes=" + entry.getValue() + "\n");
                }

                writer.write("\nEVENTS\n");
                for (int i = 0; i < devEvents.size(); i++) {
                    writer.write(devEvents.get(i));
                    writer.write("\n");
                }
            } finally {
                writer.close();
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString("dev_last_log_file", out.getAbsolutePath())
                    .commit();
        } catch (Exception e) {
            android.util.Log.e("DEVLOG", "Cannot write dev packet report", e);
        } finally {
            devSessionActive = false;
            devEvents.clear();
            devKindCounts.clear();
            devSizeCounts.clear();
            devDroppedEvents = 0;
            devUnknownActiveEvents = 0;
            devProtocolDropEvents = 0;
            devNonIpv4Drops = 0;
            devNonUdpDrops = 0;
        }
    }

    private static <T> void incCount(HashMap<T, Integer> map, T key) {
        Integer old = map.get(key);
        map.put(key, old == null ? 1 : old.intValue() + 1);
    }

    private static String formatTime(long timeMs) {
        if (timeMs <= 0L) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(timeMs));
    }

    private static String ipToString(int ip) {
        return ((ip >> 24) & 0xff) + "."
                + ((ip >> 16) & 0xff) + "."
                + ((ip >> 8) & 0xff) + "."
                + (ip & 0xff);
    }

    private static String hex(byte[] data, int off, int len) {
        if (data == null || len <= 0 || off < 0 || off >= data.length) return "";
        int safeLen = Math.min(len, data.length - off);
        int max = Math.min(safeLen, DEV_MAX_PAYLOAD_BYTES);
        char[] out = new char[max * 2 + (safeLen > max ? 3 : 0)];
        final char[] table = "0123456789ABCDEF".toCharArray();
        int p = 0;
        for (int i = 0; i < max; i++) {
            int v = data[off + i] & 0xff;
            out[p++] = table[v >>> 4];
            out[p++] = table[v & 0x0f];
        }
        if (safeLen > max) {
            out[p++] = '.';
            out[p++] = '.';
            out[p++] = '.';
        }
        return new String(out, 0, p);
    }

    private void startStats() {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    try { Thread.sleep(1500); } catch (InterruptedException e) { return; }
                    long sent  = pSent.get();
                    long drop  = pDrop.get();
                    long total = sent + drop;
                    int  loss  = total > 0 ? (int)(drop * 100L / total) : 0;
                    String mode = isActive ? "ACTIVE" : "STANDBY";
                    Intent i = new Intent("com.fakelag.cryhard8.STATS");
                    i.putExtra("ping", (int) rtt);
                    i.putExtra("loss", loss);
                    i.putExtra(EXTRA_BYTES_IN,  bIn.get());
                    i.putExtra(EXTRA_BYTES_OUT, bOut.get());
                    i.putExtra(EXTRA_FILTER_ENABLED, isFilterEnabled);
                    i.putExtra("mode", mode);
                    sendBroadcast(i);
                }
            }
        });
    }

    private void stop() {
        synchronized (devLock) {
            if (devSessionActive) {
                dumpDevReportLocked();
            }
        }
        isFilterEnabled = false;
        isFreeze = false;
        isGhost = false;
        isJitter = false;
        isJitterV2 = false;
        isDelayPing = false;
        isTeleSpeed = false;
        _teleReplaying = false;
        _teleV2Replaying = false;
        _delayPingReplaying = false;
        _teleSpeedReplaying = false;
        _teleBuf.clear();
        clearTeleCatchupBuffer();
        clearTeleV2Buffer();
        clearTeleSpeedBuffers();
        synchronized (_delayPingLock) {
            _delayPingGeneration++;
            _delayPingBuf.clear();
            _delayPingReplayQueue.clear();
            _delayPingReplaying = false;
            _delayPingLoopRunning = false;
        }
        isRun = false;
        try { if (tun != null) tun.close(); } catch (Exception e) { }
        for (SE se : udpMap.values()) {
            try { se.s.close(); } catch (Exception e) { }
        }
        udpMap.clear();
        if (threadPool != null) threadPool.shutdownNow();
        stopSelf();
    }
}
