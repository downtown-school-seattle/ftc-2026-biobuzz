package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red Auto", group="Robot")
public class RedAutoMode extends AutoController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Red;
    }

    @Override
    protected void runAuto() {
        drive(FORWARD, 1);
        drive(LEFT, 1);
        drive(BACK, 1);
        drive(RIGHT, 1);
        turn(-0.5);
        turn(0.5);
    }
}
