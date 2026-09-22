package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.internal.webserver.websockets.CommandNotImplementedException;

abstract public class RobotController extends LinearOpMode {
    /** Enum representing our color. */
    public enum AllianceColor {
        Blue,
        Red
    }

    /** Enum representing the two physical bots we have. */
    public enum BotIdentity {
        Bernard,
        Gladys
    }

    abstract public AllianceColor getAllianceColor();

    /** Return which bot we are running on. */
    BotIdentity getIdentity() {
        throw new UnsupportedOperationException("not yet");
    }

    /** Switch a constant between two values based on what robot we are running this on. */
    protected final <T> T switchOnBot(T bernardValue, T gladysValue) {
        switch (getIdentity()) {
            case Bernard: return bernardValue;
            case Gladys: return gladysValue;
        }
        throw new Error("Constant not defined for bot identity.");
    }
}
