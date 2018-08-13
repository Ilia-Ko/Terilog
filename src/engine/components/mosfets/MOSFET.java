package engine.components.mosfets;

import engine.components.Component;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

abstract class MOSFET extends Component {

    Pin source, gate, drain;

    // initialization
    MOSFET(ControlMain control) {
        super(control);
    }
    MOSFET(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/mosfets/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        source = new Pin(this, false, 1, 0, 2);
        gate = new Pin(this, true, 1, 2, 0);
        drain = new Pin(this, false, 1, 4, 2);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(source);
        pins.add(gate);
        pins.add(drain);
        return pins;
    }

}
