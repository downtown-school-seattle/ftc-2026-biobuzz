package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.RobotController;

/**
 * Barebones modular auto. Override {@link #runAuto()} in an auto mode and write the path:
 *
 * <pre>
 * drive(FORWARD, 24);   // inches
 * turn(90);             // degrees, positive = clockwise
 * drive(LEFT, 12);
 * </pre>
 *
 * Wheels follow the port order of {@link RobotController}: 0 = fl, 1 = fr, 2 = bl, 3 = br.
 */
abstract public class AutoController extends RobotController {
    public enum Direction { FORWARD, BACK, LEFT, RIGHT }
    public static final Direction FORWARD = Direction.FORWARD;
    public static final Direction BACK = Direction.BACK;
    public static final Direction LEFT = Direction.LEFT;
    public static final Direction RIGHT = Direction.RIGHT;

    // ---- Tune these for the robot ----
    protected double ticksPerInch = 40;       // encoder ticks per inch driven
    protected double ticksPerDegree = 10;     // encoder ticks (per wheel) per degree turned
    protected double strafeMultiplier = 1.1;  // strafing slips, so it needs extra ticks
    protected double power = 0.5;

    /** Write your auto here. */
    protected void runAuto() {}

    @Override
    public void runOpMode() {
        initRobotController();
        // Left side is mounted mirrored; flip if the robot drives backwards.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        for (DcMotor m : motors()) m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
        if (opModeIsActive()) runAuto();
    }

    /** Drive in a direction for a distance in inches. */
    protected void drive(Direction direction, double inches) {
        double ticks = inches * ticksPerInch;
        switch (direction) {
            case FORWARD: move(ticks, ticks, ticks, ticks); break;
            case BACK:    move(-ticks, -ticks, -ticks, -ticks); break;
            case RIGHT:   ticks *= strafeMultiplier; move(ticks, -ticks, -ticks, ticks); break;
            case LEFT:    ticks *= strafeMultiplier; move(-ticks, ticks, ticks, -ticks); break;
        }
    }

    /** Turn in place by degrees. Positive = clockwise (right), negative = counter-clockwise. */
    protected void turn(double degrees) {
        double t = degrees * ticksPerDegree;
        move(t, -t, t, -t);
    }

    /** Move each wheel (fl, fr, bl, br) by the given number of ticks and block until done. */
    private void move(double fl, double fr, double bl, double br) {
        DcMotor[] motors = motors();
        double[] deltas = {fl, fr, bl, br};
        for (int i = 0; i < 4; i++) {
            motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motors[i].setTargetPosition((int) Math.round(deltas[i]));
            motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motors[i].setPower(power);
        }
        while (opModeIsActive() && anyBusy(motors)) {
            idle();
        }
        for (DcMotor m : motors) m.setPower(0);
    }

    private DcMotor[] motors() {
        return new DcMotor[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};
    }

    private boolean anyBusy(DcMotor[] motors) {
        for (DcMotor m : motors) if (m.isBusy()) return true;
        return false;
    }
}
