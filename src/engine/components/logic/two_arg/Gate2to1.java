package engine.components.logic.two_arg;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

import static engine.LogicLevel.ERR;

abstract class Gate2to1 extends Component {

    Pin inA, inB, out;

    // initialization
    Gate2to1(ControlMain control) {
        super(control);
    }
    Gate2to1(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/two_arg/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        inA = new Pin(this, true, 0, 1);
        inB = new Pin(this, true, 0, 3);
        out = new Pin(this, false, 8, 2);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(out);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel a = inA.get();
        LogicLevel b = inB.get();

        if (a.isUnstable() || b.isUnstable()) {
            if (a == b) out.put(a);
            else out.put(ERR);
        } else out.put(function(a, b));
    }
    abstract LogicLevel function(LogicLevel a, LogicLevel b);

}
