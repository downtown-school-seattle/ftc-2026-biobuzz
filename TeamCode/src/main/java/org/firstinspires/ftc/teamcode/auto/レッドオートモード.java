package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="レッド", group="ロボット")
public class レッドオートモード extends オートコントローラー {
    @Override
    public アライアンスカラー getAllianceColor() {
        return アライアンスカラー.Red;
    }

    @Override
    protected void runAuto() {
        drive(フォワードホウコウ, 1);
        drive(レフトホウコウ, 1);
        drive(バックホウコウ, 1);
        drive(ライトホウコウ, 1);
        turn(-0.5);
        turn(0.5);
        run(motorCmd(motor0e, 4000), driveCmd(レフトホウコウ, 18));
    }
}
