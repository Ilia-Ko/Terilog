package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

public class ForkTryte extends Component {

    private Pin bus, singles[];

    public ForkTryte(ControlMain control) {
        super(control);
    }
    public ForkTryte(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        bus = new Pin(this, false, 6, 0, 3);
        pins.add(bus);

        singles = new Pin[6];
        for (int i = 0; i < 6; i++) {
            singles[i] = new Pin(this, false, 1, 2, i + i / 3);
            pins.add(singles[i]);
        }

        return pins;
    }

    @Override public void simulate() {
        LogicLevel[] bus = this.bus.get();
        LogicLevel[] res = new LogicLevel[6];

        for (int i = 0; i < 6; i++) {
            LogicLevel sig = singles[i].get()[0];
            if (bus[i].conflicts(sig)) res[i] = LogicLevel.ERR;
            else if (bus[i].suppresses(sig)) res[i] = bus[i];
            else if (sig.suppresses(bus[i])) res[i] = sig;
            else res[i] = LogicLevel.ZZZ;
            singles[i].put(res[i]);
        }
        this.bus.put(res);
    }

    @Override public void itIsAFinalCountdown(Circuit.Summary summary) { }

}
