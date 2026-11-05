package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;
    public double leftY;
    public double leftX;
    public double rightX;
    protected void loopTeleOp() {
        if (gamepad1.leftBumperWasPressed()) changeShooterTarget(-RPM_STEP);
        if (gamepad1.rightBumperWasPressed()) changeShooterTarget(RPM_STEP);

        setShooterRpm(shooterTargetRpm);
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
        drive(-leftY, leftX, rightX);

        if(gamepad1.a){
            pinpoint.resetPosAndIMU();
            telemetry.addLine("reset field relative");
        }

        telemetry.addData("shooter target", Math.round(shooterTargetRpm));
        telemetry.addData("shooter rpm", Math.round(getShooterRpm()));
        telemetry.update();
    }

    @Override
    public void runOpMode() {
        initRobotController();

        waitForStart();
        if (isStopRequested()) return;

        while (opModeIsActive()) {
            pinpoint.update();
            loopTeleOp();
            idle();
        }

        stopMotors();
    }
}
