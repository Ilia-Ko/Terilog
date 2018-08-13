package engine.components.logic.path;

import engine.Circuit;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import engine.components.logic.two_arg.CKEY;
import engine.components.lumped.Reconciliator;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

import static engine.LogicLevel.NIL;
import static engine.LogicLevel.ZZZ;

public class Demux_1_3 extends Component {

    private Pin in, outNEG, outNIL, outPOS, sel;

    // initialization
    public Demux_1_3(ControlMain control) {
        super(control);
    }
    public Demux_1_3(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            return FXMLLoader.load(Main.class.getResource("view/components/logic/path/demux_1_3.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        in = new Pin(this, true, 1, 0, 2);
        sel = new Pin(this, true, 1, 1, 4);
        outNEG = new Pin(this, false, 1, 2, 1);
        outNIL = new Pin(this, false, 1, 2, 2);
        outPOS = new Pin(this, false, 1, 2, 3);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(sel);
        pins.add(outNEG);
        pins.add(outNIL);
        pins.add(outPOS);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        switch (sel.get()[0]) {
            case NEG:
                outNEG.put(in.get());
                outNIL.put(NIL);
                outPOS.put(NIL);
                break;
            case NIL:
                outNEG.put(NIL);
                outNIL.put(in.get());
                outPOS.put(NIL);
                break;
            case POS:
                outNEG.put(NIL);
                outNIL.put(NIL);
                outPOS.put(in.get());
                break;
            default:
                outNEG.put(ZZZ);
                outNIL.put(ZZZ);
                outPOS.put(ZZZ);
                break;
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        CKEY.countdown(summary);
        CKEY.countdown(summary);
        CKEY.countdown(summary);
        STI.countdown(summary);
        STI.countdown(summary);
        STI.countdown(summary);
        Reconciliator.countdown(summary);
        Reconciliator.countdown(summary);
        Reconciliator.countdown(summary);
        Decoder_1_3.countdown(summary);
    }

}
