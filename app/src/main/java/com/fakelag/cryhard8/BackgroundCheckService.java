package com.fakelag.cryhard8;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class BackgroundCheckService extends Service {
    private Thread checkThread;
    private volatile boolean isRunning = true;

    @Override
    public void onCreate() {
        super.onCreate();
        checkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                while (isRunning) {
                    try {
                        // Chờ 10 giây trước khi check lại
                        Thread.sleep(10000); 
                        
                        KeyManager.checkStatusSilent(getApplicationContext(), deviceId, new KeyManager.StatusCallback() {
                            @Override
                            public void onResult(String status) {
                                if ("BANNED".equals(status) || "KICKED".equals(status)) {
                                    // Nếu bị Ban hoặc Kick, gửi Broadcast yêu cầu tắt App
                                    Intent intent = new Intent("com.fakelag.cryhard8.KILL_APP");
                                    intent.putExtra("reason", status);
                                    sendBroadcast(intent);
                                    
                                    // Dừng Service
                                    isRunning = false;
                                    stopSelf();
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        checkThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (checkThread != null) checkThread.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
