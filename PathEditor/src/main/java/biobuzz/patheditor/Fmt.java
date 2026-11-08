package biobuzz.patheditor;

import java.util.Locale;

public final class Fmt {
    private Fmt() {}

    public static String num(double v, int places) {
        String s = String.format(Locale.ROOT, "%." + places + "f", v);
        if (s.indexOf('.') >= 0) {
            int end = s.length();
            while (s.charAt(end - 1) == '0') end--;
            if (s.charAt(end - 1) == '.') end--;
            s = s.substring(0, end);
        }
        return s.equals("-0") ? "0" : s;
    }

    public static double norm180(double deg) {
        double d = deg % 360;
        if (d > 180) d -= 360;
        if (d <= -180) d += 360;
        return d;
    }

    public static double norm360(double deg) {
        double d = deg % 360;
        return d < 0 ? d + 360 : d;
    }
}
