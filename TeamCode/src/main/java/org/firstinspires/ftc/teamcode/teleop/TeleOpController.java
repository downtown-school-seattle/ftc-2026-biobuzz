package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.MotorUtil;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double INTAKE_RPM = 3;

    protected boolean intakeReversed = false;
    protected double lastIntakeTarget = 0;

    protected void loopTeleOp() {
        if (gamepad1.rightBumperWasPressed()) intakeReversed = !intakeReversed;

        double intakeTarget = 0;
        if (gamepad1.right_trigger_pressed) intakeTarget = intakeReversed ? -INTAKE_RPM : INTAKE_RPM;
        if (gamepad1.rightTriggerWasReleased()) setIntakeRpm(0);
        else setIntakeRpm(intakeTarget);
        lastIntakeTarget = intakeTarget;

        setShooterRpm(shooterTargetRpm);

        drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        telemetry.addData("shooter target", Math.round(shooterTargetRpm));
        telemetry.addData("shooter rpm", Math.round(getShooterRpm()));
        telemetry.addData("intake target", lastIntakeTarget);
        telemetry.addData("intake rpm", getIntakeRpm());
        telemetry.addData("right trigger", gamepad1.right_trigger);
        telemetry.update();
    }

    @Override
    public void runOpMode() {
        initRobotController();

        waitForStart();
        if (isStopRequested()) return;

        MotorUtil.stop(intakeMotor);
        while (opModeIsActive()) {
            loopTeleOp();
            idle();
        }

        MotorUtil.stop(shooterMotor);
        MotorUtil.stop(intakeMotor);
    }
}
