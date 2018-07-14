package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Clock extends Component {

    public static final double DEF_FREQUENCY = 2; // Hz

    private int phase, dir;
    private Pin drain;

    // initialization
    public Clock(ControlMain control) {
        super(control);
        phase = 0;
        dir = 1;
    }
    public Clock(ControlMain control, Element data) {
        super(control, data);
        phase = 0;
        dir = 1;
    }
    @Override protected HashSet<Pin> initPins() {
        drain = new Pin(this, false, 4, 2);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(drain);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        // compute next impulse
        if (Math.abs(phase) == 1) dir *= -1;
        phase += dir;
        drain.put(LogicLevel.parseValue(phase));
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.takeClockIntoAccount();
        summary.addInput(LogicLevel.NIL, 1);
    }

}
