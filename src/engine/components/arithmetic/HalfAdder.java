package engine.components.arithmetic;

import engine.Circuit;
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

public class HalfAdder extends Component {

    private Pin inA, inB; // inputs: trit A and trit B
    private Pin outS, outC; // outputs: trit Sum and trit Carry

    // initialization
    public HalfAdder(ControlMain control) {
        super(control);
        DoubleProperty right = new SimpleDoubleProperty(5.5);

        // position label for Pin outS
        Label lblS = (Label) getRoot().lookup("#lblS");
        lblS.layoutXProperty().bind(right.subtract(lblS.widthProperty()));

        // position label for Pin outC
        Label lblC = (Label) getRoot().lookup("#lblC");
        lblC.layoutXProperty().bind(right.subtract(lblC.widthProperty()));
    }
    public HalfAdder(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/arithmetic/halfadder.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        inA = new Pin(this, Pin.IN, 0, 1);
        inB = new Pin(this, Pin.IN, 0, 3);
        outS = new Pin(this, Pin.OUT, 8, 1);
        outC = new Pin(this, Pin.OUT, 8, 3);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(outS);
        pins.add(outC);
        return pins;
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        boolean changedS, changedC;
        LogicLevel a = inA.querySigFromNode();
        LogicLevel b = inB.querySigFromNode();

        // simulate
        if (a.isUnstable() || b.isUnstable()) {
            changedS = outS.update(LogicLevel.ERR);
            changedC = outC.update(LogicLevel.ERR);
        } else {
            int s = (a.volts() + b.volts());
            if (s == -2) s = +1;
            else if (s == +2) s = -1;
            s *= -1;
            int c = (a == b ? a.volts() : 0);
            c *= -1;
            changedS = outS.update(LogicLevel.parseValue(s));
            changedC = outC.update(LogicLevel.parseValue(c));
        }

        // report about affected nodes
        HashSet<Node> affected = new HashSet<>();
        if (changedS) affected.add(outS.getNode());
        if (changedC) affected.add(outC.getNode());
        return affected;
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 5);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 5);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 3);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 3);
        summary.addResistor();
        summary.addResistor();
        summary.addInput(LogicLevel.NIL);
        summary.addInput(LogicLevel.POS);
        summary.addInput(LogicLevel.POS);
        summary.addInput(LogicLevel.NEG);
        summary.addInput(LogicLevel.NEG);
    }

}
