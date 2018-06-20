package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

public class Reconciliator extends Component {

    public static final String ATTR_CLASS = "reconciliator";

    private Pin source, pull, drain;

    // initialization
    public Reconciliator(ControlMain control) {
        super(control);
    }
    public Reconciliator(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        Pane pane = super.loadContent();
        source = new Pin(pane, "source", 0, 1);
        pull = new Pin(pane, "pull", 1, 0);
        drain = new Pin(pane, "drain", 2, 1);
        return pane;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel s = source.query();

        if (s.isUnstable())
            drain.announce(pull.query());
        else
            drain.announce(s);
    }

    // xml info
    @Override protected String getAttrClass() {
        return ATTR_CLASS;
    }

}
