package org.firstinspires.ftc.teamcode.auto;

/**
 * One step of an auto. The runner calls {@link #start()} once, then {@link #update()} every loop
 * until {@link #isDone()}, then {@link #end()} once. Several commands can run at the same time.
 */
public interface AutoCommand {
    void start();
    void update();
    boolean isDone();
    void end();
}
