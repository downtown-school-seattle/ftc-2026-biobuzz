package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Controls the "#5" motor: motor port 0 on the Expansion Hub. The port is chosen in the robot
 * configuration in the Robot Controller / Driver Station app (Configure Robot -> Expansion Hub ->
 * Motors -> port 0 -> name it {@link #CONFIG_NAME}), so moving it to another port needs no code change.
 */
public class Motor5 {
    /** Name this motor must be given in the Configure Robot screen. */
    public static final String CONFIG_NAME = "motor_5";

    private final DcMotor motor;

    public Motor5(HardwareMap hardwareMap) {
        this(hardwareMap, CONFIG_NAME);
    }

    public Motor5(HardwareMap hardwareMap, String configName) {
        motor = hardwareMap.get(DcMotor.class, configName);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /** Set power from -1 to 1. */
    public void setPower(double power) {
        motor.setPower(Math.max(-1, Math.min(1, power)));
    }

    public void stop() {
        motor.setPower(0);
    }

    public void setReversed(boolean reversed) {
        motor.setDirection(reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }

    public double getPower() {
        return motor.getPower();
    }

    public int getPosition() {
        return motor.getCurrentPosition();
    }
}
