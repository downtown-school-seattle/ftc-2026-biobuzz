package biobuzz.patheditor;

import java.util.List;

public final class Playback {
    private final List<Step> steps;
    private final double[] ends;
    private final double[] starts;
    private final double[] startPose;
    public final double total;

    public Playback(List<Step> steps, AutoPath path, Config cfg) {
        this.steps = steps;
        this.starts = new double[steps.size()];
        this.ends = new double[steps.size()];
        double t = 0;
        for (int i = 0; i < steps.size(); i++) {
            starts[i] = t;
            t += steps.get(i).duration(cfg);
            ends[i] = t;
        }
        total = t;
        if (path.waypoints.isEmpty()) startPose = new double[]{0, 0, path.startHeading};
        else startPose = new double[]{path.waypoints.get(0).x, path.waypoints.get(0).y, path.startHeading};
    }

    public double[] pose(double time) {
        double[] pose = startPose.clone();
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            if (time >= ends[i]) {
                pose[0] = s.x1;
                pose[1] = s.y1;
                pose[2] = s.h1;
                continue;
            }
            double span = ends[i] - starts[i];
            double f = span <= 0 ? 1 : Math.max(0, (time - starts[i]) / span);
            pose[0] = s.x0 + (s.x1 - s.x0) * f;
            pose[1] = s.y0 + (s.y1 - s.y0) * f;
            pose[2] = s.h0 + Fmt.norm180(s.h1 - s.h0) * f;
            break;
        }
        return pose;
    }

    public String status(double time) {
        String text = "";
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            if (starts[i] > time) break;
            if (s.type != Step.Type.DRIVE && s.action != null) text = s.action.label + " " + Fmt.num(s.value, 2) + " " + s.action.unit;
        }
        return text;
    }
}
