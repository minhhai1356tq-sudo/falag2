package com.fakelag.cryhard8;

import android.content.Context;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class VpnDetectionUtils {

    public static class DetectResult {
        public boolean vpnDetected   = false;
        public boolean tunInterface  = false;
        public boolean vpnCapability = false;
        public boolean suspiciousIP  = false;
        public String  localIP       = "";
        public String  detectedIface = "";
        public int     confidenceScore = 0;
    }
//Made by NAHIYAN
    public static DetectResult fullDetect(Context ctx) {
        DetectResult r = new DetectResult();
        try {
            Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces == null) return r;
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                if (!iface.isUp()) continue;
                String name = iface.getName().toLowerCase();
                if (name.startsWith("tun") || name.startsWith("ppp")
                        || name.startsWith("vpn") || name.contains("vpn")) {
                    r.tunInterface   = true;
                    r.detectedIface  = name;
                    r.vpnDetected    = true;
                    r.confidenceScore = 80;
                    Enumeration addrs = iface.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = (InetAddress) addrs.nextElement();
                        if (addr.isLoopbackAddress()) continue;
                        r.localIP = addr.getHostAddress();
                    }
                    return r;
                }
            }
        } catch (Exception e) { }
        return r;
    }
}
