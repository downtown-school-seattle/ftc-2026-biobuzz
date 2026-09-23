package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Red TeleOp", group="Robot")
public class RedTeleMode extends TeleController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Red;
    }
}
