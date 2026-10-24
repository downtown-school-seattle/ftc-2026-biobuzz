package org.firstinspires.ftc.teamcode;

public enum ExMotorTable {
    //denoted as series then gearing ratio
    GOBILDA_6000_RPM (6000, 28.0), //5203 series, 1:1
    GOBILDA_1620_RPM (1620, 103.8), //5203 series, 3.7:1
    GOBILDA_312_RPM (312, 537.7);

    public final double freeSpeedRpm;
    public final double encoderResolution; //also known as PPR, encoder ticks per revolution

    ExMotorTable(double freeSpeedRpm, double encoderResolution) {
        this.freeSpeedRpm = freeSpeedRpm;
        this.encoderResolution = encoderResolution;
    }
    public double rpmToTicksPerSec(double rpm) {
        return rpm /60.0 * encoderResolution;
    }

    public double ticksPerSecToRpm(double ticksPerSec){
        return ticksPerSec/encoderResolution * 60.0;
    }

    /** Approximate power fraction (-1 to 1) for open loop control */
    public double rpmToPower(double rpm){
        return rpm / freeSpeedRpm;
    }
}