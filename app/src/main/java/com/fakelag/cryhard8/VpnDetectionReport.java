package com.fakelag.cryhard8;

public class VpnDetectionReport {
    public boolean vpnDetected = false;
    public String  localIP     = "";
    public String Creator	  = "XenoxModder";
    public String  ifaceName   = "";
    public int     confidence  = 0;

    public static VpnDetectionReport fromDetectResult(VpnDetectionUtils.DetectResult r) {
        VpnDetectionReport report = new VpnDetectionReport();
        report.vpnDetected = r.vpnDetected;
        report.localIP     = r.localIP;
        report.ifaceName   = r.detectedIface;
        report.confidence  = r.confidenceScore;
        return report;
    }
}
