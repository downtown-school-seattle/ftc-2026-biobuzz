package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.MotorUtil;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;

    protected void loopTeleOp() {
        if (gamepad1.leftBumperWasPressed()) changeShooterTarget(-RPM_STEP);
        if (gamepad1.rightBumperWasPressed()) changeShooterTarget(RPM_STEP);

        if(driveMode == DriveMode.FIELD_RELATIVE){
            driveFieldRelative(-gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1.right_stick_x);
        }else if(driveMode == DriveMode.ROBOT_RELATIVE){
            drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);
        }
      
        setShooterRpm(shooterTargetRpm);
        telemetry.addData("shooter target", Math.round(shooterTargetRpm));
        telemetry.addData("shooter rpm", Math.round(getShooterRpm()));
        telemetry.update();
    }
    public DriveMode driveMode = DriveMode.FIELD_RELATIVE;

    @Override
    public void runOpMode() {
        initRobotController();

        while (!isStarted()){
            if(gamepad1.left_bumper && gamepad1.right_bumper){
                driveMode = DriveMode.NO_OP;
            }else if(gamepad1.left_bumper){
                driveMode = DriveMode.ROBOT_RELATIVE;
            }else if(gamepad1.right_bumper){
                driveMode = DriveMode.FIELD_RELATIVE;
            }
            telemetry.addLine("drive mode: " + driveMode);
            telemetry.update();
        }
        if (isStopRequested()) return;

        while (opModeIsActive()) {
            loopTeleOp();
            idle();
        }

        MotorUtil.stop(shooterMotor);
        drive(0, 0, 0);
    }
}
