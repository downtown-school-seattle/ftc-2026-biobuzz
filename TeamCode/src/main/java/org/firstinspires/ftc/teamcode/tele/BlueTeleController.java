package org.firstinspires.ftc.teamcode.tele;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Blue TeleOp", group="Robot")
public class BlueTeleController extends TeleController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }
}
