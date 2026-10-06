package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

abstract public class RobotController extends LinearOpMode {
    public GoBildaPinpointDriver pinpoint;
    public DcMotor frontLeftDrive;
    public DcMotor frontRightDrive;
    public DcMotor backLeftDrive;
    public DcMotor backRightDrive;

    public void initRobotController() {

//        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
//        configurePinpoint();

        /// Port 0
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        /// Port 1
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        /// Port 2
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        /// Port 3
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");

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
    // Thanks to FTC16072 for sharing this code!!
    public void drive(double forward, double right, double rotate) {
        // This calculates the power needed for each wheel based on the amount of forward,
        // strafe right, and rotate
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        double maxPower = 1.0;
        double maxSpeed = 1.0;  // make this slower for outreaches

        // This is needed to make sure we don't pass > 1.0 to any wheel
        // It allows us to keep all of the motors in proportion to what they should
        // be and not get clipped
        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
        maxPower = Math.max(maxPower, Math.abs(backRightPower));
        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        // We multiply by maxSpeed so that it can be set lower for outreaches
        // When a young child is driving the robot, we may not want to allow full
        // speed.
        frontLeftDrive.setPower(maxSpeed * (frontLeftPower / maxPower));
        frontRightDrive.setPower(maxSpeed * (frontRightPower / maxPower));
        backLeftDrive.setPower(maxSpeed * (backLeftPower / maxPower));
        backRightDrive.setPower(maxSpeed * (backRightPower / maxPower));
    }

}
