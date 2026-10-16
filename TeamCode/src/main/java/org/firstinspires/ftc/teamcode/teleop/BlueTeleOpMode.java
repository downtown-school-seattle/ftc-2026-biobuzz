package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Blue TeleOp", group="Robot")
public class BlueTeleOpMode extends TeleOpController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }
}
