package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

import static engine.LogicLevel.*;

public class CMP extends Gate2to1 {

    // initialization
    public CMP(ControlMain control) {
        super(control);
    }
    public CMP(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = super.initPins();
        pins.remove(out);
        out = new Pin(this, false, 1, 4, 5);
        pins.add(out);
        return pins;
    }
    @Override protected HashSet<Pin> getDependentPins() {
        HashSet<Pin> dep = getPins();
        dep.remove(out);
        return dep;
    }

    @Override public void simulate() {
        LogicLevel[] a = inA.get();
        LogicLevel[] b = inB.get();
        LogicLevel res = NIL;

        int cap = capacity.get();
        for (int i = cap - 1; i >= 0; i--) {
            if (a[i].isUnstable() || b[i].isUnstable()) {
                if (a[i] == b[i]) res = a[i];
                else res = ZZZ;
            } else if (a[i].volts() > b[i].volts()) {
                res = POS;
                break;
            } else if (a[i].volts() < b[i].volts()) {
                res = NEG;
                break;
            }
        }
        out.put(res);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return NANY.func(LogicLevel.parseValue(-a.volts()), b);
    }
    @Override protected void singleCountdown(Circuit.Summary summary) {
        NANY.countdown(summary);
        STI.countdown(summary);
    }

}
