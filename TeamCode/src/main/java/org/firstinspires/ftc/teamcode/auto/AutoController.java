package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.MotorTable;
import org.firstinspires.ftc.teamcode.RobotController;

/*
there are 3 commands that work in autonomous as well as a way to group them, they are: drive(), turn(), motor()
there is also run() that allows you to run multiple commands at a time however, they need to be written as "[command]Cmd"

examples:
driveDistance(90 [degrees], 12 [inches])
turnDistance(45 [degrees])
motor(nectarShooter [motor name], 4200 [rpm])

run(turnCmd(30), motorCmd(motor0e, 4000)) currently running a drive and turn command at the same time doesn't work but I am working on that right now
 */

abstract public class AutoController extends RobotController {
    private static final double MIN_MOVING_RPM = 5;

    public static final double FORWARD = 90;
    public static final double RIGHT = 0;
    public static final double BACK = 270;
    public static final double LEFT = 180;
    protected double driveWheelTicksPerRev = MotorTable.GOBILDA_312_RPM.encoderResolution;
    protected double driveSpeed = 0.5;

    protected abstract void runAuto();

    @Override
    public void runOpMode() {
        initRobotController();

        waitForStart();
        if (opModeIsActive()) runAuto();
        drive(0, 0, 0);
        setShooterRpm(0);
        setIntakeRpm(0);
    }

    /**
     * Drive in a line.
     * Refactored from original MoveCommand.
     */
    protected void driveForSeconds(double heading, double seconds) {
        // Turn on motors
        double forward = Math.sin(heading) * driveSpeed;
        double right = Math.cos(heading) * driveSpeed;
        drive(forward, right, 0);

        // Wait for seconds
        ElapsedTime timer = new ElapsedTime();
        while (timer.seconds() < seconds) {
            idle();
        }

        // Turn off motors
        drive(0, 0, 0);
    }

    /** Refactored from original MoveCommand */
    private void turnForSeconds(boolean ccw, double seconds) {
        // Turn on motors
        drive(0, 0, ccw ? -driveSpeed : driveSpeed);

        // Wait for seconds
        ElapsedTime timer = new ElapsedTime();
        while (timer.seconds() < seconds) {
            idle();
        }

        // Turn off motors
        drive(0, 0, 0);
    }

    /** Turn clockwise in place. */
    protected void turnCwForSeconds(double seconds) {
        turnForSeconds(false, seconds);
    }

    /** Turn counter-clockwise in place. */
    protected void turnCcwForSeconds(double seconds) {
        turnForSeconds(true, seconds);
    }
}
