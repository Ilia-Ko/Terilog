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

import static engine.LogicLevel.ZZZ;

public class OKEY extends GatePath {

    private Pin in, out, keyP, keyN;

    // initialization
    public OKEY(ControlMain control) {
        super(control);
    }
    public OKEY(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        in = new Pin(this, true, 1, 0, 1);
        out = new Pin(this, false, 1, 4, 1);
        keyP = new Pin(this, true, 1, 2, 0);
        keyN = new Pin(this, true, 1, 2, 2);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(out);
        pins.add(keyP);
        pins.add(keyN);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] res = new LogicLevel[capacity.get()];

        if (keyP.get()[0].volts() >= 0 && keyN.get()[0].volts() <= 0) {
            LogicLevel[] inp = in.get();
            for (int i = 0; i < capacity.get(); i++) res[i] = STI.func(inp[i]);
        } else Arrays.fill(res, ZZZ);

        out.put(res);
    }
    @Override protected void singleCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 1);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 1);
        STI.countdown(summary);
        Reconciliator.countdown(summary);
    }

}
