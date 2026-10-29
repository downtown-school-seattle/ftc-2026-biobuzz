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
        driveForSeconds(FORWARD, .1);
        driveForSeconds(RIGHT, .1);
        turnCwForSeconds(0.25);
    }
}
