package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red Auto", group="Robot")
public class RedAutoMode extends AutoController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Red;
    }

    /** Mirror of the Blue test path (left/right and turns swapped). */
    @Override
    protected void runAuto() {
        drive(FORWARD, 24);
        drive(LEFT, 24);
        drive(BACK, 24);
        drive(RIGHT, 24);
        turn(-90);
        turn(90);
    }
}
