package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Blue TeleOp", group="Robot")
public class BlueTeleMode extends TeleController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }
}
