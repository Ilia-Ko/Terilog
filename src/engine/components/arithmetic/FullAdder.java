package engine.components.arithmetic;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public class FullAdder extends Component {

    private Pin inA, inB, inC; // input trits
    private Pin outS, outC; // output trits

    // initialization
    public FullAdder(ControlMain control) {
        super(control);

        DoubleProperty right = new SimpleDoubleProperty(4.5);

        // position label for Pin outS
        Label lblS = (Label) getRoot().lookup("#lblS");
        lblS.layoutXProperty().bind(right.subtract(lblS.widthProperty()));

        // position label for Pin outC
        Label lblC = (Label) getRoot().lookup("#lblC");
        lblC.layoutXProperty().bind(right.subtract(lblC.widthProperty()));
    }
    public FullAdder(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/arithmetic/fulladder.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        inA = new Pin(this, Pin.IN, 0, 1);
        inB = new Pin(this, Pin.IN, 0, 3);
        inC = new Pin(this, Pin.IN, 0, 5);
        outS = new Pin(this, Pin.OUT, 6, 2);
        outC = new Pin(this, Pin.OUT, 6, 4);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(inC);
        pins.add(outS);
        pins.add(outC);
        return pins;
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        boolean changedS, changedC;
        LogicLevel a = inA.querySigFromNode();
        LogicLevel b = inB.querySigFromNode();
        LogicLevel c = inC.querySigFromNode();

        // simulate
        if (a.isUnstable() || b.isUnstable() || c.isUnstable()) {
            changedS = outS.update(LogicLevel.ERR);
            changedC = outC.update(LogicLevel.ERR);
        } else {
            int sum, carry;
            sum = a.volts() + b.volts() - c.volts(); // note that Cin is inverted
            if (sum == -2) {
                sum = +1;
                carry = -1;
            } else if (sum == +2) {
                sum = -1;
                carry = +1;
            } else {
                carry = sum / 3;
                sum %= 3;
            }
            changedS = outS.update(LogicLevel.parseValue(sum));
            changedC = outC.update(LogicLevel.parseValue(-carry)); // note that Cout is inverted too
        }

        // report about affected nodes
        HashSet<Node> affected = new HashSet<>();
        if (changedS) affected.add(outS.getNode());
        if (changedC) affected.add(outC.getNode());
        return affected;
    }

}
