package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Motor0e;
import org.firstinspires.ftc.teamcode.RobotController;

import java.util.LinkedHashMap;
import java.util.Map;

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

    protected static final String motor0e = "motor0e";
    protected static final String motor1e = "motor1e";
    protected static final String motor2e = "motor2e";
    protected static final String motor3e = "motor3e";

    private final Map<String, Motor0e> expansionMotors = new LinkedHashMap<>();

    abstract public AllianceColor getAllianceColor();

    protected void runAuto() {}

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        initRobotController();
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        for (DcMotor m : motors()) m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
        if (opModeIsActive()) runAuto();
        for (Motor0e m : expansionMotors.values()) m.stop();
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
        return new MoveCommand(name, forward + right, forward + right, right - forward, right - forward);
    }

    protected AutoCommand turnCmd(double degrees) {
        double t = degrees * ticksPerDegree;
        return new MoveCommand("turn " + degrees + " deg", -t, t, -t, t);
    }

    protected AutoCommand motorCmd(String configName, double rpm) {
        return new MotorCommand(configName, rpm);
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
            for (Motor0e m : expansionMotors.values()) {
                m.update();
                m.addTelemetry(telemetry);
            }
            telemetry.update();
            if (allDone) break;
            idle();
        }
        for (AutoCommand c : commands) c.end();
    }

    private Motor0e expansionMotor(String configName) {
        Motor0e m = expansionMotors.get(configName);
        if (m == null) {
            m = new Motor0e(hardwareMap, configName);
            expansionMotors.put(configName, m);
        }
        return m;
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
            DcMotor[] motors = motors();
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
            addWheelTelemetry(motors());
        }

        @Override
        public boolean isDone() {
            return timedOut || reachedTargets(motors());
        }

        @Override
        public void end() {
            for (DcMotor m : motors()) {
                m.setPower(0);
                m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            RobotLog.ii(TAG, "finished: " + name);
        }
    }

    private class MotorCommand implements AutoCommand {
        private final String configName;
        private final double rpm;

        MotorCommand(String configName, double rpm) {
            this.configName = configName;
            this.rpm = rpm;
        }

        @Override
        public void start() {
            try {
                expansionMotor(configName).setRpm(rpm);
                RobotLog.ii(TAG, "motor " + configName + " set to " + rpm + " rpm");
            } catch (IllegalArgumentException e) {
                RobotLog.ee(TAG, "motor not found in config: " + configName);
            }
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

    private DcMotor[] motors() {
        return new DcMotor[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};
    }
}
