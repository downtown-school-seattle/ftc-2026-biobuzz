package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;
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
    private static final double MIN_MOVING_POWER = .1;

    public static final double FORWARD = 0;
    public static final double RIGHT = 90;
    public static final double BACK = 180;
    public static final double LEFT = 270;
    protected double driveSpeed = 0.5;


    /** Target X (forward), in millimeters. */
    private double targetX = 0.0;
    /** Target Y (right), in millimeters. */
    private double targetY = 0.0;
    /** Target heading, in radians. */
    private double targetHeading = 0.0;
    /** Distance at which the robot will start trying to slow down. */
    private static final double BRAKING_DISTANCE_MM = 150.0;
    /** Distance at which the robot will start trying to slow down. */
    private static final double BRAKING_DISTANCE_RAD = AngleUnit.DEGREES.toRadians(60);
    /** Distance at which the robot will consider the target reached. */
    private static final double TOLERANCE_DISTANCE_MM = 30.0;
    /** Distance at which the robot will consider the target reached. */
    private static final double TOLERANCE_DISTANCE_RAD = AngleUnit.DEGREES.toRadians(5);

    protected abstract void runAuto();

    @Override
    public void runOpMode() {
        initRobotController();

        waitForStart();
        if (opModeIsActive()) runAuto();
        stopMotors();
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

    /**
     * Set the target X and Y position, relative to the initial position.
     */
    protected void setTargetPosition(double forward, double right, DistanceUnit units) {
        targetX = units.toMm(forward);
        targetY = units.toMm(right);
    }

    /**
     * Set the target angle, relative to the initial position.
     */
    protected void setTargetHeading(double heading, AngleUnit units) {
        targetHeading = units.toRadians(heading);
    }

    /**
     * Move using the pinpoint sensors to the target position
     */
    protected void moveToTarget() {
        // These are initialized to infinity to ensure the loops runs at least once.
        double distancePositional = Double.POSITIVE_INFINITY;
        double distanceHeading = Double.POSITIVE_INFINITY;

        while (
                // ...we still have distance to move/turn
                !equalWithTolerance(distancePositional, 0, TOLERANCE_DISTANCE_MM)
                || !equalWithTolerance(distanceHeading, 0, TOLERANCE_DISTANCE_RAD)
        ) {
            // Recalculate distance & readjust motors

            pinpoint.update();
            // BAND-AID FIX: pinpoint is currently mounted backwards
            double currentX = -pinpoint.getPosX(DistanceUnit.MM);
            double currentY = pinpoint.getPosY(DistanceUnit.MM);
            double distanceX = targetX - currentX;
            double distanceY = targetY - currentY;

            telemetry.addData("target X" , targetX);
            telemetry.addData("current X" , currentX);
            telemetry.addData("target Y" , targetY);
            telemetry.addData("current Y" , currentY);


            // This is the diagonal distance to the target.
            distancePositional = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            telemetry.addData("total distance", distancePositional);

            double falloff = falloffMm(distancePositional); // Falloff lets us break before we reach the target
            // We scale the distances down so they don't exceed 100% motor power.
            double maxComponent = Math.max(Math.abs(distanceX), Math.abs(distanceY));
            double normalizedForward = distanceX / maxComponent * falloff;
            double normalizedRight = distanceY / maxComponent * falloff;
            telemetry.addData("normalized forward", normalizedForward);
            telemetry.addData("normalized right", normalizedRight);

            double currentHeading = pinpoint.getHeading(AngleUnit.RADIANS);
            // We use `normalizeRadians` to take the shortest path to the target.
            distanceHeading = AngleUnit.normalizeRadians(targetHeading - currentHeading);
            double turningPower = falloffRad(distanceHeading) * Math.signum(distanceHeading);

            driveFieldRelative(normalizedForward, normalizedRight, turningPower); // Falloff lets us break before we reach the target
            telemetry.update();

            idle();
        }

        sleep(1000);

        stopDriveMotors();
        targetX = pinpoint.getPosX(DistanceUnit.MM);
        targetY = pinpoint.getPosY(DistanceUnit.MM);
        targetHeading = pinpoint.getHeading(AngleUnit.RADIANS);
    }

    /**
     * This method handles decreasing the power for motors as we get closer to a target
     * in millimeters. Without it, the robot would only try to brake when it had already
     * reached its target. We want it to start braking before.
     * @param distance The distance to the target, in millimeters.
     * @return The recommended motor power.
     */
    private double falloffMm(double distance) {
        return Math.max(Math.abs(distance) / BRAKING_DISTANCE_MM, 1);
    }

    /**
     * This method handles decreasing the power for motors as we get closer to a target
     * in radians. Without it, the robot would only try to brake when it had already
     * reached its target. We want it to start braking before.
     * @param distance The distance to the target, in radians.
     * @return The recommended motor power.
     */
    private double falloffRad(double distance) {
        return Math.max(Math.abs(distance) / BRAKING_DISTANCE_RAD, 1);
    }
}
