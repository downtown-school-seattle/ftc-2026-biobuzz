package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="ブルー", group="ロボット")
public class BlueAutoMode extends AutoController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }

    @Override
    protected void runAuto() {
        drive(0, 24);
        drive(90, 24);
        drive(180, 24);
        drive(270, 24);
        turn(180);
        turn(-90);
        run(motorCmd(motor0e, 4000), driveCmd(90, 18), turnCmd(30));
    }
}
