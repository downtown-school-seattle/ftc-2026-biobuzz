package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Red TeleOp", group="Robot")
public class RedTeleOp extends TeleOpMode {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Red;
    }
}
