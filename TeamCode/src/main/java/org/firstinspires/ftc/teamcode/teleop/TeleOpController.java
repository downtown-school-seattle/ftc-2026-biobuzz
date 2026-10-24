package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.MotorUtil;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;

    protected void loopTeleOp() {
        drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);
    }

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        initRobotController();

        waitForStart();
        if (isStopRequested()) return;

        boolean leftWas = false;
        boolean rightWas = false;
        while (opModeIsActive()) {
            if (gamepad1.left_bumper && !leftWas) changeShooterTarget(-RPM_STEP);
            if (gamepad1.right_bumper && !rightWas) changeShooterTarget(RPM_STEP);
            leftWas = gamepad1.left_bumper;
            rightWas = gamepad1.right_bumper;

            setShooterRpm(shooterTargetRpm);
            loopTeleOp();

            telemetry.addData("shooter target", Math.round(shooterTargetRpm));
            telemetry.addData("shooter rpm", Math.round(getShooterRpm()));
            telemetry.update();
            idle();
        }
        MotorUtil.stop(shooterMotor);
    }
}
