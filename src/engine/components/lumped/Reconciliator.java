package engine.components.lumped;

import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

public class Reconciliator extends Component {

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
        // TODO: implement simulation logic
    }

    // xml info
    @Override protected String getAttrClass() {
        return "reconciliator";
    }

}
