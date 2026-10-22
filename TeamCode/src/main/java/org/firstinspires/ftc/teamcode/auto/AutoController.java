package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.NectarShooter;
import org.firstinspires.ftc.teamcode.RobotController;

/*
there are 3 commands that work in autonomous as well as a way to group them, they are: drive(), turn(), motor()
there is also run() that allows you to run multiple commands at a time however, they need to be written as "[command]Cmd"

examples:
drive(90 [degrees], 12 [inches])
turn(45 [degrees])
motor(nectarShooter [motor name], 4200 [rpm])

run(turnCmd(30), motorCmd(motor0e, 4000)) currently running a drive and turn command at the same time doesn't work but I am working on that right now
 */

abstract public class AutoController extends RobotController {
    private static final String TAG = "AutoController";
    private static final double MIN_MOVING_RPM = 5;
    private static final String[] WHEELS = {"fl", "fr", "bl", "br"};

    public static final double FORWARD = 0;
    public static final double RIGHT = 90;
    public static final double BACK = 180;
    public static final double LEFT = 270;

    protected double ticksPerInch = 40;
    protected double ticksPerDegree = 10;
    protected double wheelTicksPerRev = 537.7;
    protected double power = 0.5;
    protected int tolerance = 15;
    protected double moveTimeoutSeconds = 5;

    private static final double TELEMETRY_INTERVAL_MS = 50;

    private NectarShooter shooter;
    private DcMotor[] wheels;
    private final ElapsedTime telemetryTimer = new ElapsedTime();
    private boolean telemetryDue;

    abstract public AllianceColor getAllianceColor();

    protected void runAuto() {}

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        initRobotController();
        shooter = new NectarShooter(nectarShooter);
        wheels = new DcMotor[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};
        for (DcMotor m : wheels) m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
        if (opModeIsActive()) runAuto();
        shooter.stop();
    }

    protected void drive(double degrees, double inches) {
        run(driveCmd(degrees, inches));
    }

    protected void turn(double degrees) {
        run(turnCmd(degrees));
    }

    protected AutoCommand driveCmd(double degrees, double inches) {
        double radians = Math.toRadians(degrees);
        double forward = Math.cos(radians) * inches * ticksPerInch;
        double right = Math.sin(radians) * inches * ticksPerInch;
        String name = "drive " + degrees + " deg " + inches + " in";
        return new MoveCommand(name, wheelTargets(forward, right, 0));
    }

    protected AutoCommand turnCmd(double degrees) {
        return new MoveCommand("turn " + degrees + " deg", wheelTargets(0, 0, degrees * ticksPerDegree));
    }

    private static double[] wheelTargets(double forward, double right, double turn) {
        return new double[]{forward + right - turn, forward + right + turn, right - forward - turn, right - forward + turn};
    }

    protected AutoCommand motorCmd(DcMotorEx motor, double rpm) {
        return new MotorCommand(motor, rpm);
    }

    protected void run(AutoCommand... commands) {
        int moves = 0;
        for (AutoCommand c : commands) if (c instanceof MoveCommand) moves++;
        if (moves > 1) throw new IllegalArgumentException("only one turn command can run in a group.");

        for (AutoCommand c : commands) c.start();
        while (opModeIsActive()) {
            boolean allDone = true;
            for (AutoCommand c : commands) {
                if (c.isDone()) continue;
                c.update();
                allDone = false;
            }
            shooter.update();
            telemetryDue = telemetryTimer.milliseconds() >= TELEMETRY_INTERVAL_MS;
            if (telemetryDue) {
                telemetryTimer.reset();
                shooter.addTelemetry(telemetry);
                telemetry.update();
            }
            if (allDone) break;
            idle();
        }
        for (AutoCommand c : commands) c.end();
    }

    private class MoveCommand implements AutoCommand {
        private final String name;
        private final double[] targets;
        private final ElapsedTime timer = new ElapsedTime();
        private boolean timedOut = false;

        MoveCommand(String name, double... targets) {
            this.name = name;
            this.targets = targets;
        }

        @Override
        public void start() {
            RobotLog.ii(TAG, "starting: " + name);
            DcMotor[] motors = wheels;
            double farthest = 0;
            for (double t : targets) farthest = Math.max(farthest, Math.abs(t));
            for (int i = 0; i < motors.length; i++) {
                motors[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                motors[i].setTargetPosition((int) Math.round(targets[i]));

                motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
                motors[i].setPower(farthest == 0 ? 0 : power * Math.abs(targets[i]) / farthest);
            }
            timer.reset();
        }

        @Override
        public void update() {
            if (timer.seconds() > moveTimeoutSeconds) {
                timedOut = true;
                RobotLog.ee(TAG, "timed out: " + name);
                return;
            }
            if (telemetryDue) addWheelTelemetry(wheels);
        }

        @Override
        public boolean isDone() {
            return timedOut || reachedTargets(wheels);
        }

        @Override
        public void end() {
            for (DcMotor m : wheels) {
                m.setPower(0);
                m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            RobotLog.ii(TAG, "finished: " + name);
        }
    }

    private class MotorCommand implements AutoCommand {
        private final DcMotorEx motor;
        private final double rpm;

        MotorCommand(DcMotorEx motor, double rpm) {
            if (motor != nectarShooter) throw new IllegalArgumentException("motorCmd only supports nectarShooter");
            this.motor = motor;
            this.rpm = rpm;
        }

        @Override
        public void start() {
            shooter.setRpm(rpm);
            RobotLog.ii(TAG, "nectar shooter set to " + rpm + " rpm");
        }

        @Override
        public void update() {}

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public void end() {}
    }

    private void addWheelTelemetry(DcMotor[] motors) {
        double[] rpm = new double[motors.length];
        for (int i = 0; i < motors.length; i++) {
            rpm[i] = ((DcMotorEx) motors[i]).getVelocity() / wheelTicksPerRev * 60;
            telemetry.addData(WHEELS[i], "%.0f rpm", rpm[i]);
        }

        double forward = (rpm[0] + rpm[1] - rpm[2] - rpm[3]) / 4;
        double right = (rpm[0] + rpm[1] + rpm[2] + rpm[3]) / 4;
        if (Math.hypot(forward, right) < MIN_MOVING_RPM) {
            telemetry.addData("direction", "-");
        } else {
            double degrees = (Math.toDegrees(Math.atan2(right, forward)) + 360) % 360;
            telemetry.addData("direction", "%.0f deg (0 forward, 90 right)", degrees);
        }
    }

    private boolean reachedTargets(DcMotor[] motors) {
        for (DcMotor m : motors) {
            if (Math.abs(m.getTargetPosition() - m.getCurrentPosition()) > tolerance) return false;
        }
        return true;
    }
}
