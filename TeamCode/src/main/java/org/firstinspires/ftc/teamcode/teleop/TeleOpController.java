package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Motor5;
import org.firstinspires.ftc.teamcode.RobotController;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;

abstract public class TeleOpController extends RobotController {
    @Override
    public void runOpMode() {
        initRobotController();
        frontLeftDrive.setPower(1);

        Motor5 motor5 = null;
        try {
            motor5 = new Motor5(hardwareMap);
        } catch (IllegalArgumentException e) {
            telemetry.addData("Motor5", "not found in config as \"" + Motor5.CONFIG_NAME + "\"");
        }

        // INIT: adjust motor 5's power with gamepad1 dpad up/down (+/- 0.05) before pressing start.
        double motor5Power = 0.5;
        boolean upWas = false, downWas = false;
        while (!isStarted() && !isStopRequested()) {
            if (gamepad1.dpad_up && !upWas) motor5Power += 0.05;
            if (gamepad1.dpad_down && !downWas) motor5Power -= 0.05;
            upWas = gamepad1.dpad_up;
            downWas = gamepad1.dpad_down;
            motor5Power = Math.max(-1, Math.min(1, motor5Power));

            telemetry.addData("Motor5 power (dpad up/down to change)", "%.2f", motor5Power);
            telemetry.addLine("Press START to run it");
            telemetry.update();
        }
        if (isStopRequested()) return;

        // RUN: motor 5 runs at the chosen power, shown live until the op mode stops.
        if (motor5 != null) motor5.setPower(motor5Power);
        while (opModeIsActive()) {
            telemetry.addData("Motor5 power", "%.2f", motor5 == null ? 0 : motor5.getPower());
            telemetry.addData("Motor5 position", motor5 == null ? "n/a" : motor5.getPosition());
            telemetry.update();
        }
        if (motor5 != null) motor5.stop();
    }
}