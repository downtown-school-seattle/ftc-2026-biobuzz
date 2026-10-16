package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.RobotLog;

public class DipTracker {
    private static final double DROP_FRACTION = 0.07;
    private static final double RECOVERY_FRACTION = 1.00;
    private static final double WINDOW_SECONDS = 1.0;
    private static final int HISTORY_SIZE = 512;

    private final String tag;
    private final double[] sampleTimes = new double[HISTORY_SIZE];
    private final double[] sampleRpms = new double[HISTORY_SIZE];
    private int historyStart = 0;
    private int historyCount = 0;
    private boolean dropping = false;
    private double dropFromRpm;
    private double lowestRpm;
    private double dropStartSeconds;
    private String summary = "";

    public DipTracker(String tag) {
        this.tag = tag;
    }

    public void update(double now, double rpm) {
        if (dropping) {
            lowestRpm = Math.min(lowestRpm, rpm);
            double elapsed = now - dropStartSeconds;
            if (rpm >= dropFromRpm * RECOVERY_FRACTION) {
                dropping = false;
                clearHistory();
                summary = String.format("dip %.0f -> low %.0f rpm, back up in %.2f s", dropFromRpm, lowestRpm, elapsed);
                RobotLog.ii(tag, summary);
            } else {
                summary = String.format("dip %.0f -> low %.0f rpm, recovering %.2f s...", dropFromRpm, lowestRpm, elapsed);
            }
            return;
        }

        if (historyCount == HISTORY_SIZE) {
            historyStart = (historyStart + 1) % HISTORY_SIZE;
            historyCount--;
        }
        int end = (historyStart + historyCount) % HISTORY_SIZE;
        sampleTimes[end] = now;
        sampleRpms[end] = rpm;
        historyCount++;
        while (historyCount > 0 && sampleTimes[historyStart] < now - WINDOW_SECONDS) {
            historyStart = (historyStart + 1) % HISTORY_SIZE;
            historyCount--;
        }
        double peak = 0;
        for (int i = 0; i < historyCount; i++) peak = Math.max(peak, sampleRpms[(historyStart + i) % HISTORY_SIZE]);
        if (rpm < peak * (1 - DROP_FRACTION)) {
            dropping = true;
            dropFromRpm = peak;
            lowestRpm = rpm;
            dropStartSeconds = now;
        }
    }

    public void reset() {
        dropping = false;
        clearHistory();
    }

    public String getSummary() {
        return summary;
    }

    private void clearHistory() {
        historyStart = 0;
        historyCount = 0;
    }
}
