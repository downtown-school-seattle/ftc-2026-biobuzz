package biobuzz.patheditor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AutoPath {
    public static final class Waypoint {
        public double x;
        public double y;
        public final Map<String, Double> actions = new LinkedHashMap<>();

        public Waypoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static final class Marker {
        public int segment;
        public double t;
        public String action;
        public double value;

        public Marker(int segment, double t, String action, double value) {
            this.segment = segment;
            this.t = t;
            this.action = action;
            this.value = value;
        }
    }

    public String name = "auto";
    public double startHeading = 90;
    public final List<Waypoint> waypoints = new ArrayList<>();
    public final List<Marker> markers = new ArrayList<>();

    public int segments() {
        return Math.max(0, waypoints.size() - 1);
    }

    public double length(int seg) {
        Waypoint a = waypoints.get(seg);
        Waypoint b = waypoints.get(seg + 1);
        return Math.hypot(b.x - a.x, b.y - a.y);
    }

    public double x(int seg, double t) {
        Waypoint a = waypoints.get(seg);
        Waypoint b = waypoints.get(seg + 1);
        return a.x + (b.x - a.x) * t;
    }

    public double y(int seg, double t) {
        Waypoint a = waypoints.get(seg);
        Waypoint b = waypoints.get(seg + 1);
        return a.y + (b.y - a.y) * t;
    }

    public double project(int seg, double px, double py) {
        Waypoint a = waypoints.get(seg);
        Waypoint b = waypoints.get(seg + 1);
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0) return 0;
        return Math.max(0, Math.min(1, ((px - a.x) * dx + (py - a.y) * dy) / len2));
    }

    public List<Marker> markersOn(int seg) {
        List<Marker> out = new ArrayList<>();
        for (Marker m : markers) if (m.segment == seg) out.add(m);
        Collections.sort(out, (p, q) -> Double.compare(p.t, q.t));
        return out;
    }

    public int addWaypoint(double x, double y) {
        waypoints.add(new Waypoint(x, y));
        return waypoints.size() - 1;
    }

    public int insertWaypoint(int seg, double t) {
        double split = Math.max(0.02, Math.min(0.98, t));
        double x = x(seg, split);
        double y = y(seg, split);
        for (Marker m : markers) {
            if (m.segment > seg) {
                m.segment++;
            } else if (m.segment == seg) {
                if (m.t < split) {
                    m.t = m.t / split;
                } else {
                    m.segment = seg + 1;
                    m.t = (m.t - split) / (1 - split);
                }
            }
        }
        waypoints.add(seg + 1, new Waypoint(x, y));
        return seg + 1;
    }

    public void removeWaypoint(int k) {
        int n = waypoints.size();
        if (k < 0 || k >= n) return;
        double l1 = 0;
        double l2 = 0;
        if (k > 0 && k < n - 1) {
            l1 = length(k - 1);
            l2 = length(k);
        }
        double sum = l1 + l2;
        Iterator<Marker> it = markers.iterator();
        while (it.hasNext()) {
            Marker m = it.next();
            if (k == 0) {
                if (m.segment == 0) it.remove();
                else m.segment--;
            } else if (k == n - 1) {
                if (m.segment == n - 2) it.remove();
            } else if (m.segment == k - 1) {
                m.t = sum == 0 ? 0 : m.t * l1 / sum;
            } else if (m.segment == k) {
                m.segment = k - 1;
                m.t = sum == 0 ? 0 : (l1 + m.t * l2) / sum;
            } else if (m.segment > k) {
                m.segment--;
            }
        }
        waypoints.remove(k);
    }

    public Marker addMarker(int seg, double t, String action, double value) {
        Marker m = new Marker(seg, t, action, value);
        markers.add(m);
        return m;
    }

    private static double round2(double v) {
        return Math.round(v * 100) / 100.0;
    }

    public void save(File file) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", name);
        root.put("startHeading", startHeading);
        List<Object> wps = new ArrayList<>();
        for (Waypoint w : waypoints) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("x", round2(w.x));
            p.put("y", round2(w.y));
            if (!w.actions.isEmpty()) p.put("actions", new LinkedHashMap<String, Object>(w.actions));
            wps.add(p);
        }
        root.put("waypoints", wps);
        List<Object> ms = new ArrayList<>();
        for (Marker m : markers) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("segment", m.segment);
            e.put("t", Math.round(m.t * 10000) / 10000.0);
            e.put("action", m.action);
            e.put("value", m.value);
            ms.add(e);
        }
        root.put("markers", ms);
        Files.write(file.toPath(), (Json.write(root) + "\n").getBytes(StandardCharsets.UTF_8));
    }

    public static AutoPath load(File file) throws IOException {
        Map<String, Object> root = Json.obj(Json.parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
        AutoPath p = new AutoPath();
        p.name = Json.str(root, "name", "auto");
        p.startHeading = Json.num(root, "startHeading", 90);
        for (Object o : Json.list(root.get("waypoints"))) {
            Waypoint w;
            if (o instanceof List) {
                List<Object> xy = Json.list(o);
                w = new Waypoint(((Number) xy.get(0)).doubleValue(), ((Number) xy.get(1)).doubleValue());
            } else {
                Map<String, Object> m = Json.obj(o);
                w = new Waypoint(Json.num(m, "x", 0), Json.num(m, "y", 0));
                for (Map.Entry<String, Object> e : Json.obj(m.get("actions")).entrySet()) {
                    if (e.getValue() instanceof Number) w.actions.put(e.getKey(), ((Number) e.getValue()).doubleValue());
                }
            }
            p.waypoints.add(w);
        }
        for (Object o : Json.list(root.get("markers"))) {
            Map<String, Object> m = Json.obj(o);
            int seg = (int) Json.num(m, "segment", 0);
            if (seg < 0 || seg >= p.segments()) continue;
            p.markers.add(new Marker(seg, Json.num(m, "t", 0), Json.str(m, "action", ""), Json.num(m, "value", 0)));
        }
        return p;
    }
}
