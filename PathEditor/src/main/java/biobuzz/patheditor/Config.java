package biobuzz.patheditor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Config {
    public static final class ActionDef {
        public String id;
        public String label;
        public String kind;
        public String unit;
        public String template;
        public double def;

        @Override
        public String toString() {
            return label;
        }
    }

    public String game;
    public double fieldWidth;
    public double fieldHeight;
    public double gridSize;
    public String fieldImage;
    public double robotWidth;
    public double robotLength;
    public double driveSpeed;
    public double turnSpeed;
    public String indent;
    public String runTemplate;
    public String driveTemplate;
    public String turnTemplate;
    public double driveSign;
    public double turnSign;
    public final List<ActionDef> actions = new ArrayList<>();

    public static Config load(File file) throws IOException {
        Map<String, Object> root = Json.obj(Json.parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
        Config c = new Config();
        c.game = Json.str(root, "game", "");
        Map<String, Object> field = Json.obj(root.get("field"));
        c.fieldWidth = Json.num(field, "width", 144);
        c.fieldHeight = Json.num(field, "height", 144);
        c.gridSize = Json.num(field, "gridSize", 24);
        c.fieldImage = Json.str(field, "image", "");
        Map<String, Object> robot = Json.obj(root.get("robot"));
        c.robotWidth = Json.num(robot, "width", 18);
        c.robotLength = Json.num(robot, "length", 18);
        Map<String, Object> motion = Json.obj(root.get("motion"));
        c.driveSpeed = Json.num(motion, "driveSpeed", 40);
        c.turnSpeed = Json.num(motion, "turnSpeed", 180);
        Map<String, Object> export = Json.obj(root.get("export"));

        // auto code stuff
        c.indent = Json.str(export, "indent", "    ");
        c.runTemplate = Json.str(export, "run", "run({commands});");
        c.driveTemplate = Json.str(export, "drive", "driveCmd({degrees}, {inches})");
        c.turnTemplate = Json.str(export, "turn", "turnCmd({degrees})");

        c.driveSign = Json.num(export, "driveSign", 1);
        c.turnSign = Json.num(export, "turnSign", 1);
        for (Object o : Json.list(root.get("actions"))) {
            Map<String, Object> a = Json.obj(o);
            ActionDef d = new ActionDef();
            d.id = Json.str(a, "id", "");
            d.label = Json.str(a, "label", d.id);
            d.kind = Json.str(a, "kind", "command");
            d.unit = Json.str(a, "unit", "");
            d.template = Json.str(a, "template", "");
            d.def = Json.num(a, "default", 0);
            c.actions.add(d);
        }
        return c;
    }

    public ActionDef action(String id) {
        for (ActionDef a : actions) if (a.id.equals(id)) return a;
        return null;
    }
}
