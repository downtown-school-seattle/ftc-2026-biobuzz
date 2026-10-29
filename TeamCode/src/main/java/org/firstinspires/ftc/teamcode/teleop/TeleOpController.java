package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.MotorUtil;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;

    protected void loopTeleOp() {
        if (gamepad1.leftBumperWasPressed()) changeShooterTarget(-RPM_STEP);
        if (gamepad1.rightBumperWasPressed()) changeShooterTarget(RPM_STEP);

        setShooterRpm(shooterTargetRpm);
        drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
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

        MotorUtil.stop(shooterMotor);
        drive(0, 0, 0);
    }
}
