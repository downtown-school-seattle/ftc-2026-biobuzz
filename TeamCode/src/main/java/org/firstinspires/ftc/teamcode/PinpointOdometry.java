package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/*
 gobilda pinpoint odometry stuff

 usage (in opmodes)
 odometry.update();  (RobotController creates "odometry")
 double x = odometry.getX();  (in mm)
 odometry.addTelemetry(telemetry);
 */
public class PinpointOdometry {
    // TODO: measure these on the robot. offsets are from the tracking point (robot center) to each pod
    // X pod offset: sideways distance, left of center is positive
    // Y pod offset: forward distance, forward of center is positive
    public static final double X_POD_OFFSET_MM = 0.0;
    public static final double Y_POD_OFFSET_MM = 0.0;

    // TODO: flip if reversed smh if the value goes the wrong way (X increases forward, Y increases left)
    public static final GoBildaPinpointDriver.EncoderDirection X_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.REVERSED;
    public static final GoBildaPinpointDriver.EncoderDirection Y_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.FORWARD;

    private final GoBildaPinpointDriver pinpoint;

    public PinpointOdometry(GoBildaPinpointDriver pinpoint) {
        this.pinpoint = pinpoint;
        pinpoint.setOffsets(X_POD_OFFSET_MM, Y_POD_OFFSET_MM, DistanceUnit.MM);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(X_DIRECTION, Y_DIRECTION);
        // Zeroes the position and recalibrates the IMU; the robot must be stationary.
        pinpoint.resetPosAndIMU();
    }

    // read new data from the pinpoint, you should call once per loop
    public void update() {
        pinpoint.update();
    }

    public void setPose(double xMm, double yMm, double headingDegrees) {
        pinpoint.setPosition(new Pose2D(DistanceUnit.MM, xMm, yMm, AngleUnit.DEGREES, headingDegrees));
    }

    // reset position to 0,0 and recalibrate the IMU (robot must be stationary)
    public void reset() {
        pinpoint.resetPosAndIMU();
    }

    public Pose2D getPose() {
        return pinpoint.getPosition();
    }

    public double getX() {
        return getPose().getX(DistanceUnit.MM);
    }

    public double getY() {
        return getPose().getY(DistanceUnit.MM);
    }

    public double getHeading() {
        return getPose().getHeading(AngleUnit.DEGREES);
    }

    public double getHeadingRadians() {
        return getPose().getHeading(AngleUnit.RADIANS);
    }

    public double getVelocityX() {
        return pinpoint.getVelX(DistanceUnit.MM);
    }

    public double getVelocityY() {
        return pinpoint.getVelY(DistanceUnit.MM);
    }

    public GoBildaPinpointDriver.DeviceStatus getStatus() {
        return pinpoint.getDeviceStatus();
    }

    public double getFrequency() {
        return pinpoint.getFrequency();
    }

    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Pinpoint X (mm)", "%.2f", getX());
        telemetry.addData("Pinpoint Y (mm)", "%.2f", getY());
        telemetry.addData("Pinpoint Heading (deg)", "%.2f", getHeading());
        telemetry.addData("Pinpoint Vel X (mm/s)", "%.2f", getVelocityX());
        telemetry.addData("Pinpoint Vel Y (mm/s)", "%.2f", getVelocityY());
        telemetry.addData("Pinpoint Status", getStatus());
        telemetry.addData("Pinpoint Hz", "%.0f", getFrequency());
    }
}
