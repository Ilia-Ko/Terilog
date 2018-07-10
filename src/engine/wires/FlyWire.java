package engine.wires;

import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
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

    public FlyWire(ControlMain control) {
        this.control = control;
        IntegerProperty mouseX = control.getMouseX();
        IntegerProperty mouseY = control.getMouseY();
        IntegerProperty x0 = new SimpleIntegerProperty(mouseX.get());
        IntegerProperty y0 = new SimpleIntegerProperty(mouseY.get());

        lineH = new Wire(control, x0, y0, mouseX, y0);
        lineV = new Wire(control, mouseX, y0, mouseX, mouseY);
        isFirstH = true;
    }

    public void flip() {
        isFirstH = !isFirstH;
        DoubleProperty x, y;
        if (isFirstH) {
            x = lineH.endXProperty();
            y = lineV.startYProperty();
        } else {
            x = lineH.startXProperty();
            y = lineV.endYProperty();
        }
        lineH.startYProperty().bind(y);
        lineH.endYProperty().bind(y);
        lineV.startXProperty().bind(x);
        lineV.endXProperty().bind(x);
    }
    public void confirm() { // enter main mode
        if (lineV.getStartY() == lineV.getEndY()) { // lineV is redundant
            lineH.confirm();
            control.getParent().getChildren().remove(lineV);
        } else if (lineH.getStartX() == lineH.getEndX()) { // lineH is redundant
            lineV.confirm();
            control.getParent().getChildren().remove(lineH);
        } else { // nobody is redundant
            lineH.confirm();
            lineV.confirm();
        }
    }
    public void delete() {
        control.getParent().getChildren().removeAll(lineH, lineV);
    }

}
