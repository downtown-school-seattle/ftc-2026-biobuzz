package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayDeque;

public class Motor0e {
    public static final String CONFIG_NAME = "motor0e";
    public static final double フリースピードアールピーエム = 6000;
    public static final double ミンアールピーエム = 3000;
    public static final double マックスアールピーエム = 4500;
    public static final double スタートアールピーエム = 4200;
    public static final double ティックスパーレブ = 28;
    private static final double ケーピー = 0.0001;
    private static final double ケーアイ = 0.0003;
    private static final double マックスインテグラル = 0.5;
    private static final double マックスディーティーセカンズ = 0.1;
    private static final double ドロップフラクション = 0.07;
    private static final double リカバリーフラクション = 1.00;
    private static final double ウィンドウセカンズ = 1.0;

    private final DcMotorEx motor;
    private double ターゲットアールピーエム = スタートアールピーエム;
    private boolean ランニング = false;

    private final ElapsedTime クロック = new ElapsedTime();
    private final ArrayDeque<double[]> ヒストリー = new ArrayDeque<>();
    private boolean ドロッピング = false;
    private double ドロップフロムアールピーエム;
    private double ロウエストアールピーエム;
    private double ドロップスタートセカンズ;
    private double インテグラル = 0;
    private double ラストコントロールセカンズ = 0;
    private double コマンデッドパワー = 0;
    private String ディップサマリー = "";

    public Motor0e(HardwareMap ハードウェアマップ) {
        this(ハードウェアマップ, CONFIG_NAME);
    }

    public Motor0e(HardwareMap ハードウェアマップ, String コンフィグネーム) {
        motor = ハードウェアマップ.get(DcMotorEx.class, コンフィグネーム);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void changeRpm(double デルタ) {
        ターゲットアールピーエム = Math.max(ミンアールピーエム, Math.min(マックスアールピーエム, ターゲットアールピーエム + デルタ));
        resetDipTracking();
        apply();
    }

    public void setRpm(double アールピーエム) {
        if (アールピーエム <= 0) {
            stop();
            return;
        }
        ターゲットアールピーエム = Math.min(マックスアールピーエム, アールピーエム);
        if (ランニング) {
            resetDipTracking();
            apply();
        } else {
            start();
        }
    }

    public double getTargetRpm() {
        return ターゲットアールピーエム;
    }

    public double getTicksPerSecond() {
        return Math.abs(motor.getVelocity());
    }

    public double getMeasuredRpm() {
        return getTicksPerSecond() / ティックスパーレブ * 60;
    }

    public void start() {
        ランニング = true;
        インテグラル = 0;
        ラストコントロールセカンズ = クロック.seconds();
        resetDipTracking();
        apply();
    }

    public void stop() {
        ランニング = false;
        resetDipTracking();
        apply();
    }

    public void update() {
        if (!ランニング) return;
        double ナウ = クロック.seconds();
        double アールピーエム = getMeasuredRpm();
        regulate(ナウ, アールピーエム);

        if (ドロッピング) {
            ロウエストアールピーエム = Math.min(ロウエストアールピーエム, アールピーエム);
            double イラプスド = ナウ - ドロップスタートセカンズ;
            if (アールピーエム >= ドロップフロムアールピーエム * リカバリーフラクション) {
                ドロッピング = false;
                ヒストリー.clear();
                ディップサマリー = String.format("ディップ %.0f -> ロー %.0f アールピーエム、バック アップ イン %.2f エス", ドロップフロムアールピーエム, ロウエストアールピーエム, イラプスド);
                RobotLog.ii("Motor0e", ディップサマリー);
            } else {
                ディップサマリー = String.format("ディップ %.0f -> ロー %.0f アールピーエム、リカバリング %.2f エス...", ドロップフロムアールピーエム, ロウエストアールピーエム, イラプスド);
            }
            return;
        }

        ヒストリー.addLast(new double[]{ナウ, アールピーエム});
        while (ヒストリー.peekFirst()[0] < ナウ - ウィンドウセカンズ) ヒストリー.removeFirst();
        double ピーク = 0;
        for (double[] サンプル : ヒストリー) ピーク = Math.max(ピーク, サンプル[1]);
        if (アールピーエム < ピーク * (1 - ドロップフラクション)) {
            ドロッピング = true;
            ドロップフロムアールピーエム = ピーク;
            ロウエストアールピーエム = アールピーエム;
            ドロップスタートセカンズ = ナウ;
        }
    }

    public void setReversed(boolean リバースド) {
        motor.setDirection(リバースド ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }

    public void addTelemetry(Telemetry テレメトリー) {
        テレメトリー.addData("motor0e", "%s  ターゲット %.0f  メジャード %.0f アールピーエム  パワー %.2f  (%.0f ティックス/エス)",
                ランニング ? "オン" : "オフ", ターゲットアールピーエム, getMeasuredRpm(), コマンデッドパワー, getTicksPerSecond());
        if (!ディップサマリー.isEmpty()) {
            テレメトリー.addLine("<font color=\"#00b300\">" + ディップサマリー + "</font>");
        }
    }

    private void regulate(double ナウ, double アールピーエム) {
        double ディーティー = Math.min(ナウ - ラストコントロールセカンズ, マックスディーティーセカンズ);
        ラストコントロールセカンズ = ナウ;

        double エラー = ターゲットアールピーエム - アールピーエム;
        double アンクランプド = ターゲットアールピーエム / フリースピードアールピーエム + ケーピー * エラー + インテグラル;
        double パワー = Math.max(0, Math.min(1, アンクランプド));
        if (パワー == アンクランプド) {
            インテグラル = Math.max(-マックスインテグラル, Math.min(マックスインテグラル, インテグラル + ケーアイ * エラー * ディーティー));
        }
        setCommandedPower(パワー);
    }

    private void setCommandedPower(double パワー) {
        コマンデッドパワー = パワー;
        motor.setPower(パワー);
    }

    private void resetDipTracking() {
        ドロッピング = false;
        ヒストリー.clear();
    }

    private void apply() {
        setCommandedPower(ランニング ? ターゲットアールピーエム / フリースピードアールピーエム + インテグラル : 0);
    }
}
