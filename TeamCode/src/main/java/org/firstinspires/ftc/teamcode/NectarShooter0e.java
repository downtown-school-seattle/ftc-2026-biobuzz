package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/*
the diptracker stuff is special things for measuring how much rpm the wheel loses when it shoots a ball, it's all commented out right now but feel free to use it
 */

public class NectarShooter0e {
    public static final double FREE_SPEED_RPM = 6000;
    public static final double MIN_RPM = 3000;
    public static final double MAX_RPM = 4500;
    public static final double START_RPM = 4200;
    public static final double TICKS_PER_REV = 28;

    private final DcMotorEx motor;
    private double targetRpm = START_RPM;
    private boolean running = false;

    private final ElapsedTime clock = new ElapsedTime();
//    private final DipTracker dipTracker = new DipTracker("NectarShooter0e");
    private double commandedPower = 0;

    public NectarShooter0e(DcMotorEx motor) {
        this.motor = motor;
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void changeRpm(double delta) {
        targetRpm = Math.max(MIN_RPM, Math.min(MAX_RPM, targetRpm + delta));
//        dipTracker.reset();
        apply();
    }

    public void setRpm(double rpm) {
        if (rpm <= 0) {
            stop();
            return;
        }
        targetRpm = Math.min(MAX_RPM, rpm);
        if (running) {
//            dipTracker.reset();
            apply();
        } else {
            start();
        }
    }

    public double getTargetRpm() {
        return targetRpm;
    }

    public double getTicksPerSecond() {
        return Math.abs(motor.getVelocity());
    }

    public double getMeasuredRpm() {
        return getTicksPerSecond() / TICKS_PER_REV * 60;
    }

    public void start() {
        running = true;
//        dipTracker.reset();
        apply();
    }

    public void stop() {
        running = false;
//        dipTracker.reset();
        apply();
    }

    public void update() {
        if (!running) return;
//        dipTracker.update(clock.seconds(), getMeasuredRpm());
    }

    public void setReversed(boolean reversed) {
        motor.setDirection(reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }

    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("nectar shooter", "%s  target %.0f rpm  measured %.0f rpm", running ? "ON" : "off", targetRpm, getMeasuredRpm());
//        if (!dipTracker.getSummary().isEmpty()) {
//            telemetry.addLine("<font color=\"#00b300\">" + dipTracker.getSummary() + "</font>");
//        }
    }

    private void setCommandedPower(double power) {
        commandedPower = power;
        motor.setPower(power);
    }

    private void apply() {
        setCommandedPower(running ? targetRpm / FREE_SPEED_RPM : 0);
    }
}
