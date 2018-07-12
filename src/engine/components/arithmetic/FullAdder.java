package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
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

import static engine.LogicLevel.ERR;
import static engine.LogicLevel.parseValue;

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
        inA = new Pin(this, true, 0, 1);
        inB = new Pin(this, true, 0, 3);
        inC = new Pin(this, true, 0, 5);
        outS = new Pin(this, false, 6, 2);
        outC = new Pin(this, false, 6, 4);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(inC);
        pins.add(outS);
        pins.add(outC);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel a = inA.get();
        LogicLevel b = inB.get();
        LogicLevel c = inC.get();

        if (a.isUnstable() || b.isUnstable() || c.isUnstable()) {
            if (a == b && a == c) {
                outS.put(b);
                outC.put(c);
            } else {
                outS.put(ERR);
                outC.put(ERR);
            }
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
            outS.put(parseValue(sum));
            outC.put(parseValue(-carry)); // note that Cout is inverted too
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 13);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 13);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 8);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 8);
        summary.addResistor(5);
        summary.addInput(LogicLevel.NIL, 1);
        summary.addInput(LogicLevel.POS, 5);
        summary.addInput(LogicLevel.NEG, 5);
    }

}
