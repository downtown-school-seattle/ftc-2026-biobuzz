package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Motor0e;
import org.firstinspires.ftc.teamcode.RobotController;

import java.util.LinkedHashMap;
import java.util.Map;

abstract public class AutoController extends RobotController {
    private static final String タグ = "オートコントローラー";
    private static final double ミンムービングアールピーエム = 5;
    private static final String[] ホイールズ = {"エフエル", "エフアール", "ビーエル", "ビーアール"};

    public static final double フォワードホウコウ = 0;
    public static final double ライトホウコウ = 90;
    public static final double バックホウコウ = 180;
    public static final double レフトホウコウ = 270;

    protected double ティックスパーインチ = 40;
    protected double ティックスパーデグリー = 10;
    protected double ホイールティックスパーレブ = 537.7;
    protected double パワー = 0.5;
    protected int トレランス = 15;
    protected double ムーブタイムアウトセカンズ = 5;

    protected static final String motor0e = "motor0e";
    protected static final String motor1e = "motor1e";
    protected static final String motor2e = "motor2e";
    protected static final String motor3e = "motor3e";

    private final Map<String, Motor0e> エクスパンションモーターズ = new LinkedHashMap<>();

    abstract public AllianceColor getAllianceColor();

    protected void runAuto() {}

    @Override
    public void runOpMode() {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        initRobotController();
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        for (DcMotor エム : モーターズ()) エム.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
        if (opModeIsActive()) runAuto();
        for (Motor0e エム : エクスパンションモーターズ.values()) エム.stop();
    }

    protected void drive(double デグリーズ, double インチズ) {
        run(driveCmd(デグリーズ, インチズ));
    }

    protected void turn(double デグリーズ) {
        run(turnCmd(デグリーズ));
    }

    protected AutoCommand driveCmd(double デグリーズ, double インチズ) {
        double ラジアンズ = Math.toRadians(デグリーズ);
        double フォワード = Math.cos(ラジアンズ) * インチズ * ティックスパーインチ;
        double ライト = Math.sin(ラジアンズ) * インチズ * ティックスパーインチ;
        String ネーム = "ドライブ " + デグリーズ + " デグ " + インチズ + " イン";
        return new MoveCommand(ネーム, フォワード + ライト, フォワード + ライト, ライト - フォワード, ライト - フォワード);
    }

    protected AutoCommand turnCmd(double デグリーズ) {
        double ティー = デグリーズ * ティックスパーデグリー;
        return new MoveCommand("ターン " + デグリーズ + " デグ", -ティー, ティー, -ティー, ティー);
    }

    protected AutoCommand motorCmd(String コンフィグネーム, double アールピーエム) {
        return new MotorCommand(コンフィグネーム, アールピーエム);
    }

    protected void run(AutoCommand... コマンズ) {
        int ムーブズ = 0;
        for (AutoCommand シー : コマンズ) if (シー instanceof MoveCommand) ムーブズ++;
        if (ムーブズ > 1) throw new IllegalArgumentException("オンリー ワン ターン コマンド キャン ラン イン ア グループ。");

        for (AutoCommand シー : コマンズ) シー.start();
        while (opModeIsActive()) {
            boolean オールダン = true;
            for (AutoCommand シー : コマンズ) {
                if (シー.isDone()) continue;
                シー.update();
                オールダン = false;
            }
            for (Motor0e エム : エクスパンションモーターズ.values()) {
                エム.update();
                エム.addTelemetry(telemetry);
            }
            telemetry.update();
            if (オールダン) break;
            idle();
        }
        for (AutoCommand シー : コマンズ) シー.end();
    }

    private Motor0e expansionMotor(String コンフィグネーム) {
        Motor0e エム = エクスパンションモーターズ.get(コンフィグネーム);
        if (エム == null) {
            エム = new Motor0e(hardwareMap, コンフィグネーム);
            エクスパンションモーターズ.put(コンフィグネーム, エム);
        }
        return エム;
    }

    private class MoveCommand implements AutoCommand {
        private final String ネーム;
        private final double[] ターゲッツ;
        private final ElapsedTime タイマー = new ElapsedTime();
        private boolean タイムドアウト = false;

        MoveCommand(String ネーム, double... ターゲッツ) {
            this.ネーム = ネーム;
            this.ターゲッツ = ターゲッツ;
        }

        @Override
        public void start() {
            RobotLog.ii(タグ, "スターティング: " + ネーム);
            DcMotor[] モーターズ = モーターズ();
            double ファーセスト = 0;
            for (double ティー : ターゲッツ) ファーセスト = Math.max(ファーセスト, Math.abs(ティー));
            for (int アイ = 0; アイ < モーターズ.length; アイ++) {
                モーターズ[アイ].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                モーターズ[アイ].setTargetPosition((int) Math.round(ターゲッツ[アイ]));
                モーターズ[アイ].setMode(DcMotor.RunMode.RUN_TO_POSITION);
                モーターズ[アイ].setPower(ファーセスト == 0 ? 0 : パワー * Math.abs(ターゲッツ[アイ]) / ファーセスト);
            }
            タイマー.reset();
        }

        @Override
        public void update() {
            if (タイマー.seconds() > ムーブタイムアウトセカンズ) {
                タイムドアウト = true;
                RobotLog.ee(タグ, "タイムド アウト: " + ネーム);
                return;
            }
            addWheelTelemetry(モーターズ());
        }

        @Override
        public boolean isDone() {
            return タイムドアウト || reachedTargets(モーターズ());
        }

        @Override
        public void end() {
            for (DcMotor エム : モーターズ()) {
                エム.setPower(0);
                エム.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            RobotLog.ii(タグ, "フィニッシュド: " + ネーム);
        }
    }

    private class MotorCommand implements AutoCommand {
        private final String コンフィグネーム;
        private final double アールピーエム;

        MotorCommand(String コンフィグネーム, double アールピーエム) {
            this.コンフィグネーム = コンフィグネーム;
            this.アールピーエム = アールピーエム;
        }

        @Override
        public void start() {
            try {
                expansionMotor(コンフィグネーム).setRpm(アールピーエム);
                RobotLog.ii(タグ, "モーター " + コンフィグネーム + " セット トゥ " + アールピーエム + " アールピーエム");
            } catch (IllegalArgumentException イー) {
                RobotLog.ee(タグ, "モーター ノット ファウンド イン コンフィグ: " + コンフィグネーム);
            }
        }

        @Override
        public void update() {}

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public void end() {}
    }

    private void addWheelTelemetry(DcMotor[] モーターズ) {
        double[] アールピーエム = new double[モーターズ.length];
        for (int アイ = 0; アイ < モーターズ.length; アイ++) {
            アールピーエム[アイ] = ((DcMotorEx) モーターズ[アイ]).getVelocity() / ホイールティックスパーレブ * 60;
            telemetry.addData(ホイールズ[アイ], "%.0f アールピーエム", アールピーエム[アイ]);
        }

        double フォワード = (アールピーエム[0] + アールピーエム[1] - アールピーエム[2] - アールピーエム[3]) / 4;
        double ライト = (アールピーエム[0] + アールピーエム[1] + アールピーエム[2] + アールピーエム[3]) / 4;
        if (Math.hypot(フォワード, ライト) < ミンムービングアールピーエム) {
            telemetry.addData("ディレクション", "-");
        } else {
            double デグリーズ = (Math.toDegrees(Math.atan2(ライト, フォワード)) + 360) % 360;
            telemetry.addData("ディレクション", "%.0f デグ (0 フォワード, 90 ライト)", デグリーズ);
        }
    }

    private boolean reachedTargets(DcMotor[] モーターズ) {
        for (DcMotor エム : モーターズ) {
            if (Math.abs(エム.getTargetPosition() - エム.getCurrentPosition()) > トレランス) return false;
        }
        return true;
    }

    private DcMotor[] モーターズ() {
        return new DcMotor[]{frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive};
    }
}
