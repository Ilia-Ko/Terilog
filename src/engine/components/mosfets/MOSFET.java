package engine.components.mosfets;

import engine.components.Component;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;

public abstract class MOSFET extends Component {

    protected Pin source, gate, drain;

    MOSFET(ControlMain control) {
        super(control);
    }
    MOSFET(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/mosfets/" + getAttrClass() + ".fxml";
            Pane pane = FXMLLoader.load(Main.class.getResource(location));
            createPins(pane);
            return pane;
        } catch (IOException e) {
            return new Pane();
        }
    }
    private void createPins(Pane pane) {
        source = new Pin(pane, "source", 0, 2);
        gate = new Pin(pane, "gate", 2, 0);
        drain = new Pin(pane, "drain", 4, 2);
    }

    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        // TODO: read pins
    }

}
