package org.firstinspires.ftc.teamcode.tele;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Red TeleOp", group="Robot")
public class RedTeleController extends TeleController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Red;
    }
}
