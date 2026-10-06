package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.DcMotor;
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
    protected double strafeMultiplier = 1.1;  // for sliding error and shit
    protected double power = 0.5;

    // auto here
    protected void runAuto() {}

    @Override
    public void runOpMode() {
        initRobotController();
        // Set so a positive command drives each wheel "forward" (back wheels are wired opposite to the fronts).
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        for (DcMotor m : motors()) m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
        if (opModeIsActive()) runAuto();
    }
    // drive (inches)
    protected void drive(Direction direction, double inches) {
        double ticks = inches * ticksPerInch;
        switch (direction) {
            case FORWARD: move("Drive FORWARD " + inches + " in", ticks, ticks, ticks, ticks); break;
            case BACK:    move("Drive BACK " + inches + " in", -ticks, -ticks, -ticks, -ticks); break;
            case RIGHT:   ticks *= strafeMultiplier; move("Strafe RIGHT " + inches + " in", ticks, -ticks, -ticks, ticks); break;
            case LEFT:    ticks *= strafeMultiplier; move("Strafe LEFT " + inches + " in", -ticks, ticks, ticks, -ticks); break;
        }
    }

    // turn (degrees)
    protected void turn(double degrees) {
        double t = degrees * ticksPerDegree;
        move("Turn " + degrees + " deg", t, -t, t, -t);
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
        while (opModeIsActive() && anyBusy(motors)) {
            telemetry.addData("Doing", action);
            telemetry.addData("fl", motors[0].getCurrentPosition() + " / " + motors[0].getTargetPosition());
            telemetry.addData("fr", motors[1].getCurrentPosition() + " / " + motors[1].getTargetPosition());
            telemetry.addData("bl", motors[2].getCurrentPosition() + " / " + motors[2].getTargetPosition());
            telemetry.addData("br", motors[3].getCurrentPosition() + " / " + motors[3].getTargetPosition());
            telemetry.update();
            idle();
        }
        for (DcMotor m : motors) m.setPower(0);
        RobotLog.ii("AutoController", "Finished: " + action);
    }

    private DcMotor[] motors() {
        return new DcMotor[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};
    }

    private boolean anyBusy(DcMotor[] motors) {
        for (DcMotor m : motors) if (m.isBusy()) return true;
        return false;
    }
}
