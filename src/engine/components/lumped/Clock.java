package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Clock extends Component {

    public static final double DEF_FREQUENCY = 2; // Hz

    private int phase;
    private Pin drain;

    // initialization
    public Clock(ControlMain control) {
        super(control);
        phase = 0;
    }
    public Clock(ControlMain control, Element data) {
        super(control, data);
        phase = 0;
    }
    @Override protected HashSet<Pin> initPins() {
        drain = new Pin(this, Pin.OUT, 4, 2);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(drain);
        return pins;
    }

    // simulation
    @Override public boolean isEntryPoint() {
        return true;
    }
    @Override public HashSet<Node> simulate() {
        // compute next impulse
        phase++;
        if (phase == 2) phase = -1;
        LogicLevel impulse = LogicLevel.parseValue(phase);

        HashSet<Node> affected = new HashSet<>();
        if (drain.update(impulse)) affected.add(drain.getNode());
        return affected;
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.takeClockIntoAccount();
        summary.addInput(LogicLevel.NIL);
    }

}
