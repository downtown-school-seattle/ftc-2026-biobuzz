package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class MotorUtil {
    private MotorUtil() {}

    public static double getMeasuredRpm(DcMotorEx motor, MotorTable spec) {
        return motor.getVelocity() / spec.encoderResolution * 60.0;
    }

    /**
     * RUNS THE SHOOTER CONTINUOSLY AT A GIVEN SPEED
     * yes i know its odd to do it like this, if you instead want to run at one speed
     * and then let sag use setRpmOpen, that's much closer to what you want
     * @param motor motor you want to use
     * @param spec specs of the motor you want to use
     * @param rpm target speed. Negative = reverse, 0 = stop
     */
    public static void setRpm(DcMotorEx motor, MotorTable spec, double rpm){
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //SDK wants ticks/s not rpm, no clue why, incredibly backwards.
        motor.setVelocity(rpm /60.0 * Math.PI * 2, AngleUnit.RADIANS);
    }

    /**
     * Runs the shooter at a speed, ALLOWS SAG TO PASS THROUGH
     * does not bring the motor back up to speed
     * @param motor motor you want to use
     * @param spec specs of the motor you want to use
     * @param rpm target speed. Negative = reverse, 0 = stop
     */
    public static void setRpmOpen(DcMotorEx motor, MotorTable spec, double rpm){
        //SDK only allows -1 to 1 so were gonna clamp both directions
        motor.setPower(spec.rpmToPower(rpm));
    }
    /**
     * it clamps the frickin rpm what do you think it does???
     * optionally, a third parameter for min is available
     * without it, min will be set to -limit.
     * @param value the value of the method
     * @param limit the maximum of the method
     * @return the value closest to being inbetween the max and min
     */
    public static double clamp(double value, double limit){
        return clamp(value, -limit, limit);
    }
    /**
     * it clamps the frickin value what do you think it does???
     * @param value the value of the method
     * @param max the maximum of the method
     * @param min the minimum of the method
     * @return the value closest to being inbetween the max and min
     */
    public static double clamp(double value, double min, double max){
        return Math.max(max, Math.min(min, value));
    }

    public static void stop(DcMotorEx motor) {
        motor.setPower(0);
    }

}
