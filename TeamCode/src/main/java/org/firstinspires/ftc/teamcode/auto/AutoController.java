package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Rotation;
import org.firstinspires.ftc.teamcode.MotorTable;
import org.firstinspires.ftc.teamcode.RobotController;

/*
there are 3 commands that work in autonomous as well as a way to group them, they are: drive(), turn(), motor()
there is also run() that allows you to run multiple commands at a time however, they need to be written as "[command]Cmd"

examples:
driveForSeconds(90 [degrees], 12 [seconds])
turnForSeconds(Rotation.CW, 45 [seconds])
motor(nectarShooter [motor name], 4200 [rpm])

run(turnCmd(30), motorCmd(motor0e, 4000)) currently running a drive and turn command at the same time doesn't work but I am working on that right now
 */

abstract public class AutoController extends RobotController {
    // todo: use this for pinpoint
    private static final double MIN_MOVING_RPM = 5;

    public static final double FORWARD = 0;
    public static final double RIGHT = 90;
    public static final double BACK = 180;
    public static final double LEFT = 270;
    protected double driveSpeed = 0.5;

    protected abstract void runAuto();

    @Override
    public void runOpMode() {
        initRobotController();

        waitForStart();
        if (opModeIsActive()) runAuto();
        turnOff();
    }

    /**
     * Drive in a line.
     * Refactored from original MoveCommand.
     */
    protected void driveForSeconds(double heading, double seconds) {
        // Turn on drive motors
        double forward = Math.cos(heading) * driveSpeed;
        double right = Math.sin(heading) * driveSpeed;
        drive(forward, right, 0);

        // Wait for seconds
        ElapsedTime timer = new ElapsedTime();
        while (timer.seconds() < seconds) {
            idle();
        }

        // Turn off drive motors
        stopDriveMotors();
    }

    /**
     * Refactored from original MoveCommand.
     * Call with Rotation.CCW or Rotation.CW to change the direction of motion.
     */
    protected void turnForSeconds(Rotation direction, double seconds) {
        // Turn on drive motors
        drive(0, 0, direction == Rotation.CCW ? -driveSpeed : driveSpeed);

        // Wait for seconds
        ElapsedTime timer = new ElapsedTime();
        while (timer.seconds() < seconds) {
            idle();
        }

        // Turn off drive motors
        stopDriveMotors();
    }

    protected void shooterGateOpen(boolean open) {
        if(open) {
            shooterGate.setPosition(0);
        } else {
            shooterGate.setPosition(.15);
        }
    }

    protected void waitMs(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
