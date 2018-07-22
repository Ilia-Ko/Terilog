package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.lumped.Reconciliator;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

import static engine.LogicLevel.NIL;

public class TryteCMP extends Component {

    private Pin[] inA, inB;
    private Pin out;

    // initialization
    public TryteCMP(ControlMain control) {
        super(control);
    }
    public TryteCMP(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/two_arg/trytecmp.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        out = new Pin(this, false, 4, 7);
        pins.add(out);

        inA = new Pin[6];
        inB = new Pin[6];
        for (int i = 0; i < 6; i++) {
            inA[i] = new Pin(this, true, 0, i + 1);
            inB[i] = new Pin(this, true, 0, i + 8);
            pins.add(inA[i]);
            pins.add(inB[i]);
        }

        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel res = NIL;
        for (int i = 0; i < 6 && res == NIL; i++) res = CMP.func(inA[i].get(), inB[i].get());
        out.put(res);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 6; i++) CMP.countdown(summary);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 6);
        summary.addDiode(6);
        Reconciliator.countdown(summary);
    }

}
