package org.firstinspires.ftc.teamcode.auto;

public interface AutoCommand {
    void start();
    void update();
    boolean isDone();
    void end();
}
