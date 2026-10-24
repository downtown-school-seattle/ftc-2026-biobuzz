package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

public class MotorUtil {
    private MotorUtil() {}

    public static double getMeasuredRpm(DcMotorEx motor, ExMotorTable spec) {
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
    public static void setRpm(DcMotorEx motor, ExMotorTable spec, double rpm){
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //SDK wants ticks/s not rpm, no clue why, incredibly backwards.
        motor.setVelocity(rpm /60.0 * spec.encoderResolution);
    }

    /**
     * Runs the shooter at a speed, ALLOWS SAG TO PASS THROUGH
     * does not bring the motor back up to speed
     * @param motor motor you want to use
     * @param spec specs of the motor you want to use
     * @param rpm target speed. Negative = reverse, 0 = stop
     */
    public static void setRpmOpen(DcMotorEx motor, ExMotorTable spec, double rpm){
        double power = rpm / spec.freeSpeedRpm;
        //SDK only allows -1 to 1 so were gonna clamp both directions
        motor.setPower(Math.max(-1.0, Math.min(1.0, power)));
    }

    /**
     * it clamps the frickin rpm what do you think it does???
     * optionally, a third parameter for min Rpm is also available
     * min Rpm also works for maximum reverse Rpm
     * @param rpm rpm it is currently running at
     * @param maxRpm the maximum rpm it should be able to run at
     * @return
     */
    //why the frick doesnt java have default parameter values????? i hate it here???
    public static double clampRpm(double rpm, double maxRpm) {
        return Math.max(-maxRpm, Math.min(maxRpm, rpm));
    }
    //okay owen i know this looks weird but since java doesnt have default parameters this is the workaround
    /**
     * it clamps the frickin rpm what do you think it does???
     * optionally, a third parameter for min Rpm is also available
     * min Rpm also works for maximum reverse Rpm
     * @param rpm rpm it is currently running at
     * @param maxRpm the maximum rpm it should be able to run
     * @param minRpm the minimum rpm it should be able to run at, THIS INCLUDES REVERSE
     * @return
     */
    public static double clampRpm(double rpm, double maxRpm, double minRpm){
        return Math.max(minRpm, Math.min(maxRpm, rpm));
    }

    public static void stop(DcMotorEx motor) {
        motor.setPower(0);
    }

}
