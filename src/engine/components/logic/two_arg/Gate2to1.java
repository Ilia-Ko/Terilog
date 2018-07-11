package engine.components.logic.two_arg;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

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
        inA = new Pin(this, Pin.IN, 0, 1);
        inB = new Pin(this, Pin.IN, 0, 3);
        out = new Pin(this, Pin.OUT, 8, 2);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(out);
        return pins;
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        boolean changed;
        LogicLevel a = inA.querySigFromNode();
        LogicLevel b = inB.querySigFromNode();

        // simulate
        if (a.isUnstable() || b.isUnstable()) changed = out.update(LogicLevel.ERR);
        else changed = out.update(function(a, b));

        // report about affected nodes
        HashSet<Node> affected = new HashSet<>();
        if (changed) affected.add(out.getNode());
        return affected;
    }
    abstract LogicLevel function(LogicLevel a, LogicLevel b);

}
