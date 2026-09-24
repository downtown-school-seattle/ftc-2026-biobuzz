package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotController;

@TeleOp
abstract public class TeleOpController extends RobotController {
    @Override
    public void runOpMode() {
        initRobotController();
    }
}