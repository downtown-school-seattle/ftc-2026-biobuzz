package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

abstract public class RobotController extends LinearOpMode {
    /** Enum representing the two physical bots we have. */
    public enum BotIdentity {
        Bernard,
        Gladys
    }

    /** Return which bot we are running on. */
    abstract BotIdentity getIdentity();

    /** Switch a constant between two values based on what robot we are running this on. */
    protected final <T> T switchOnBot(T bernardValue, T gladysValue) {
        switch (getIdentity()) {
            case Bernard: return bernardValue;
            case Gladys: return gladysValue;
        }
        throw new Error("Constant not defined for bot identity.");
    }
}
