package engine;

import javafx.scene.paint.Color;

public enum LogicLevel {

    POS("HIGH", '1',      +1, true,  Color.ORANGERED) {
        @Override public boolean conflicts(LogicLevel signal) {
            return signal != ZZZ && signal != POS;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal == ZZZ;
        }
    },
    NIL("MID",  '0',       0, true,  Color.FORESTGREEN) {
        @Override public boolean conflicts(LogicLevel signal) {
            return signal != ZZZ && signal != NIL;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal == ZZZ;
        }
    },
    NEG("LOW",  '\u03bb', -1, true,  Color.DEEPSKYBLUE) {
        @Override public boolean conflicts(LogicLevel signal) {
            return signal != ZZZ && signal != NEG;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal == ZZZ;
        }
    },
    ZZZ("Z",    '?',       2, false, Color.BLACK) {
        @Override public boolean conflicts(LogicLevel signal) {
            return false;
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return false;
        }
    }, // undefined, non-critical, like QNaN in FPU
    ERR("E",    '!',      22, false, Color.LIGHTGOLDENRODYELLOW) {
        @Override public boolean conflicts(LogicLevel signal) {
            return !signal.isUnstable();
        }
        @Override public boolean suppresses(LogicLevel signal) {
            return signal != ERR;
        }
    }; // undefined, critical, like SNaN in FPU

    public static final int HARD_VOLTAGE = 2;
    public static final int SOFT_VOLTAGE = 1;

    private String standardName;
    private char digit;
    private int voltage;
    private Color colour;
    private boolean unstable;

    LogicLevel(String name0, char symbol, int val, boolean isStable, Color col) {
        standardName = name0;
        digit = symbol;
        voltage = val;
        unstable = !isStable;
        colour = col;
    }

    // names
    public String getStandardName() {
        return standardName;
    }
    public char getDigitCharacter() {
        return digit;
    }
    public static LogicLevel parseName(String standardName) {
        for (LogicLevel signal : values())
            if (signal.getStandardName().equals(standardName))
                return signal;
        return null;
    }
    public static LogicLevel parseValue(int value) {
        for (LogicLevel signal : values())
            if (signal.voltage == value)
                return signal;
        return ERR;
    }
    public static LogicLevel parseDigit(char digit) {
        for (LogicLevel signal : values())
            if (signal.digit == digit)
                return signal;
        return null;
    }

    // useful info
    public int volts() {
        return voltage;
    }
    public Color colour() {
        return colour;
    }
    public boolean isUnstable() {
        return unstable;
    }

    // signal interaction
    public abstract boolean conflicts(LogicLevel signal);
    public abstract boolean suppresses(LogicLevel signal);

}
