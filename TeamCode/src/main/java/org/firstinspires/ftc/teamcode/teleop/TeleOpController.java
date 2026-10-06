package org.firstinspires.ftc.teamcode.teleop;



import org.firstinspires.ftc.teamcode.RobotController;



abstract public class TeleOpController extends RobotController {
    @Override
    public void runOpMode() {
        initRobotController();
        waitForStart();
        while (!isStopRequested()) loopIteration();
    }

    private void loopIteration() {
        drive(gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);

    }



}