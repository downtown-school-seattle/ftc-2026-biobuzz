package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotController;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;

abstract public class TeleOpController extends RobotController {
    @Override
    public void runOpMode() {
        initRobotController();
        FrontLeftDrive.setPower(1);
        waitForStart();
    }
}