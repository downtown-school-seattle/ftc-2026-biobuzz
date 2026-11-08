package biobuzz.patheditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public final class FieldPanel extends JPanel {
    public enum Tool {SELECT, WAYPOINT, MARKER}

    public interface Listener {
        void pathChanged();

        void selectionChanged();
    }

    private static final int MARGIN = 20;
    private static final double HIT_POINT = 10;
    private static final double HIT_LINE = 8;
    private static final Color[] PALETTE = {
            new Color(0xE8833A), new Color(0x3AA6E8), new Color(0x7BC96F), new Color(0xC47BE0), new Color(0xE8C93A), new Color(0xE85A7B)
    };

    private final Config cfg;
    private AutoPath path;
    private Listener listener;
    private Tool tool = Tool.SELECT;
    private String markerAction;
    private int selectedWaypoint = -1;
    private AutoPath.Marker selectedMarker;
    private boolean draggingWaypoint;
    private boolean draggingMarker;
    private Playback playback;
    private double playTime;
    private BufferedImage image;
    private double scale = 1;
    private double originX;
    private double originY;

    public FieldPanel(Config cfg, AutoPath path) {
        this.cfg = cfg;
        this.path = path;
        this.markerAction = cfg.actions.isEmpty() ? "" : cfg.actions.get(0).id;
        setBackground(new Color(0x1B1D21));
        setFocusable(true);
        loadImage();
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                press(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                drag(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingWaypoint = false;
                draggingMarker = false;
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) deleteSelection();
            }
        });
    }

    private void loadImage() {
        image = null;
        if (cfg.fieldImage == null || cfg.fieldImage.isEmpty()) return;
        try {
            File f = new File(cfg.fieldImage);
            if (f.exists()) image = ImageIO.read(f);
        } catch (Exception ignored) {
            image = null;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setPath(AutoPath path) {
        this.path = path;
        selectedWaypoint = -1;
        selectedMarker = null;
        notifySelection();
        repaint();
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public void setMarkerAction(String id) {
        this.markerAction = id;
    }

    public void setPlayback(Playback playback, double time) {
        this.playback = playback;
        this.playTime = time;
        repaint();
    }

    public int getSelectedWaypoint() {
        return selectedWaypoint;
    }

    public AutoPath.Marker getSelectedMarker() {
        return selectedMarker;
    }

    public void deleteSelection() {
        if (selectedMarker != null) {
            path.markers.remove(selectedMarker);
        } else if (selectedWaypoint >= 0) {
            path.removeWaypoint(selectedWaypoint);
        } else {
            return;
        }
        selectedMarker = null;
        selectedWaypoint = -1;
        notifyChanged();
        notifySelection();
    }

    private void notifyChanged() {
        repaint();
        if (listener != null) listener.pathChanged();
    }

    private void notifySelection() {
        if (listener != null) listener.selectionChanged();
    }

    private double sx(double x) {
        return originX + (x + cfg.fieldWidth / 2) * scale;
    }

    private double sy(double y) {
        return originY + (cfg.fieldHeight / 2 - y) * scale;
    }

    private double fx(double px) {
        return (px - originX) / scale - cfg.fieldWidth / 2;
    }

    private double fy(double py) {
        return cfg.fieldHeight / 2 - (py - originY) / scale;
    }

    private void layoutField() {
        scale = Math.min((getWidth() - 2.0 * MARGIN) / cfg.fieldWidth, (getHeight() - 2.0 * MARGIN) / cfg.fieldHeight);
        originX = (getWidth() - cfg.fieldWidth * scale) / 2;
        originY = (getHeight() - cfg.fieldHeight * scale) / 2;
    }

    private double clampX(double x) {
        double lim = cfg.fieldWidth / 2 - robotInset();
        return Math.max(-lim, Math.min(lim, x));
    }

    private double clampY(double y) {
        double lim = cfg.fieldHeight / 2 - robotInset();
        return Math.max(-lim, Math.min(lim, y));
    }

    // keeps the robot's body (not just its center) inside the field walls
    private double robotInset() {
        return Math.max(cfg.robotWidth, cfg.robotLength) / 2;
    }

    private int hitWaypoint(double px, double py) {
        for (int i = path.waypoints.size() - 1; i >= 0; i--) {
            AutoPath.Waypoint w = path.waypoints.get(i);
            if (Math.hypot(sx(w.x) - px, sy(w.y) - py) <= HIT_POINT) return i;
        }
        return -1;
    }

    private AutoPath.Marker hitMarker(double px, double py) {
        for (int i = path.markers.size() - 1; i >= 0; i--) {
            AutoPath.Marker m = path.markers.get(i);
            if (Math.hypot(sx(path.x(m.segment, m.t)) - px, sy(path.y(m.segment, m.t)) - py) <= HIT_POINT) return m;
        }
        return null;
    }

    private int hitSegment(double px, double py) {
        int best = -1;
        double bestDist = HIT_LINE;
        for (int i = 0; i < path.segments(); i++) {
            AutoPath.Waypoint a = path.waypoints.get(i);
            AutoPath.Waypoint b = path.waypoints.get(i + 1);
            double ax = sx(a.x);
            double ay = sy(a.y);
            double dx = sx(b.x) - ax;
            double dy = sy(b.y) - ay;
            double len2 = dx * dx + dy * dy;
            double t = len2 == 0 ? 0 : Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / len2));
            double d = Math.hypot(ax + dx * t - px, ay + dy * t - py);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    private void press(MouseEvent e) {
        double px = e.getX();
        double py = e.getY();
        AutoPath.Marker marker = tool == Tool.WAYPOINT ? null : hitMarker(px, py);
        int wp = marker != null ? -1 : hitWaypoint(px, py);
        if (marker != null) {
            selectedMarker = marker;
            selectedWaypoint = -1;
            draggingMarker = true;
        } else if (wp >= 0) {
            selectedWaypoint = wp;
            selectedMarker = null;
            draggingWaypoint = true;
        } else if (tool == Tool.WAYPOINT) {
            int seg = hitSegment(px, py);
            if (seg >= 0) {
                selectedWaypoint = path.insertWaypoint(seg, path.project(seg, fx(px), fy(py)));
            } else {
                selectedWaypoint = path.addWaypoint(clampX(fx(px)), clampY(fy(py)));
            }
            selectedMarker = null;
            draggingWaypoint = true;
            notifyChanged();
        } else if (tool == Tool.MARKER) {
            int seg = hitSegment(px, py);
            Config.ActionDef def = cfg.action(markerAction);
            if (seg >= 0 && def != null) {
                selectedMarker = path.addMarker(seg, path.project(seg, fx(px), fy(py)), def.id, def.def);
                selectedWaypoint = -1;
                draggingMarker = true;
                notifyChanged();
            }
        } else {
            selectedMarker = null;
            selectedWaypoint = -1;
        }
        notifySelection();
        repaint();
    }

    private void drag(MouseEvent e) {
        if (draggingWaypoint && selectedWaypoint >= 0) {
            AutoPath.Waypoint w = path.waypoints.get(selectedWaypoint);
            w.x = clampX(fx(e.getX()));
            w.y = clampY(fy(e.getY()));
            notifyChanged();
            notifySelection();
        } else if (draggingMarker && selectedMarker != null) {
            selectedMarker.t = path.project(selectedMarker.segment, fx(e.getX()), fy(e.getY()));
            notifyChanged();
            notifySelection();
        }
    }

    private static boolean isTurn(Config.ActionDef def) {
        return "turn".equals(def.kind) || "turnDrive".equals(def.kind);
    }

    private Color colorFor(String actionId) {
        int idx = 0;
        for (int i = 0; i < cfg.actions.size(); i++) if (cfg.actions.get(i).id.equals(actionId)) idx = i;
        return PALETTE[idx % PALETTE.length];
    }

    private void drawRobot(Graphics2D g, double x, double y, double heading, Color fill, Color edge) {
        AffineTransform saved = g.getTransform();
        g.translate(sx(x), sy(y));
        g.rotate(-Math.toRadians(heading));
        double l = cfg.robotLength * scale;
        double w = cfg.robotWidth * scale;
        g.setColor(fill);
        g.fillRect((int) (-l / 2), (int) (-w / 2), (int) l, (int) w);
        g.setColor(edge);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect((int) (-l / 2), (int) (-w / 2), (int) l, (int) w);
        g.drawLine(0, 0, (int) (l / 2), 0);
        g.setTransform(saved);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        layoutField();
        int fxp = (int) Math.round(originX);
        int fyp = (int) Math.round(originY);
        int fw = (int) Math.round(cfg.fieldWidth * scale);
        int fh = (int) Math.round(cfg.fieldHeight * scale);
        g.setColor(new Color(0x2A2D33));
        g.fillRect(fxp, fyp, fw, fh);
        if (image != null) g.drawImage(image, fxp, fyp, fw, fh, null);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(255, 255, 255, 30));
        if (cfg.gridSize > 0) {
            for (double x = 0; x <= cfg.fieldWidth + 1e-6; x += cfg.gridSize) {
                int px = (int) Math.round(originX + x * scale);
                g.drawLine(px, fyp, px, fyp + fh);
            }
            for (double y = 0; y <= cfg.fieldHeight + 1e-6; y += cfg.gridSize) {
                int py = (int) Math.round(originY + y * scale);
                g.drawLine(fxp, py, fxp + fw, py);
            }
        }
        g.setColor(new Color(255, 255, 255, 110));
        g.drawRect(fxp, fyp, fw, fh);

        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < path.segments(); i++) {
            AutoPath.Waypoint a = path.waypoints.get(i);
            AutoPath.Waypoint b = path.waypoints.get(i + 1);
            g.setColor(new Color(0xD9DCE3));
            g.drawLine((int) sx(a.x), (int) sy(a.y), (int) sx(b.x), (int) sy(b.y));
        }

        if (!path.waypoints.isEmpty()) {
            AutoPath.Waypoint s = path.waypoints.get(0);
            drawRobot(g, s.x, s.y, path.startHeading, new Color(255, 255, 255, 25), new Color(255, 255, 255, 90));
        }
        if (selectedWaypoint >= 0) {
            AutoPath.Waypoint w = path.waypoints.get(selectedWaypoint);
            for (Config.ActionDef def : cfg.actions) {
                Double h = w.actions.get(def.id);
                if (h != null && isTurn(def)) {
                    Color c = colorFor(def.id);
                    drawRobot(g, w.x, w.y, h, new Color(c.getRed(), c.getGreen(), c.getBlue(), 40), c);
                }
            }
        }
        for (AutoPath.Marker m : path.markers) {
            Config.ActionDef def = cfg.action(m.action);
            if (def != null && isTurn(def) && m == selectedMarker) {
                Color c = colorFor(m.action);
                drawRobot(g, path.x(m.segment, m.t), path.y(m.segment, m.t), m.value, new Color(c.getRed(), c.getGreen(), c.getBlue(), 40), c);
            }
        }

        g.setFont(getFont().deriveFont(Font.BOLD, 11f));
        for (int i = 0; i < path.waypoints.size(); i++) {
            AutoPath.Waypoint w = path.waypoints.get(i);
            int r = 7;
            g.setColor(i == selectedWaypoint ? new Color(0xFFFFFF) : new Color(0x8A93A6));
            g.fillOval((int) sx(w.x) - r, (int) sy(w.y) - r, 2 * r, 2 * r);
            g.setColor(new Color(0x1B1D21));
            g.drawString(i == 0 ? "S" : String.valueOf(i + 1), (int) sx(w.x) - (i >= 9 ? 6 : 3), (int) sy(w.y) + 4);
            int row = 0;
            for (Config.ActionDef def : cfg.actions) {
                Double v = w.actions.get(def.id);
                if (v == null) continue;
                String text = def.label + " " + Fmt.num(v, 1);
                int tx = (int) sx(w.x) + 12;
                int ty = (int) sy(w.y) - 8 + row * 14;
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(tx - 2, ty - 11, g.getFontMetrics().stringWidth(text) + 6, 14);
                g.setColor(colorFor(def.id));
                g.drawString(text, tx, ty);
                row++;
            }
        }
        for (AutoPath.Marker m : path.markers) {
            Config.ActionDef def = cfg.action(m.action);
            double px = sx(path.x(m.segment, m.t));
            double py = sy(path.y(m.segment, m.t));
            Path2D.Double d = new Path2D.Double();
            d.moveTo(px, py - 9);
            d.lineTo(px + 9, py);
            d.lineTo(px, py + 9);
            d.lineTo(px - 9, py);
            d.closePath();
            g.setColor(colorFor(m.action));
            g.fill(d);
            g.setColor(m == selectedMarker ? Color.WHITE : new Color(0x1B1D21));
            g.setStroke(new BasicStroke(m == selectedMarker ? 2.5f : 1.5f));
            g.draw(d);
            if (def != null) {
                g.setColor(Color.WHITE);
                g.drawString(def.label + " " + Fmt.num(m.value, 1), (int) px + 12, (int) py - 10);
            }
        }

        if (playback != null) {
            double[] pose = playback.pose(playTime);
            drawRobot(g, pose[0], pose[1], pose[2], new Color(0x3AA6E8, false).brighter(), Color.WHITE);
            String status = playback.status(playTime);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g.drawString(String.format("t=%.2fs / %.2fs   x=%.1f y=%.1f h=%.0f   %s", playTime, playback.total, pose[0], pose[1], Fmt.norm360(pose[2]), status), 10, getHeight() - 8);
        }
    }
}
