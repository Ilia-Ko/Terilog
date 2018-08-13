package engine.wires;

import gui.control.ControlMain;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class FlyWire {

    // FlyWire is a group of two Wires: lineH and lineV.
    // It is dedicated for wire layout convenience.
    // LineV and lineH form a 'corner' - so that they can connect two arbitrary points.
    // The 'corner' can be flipped by pressing SPACE.
    // After layout is done, wires should be confirmed.

    private Wire lineH, lineV;
    private boolean isFirstH;
    private ControlMain control;

    public FlyWire(ControlMain control, int busLength) {
        this.control = control;
        IntegerProperty mouseX = control.getMouseX();
        IntegerProperty mouseY = control.getMouseY();
        IntegerProperty x0 = new SimpleIntegerProperty(mouseX.get());
        IntegerProperty y0 = new SimpleIntegerProperty(mouseY.get());

        lineH = new Wire(control, busLength, x0, y0, mouseX, y0);
        lineV = new Wire(control, busLength, mouseX, y0, mouseX, mouseY);
        isFirstH = true;
    }

    public void flip() {
        isFirstH = !isFirstH;
        IntegerProperty x, y;
        if (isFirstH) {
            x = lineH.x1;
            y = lineV.y0;
        } else {
            x = lineH.x0;
            y = lineV.y1;
        }
        lineH.y0.bind(y);
        lineH.y1.bind(y);
        lineV.x0.bind(x);
        lineV.x1.bind(x);
    }
    public void confirm() { // enter main mode
        boolean h0 = lineH.x0.get() == lineH.x1.get();
        boolean v0 = lineV.y0.get() == lineV.y1.get();
        if (h0 && v0) {
            lineH.delete(false);
            lineV.delete(false);
        } else if (h0) {
            lineV.confirm();
            lineH.delete(false);
        } else if (v0) {
            lineH.confirm();
            lineV.delete(false);
        } else {
            lineH.confirm();
            lineV.confirm();
        }
    }
    public void delete() {
        control.getParent().getChildren().removeAll(lineH, lineV);
    }

}
