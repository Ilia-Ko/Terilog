package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

public class Diode extends Component {

    public static final String ATTR_CLASS = "diode";

    private Pin anode, cathode;

    // initialization
    public Diode(ControlMain control) {
        super(control);
    }
    public Diode(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        Pane pane = super.loadContent();
        anode = new Pin(pane, "anode", 0, 1);
        cathode = new Pin(pane, "cathode", 4, 1);
        return pane;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel a = anode.query();
        LogicLevel c = cathode.query();

        if (a == LogicLevel.ERR || c == LogicLevel.ERR) {
            anode.announce(LogicLevel.ERR);
            cathode.announce(LogicLevel.ERR);
        } else if (a == LogicLevel.ZZZ && c == LogicLevel.NEG)
            anode.announce(LogicLevel.NEG);
        else if (a == LogicLevel.POS && c == LogicLevel.ZZZ)
            cathode.announce(LogicLevel.POS);
        else if (a == LogicLevel.POS && c == LogicLevel.NEG) {
            anode.announce(LogicLevel.ERR);
            cathode.announce(LogicLevel.ERR);
        }
    }

    // xml info
    @Override protected String getAttrClass() {
        return ATTR_CLASS;
    }

}
