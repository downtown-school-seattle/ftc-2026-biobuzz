package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Motor0e;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;

    protected Motor0e shooter;

    protected void initTeleOp() {
        initRobotController();
    }

    protected void loopTeleOp() {}

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        initTeleOp();
        initShooter();

        while (opModeInInit()) {
            addShooterTelemetry();
            telemetry.update();
        }
        if (isStopRequested()) return;

        if (shooter != null) shooter.start();
        boolean leftWas = false;
        boolean rightWas = false;
        while (opModeIsActive()) {
            if (shooter != null) {
                if (gamepad1.left_bumper && !leftWas) shooter.changeRpm(-RPM_STEP);
                if (gamepad1.right_bumper && !rightWas) shooter.changeRpm(RPM_STEP);
            }
            leftWas = gamepad1.left_bumper;
            rightWas = gamepad1.right_bumper;

            if (shooter != null) shooter.update();
            loopTeleOp();
            addShooterTelemetry();
            telemetry.update();
        }
        if (shooter != null) shooter.stop();
    }

    private void initShooter() {
        try {
            shooter = new Motor0e(hardwareMap);
        } catch (IllegalArgumentException e) {
            shooter = null;
        }
    }

    private void addShooterTelemetry() {
        if (shooter == null) {
            telemetry.addData("motor0e", "not found in robot config as \"" + Motor0e.CONFIG_NAME + "\"");
            return;
        }
        shooter.addTelemetry(telemetry);
    }
}
