package org.firstinspires.ftc.teamcode.teleop;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.MotorUtil;
import org.firstinspires.ftc.teamcode.RobotController;

abstract public class TeleOpController extends RobotController {
    private static final double INTAKE_RPM = 2.5;

    protected double lastIntakeTarget = 0;

    protected void loopTeleOp() {
        double intakeTarget = 0;
        if (gamepad1.right_bumper) intakeTarget = INTAKE_RPM;
        else if (gamepad1.left_bumper) intakeTarget = -INTAKE_RPM;

        if (intakeTarget == 0) MotorUtil.stop(intakeMotor);
        else setIntakeRpm(intakeTarget);
        lastIntakeTarget = intakeTarget;

        setShooterRpm(shooterTargetRpm);

        drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

        telemetry.addData("shooter target", Math.round(shooterTargetRpm));
        telemetry.addData("shooter rpm", Math.round(getShooterRpm()));
        telemetry.addData("intake target", lastIntakeTarget);
        telemetry.addData("intake rpm", getIntakeRpm());
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
