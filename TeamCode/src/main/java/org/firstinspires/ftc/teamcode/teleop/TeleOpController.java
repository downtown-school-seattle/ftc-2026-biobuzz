package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.NectarShooter;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double RPM_STEP = 100;

    protected NectarShooter shooter;

    protected void loopTeleOp() {
        drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);
    }

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        initRobotController();
        waitForStart();
        while (!isStopRequested()) loopIteration();
    }

    private void loopIteration() {
        drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);

    }
}
