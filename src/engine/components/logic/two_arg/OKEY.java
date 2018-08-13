package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Pin;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

import static engine.LogicLevel.*;

public class OKEY extends Gate2to1 {

    // initialization
    public OKEY(ControlMain control) {
        super(control);
    }
    public OKEY(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        inA = new Pin(this, true, 1, 0, 1);
        inB = new Pin(this, true, 1, 2, 2);
        out = new Pin(this, false, 1, 4, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(out);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        out.put(function(inA.get()[0], inB.get()[0]));
    }
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        if (b == NIL || b == POS) return a;
        else return ZZZ;
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 1);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 1);
    }

}
