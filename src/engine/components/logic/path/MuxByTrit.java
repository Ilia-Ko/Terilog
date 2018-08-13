package engine.components.logic.path;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;

import static engine.LogicLevel.ZZZ;

public class MuxByTrit extends GatePath {

    private Pin inNEG, inNIL, inPOS, out, sel;

    // initialization
    public MuxByTrit(ControlMain control) {
        super(control);
    }
    public MuxByTrit(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        int cap = (capacity == null) ? 1 : capacity.get();
        inNEG = new Pin(this, true, cap, 0, 1);
        inNIL = new Pin(this, true, cap, 0, 4);
        inPOS = new Pin(this, true, cap, 0, 7);
        out = new Pin(this, false, cap, 2, 4);
        sel = new Pin(this, true, 1, 1, 8);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(inNEG);
        pins.add(inNIL);
        pins.add(inPOS);
        pins.add(out);
        pins.add(sel);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] res;
        switch (sel.get()[0]) {
            case NEG:
                res = inNEG.get();
                break;
            case NIL:
                res = inNIL.get();
                break;
            case POS:
                res = inPOS.get();
                break;
            default:
                res = new LogicLevel[capacity.get()];
                Arrays.fill(res, ZZZ);
        }
        for (int i = 0; i < capacity.get(); i++) res[i] = STI.func(res[i]);
        out.put(res);
    }

    // countdown
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        super.itIsAFinalCountdown(summary);
        DecoderTrit.countdown(summary);
    }
    @Override protected void singleCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        CKEY.countdown(summary);
        CKEY.countdown(summary);
        CKEY.countdown(summary);
        STI.countdown(summary);
        STI.countdown(summary);
        STI.countdown(summary);
    }

}
