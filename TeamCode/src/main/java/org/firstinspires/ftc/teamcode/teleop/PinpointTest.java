package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotController;

@TeleOp(name="Pinpoint Test", group="Test")
public class PinpointTest extends RobotController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Red;
    }

    @Override
    public void runOpMode() {
        initRobotController();

        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.a) {
                // because I'm nice you can hold a to reset position
                odometry.setPose(0, 0, 0);
            }
            odometry.update();

            odometry.addTelemetry(telemetry);
            telemetry.update();
        }
    }
}
