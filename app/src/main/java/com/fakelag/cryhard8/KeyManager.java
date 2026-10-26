package com.fakelag.cryhard8;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KeyManager {
    private static final String RENDER_URL = "https://minizo-sevaapi.onrender.com/api/check_key"; // Link Render của bạn
    private static final String APP_SECRET = "APP_SECRET_456"; // Phải khớp với app.py
    private static final String PREFS_NAME = "key_prefs";
    private static final String KEY_ACTIVATED = "is_activated";

    public interface KeyCallback {
        void onResult(boolean success, String message);
    }

    public static void checkKeyOnline(final Context context, final String keyCode, final KeyCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(RENDER_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("X-App-Key", APP_SECRET);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(60000); // Chờ 60s vì Render có thể ngủ
                    conn.setReadTimeout(60000);

                    // Lấy thông tin máy
                    String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
                    String androidVer = Build.VERSION.RELEASE;
                    String carrier = "Unknown";
                    try {
                        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (tm != null) carrier = tm.getNetworkOperatorName();
                    } catch (Exception ignored) {}
                    
                    String ip = "0.0.0.0";
                    try {
                        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (wm != null) {
                            int ipAddress = wm.getConnectionInfo().getIpAddress();
                            ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                        }
                    } catch (Exception ignored) {}

                    // Tạo JSON Body
                    JSONObject json = new JSONObject();
                    json.put("key_code", keyCode);
                    json.put("device_id", deviceId);
                    json.put("device_name", deviceName);
                    json.put("android_version", androidVer);
                    json.put("carrier", carrier);
                    json.put("ip", ip);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.close();

                    // Đọc phản hồi
                    int responseCode = conn.getResponseCode();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    JSONObject resJson = new JSONObject(response.toString());
                    String status = resJson.optString("status");
                    String message = resJson.optString("message", "Lỗi không xác định");

                    if (status.equals("OK")) {
                        // Lưu trạng thái đã kích hoạt
                        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putBoolean(KEY_ACTIVATED, true).putString("saved_key", keyCode).apply();
                        callback.onResult(true, message);
                    } else if (status.equals("LOCKED")) {
                        callback.onResult(false, "UPDATE|" + message);
                    } else {
                        callback.onResult(false, message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onResult(false, "Không thể kết nối Server. Vui lòng thử lại!");
                }
            }
        });
    }

    public static boolean isActivated(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ACTIVATED, false);
    }
}