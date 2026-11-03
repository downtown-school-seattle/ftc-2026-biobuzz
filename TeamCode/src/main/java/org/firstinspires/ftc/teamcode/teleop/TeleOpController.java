package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.NectarShooter;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;

    protected NectarShooter shooter;

    public enum DriveMode{
        ROBOT_RELATIVE,
        FIELD_RELATIVE,
        NO_OP;
    }
    public DriveMode driveMode = DriveMode.FIELD_RELATIVE;

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
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
        while (!isStopRequested()) {
            pinpoint.update();
            loopIteration();
        }
    }

    private void loopIteration() {
        if(driveMode == DriveMode.FIELD_RELATIVE){
            driveFieldRelative(-gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1.right_stick_x);
        }else if(driveMode == DriveMode.ROBOT_RELATIVE){
            drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);
        }
    }
}
