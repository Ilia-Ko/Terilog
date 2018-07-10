package engine.components.memory;

import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.w3c.dom.Element;

public class Tryte extends Linear {

    public Tryte(ControlMain control) {
        super(control, 6);
    }
    public Tryte(ControlMain control, Element data) {
        super(control, data, 6);
    }
    @Override protected DoubleProperty widthProperty() {
        return new SimpleDoubleProperty(8.0);
    }

}
