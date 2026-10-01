package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

abstract public class RobotController extends LinearOpMode {
    public GoBildaPinpointDriver pinpoint;
    public DcMotor FrontLeftDrive;
    public DcMotor FrontRightDrive;
    public DcMotor BackLeftDrive;
    public DcMotor BackRightDrive;

    public void initRobotController() {

//        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
//        configurePinpoint();

        /// Port 0
        FrontLeftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        /// Port 1
        FrontRightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        /// Port 2
        BackLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        /// Port 3
        BackRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");

    }
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


    //pinpoint sensor stuff below
    public void configurePinpoint() {
        //change to desired value: x offset is how far right and left you put the sensor on the robot and y is forward and back on the robot
        pinpoint.setOffsets(0, 0, DistanceUnit.MM);
        //change to pinpoint sensor type if not the same as old robot
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        // should not need changing
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD);
        //does something important before starting the robot
        pinpoint.resetPosAndIMU();
    }
}
