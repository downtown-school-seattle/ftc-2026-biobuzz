package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

abstract public class RobotController extends LinearOpMode {
    public GoBildaPinpointDriver pinpoint;
    public PinpointOdometry odometry;
    public DcMotor frontLeftDrive;
    public DcMotor frontRightDrive;
    public DcMotor backLeftDrive;
    public DcMotor backRightDrive;
    public DcMotorEx shooterMotor;
    public DcMotorEx intakeMotor;

    public static final MotorTable SHOOTER_SPEC = MotorTable.GOBILDA_6000_RPM;
    public static final MotorTable INTAKE_SPEC = MotorTable.GOBILDA_1620_RPM;
    public static final double SHOOTER_MAX_RPM = 4500;
    public static final double INTAKE_MAX_RPM = 1000;
    protected double shooterTargetRpm = 4200;

    public void initRobotController() {
        // I2C (intra-integrated circuit) Port 0
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "ii0_pinpoint_sensor");
        odometry = new PinpointOdometry(pinpoint);

        /// Port 0
        frontLeftDrive = hardwareMap.get(DcMotor.class, "0_front_left_drive");
        /// Port 1
        frontRightDrive = hardwareMap.get(DcMotor.class, "1_front_right_drive");
        /// Port 2
        backLeftDrive = hardwareMap.get(DcMotor.class, "2_back_left_drive");
        /// Port 3
        backRightDrive = hardwareMap.get(DcMotor.class, "3_back_right_drive");
        frontLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        initShooter();
        initIntake();
    }
    public void initShooter() {
        shooterMotor = hardwareMap.get(DcMotorEx.class, "shooter");
        shooterMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    public void initIntake(){
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intake");
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    public void setShooterRpm(double rpm){
        MotorUtil.setRpm(shooterMotor, SHOOTER_SPEC, MotorUtil.clamp(rpm, -SHOOTER_MAX_RPM, SHOOTER_MAX_RPM));
    }
    public void setIntakeRpm(double rpm){
        MotorUtil.setRpm(intakeMotor, INTAKE_SPEC, MotorUtil.clamp(rpm, -INTAKE_MAX_RPM, INTAKE_MAX_RPM));
    }

    public double getShooterRpm() {return MotorUtil.getMeasuredRpm(shooterMotor, SHOOTER_SPEC);}
    public double getIntakeRpm() {return MotorUtil.getMeasuredRpm(intakeMotor, INTAKE_SPEC);}

    /**
     * changes the shooter target based off the current target,
     * defined in RobotController as ShooterTargetRpm
     * @param delta the amount to change by, can be positive or negative
     */
    protected void changeShooterTarget(double delta){
        shooterTargetRpm = MotorUtil.clamp(shooterTargetRpm + delta, SHOOTER_MAX_RPM, -SHOOTER_MAX_RPM);
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
    protected BotIdentity getIdentity() {
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
