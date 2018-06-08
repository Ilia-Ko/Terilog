package engine;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum LogicLevel {

    POS("HIGH", "TRUE",     "+1",   '\u0031', +1, Color.rgb(128, 64, 0)),
    NIL("MID",  "UNKNOWN",  "0",    '\u0030',  0, Color.rgb(0, 128, 0)),
    NEG("LOW",  "FALSE",    "-1",   '\u03bb', -1, Color.rgb(0, 64, 128)),
    ZZZ("Z",    "NAN",      "NaN",  '\u2205',  0, Color.rgb(255, 0, 0));

    private String standartName, boolName, mathName;
    private char digit;
    private int voltage;
    private Paint colour;

    LogicLevel(String name0, String name1, String name2, char name3, int val, Paint col) {
        standartName = name0;
        boolName = name1;
        mathName = name2;
        digit = name3;
        voltage = val;
        colour = col;
    }

    public int volts() {
        return voltage;
    }
    public Paint colour() {
        return colour;
    }

}
