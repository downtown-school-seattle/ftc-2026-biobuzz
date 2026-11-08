package biobuzz.patheditor;

public final class Step {
    public enum Type {DRIVE, TURN, ACTION}

    public Type type;
    public double x0;
    public double y0;
    public double x1;
    public double y1;
    public double h0;
    public double h1;
    public Config.ActionDef action;
    public double value;

    public static Step drive(double x0, double y0, double x1, double y1, double heading) {
        Step s = new Step();
        s.type = Type.DRIVE;
        s.x0 = x0;
        s.y0 = y0;
        s.x1 = x1;
        s.y1 = y1;
        s.h0 = heading;
        s.h1 = heading;
        return s;
    }

    public static Step turn(double x, double y, double from, double to, Config.ActionDef action) {
        Step s = new Step();
        s.type = Type.TURN;
        s.x0 = x;
        s.y0 = y;
        s.x1 = x;
        s.y1 = y;
        s.h0 = from;
        s.h1 = to;
        s.action = action;
        s.value = to;
        return s;
    }

    public static Step action(double x, double y, double heading, Config.ActionDef action, double value) {
        Step s = new Step();
        s.type = Type.ACTION;
        s.x0 = x;
        s.y0 = y;
        s.x1 = x;
        s.y1 = y;
        s.h0 = heading;
        s.h1 = heading;
        s.action = action;
        s.value = value;
        return s;
    }

    public double travelAngle() {
        return Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));
    }

    public double distance() {
        return Math.hypot(x1 - x0, y1 - y0);
    }

    public double duration(Config cfg) {
        switch (type) {
            case DRIVE: return Math.max(distance() / cfg.driveSpeed, Math.abs(Fmt.norm180(h1 - h0)) / cfg.turnSpeed);
            case TURN: return Math.abs(Fmt.norm180(h1 - h0)) / cfg.turnSpeed;
            default: return "wait".equals(action.kind) ? Math.max(0, value) : 0;
        }
    }
}
