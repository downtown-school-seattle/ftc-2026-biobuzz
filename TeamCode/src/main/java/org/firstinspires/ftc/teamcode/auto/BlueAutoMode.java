package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue Auto", group="Robot")
public class BlueAutoMode extends AutoController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }

    @Override
    protected void runAuto() {
        run(driveCmd(90, 24));
        run(driveCmd(270, 24));
    }
}
