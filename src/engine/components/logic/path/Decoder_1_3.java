package engine.components.logic.path;

import engine.Circuit;
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

import static engine.LogicLevel.*;

public class Decoder_1_3 extends Component {

    private Pin in, outNEG, outNIL, outPOS;

    // initialization
    public Decoder_1_3(ControlMain control) {
        super(control);
    }
    public Decoder_1_3(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/path/decoder_1_3.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        in = new Pin(this, true, 0, 2);
        outNEG = new Pin(this, false, 2, 1);
        outNIL = new Pin(this, false, 2, 2);
        outPOS = new Pin(this, false, 2, 3);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(outNEG);
        pins.add(outNIL);
        pins.add(outPOS);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        switch (in.get()) {
            case NEG:
                outNEG.put(POS);
                outNIL.put(NIL);
                outPOS.put(NIL);
                break;
            case NIL:
                outNEG.put(NIL);
                outNIL.put(POS);
                outPOS.put(NIL);
                break;
            case POS:
                outNEG.put(NIL);
                outNIL.put(NIL);
                outPOS.put(POS);
                break;
            default:
                outNEG.put(ZZZ);
                outNIL.put(ZZZ);
                outPOS.put(ZZZ);
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 2);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 1);
        summary.addDiode(2);
        for (int i = 0; i < 4; i++) Reconciliator.countdown(summary);
    }

}
