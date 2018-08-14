package engine.components.logic.two_arg;

import engine.LogicLevel;
import engine.components.BusComponent;
import engine.components.Pin;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

import static engine.LogicLevel.ZZZ;

abstract class Gate2to1 extends BusComponent {

    private Pin inA, inB, out;

    // initialization
    Gate2to1(ControlMain control) {
        super(control, true);
    }
    Gate2to1(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        int cap = (capacity == null) ? 1 : capacity.get();
        inA = new Pin(this, true, cap, 0, 2);
        inB = new Pin(this, true, cap, 0, 8);
        out = new Pin(this, false, cap, 4, 5);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(out);
        return pins;
    }
    @Override protected HashSet<Pin> getDependentPins() {
        return getPins();
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] a = inA.get();
        LogicLevel[] b = inB.get();
        LogicLevel[] c = new LogicLevel[capacity.get()];

        for (int i = 0; i < capacity.get(); i++) {
            if (a[i].isUnstable() || b[i].isUnstable()) {
                if (a == b) c[i] = a[i];
                else c[i] = ZZZ;
            } else c[i] = function(a[i], b[i]);
        }
        out.put(c);
    }
    abstract LogicLevel function(LogicLevel a, LogicLevel b);

}
