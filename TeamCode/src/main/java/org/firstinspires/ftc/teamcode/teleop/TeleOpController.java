package org.firstinspires.ftc.teamcode.teleop;

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
        double intakeTarget = 0;
        if (gamepad1.right_bumper) intakeTarget = INTAKE_RPM;
        else if (gamepad1.left_bumper) intakeTarget = -INTAKE_RPM;

        if (intakeTarget == 0) intakeMotor.setPower(0);
        else setIntakeRpm(intakeTarget);

        if (driveMode == DriveMode.FIELD_RELATIVE) {
            driveFieldRelative(-gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1.right_stick_x);
        } else if (driveMode == DriveMode.ROBOT_RELATIVE) {
            drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);
        }
      
        setShooterRpm(shooterTargetRpm);
        telemetry.addData("shooter target", Math.round(shooterTargetRpm));
        telemetry.addData("shooter rpm", Math.round(getShooterRpm()));
        telemetry.addData("intake target", intakeTarget);
        telemetry.addData("intake rpm", getIntakeRpm());
        telemetry.update();
    }

    @Override
    public void runOpMode() {
        initRobotController();

        while (!isStarted()) {
            if (gamepad1.left_bumper && gamepad1.right_bumper) {
                driveMode = DriveMode.NO_OP;
            } else if (gamepad1.left_bumper) {
                driveMode = DriveMode.ROBOT_RELATIVE;
            } else if (gamepad1.right_bumper) {
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

        stopMotors();
    }
}
