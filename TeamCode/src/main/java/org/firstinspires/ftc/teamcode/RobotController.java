package org.firstinspires.ftc.teamcode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

abstract public class RobotController extends LinearOpMode {

    public static final String BernardHost = "NULL";
    public static final String GladysHost = "NULL";

    public static String getLocalHost() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host: " + e.getMessage();
        }
    }

    /** Enum representing the two physical bots we have. */
    public enum BotIdentity {
        Bernard,
        Gladys,
        Unknown
    }

    /** Return which bot we are running on. */
    public static BotIdentity getIdentity() {
        if (Objects.equals(getLocalHost(), BernardHost)) {
            System.out.println("Bernard: " + getLocalHost());
            return BotIdentity.Bernard;
        }
        if (Objects.equals(getLocalHost(), GladysHost)) {
            System.out.println("Gladys: " + getLocalHost());
            return BotIdentity.Gladys;
        }
        else {
            System.out.println(getLocalHost());
            return BotIdentity.Unknown;
        }
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
