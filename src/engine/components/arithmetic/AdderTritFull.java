package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import engine.components.logic.two_arg.NANY;
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

import static engine.LogicLevel.ZZZ;
import static engine.LogicLevel.parseValue;

public class AdderTritFull extends Component {

    private Pin inA, inB, inC; // input trits
    private Pin outS, outC; // output trits

    // initialization
    public AdderTritFull(ControlMain control) {
        super(control);

        DoubleProperty right = new SimpleDoubleProperty(4.5);

        // position label for Pin outS
        Label lblS = (Label) getRoot().lookup("#lblS");
        lblS.layoutXProperty().bind(right.subtract(lblS.widthProperty()));

        // position label for Pin outC
        Label lblC = (Label) getRoot().lookup("#lblC");
        lblC.layoutXProperty().bind(right.subtract(lblC.widthProperty()));
    }
    public AdderTritFull(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/arithmetic/AdderTritFull.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        inA = new Pin(this, true, 1, 0, 3);
        inB = new Pin(this, true, 1, 0, 5);
        inC = new Pin(this, true, 1, 0, 1);
        outS = new Pin(this, false, 1, 6, 2);
        outC = new Pin(this, false, 1, 6, 4);
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
        LogicLevel a = inA.get()[0];
        LogicLevel b = inB.get()[0];
        LogicLevel c = inC.get()[0];

        if (a.isUnstable() || b.isUnstable() || c.isUnstable()) {
            if (a == b && a == c) {
                outS.put(b);
                outC.put(c);
            } else {
                outS.put(ZZZ);
                outC.put(ZZZ);
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
    public static void countdown(Circuit.Summary summary) {
        AdderTritHalf.countdown(summary);
        AdderTritHalf.countdown(summary);
        NANY.countdown(summary);
        STI.countdown(summary);
        summary.addResistor(1);
    }

}
