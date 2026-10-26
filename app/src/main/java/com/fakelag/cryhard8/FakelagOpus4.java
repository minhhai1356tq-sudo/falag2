package com.fakelag.cryhard8;

public class FakelagOpus4 {
    public boolean vpnDetected = false;
    public String  localIP     = "";
    public String Creator	  = "XenoxModder";
    public String  ifaceName   = "";
    public int     confidence  = 0;

    public static FakelagOpus4 fromDetectResult(FakelagOpus3.DetectResult r) {
        FakelagOpus4 report = new FakelagOpus4();
        report.vpnDetected = r.vpnDetected;
        report.localIP     = r.localIP;
        report.ifaceName   = r.detectedIface;
        report.confidence  = r.confidenceScore;
        return report;
    }
}
