package engine.components.logic.path;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import engine.components.lumped.Reconciliator;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;

import static engine.LogicLevel.NIL;
import static engine.LogicLevel.ZZZ;

public class DemuxByTrit extends GatePath {

    private Pin in, outNEG, outNIL, outPOS, sel;

    // initialization
    public DemuxByTrit(ControlMain control) {
        super(control);
    }
    public DemuxByTrit(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        int cap = (capacity == null) ? 1 : capacity.get();
        in = new Pin(this, true, cap, 0, 4);
        sel = new Pin(this, true, 1, 1, 8);
        outNEG = new Pin(this, false, cap, 2, 1);
        outNIL = new Pin(this, false, cap, 2, 4);
        outPOS = new Pin(this, false, cap, 2, 7);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(sel);
        pins.add(outNEG);
        pins.add(outNIL);
        pins.add(outPOS);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] nils = new LogicLevel[capacity.get()];
        LogicLevel[] zzzs = new LogicLevel[capacity.get()];
        Arrays.fill(nils, NIL);
        Arrays.fill(zzzs, ZZZ);

        switch (sel.get()[0]) {
            case NEG:
                outNEG.put(in.get());
                outNIL.put(nils);
                outPOS.put(nils);
                break;
            case NIL:
                outNEG.put(nils);
                outNIL.put(in.get());
                outPOS.put(nils);
                break;
            case POS:
                outNEG.put(nils);
                outNIL.put(nils);
                outPOS.put(in.get());
                break;
            default:
                outNEG.put(zzzs);
                outNIL.put(zzzs);
                outPOS.put(zzzs);
                break;
        }
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
        Reconciliator.countdown(summary);
        Reconciliator.countdown(summary);
        Reconciliator.countdown(summary);
    }

}
