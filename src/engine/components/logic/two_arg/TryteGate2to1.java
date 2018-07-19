package engine.components.logic.two_arg;

import engine.Circuit;
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

public abstract class TryteGate2to1 extends Component {

    private Pin[] inA, inB, out;

    // initialization
    TryteGate2to1(ControlMain control) {
        super(control);
    }
    TryteGate2to1(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/two_arg/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        inA = new Pin[6];
        inB = new Pin[6];
        out = new Pin[6];
        for (int i = 0; i < 6; i++) {
            inA[i] = new Pin(this, true, 0, i + 1);
            inB[i] = new Pin(this, true, 0, i + 8);
            out[i] = new Pin(this, false, 4, i + 7);
            pins.add(inA[i]);
            pins.add(inB[i]);
            pins.add(out[i]);
        }

        return pins;
    }

    // simulation
    @Override public void simulate() {
        for (int i = 0; i < 6; i++) out[i].put(function(inA[i].get(), inB[i].get()));
    }
    abstract LogicLevel function(LogicLevel a, LogicLevel b);
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 2 * 6);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 2 * 6);
        summary.addResistor(6);
        summary.addInput(LogicLevel.NIL, 1);
        summary.addInput(LogicLevel.POS, 1);
        summary.addInput(LogicLevel.NEG, 1);
    }

}
