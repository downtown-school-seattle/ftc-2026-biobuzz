package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayDeque;

public class Motor0e {
    public static final String CONFIG_NAME = "motor0e";
    public static final double FREE_SPEED_RPM = 6000;
    public static final double MIN_RPM = 3000;
    public static final double MAX_RPM = 4500;
    public static final double START_RPM = 4200;
    public static final double TICKS_PER_REV = 28;
    private static final double KP = 0.0001;
    private static final double KI = 0.0003;
    private static final double MAX_INTEGRAL = 0.5;
    private static final double MAX_DT_SECONDS = 0.1;
    private static final double DROP_FRACTION = 0.07;
    private static final double RECOVERY_FRACTION = 1.00;
    private static final double WINDOW_SECONDS = 1.0;

    private final DcMotorEx motor;
    private double targetRpm = START_RPM;
    private boolean running = false;

    private final ElapsedTime clock = new ElapsedTime();
    private final ArrayDeque<double[]> history = new ArrayDeque<>();
    private boolean dropping = false;
    private double dropFromRpm;
    private double lowestRpm;
    private double dropStartSeconds;
    private double integral = 0;
    private double lastControlSeconds = 0;
    private double commandedPower = 0;
    private String dipSummary = "";

    public Motor0e(HardwareMap hardwareMap) {
        this(hardwareMap, CONFIG_NAME);
    }

    public Motor0e(HardwareMap hardwareMap, String configName) {
        motor = hardwareMap.get(DcMotorEx.class, configName);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void changeRpm(double delta) {
        targetRpm = Math.max(MIN_RPM, Math.min(MAX_RPM, targetRpm + delta));
        resetDipTracking();
        apply();
    }

    public void setRpm(double rpm) {
        if (rpm <= 0) {
            stop();
            return;
        }
        targetRpm = Math.min(MAX_RPM, rpm);
        if (running) {
            resetDipTracking();
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
        integral = 0;
        lastControlSeconds = clock.seconds();
        resetDipTracking();
        apply();
    }

    public void stop() {
        running = false;
        resetDipTracking();
        apply();
    }

    public void update() {
        if (!running) return;
        double now = clock.seconds();
        double rpm = getMeasuredRpm();
        regulate(now, rpm);

        if (dropping) {
            lowestRpm = Math.min(lowestRpm, rpm);
            double elapsed = now - dropStartSeconds;
            if (rpm >= dropFromRpm * RECOVERY_FRACTION) {
                dropping = false;
                history.clear();
                dipSummary = String.format("dip %.0f -> low %.0f rpm, back up in %.2f s", dropFromRpm, lowestRpm, elapsed);
                RobotLog.ii("Motor0e", dipSummary);
            } else {
                dipSummary = String.format("dip %.0f -> low %.0f rpm, recovering %.2f s...", dropFromRpm, lowestRpm, elapsed);
            }
            return;
        }

        history.addLast(new double[]{now, rpm});
        while (history.peekFirst()[0] < now - WINDOW_SECONDS) history.removeFirst();
        double peak = 0;
        for (double[] sample : history) peak = Math.max(peak, sample[1]);
        if (rpm < peak * (1 - DROP_FRACTION)) {
            dropping = true;
            dropFromRpm = peak;
            lowestRpm = rpm;
            dropStartSeconds = now;
        }
    }

    public void setReversed(boolean reversed) {
        motor.setDirection(reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }

    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("motor0e", "%s  target %.0f  measured %.0f rpm  power %.2f  (%.0f ticks/s)",
                running ? "ON" : "off", targetRpm, getMeasuredRpm(), commandedPower, getTicksPerSecond());
        if (!dipSummary.isEmpty()) {
            telemetry.addLine("<font color=\"#00b300\">" + dipSummary + "</font>");
        }
    }

    private void regulate(double now, double rpm) {
        double dt = Math.min(now - lastControlSeconds, MAX_DT_SECONDS);
        lastControlSeconds = now;

        double error = targetRpm - rpm;
        double unclamped = targetRpm / FREE_SPEED_RPM + KP * error + integral;
        double power = Math.max(0, Math.min(1, unclamped));
        if (power == unclamped) {
            integral = Math.max(-MAX_INTEGRAL, Math.min(MAX_INTEGRAL, integral + KI * error * dt));
        }
        setCommandedPower(power);
    }

    private void setCommandedPower(double power) {
        commandedPower = power;
        motor.setPower(power);
    }

    private void resetDipTracking() {
        dropping = false;
        history.clear();
    }

    private void apply() {
        setCommandedPower(running ? targetRpm / FREE_SPEED_RPM + integral : 0);
    }
}
