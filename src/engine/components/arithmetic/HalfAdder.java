package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import engine.components.logic.two_arg.NANY;
import engine.components.logic.two_arg.NCON;
import engine.components.logic.two_arg.OKEY;
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
        inA = new Pin(this, true, 0, 1);
        inB = new Pin(this, true, 0, 3);
        outS = new Pin(this, false, 8, 1);
        outC = new Pin(this, false, 8, 3);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(outS);
        pins.add(outC);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel a = inA.get();
        LogicLevel b = inB.get();

        if (a.isUnstable() || b.isUnstable()) {
            if (a == b) {
                outS.put(a);
                outC.put(b);
            } else {
                outS.put(ERR);
                outC.put(ERR);
            }
        } else {
            int s = (a.volts() + b.volts());
            if (s == -2) s = +1;
            else if (s == +2) s = -1;
            s *= -1;
            int c = (a == b ? a.volts() : 0);
            c *= -1;
            outS.put(parseValue(s));
            outC.put(parseValue(c));
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        NANY.countdown(summary);
        NCON.countdown(summary);
        OKEY.countdown(summary);
        STI.countdown(summary);
        summary.addResistor(2);
        summary.addInput(LogicLevel.NIL, 1);
    }

}
