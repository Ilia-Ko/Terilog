package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.BusComponent;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import engine.components.memory.Flat;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;

public class Adder extends BusComponent {

    private Pin Cin, Cout;
    private Pin inA, inB, out;

    // initialization
    public Adder(ControlMain control) {
        super(control, true);
    }
    public Adder(ControlMain control, Element data) {
        super(control, true, data);
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        Cin = new Pin(this, true, 1, 2, 0);
        Cout = new Pin(this, false, 1, 2, 10);
        pins.add(Cin);
        pins.add(Cout);

        inA = new Pin(this, true, 6, 0, 2);
        inB = new Pin(this, true, 6, 0, 8);
        out = new Pin(this, false, 6, 4, 5);
        pins.add(inA);
        pins.add(inB);
        pins.add(out);

        return pins;
    }

    // simulation
    @Override public void simulate() {
        int cap = capacity.get();
        long a = Flat.encode(inA.get(), cap);
        long b = Flat.encode(inB.get(), cap);
        long c = a + b + Cin.get()[0].volts();

        LogicLevel[] res = Flat.decode(c, cap + 1);
        out.put(Arrays.copyOfRange(res, 0, cap));
        Cout.put(res[cap]);
    }

    // countdown
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        super.itIsAFinalCountdown(summary);
        STI.countdown(summary);
        STI.countdown(summary);
    }
    @Override protected void singleCountdown(Circuit.Summary summary) {
        AdderTritFull.countdown(summary);
    }

}
