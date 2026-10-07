package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.RobotController;

abstract public class AutoController extends RobotController {
    public enum Direction { FORWARD, BACK, LEFT, RIGHT }
    public static final Direction FORWARD = Direction.FORWARD;
    public static final Direction BACK = Direction.BACK;
    public static final Direction LEFT = Direction.LEFT;
    public static final Direction RIGHT = Direction.RIGHT;

    // default settings
    protected double ticksPerInch = 40;       // ticks per inch
    protected double ticksPerDegree = 10;     // ticks per wheel degree
    protected double power = 0.5;
    protected int tolerance = 15;             // ticks from target that counts as "arrived"
    protected double moveTimeoutSeconds = 5;  // give up on a move after this long

    // auto here
    protected void runAuto() {}

    @Override
    public void runOpMode() {
        initRobotController();
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        for (DcMotor m : motors()) m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
        if (opModeIsActive()) runAuto();
    }
    // DEPRICATED: time based driving drive/turn take SECONDS
    /*
    // encoder/inches version is commented out below.
    // drive (seconds)
    protected void drive(Direction direction, double seconds) {
        double p = power;
        switch (direction) {
            // back wheels (bl, br) are negated vs. standard mecanum signs; front wheels (fl, fr) are standard
            case FORWARD: move("Drive FORWARD " + seconds + " s", seconds, p, p, -p, -p); break;
            case BACK:    move("Drive BACK " + seconds + " s", seconds, -p, -p, p, p); break;
            case RIGHT:   move("Strafe RIGHT " + seconds + " s", seconds, p, p, p, p); break;
            case LEFT:    move("Strafe LEFT " + seconds + " s", seconds, -p, -p, -p, -p); break;
        }
    }

    // turn (seconds): positive = clockwise, negative = counter-clockwise
    protected void turn(double seconds) {
        double p = power * Math.signum(seconds);
        move("Turn " + seconds + " s", Math.abs(seconds), -p, p, -p, p);
    }

    // wheel independant, runs for a set time
    private void move(String action, double seconds, double fl, double fr, double bl, double br) {
        DcMotor[] motors = motors();
        double[] powers = {fl, fr, bl, br};
        RobotLog.ii("AutoController", "Starting: " + action);
        ElapsedTime timer = new ElapsedTime();
        for (int i = 0; i < 4; i++) {
            motors[i].setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motors[i].setPower(powers[i]);
        }
        while (opModeIsActive() && timer.seconds() < seconds) {
            telemetry.addData("Doing", action);
            telemetry.addData("Elapsed", "%.2f / %.2f s", timer.seconds(), seconds);
            telemetry.addData("Powers fl/fr/bl/br", "%.2f %.2f %.2f %.2f", fl, fr, bl, br);
            telemetry.update();
            idle();
        }
        for (DcMotor m : motors) m.setPower(0);
        RobotLog.ii("AutoController", "Finished: " + action);
    }
    */

    // encoder version (broken) inches / degrees
    // drive (inches)
    protected void drive(Direction direction, double inches) {
        double ticks = inches * ticksPerInch;
        switch (direction) {
            case FORWARD: move("Drive FORWARD " + inches + " in", ticks, ticks, -ticks, -ticks); break;
            case BACK:    move("Drive BACK " + inches + " in", -ticks, -ticks, ticks, ticks); break;
            case RIGHT:   move("Strafe RIGHT " + inches + " in", ticks, ticks, ticks, ticks); break;
            case LEFT:    move("Strafe LEFT " + inches + " in", -ticks, -ticks, -ticks, -ticks); break;
        }
    }

    // turn (degrees)
    protected void turn(double degrees) {
        double t = degrees * ticksPerDegree;
        move("Turn " + degrees + " deg", -t, t, -t, t);
    }

    // wheel independant
    private void move(String action, double fl, double fr, double bl, double br) {
        DcMotor[] motors = motors();
        double[] deltas = {fl, fr, bl, br};
        RobotLog.ii("AutoController", "Starting: " + action);
        for (int i = 0; i < 4; i++) {
            motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            motors[i].setTargetPosition((int) Math.round(deltas[i]));
            motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motors[i].setPower(power);
        }
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && !reachedTargets(motors)) {
            if (timer.seconds() > moveTimeoutSeconds) {
                RobotLog.ee("AutoController", "Timed out (target never reached): " + action);
                break;
            }
            telemetry.addData("Doing", action);
            telemetry.addData("fl", motors[0].getCurrentPosition() + " / " + motors[0].getTargetPosition());
            telemetry.addData("fr", motors[1].getCurrentPosition() + " / " + motors[1].getTargetPosition());
            telemetry.addData("bl", motors[2].getCurrentPosition() + " / " + motors[2].getTargetPosition());
            telemetry.addData("br", motors[3].getCurrentPosition() + " / " + motors[3].getTargetPosition());
            telemetry.update();
            idle();
        }
        // Destination reached: cut power and drop the held target so the next move starts clean.
        for (DcMotor m : motors) {
            m.setPower(0);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
        RobotLog.ii("AutoController", "Finished: " + action);
    }

    /** True once every wheel is within tolerance of its own target (doesn't rely on isBusy). */
    private boolean reachedTargets(DcMotor[] motors) {
        for (DcMotor m : motors) {
            if (Math.abs(m.getTargetPosition() - m.getCurrentPosition()) > tolerance) return false;
        }
        return true;
    }

    private DcMotor[] motors() {
        return new DcMotor[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};
    }
}
