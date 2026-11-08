package biobuzz.patheditor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class EditorFrame extends JFrame implements FieldPanel.Listener {
    private static final int SLIDER_STEPS = 1000;

    private final Config cfg;
    private AutoPath path = new AutoPath();
    private final FieldPanel field;
    private final File pathsDir = new File("paths");
    private File currentFile;
    private Playback playback;
    private double playTime;
    private final Timer timer;
    private long lastTick;
    private boolean refreshing;

    private final JTextField nameField = new JTextField(12);
    private final JSpinner startHeading = new JSpinner(new SpinnerNumberModel(90.0, -360.0, 360.0, 5.0));
    private final JSpinner startX = new JSpinner(new SpinnerNumberModel(0.0, -72.0, 72.0, 0.5));
    private final JSpinner startY = new JSpinner(new SpinnerNumberModel(0.0, -72.0, 72.0, 0.5));
    private final JLabel summary = new JLabel(" ");
    private final JSlider slider = new JSlider(0, SLIDER_STEPS, 0);
    private final JButton playButton = new JButton("Play");
    private final CardLayout inspectorCards = new CardLayout();
    private final JPanel inspector = new JPanel(inspectorCards);
    private final List<JCheckBox> pointChecks = new ArrayList<>();
    private final List<JSpinner> pointValues = new ArrayList<>();
    private final JLabel waypointTitle = new JLabel();
    private final JSpinner wpX = new JSpinner(new SpinnerNumberModel(0.0, -1000.0, 1000.0, 0.5));
    private final JSpinner wpY = new JSpinner(new SpinnerNumberModel(0.0, -1000.0, 1000.0, 0.5));
    private final JComboBox<Config.ActionDef> markerAction = new JComboBox<>();
    private final JSpinner markerValue = new JSpinner(new SpinnerNumberModel(0.0, -100000.0, 100000.0, 1.0));
    private final JLabel markerUnit = new JLabel();
    private final JSpinner markerT = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1.0, 0.01));
    private final JComboBox<Config.ActionDef> toolAction = new JComboBox<>();

    public EditorFrame(Config cfg) {
        super("Path Editor - " + cfg.game);
        this.cfg = cfg;
        field = new FieldPanel(cfg, path);
        field.setListener(this);
        for (Config.ActionDef a : cfg.actions) {
            markerAction.addItem(a);
            toolAction.addItem(a);
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(field, BorderLayout.CENTER);
        add(buildSidebar(), BorderLayout.EAST);
        timer = new Timer(16, e -> tick());
        wireInspector();
        pathsDir.mkdirs();
        rebuild();
        setSize(1200, 820);
        setLocationRelativeTo(null);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        ButtonGroup group = new ButtonGroup();
        JToggleButton select = new JToggleButton("Select", true);
        JToggleButton point = new JToggleButton("Add point");
        JToggleButton marker = new JToggleButton("Add marker");
        group.add(select);
        group.add(point);
        group.add(marker);
        select.addActionListener(e -> field.setTool(FieldPanel.Tool.SELECT));
        point.addActionListener(e -> field.setTool(FieldPanel.Tool.WAYPOINT));
        marker.addActionListener(e -> field.setTool(FieldPanel.Tool.MARKER));
        toolAction.addActionListener(e -> {
            Config.ActionDef a = (Config.ActionDef) toolAction.getSelectedItem();
            if (a != null) field.setMarkerAction(a.id);
        });
        if (!cfg.actions.isEmpty()) field.setMarkerAction(cfg.actions.get(0).id);
        JButton open = new JButton("Open");
        JButton save = new JButton("Save");
        JButton export = new JButton("Export Java");
        JButton fresh = new JButton("New");
        open.addActionListener(e -> openFile());
        save.addActionListener(e -> saveFile());
        export.addActionListener(e -> showExport());
        fresh.addActionListener(e -> newPath());
        bar.add(fresh);
        bar.add(open);
        bar.add(save);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(select);
        bar.add(point);
        bar.add(marker);
        bar.add(toolAction);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(export);
        return bar;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        side.setPreferredSize(new Dimension(260, 0));

        JPanel pathBox = new JPanel(new GridLayout(0, 2, 4, 4));
        pathBox.setBorder(BorderFactory.createTitledBorder("Path"));
        pathBox.add(new JLabel("Name"));
        pathBox.add(nameField);
        pathBox.add(new JLabel("Start X (in)"));
        pathBox.add(startX);
        pathBox.add(new JLabel("Start Y (in)"));
        pathBox.add(startY);
        pathBox.add(new JLabel("Start heading"));
        pathBox.add(startHeading);
        side.add(pathBox);
        side.add(Box.createVerticalStrut(8));

        JPanel none = new JPanel(new BorderLayout());
        JLabel hint = new JLabel("<html>Add point: click empty field to extend, click a line to split it. Tick actions on a point to run them when the robot gets there.<br><br>"
                + "Add marker: click a line to run the selected action partway along it.<br><br>"
                + "Select: drag points and markers. Delete removes the selection.</html>");
        hint.setVerticalAlignment(JLabel.TOP);
        none.add(hint, BorderLayout.NORTH);

        JPanel wp = new JPanel(new GridLayout(0, 2, 4, 4));
        wp.add(waypointTitle);
        wp.add(new JLabel());
        wp.add(new JLabel("X (in)"));
        wp.add(wpX);
        wp.add(new JLabel("Y (in)"));
        wp.add(wpY);
        for (Config.ActionDef a : cfg.actions) {
            JCheckBox check = new JCheckBox(a.label);
            JSpinner value = new JSpinner(new SpinnerNumberModel(a.def, -100000.0, 100000.0, stepFor(a)));
            check.addActionListener(e -> editPointAction(a, check, value));
            value.addChangeListener(e -> editPointAction(a, check, value));
            pointChecks.add(check);
            pointValues.add(value);
            wp.add(check);
            wp.add(value);
        }
        JPanel wpWrap = new JPanel(new BorderLayout());
        wpWrap.add(wp, BorderLayout.NORTH);

        JPanel mk = new JPanel(new GridLayout(0, 2, 4, 4));
        mk.add(new JLabel("Action"));
        mk.add(markerAction);
        mk.add(markerUnit);
        mk.add(markerValue);
        mk.add(new JLabel("Position on line"));
        mk.add(markerT);
        JPanel mkWrap = new JPanel(new BorderLayout());
        mkWrap.add(mk, BorderLayout.NORTH);

        inspector.setBorder(BorderFactory.createTitledBorder("Selection"));
        inspector.add(none, "none");
        inspector.add(wpWrap, "waypoint");
        inspector.add(mkWrap, "marker");
        inspector.setPreferredSize(new Dimension(240, 240));
        inspector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        side.add(inspector);
        side.add(Box.createVerticalStrut(8));

        JPanel play = new JPanel(new BorderLayout(4, 4));
        play.setBorder(BorderFactory.createTitledBorder("Simulation"));
        play.add(slider, BorderLayout.CENTER);
        play.add(playButton, BorderLayout.EAST);
        play.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        side.add(play);
        side.add(Box.createVerticalStrut(8));
        summary.setFont(summary.getFont().deriveFont(Font.PLAIN, 12f));
        side.add(summary);
        side.add(Box.createVerticalGlue());

        slider.addChangeListener(e -> {
            if (refreshing) return;
            playTime = playback.total * slider.getValue() / SLIDER_STEPS;
            field.setPlayback(playback, playTime);
        });
        playButton.addActionListener(e -> togglePlay());
        nameField.addActionListener(e -> path.name = nameField.getText().trim());
        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                path.name = nameField.getText().trim();
            }
        });
        startX.addChangeListener(e -> editStart());
        startY.addChangeListener(e -> editStart());
        startHeading.addChangeListener(e -> {
            if (refreshing) return;
            path.startHeading = ((Number) startHeading.getValue()).doubleValue();
            rebuild();
        });
        return side;
    }

    private void wireInspector() {
        wpX.addChangeListener(e -> editWaypoint());
        wpY.addChangeListener(e -> editWaypoint());
        markerAction.addActionListener(e -> editMarker(true));
        markerValue.addChangeListener(e -> editMarker(false));
        markerT.addChangeListener(e -> editMarker(false));
    }

    private void editStart() {
        if (refreshing) return;
        double x = ((Number) startX.getValue()).doubleValue();
        double y = ((Number) startY.getValue()).doubleValue();
        if (path.waypoints.isEmpty()) {
            path.addWaypoint(x, y);
        } else {
            path.waypoints.get(0).x = x;
            path.waypoints.get(0).y = y;
        }
        rebuild();
        refreshInspector();
        field.repaint();
    }

    private void syncStart() {
        boolean was = refreshing;
        refreshing = true;
        if (!path.waypoints.isEmpty()) {
            startX.setValue(path.waypoints.get(0).x);
            startY.setValue(path.waypoints.get(0).y);
        }
        refreshing = was;
    }

    private void editWaypoint() {
        if (refreshing) return;
        int i = field.getSelectedWaypoint();
        if (i < 0) return;
        AutoPath.Waypoint w = path.waypoints.get(i);
        w.x = ((Number) wpX.getValue()).doubleValue();
        w.y = ((Number) wpY.getValue()).doubleValue();
        rebuild();
        field.repaint();
    }

    private static double stepFor(Config.ActionDef a) {
        return "rpm".equals(a.unit) ? 100 : "deg".equals(a.unit) ? 5 : 0.5;
    }

    private void editPointAction(Config.ActionDef a, JCheckBox check, JSpinner value) {
        if (refreshing) return;
        int i = field.getSelectedWaypoint();
        if (i < 0) return;
        AutoPath.Waypoint w = path.waypoints.get(i);
        if (check.isSelected()) w.actions.put(a.id, ((Number) value.getValue()).doubleValue());
        else w.actions.remove(a.id);
        rebuild();
        field.repaint();
    }

    private void editMarker(boolean actionChanged) {
        if (refreshing) return;
        AutoPath.Marker m = field.getSelectedMarker();
        if (m == null) return;
        if (actionChanged) {
            Config.ActionDef a = (Config.ActionDef) markerAction.getSelectedItem();
            if (a != null && !a.id.equals(m.action)) {
                m.action = a.id;
                m.value = a.def;
            }
            refreshInspector();
        } else {
            m.value = ((Number) markerValue.getValue()).doubleValue();
            m.t = ((Number) markerT.getValue()).doubleValue();
        }
        rebuild();
        field.repaint();
    }

    private void refreshInspector() {
        refreshing = true;
        try {
            AutoPath.Marker m = field.getSelectedMarker();
            int wp = field.getSelectedWaypoint();
            if (m != null) {
                Config.ActionDef a = cfg.action(m.action);
                markerAction.setSelectedItem(a);
                markerValue.setValue(m.value);
                markerT.setValue(m.t);
                markerUnit.setText(a == null ? "Value" : "Value (" + a.unit + ")");
                ((SpinnerNumberModel) markerValue.getModel()).setStepSize(a == null ? 1 : stepFor(a));
                inspectorCards.show(inspector, "marker");
            } else if (wp >= 0) {
                AutoPath.Waypoint w = path.waypoints.get(wp);
                waypointTitle.setText(wp == 0 ? "Start" : "Point " + (wp + 1));
                wpX.setValue(w.x);
                wpY.setValue(w.y);
                for (int k = 0; k < cfg.actions.size(); k++) {
                    Config.ActionDef a = cfg.actions.get(k);
                    Double v = w.actions.get(a.id);
                    pointChecks.get(k).setSelected(v != null);
                    pointValues.get(k).setValue(v != null ? v : a.def);
                }
                inspectorCards.show(inspector, "waypoint");
            } else {
                inspectorCards.show(inspector, "none");
            }
        } finally {
            refreshing = false;
        }
    }

    private void rebuild() {
        syncStart();
        List<Step> steps = Compiler.compile(path, cfg);
        playback = new Playback(steps, path, cfg);
        playTime = Math.min(playTime, playback.total);
        field.setPlayback(playback, playTime);
        summary.setText(String.format("<html>%d points, %d markers<br>estimated %.1fs</html>", path.waypoints.size(), path.markers.size(), playback.total));
    }

    private void tick() {
        long now = System.nanoTime();
        playTime += (now - lastTick) / 1e9;
        lastTick = now;
        if (playTime >= playback.total) {
            playTime = playback.total;
            stopPlay();
        }
        field.setPlayback(playback, playTime);
        refreshing = true;
        slider.setValue(playback.total == 0 ? 0 : (int) Math.round(playTime / playback.total * SLIDER_STEPS));
        refreshing = false;
    }

    private void togglePlay() {
        if (timer.isRunning()) {
            stopPlay();
            return;
        }
        if (playTime >= playback.total) playTime = 0;
        lastTick = System.nanoTime();
        playButton.setText("Pause");
        timer.start();
    }

    private void stopPlay() {
        timer.stop();
        playButton.setText("Play");
    }

    @Override
    public void pathChanged() {
        rebuild();
    }

    @Override
    public void selectionChanged() {
        refreshInspector();
    }

    private void loadInto(AutoPath p) {
        stopPlay();
        path = p;
        playTime = 0;
        field.setPath(p);
        refreshing = true;
        nameField.setText(p.name);
        startHeading.setValue(p.startHeading);
        refreshing = false;
        rebuild();
    }

    private void newPath() {
        currentFile = null;
        loadInto(new AutoPath());
    }

    private JFileChooser chooser() {
        JFileChooser c = new JFileChooser(pathsDir);
        c.setFileFilter(new FileNameExtensionFilter("Path files (*.json)", "json"));
        return c;
    }

    private void openFile() {
        JFileChooser c = chooser();
        if (c.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            currentFile = c.getSelectedFile();
            loadInto(AutoPath.load(currentFile));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Open failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFile() {
        path.name = nameField.getText().trim().isEmpty() ? "auto" : nameField.getText().trim();
        if (currentFile == null) {
            JFileChooser c = chooser();
            c.setSelectedFile(new File(pathsDir, path.name + ".json"));
            if (c.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File f = c.getSelectedFile();
            currentFile = f.getName().endsWith(".json") ? f : new File(f.getPath() + ".json");
        }
        try {
            path.save(currentFile);
            setTitle("Path Editor - " + currentFile.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showExport() {
        String code = Compiler.export(Compiler.compile(path, cfg), cfg);
        JTextArea area = new JTextArea(code, 18, 70);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JDialog dialog = new JDialog(this, "Export", true);
        JButton copy = new JButton("Copy to clipboard");
        copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(code), null));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(copy);
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
