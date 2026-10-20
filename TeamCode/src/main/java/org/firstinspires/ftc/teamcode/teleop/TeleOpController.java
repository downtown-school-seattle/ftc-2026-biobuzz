package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.NectarShooter;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;

    protected NectarShooter shooter;

    protected void initTeleOp() {
        initRobotController();
    }

    protected void loopTeleOp() {
        drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);
    }

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        initTeleOp();
        shooter = new NectarShooter(nectarShooter);

        while (opModeInInit()) {
            shooter.addTelemetry(telemetry);
            telemetry.update();
        }
        if (isStopRequested()) return;

        shooter.start();
        boolean leftWas = false;
        boolean rightWas = false;
        while (opModeIsActive()) {
            if (gamepad1.left_bumper && !leftWas) shooter.changeRpm(-RPM_STEP);
            if (gamepad1.right_bumper && !rightWas) shooter.changeRpm(RPM_STEP);
            leftWas = gamepad1.left_bumper;
            rightWas = gamepad1.right_bumper;

            shooter.update();
            loopTeleOp();
            shooter.addTelemetry(telemetry);
            telemetry.update();
        }
        shooter.stop();
    }
}
