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
        drive(FORWARD, 24);
        drive(RIGHT, 24);
        drive(BACK, 24);
        drive(LEFT, 24);
        turn(180);
        turn(-90);
        // Simultaneous example: drive while motor 5 runs
        // run(driveCmd(FORWARD, 24), motor5Cmd(0.5, 2));
    }
}
