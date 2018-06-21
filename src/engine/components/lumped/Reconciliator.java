package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Reconciliator extends Component {

    private Pin source, pull, drain;

    // initialization
    public Reconciliator(ControlMain control) {
        super(control);
    }
    public Reconciliator(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pin[] initPins() {
        source = new Pin(this, true, 0, 1);
        pull = new Pin(this, true, 1, 0);
        drain = new Pin(this, false, 2, 1);
        return new Pin[] {source, pull, drain};
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        boolean changed;
        LogicLevel s = source.query();

        // simulate
        if (s.isUnstable())
            changed = drain.update(pull.query());
        else
            changed = drain.update(s);

        // report about affected nodes
        if (changed) affected.add(drain.gather());
        return affected;
    }

}
