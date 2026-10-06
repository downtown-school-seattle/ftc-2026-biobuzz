package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue Auto", group="Robot")
public class BlueAutoMode extends AutoController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }

    /** Blue is the testing environment for now: drive each direction, then turn both ways. */
    @Override
    protected void runAuto() {
        drive(FORWARD, 24);
        drive(RIGHT, 24);
        drive(BACK, 24);
        drive(LEFT, 24);
        turn(90);
        turn(-90);
    }
}
