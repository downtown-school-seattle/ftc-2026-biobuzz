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
        // TODO: blue op mode
        shooterGateOpen(true);
        waitMs(4000);
        shooterGateOpen(false);
        waitMs(4000);
    }
}
