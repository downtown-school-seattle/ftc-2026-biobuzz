package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Blue TeleOp", group="Robot")
public class BlueTeleOp extends TeleOpMode {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }
}
