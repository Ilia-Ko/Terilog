package engine.components.memory;

import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.w3c.dom.Element;

public class Triplet extends Linear {

    public Triplet(ControlMain control) {
        super(control, 3);
    }
    public Triplet(ControlMain control, Element data) {
        super(control, data, 3);
    }
    @Override protected DoubleProperty widthProperty() {
        return new SimpleDoubleProperty(4.0);
    }

}
