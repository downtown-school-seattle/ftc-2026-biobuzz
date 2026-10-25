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
        run(driveCmd(FORWARD, 1));
        run(driveCmd(LEFT, 1));
        run(driveCmd(BACK, 1));
        run(driveCmd(RIGHT, 1));
        run(turnCmd(-0.5));
        run(turnCmd(0.5));
        run(motorCmd(shooterMotor, 4000), driveCmd(LEFT, 18));
    }
}
