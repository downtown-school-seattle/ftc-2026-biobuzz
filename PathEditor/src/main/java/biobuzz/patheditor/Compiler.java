package biobuzz.patheditor;

import java.util.ArrayList;
import java.util.List;

public final class Compiler {
    private static final double EPS = 0.01;

    private Compiler() {}

    public static List<Step> compile(AutoPath p, Config cfg) {
        List<Step> out = new ArrayList<>();
        if (p.waypoints.isEmpty()) return out;
        State st = new State();
        st.heading = p.startHeading;
        st.x = p.waypoints.get(0).x;
        st.y = p.waypoints.get(0).y;
        applyPointActions(out, cfg, p.waypoints.get(0), st);
        for (int seg = 0; seg < p.segments(); seg++) {
            for (AutoPath.Marker m : p.markersOn(seg)) {
                double mx = p.x(seg, m.t);
                double my = p.y(seg, m.t);
                driveTo(out, st, mx, my);
                applyAction(out, cfg.action(m.action), m.value, st);
            }
            AutoPath.Waypoint b = p.waypoints.get(seg + 1);
            driveTo(out, st, b.x, b.y);
            applyPointActions(out, cfg, b, st);
        }
        flushPending(out, st);
        return out;
    }

    private static final class State {
        double x;
        double y;
        double heading;
        Double pending; // heading to reach during the next drive ("rotate while driving")
    }

    private static void driveTo(List<Step> out, State st, double x, double y) {
        if (Math.hypot(x - st.x, y - st.y) <= EPS) return;
        Step s = Step.drive(st.x, st.y, x, y, st.heading);
        if (st.pending != null) {
            s.h1 = st.pending;
            st.heading = st.pending;
            st.pending = null;
        }
        out.add(s);
        st.x = x;
        st.y = y;
    }

    // a rotate-while-driving with no drive after it becomes a plain turn
    private static void flushPending(List<Step> out, State st) {
        if (st.pending == null) return;
        out.add(Step.turn(st.x, st.y, st.heading, st.pending, null));
        st.heading = st.pending;
        st.pending = null;
    }

    private static void applyPointActions(List<Step> out, Config cfg, AutoPath.Waypoint w, State st) {
        for (Config.ActionDef a : cfg.actions) {
            Double v = w.actions.get(a.id);
            if (v != null) applyAction(out, a, v, st);
        }
    }

    private static void applyAction(List<Step> out, Config.ActionDef a, double value, State st) {
        if (a == null) return;
        if ("turnDrive".equals(a.kind)) {
            st.pending = value;
            return;
        }
        if ("turn".equals(a.kind)) {
            st.pending = null;
            out.add(Step.turn(st.x, st.y, st.heading, value, a));
            st.heading = value;
            return;
        }
        out.add(Step.action(st.x, st.y, st.heading, a, value));
    }

    public static String export(List<Step> steps, Config cfg) {
        StringBuilder b = new StringBuilder();
        for (Step s : steps) {
            String cmd;
            switch (s.type) {
                case DRIVE:
                    double rel = Fmt.norm360(cfg.driveSign * (s.h0 - s.travelAngle()));
                    cmd = cfg.driveTemplate
                            .replace("{degrees}", Fmt.num(rel, 1))
                            .replace("{inches}", Fmt.num(s.distance(), 2));
                    if (Math.abs(Fmt.norm180(s.h1 - s.h0)) >= 0.05) {
                        cmd += ", " + cfg.turnTemplate.replace("{degrees}", Fmt.num(cfg.turnSign * Fmt.norm180(s.h1 - s.h0), 1));
                    }
                    break;
                case TURN:
                    cmd = cfg.turnTemplate.replace("{degrees}", Fmt.num(cfg.turnSign * Fmt.norm180(s.h1 - s.h0), 1));
                    break;
                default:
                    cmd = s.action.template.replace("{value}", Fmt.num(s.value, 3));
            }
            if (s.type == Step.Type.TURN && Math.abs(Fmt.norm180(s.h1 - s.h0)) < 0.05) continue;
            b.append(cfg.indent).append(cfg.runTemplate.replace("{commands}", cmd)).append('\n');
        }
        return b.toString();
    }
}
