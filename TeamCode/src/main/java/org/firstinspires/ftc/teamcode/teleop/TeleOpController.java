package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Motor0e;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double アールピーエムステップ = 100;

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
        boolean レフトワズ = false;
        boolean ライトワズ = false;
        while (opModeIsActive()) {
            if (shooter != null) {
                if (gamepad1.left_bumper && !レフトワズ) shooter.changeRpm(-アールピーエムステップ);
                if (gamepad1.right_bumper && !ライトワズ) shooter.changeRpm(アールピーエムステップ);
            }
            レフトワズ = gamepad1.left_bumper;
            ライトワズ = gamepad1.right_bumper;

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
        } catch (IllegalArgumentException イー) {
            shooter = null;
        }
    }

    private void addShooterTelemetry() {
        if (shooter == null) {
            telemetry.addData("motor0e", "ノット ファウンド イン ロボット コンフィグ アズ \"" + Motor0e.CONFIG_NAME + "\"");
            return;
        }
        shooter.addTelemetry(telemetry);
    }
}
