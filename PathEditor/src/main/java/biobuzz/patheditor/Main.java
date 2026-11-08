package biobuzz.patheditor;

import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        File configFile = new File(args.length > 0 ? args[0] : "editor-config.json");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            try {
                new EditorFrame(Config.load(configFile)).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Could not load " + configFile.getAbsolutePath() + "\n" + ex.getMessage(), "Path Editor", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
