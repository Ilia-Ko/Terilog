package engine;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum LogicLevel {

    POS("HIGH", "TRUE",     "+1",   '\u0031', +1, true,  Color.rgb(128, 64, 0)) {
        @Override public boolean conflicts(LogicLevel signal) {
            return signal != ZZZ && signal != POS;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal == ZZZ;
        }
    },
    NIL("MID",  "UNKNOWN",  "0",    '\u0030',  0, true,  Color.rgb(0, 128, 0)) {
        @Override public boolean conflicts(LogicLevel signal) {
            return signal != ZZZ && signal != NIL;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal == ZZZ;
        }
    },
    NEG("LOW",  "FALSE",    "-1",   '\u03bb', -1, true,  Color.rgb(0, 64, 128)) {
        @Override public boolean conflicts(LogicLevel signal) {
            return signal != ZZZ && signal != NEG;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal == ZZZ;
        }
    },
    ZZZ("Z",    "N/A",      "n/a",  '\u2205',  0, false, Color.rgb(0, 0, 0)) {
        @Override public boolean conflicts(LogicLevel signal) {
            return false;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return false;
        }
    }, // undefined, non-critical, like QNaN in FPU
    ERR("E",    "NAN",      "NaN",  '!',      22, false, Color.rgb(255, 0, 0)) {
        @Override public boolean conflicts(LogicLevel signal) {
            return true;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal != ERR;
        }
    }; // undefined, critical, like SNaN in FPU

    private String standardName, boolName, mathName;
    private char digit;
    private int voltage;
    private Paint colour;
    private boolean stable;

    LogicLevel(String name0, String name1, String name2, char name3, int val, boolean isStable, Paint col) {
        standardName = name0;
        boolName = name1;
        mathName = name2;
        digit = name3;
        voltage = val;
        stable = isStable;
        colour = col;
    }

    // names
    public String getStandartName() {
        return standardName;
    }
    public String getBooleanName() {
        return boolName;
    }
    public String getMathematicalName() {
        return mathName;
    }
    public char getDigitCharacter() {
        return digit;
    }

    // useful info
    public int volts() {
        return voltage;
    }
    public Paint colour() {
        return colour;
    }
    public boolean stable() {
        return stable;
    }

    // signal interaction
    public abstract boolean conflicts(LogicLevel signal);
    public abstract boolean suppresses(LogicLevel signal);

}
