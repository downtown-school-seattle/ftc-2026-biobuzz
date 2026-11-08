package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double INTAKE_RPM = 2.5;

    public enum DriveMode {
        FIELD_RELATIVE,
        ROBOT_RELATIVE,
        NO_OP
    }
    public DriveMode driveMode = DriveMode.ROBOT_RELATIVE;

    protected void loopTeleOp() {
        if (gamepad1.right_bumper || gamepad1.left_bumper) {
            if (gamepad1.left_bumper) intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            else intakeMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            setIntakeRpm(INTAKE_RPM);
        } else intakeMotor.setPower(0);

        if (driveMode == DriveMode.FIELD_RELATIVE) {
            driveFieldRelative(-gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1.right_stick_x);
        } else if (driveMode == DriveMode.ROBOT_RELATIVE) {
            drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        }

        if (gamepad1.dpad_right) {
            if (equalWithTolerance(shooterTargetRpm, 0, 10)) {
                shooterTargetRpm = SHOOTER_INITIAL_RPM;
            } else {
                shooterTargetRpm = 0;
            }
        }
        setShooterRpm(shooterTargetRpm);

        telemetry.addData("shooter target", Math.round(shooterTargetRpm));
        telemetry.addData("shooter rpm", Math.round(getShooterRpm()));
        telemetry.addData("intake target", INTAKE_RPM);
        telemetry.addData("intake rpm", getIntakeRpm());
        telemetry.update();
    }

    @Override
    public void runOpMode() {
        initRobotController();

        while (!isStarted()) {
            if (gamepad1.dpad_left) {
                driveMode = DriveMode.NO_OP;
            } else if (gamepad1.dpad_up) {
                driveMode = DriveMode.ROBOT_RELATIVE;
            } else if (gamepad1.dpad_down) {
                driveMode = DriveMode.FIELD_RELATIVE;
            }
            telemetry.addLine("drive mode: " + driveMode);
            telemetry.update();
        }
        if (isStopRequested()) return;

        while (opModeIsActive()) {
            if (pinpoint != null) pinpoint.update();
            loopTeleOp();
            idle();
        }

        turnOff();
    }
}
