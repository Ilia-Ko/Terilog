package engine.components.memory.linear;

import gui.control.ControlMain;
import org.w3c.dom.Element;

public class Tryte extends Linear {

    public Tryte(ControlMain control) {
        super(control, 6);
    }
    public Tryte(ControlMain control, Element data) {
        super(control, data, 6);
    }

}
