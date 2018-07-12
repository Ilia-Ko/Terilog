package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

import static engine.LogicLevel.*;

public class Diode extends Component {

    private Pin anode, cathode;

    // initialization
    public Diode(ControlMain control) {
        super(control);
    }
    public Diode(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        anode = new Pin(this, false, 0, 1);
        cathode = new Pin(this, false, 4, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(anode);
        pins.add(cathode);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel a = anode.get();
        LogicLevel c = cathode.get();

        if (a == ERR || c == ERR) {
            anode.put(ERR);
            cathode.put(ERR);
        } else if (a == ZZZ && c == NEG)
            anode.put(NEG);
        else if (a == POS && c == ZZZ)
            cathode.put(POS);
        else if (a.volts() > c.volts()) {
            anode.put(ERR);
            cathode.put(ERR);
        } else {
            anode.put(ZZZ);
            cathode.put(ZZZ);
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addDiode();
    }

}
