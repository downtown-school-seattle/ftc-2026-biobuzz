package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="tele", group="Robot")
public class TeleOpMode extends TeleOpController {
    @Override
    protected void loopTeleOp() {
        drive(-gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_stick_x);
    }
}
