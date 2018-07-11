package engine.components.memory;

import gui.control.ControlMain;
import org.w3c.dom.Element;

public class Dword extends Linear {

    public Dword(ControlMain control) {
        super(control, 24);
    }
    public Dword(ControlMain control, Element data) {
        super(control, data, 24);
    }

}
