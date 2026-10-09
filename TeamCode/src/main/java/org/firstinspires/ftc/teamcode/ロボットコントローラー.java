package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

abstract public class ロボットコントローラー extends LinearOpMode {
    public GoBildaPinpointDriver ピンポイント;
    public DcMotor frontLeftDrive;
    public DcMotor frontRightDrive;
    public DcMotor backLeftDrive;
    public DcMotor backRightDrive;

    public void initRobotController() {
        frontLeftDrive = hardwareMap.get(DcMotor.class, "motor0");
        frontRightDrive = hardwareMap.get(DcMotor.class, "motor1");
        backLeftDrive = hardwareMap.get(DcMotor.class, "motor2");
        backRightDrive = hardwareMap.get(DcMotor.class, "motor3");
    }

    public enum アライアンスカラー {
        Blue,
        Red
    }

    public enum ボットアイデンティティ {
        Bernard,
        Gladys
    }

    /** Return which bot we are running on. */
    ボットアイデンティティ getIdentity() {
        throw new UnsupportedOperationException("ノット イェット");
    }

    protected final <T> T switchOnBot(T バーナードバリュー, T グラディスバリュー) {
        switch (getIdentity()) {
            case Bernard: return バーナードバリュー;
            case Gladys: return グラディスバリュー;
        }
        throw new Error("コンスタント ノット ディファインド フォー ボット アイデンティティ。");
    }


    public void configurePinpoint() {
        ピンポイント.setOffsets(0, 0, DistanceUnit.MM);
        ピンポイント.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        ピンポイント.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD);
        ピンポイント.resetPosAndIMU();
    }
}
