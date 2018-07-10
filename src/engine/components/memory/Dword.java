package engine.components.memory;

import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.w3c.dom.Element;

public class Dword extends Linear {

    public Dword(ControlMain control) {
        super(control, 24);
    }
    public Dword(ControlMain control, Element data) {
        super(control, data, 24);
    }
    @Override protected DoubleProperty widthProperty() {
        return new SimpleDoubleProperty(30.0);
    }

}
