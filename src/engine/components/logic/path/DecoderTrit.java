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

public class DecoderTrit extends Component {

    private Pin in, outNEGp, outNEGn, outNILp, outNILn, outPOSp, outPOSn;

    // initialization
    public DecoderTrit(ControlMain control) {
        super(control);
    }
    public DecoderTrit(ControlMain control, Element data) {
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
        in = new Pin(this, true, 1, 0, 1);
        outNEGp = new Pin(this, false, 1, 1, 0);
        outNEGn = new Pin(this, false, 1, 1, 2);
        outNILp = new Pin(this, false, 1, 2, 0);
        outNILn = new Pin(this, false, 1, 2, 2);
        outPOSp = new Pin(this, false, 1, 3, 0);
        outPOSn = new Pin(this, false, 1, 3, 2);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(outNEGp);
        pins.add(outNILp);
        pins.add(outPOSp);
        pins.add(outPOSn);
        pins.add(outPOSn);
        pins.add(outPOSn);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        switch (in.get()[0]) {
            case NEG:
                outNEGp.put(POS);
                outNEGn.put(NEG);
                outNILp.put(NIL);
                outNILn.put(NIL);
                outPOSp.put(NIL);
                outPOSn.put(NIL);
                break;
            case NIL:
                outNEGp.put(NIL);
                outNEGn.put(NIL);
                outNILp.put(POS);
                outNILn.put(NEG);
                outPOSp.put(NIL);
                outPOSn.put(NIL);
                break;
            case POS:
                outNEGp.put(NIL);
                outNEGn.put(NIL);
                outNILp.put(NIL);
                outNILn.put(NIL);
                outPOSp.put(POS);
                outPOSn.put(NEG);
                break;
            default:
                outNEGp.put(ZZZ);
                outNEGn.put(ZZZ);
                outNEGp.put(ZZZ);
                outNEGn.put(ZZZ);
                outNILp.put(ZZZ);
                outPOSn.put(ZZZ);
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 2);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 2);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 1);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 1);
        summary.addDiode(4);
        for (int i = 0; i < 8; i++) Reconciliator.countdown(summary);
    }

}
