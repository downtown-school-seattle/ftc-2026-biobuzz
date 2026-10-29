package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.RobotController;

@TeleOp(name = "Measure mode", group = "helper")
public class MeasureMode extends RobotController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }

    @Override
    public void runOpMode() {
        initRobotController();
        telemetry.addLine("Selected measure mode. This mode is meant for testing purposes.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            pinpoint.update();
            Pose2D pose = pinpoint.getPosition();

            telemetry.addData("x pos", pose.getX(DistanceUnit.MM));
            telemetry.addData("y pos", pose.getY(DistanceUnit.MM));
            telemetry.addData("heading (yaw)", pose.getHeading(AngleUnit.RADIANS));
            telemetry.update();
        }
    }
}