package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double INTAKE_RPM = 2.5;
    public double leftY;
    public double leftX;
    public double rightX;

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

        if(gamepad1.left_stick_y > 0.2 || gamepad1.left_stick_y < -0.2){
            leftY = gamepad1.left_stick_y;
        }
        else {
            leftY = 0;
        }
        if(gamepad1.left_stick_x > 0.2 || gamepad1.left_stick_x < -0.2){
            leftX = gamepad1.left_stick_x;
        }
        else {
            leftX = 0;
        }
        if(gamepad1.right_stick_x > 0.2 || gamepad1.right_stick_x < -0.2){
            rightX = gamepad1.right_stick_x;
        }
        else {
            rightX = 0;
        }
        if(gamepad1.left_trigger_pressed){
            rightX /= 2;
            leftY /= 2;
            leftX /= 2;
        }

        if (driveMode == DriveMode.FIELD_RELATIVE) {
            driveFieldRelative(-leftY, -leftX, rightX);
        } else if (driveMode == DriveMode.ROBOT_RELATIVE) {
            drive(-leftY, leftX, rightX);
        }

        if(gamepad1.a && driveMode == DriveMode.FIELD_RELATIVE){
            pinpoint.resetPosAndIMU();
            telemetry.addLine("reset field relative");
        }

        if(gamepad1.dpad_down){
            driveMode = DriveMode.ROBOT_RELATIVE;
        }else if(gamepad1.dpad_up){
            driveMode = DriveMode.FIELD_RELATIVE;
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
        telemetry.addData("current drive mode", driveMode);
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
