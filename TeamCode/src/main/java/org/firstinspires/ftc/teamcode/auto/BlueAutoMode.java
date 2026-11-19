package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Autonomous(name="Blue Auto", group="Robot")
public class BlueAutoMode extends AutoController {
    @Override
    public AllianceColor getAllianceColor() {
        return AllianceColor.Blue;
    }

    @Override
    protected void runAuto() {
        // 24in = 1 tile
        setTargetPosition(24.0, 24.0, DistanceUnit.INCH);
        setTargetHeading(30, AngleUnit.DEGREES);
        moveToTarget();
    }
}
