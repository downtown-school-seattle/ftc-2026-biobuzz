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

    // ---- Running commands -------------------------------------------------------------------
    // drive(...) / turn(...) run one command and wait for it. To do several things at once, pass
    // them all to run(...):   run(driveCmd(FORWARD, 24), motor5Cmd(0.5, 2));
    // Only one drivetrain command (driveCmd/turnCmd) is allowed per group; they'd fight over the wheels.

    /** Drive in a direction for a distance in inches, then wait. */
    protected void drive(Direction direction, double inches) { run(driveCmd(direction, inches)); }

    /** Turn in place by degrees, then wait. */
    protected void turn(double degrees) { run(turnCmd(degrees)); }

    /** Run all the given commands simultaneously; returns when every one is done. */
    protected void run(AutoCommand... commands) {
        int driveCommands = 0;
        for (AutoCommand c : commands) if (c instanceof MoveCommand) driveCommands++;
        if (driveCommands > 1) {
            throw new IllegalArgumentException("Only one drive/turn command can run in a group.");
        }

        for (AutoCommand c : commands) c.start();
        while (opModeIsActive()) {
            boolean allDone = true;
            for (AutoCommand c : commands) {
                if (c.isDone()) continue;
                c.update();
                allDone = false;
            }
            telemetry.update();
            if (allDone) break;
            idle();
        }
        for (AutoCommand c : commands) c.end();
    }

    /** Command: drive in a direction for a distance in inches. */
    protected AutoCommand driveCmd(Direction direction, double inches) {
        double ticks = inches * ticksPerInch;
        String name = "Drive " + direction + " " + inches + " in";
        switch (direction) {
            case FORWARD: return new MoveCommand(name, ticks, ticks, -ticks, -ticks);
            case BACK:    return new MoveCommand(name, -ticks, -ticks, ticks, ticks);
            case RIGHT:   return new MoveCommand(name, ticks, ticks, ticks, ticks);
            default:      return new MoveCommand(name, -ticks, -ticks, -ticks, -ticks); // LEFT
        }
    }

    /** Command: turn in place by degrees. */
    protected AutoCommand turnCmd(double degrees) {
        double t = degrees * ticksPerDegree;
        return new MoveCommand("Turn " + degrees + " deg", -t, t, -t, t);
    }

    /** Command: run motor 5 at a power (-1..1) for some seconds, then stop it. */
    protected AutoCommand motor5Cmd(double motorPower, double seconds) {
        return timedCmd("Motor5 " + motorPower + " for " + seconds + " s", seconds,
                () -> motor5().setPower(motorPower),
                () -> motor5().stop());
    }

    /** Command: run onStart, wait some seconds, then run onEnd. Handy for custom mechanisms. */
    protected AutoCommand timedCmd(String name, double seconds, Runnable onStart, Runnable onEnd) {
        return new TimedCommand(name, seconds, onStart, onEnd);
    }

    private Motor5 motor5;

    /** Motor 5 (expansion hub), created the first time it's needed. */
    protected Motor5 motor5() {
        if (motor5 == null) motor5 = new Motor5(hardwareMap);
        return motor5;
    }

    /** Drivetrain move: each wheel (fl, fr, bl, br) goes to its own encoder target. */
    private class MoveCommand implements AutoCommand {
        private final String name;
        private final double[] deltas;
        private final ElapsedTime timer = new ElapsedTime();
        private boolean timedOut = false;

        MoveCommand(String name, double fl, double fr, double bl, double br) {
            this.name = name;
            this.deltas = new double[]{fl, fr, bl, br};
        }

        @Override public void start() {
            RobotLog.ii("AutoController", "Starting: " + name);
            DcMotor[] motors = motors();
            for (int i = 0; i < 4; i++) {
                motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                motors[i].setTargetPosition((int) Math.round(deltas[i]));
                motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
                motors[i].setPower(power);
            }
            timer.reset();
        }

        @Override public void update() {
            if (timer.seconds() > moveTimeoutSeconds) {
                timedOut = true;
                RobotLog.ee("AutoController", "Timed out (target never reached): " + name);
                return;
            }
            DcMotor[] motors = motors();
            telemetry.addData("Doing", name);
            telemetry.addData(name + " fl/fr", motors[0].getCurrentPosition() + "/" + motors[0].getTargetPosition()
                    + "  " + motors[1].getCurrentPosition() + "/" + motors[1].getTargetPosition());
            telemetry.addData(name + " bl/br", motors[2].getCurrentPosition() + "/" + motors[2].getTargetPosition()
                    + "  " + motors[3].getCurrentPosition() + "/" + motors[3].getTargetPosition());
        }

        @Override public boolean isDone() { return timedOut || reachedTargets(motors()); }

        @Override public void end() {
            // Destination reached: cut power and drop the held target so the next move starts clean.
            for (DcMotor m : motors()) {
                m.setPower(0);
                m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            RobotLog.ii("AutoController", "Finished: " + name);
        }
    }

    private class TimedCommand implements AutoCommand {
        private final String name;
        private final double seconds;
        private final Runnable onStart, onEnd;
        private final ElapsedTime timer = new ElapsedTime();

        TimedCommand(String name, double seconds, Runnable onStart, Runnable onEnd) {
            this.name = name;
            this.seconds = seconds;
            this.onStart = onStart;
            this.onEnd = onEnd;
        }

        @Override public void start() {
            RobotLog.ii("AutoController", "Starting: " + name);
            timer.reset();
            onStart.run();
        }

        @Override public void update() {
            telemetry.addData("Doing", name);
            telemetry.addData(name, "%.2f / %.2f s", timer.seconds(), seconds);
        }

        @Override public boolean isDone() { return timer.seconds() >= seconds; }

        @Override public void end() {
            onEnd.run();
            RobotLog.ii("AutoController", "Finished: " + name);
        }
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
