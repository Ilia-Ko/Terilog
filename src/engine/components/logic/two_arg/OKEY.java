package engine.components.logic.two_arg;

import engine.LogicLevel;
import engine.components.Pin;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

public class OKEY extends Gate2to1 {

    // initialization
    public OKEY(ControlMain control) {
        super(control);
    }
    public OKEY(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        inA = new Pin(this, true, false, 0, 1);
        inB = new Pin(this, true, false, 2, 2);
        out = new Pin(this, false, true, 4, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(out);
        return pins;
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        if (a == b) return LogicLevel.ZZZ;
        else return a;
    }

}
