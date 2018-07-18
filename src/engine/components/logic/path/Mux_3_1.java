package engine.components.logic.path;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.two_arg.CKEY;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public class Mux_3_1 extends Component {

    private Pin inNEG, inNIL, inPOS, out, sel;

    // initialization
    public Mux_3_1(ControlMain control) {
        super(control);
    }
    public Mux_3_1(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/path/mux_3_1.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        inNEG = new Pin(this, true, 0, 1);
        inNIL = new Pin(this, true, 0, 2);
        inPOS = new Pin(this, true, 0, 3);
        out = new Pin(this, false, 2, 2);
        sel = new Pin(this, true, 1, 4);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(inNEG);
        pins.add(inNIL);
        pins.add(inPOS);
        pins.add(out);
        pins.add(sel);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        switch (sel.get()) {
            case NEG:
                out.put(inNEG.get());
                break;
            case NIL:
                out.put(inNIL.get());
                break;
            case POS:
                out.put(inPOS.get());
                break;
            default:
                out.put(LogicLevel.ZZZ);
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        CKEY.countdown(summary);
        CKEY.countdown(summary);
        CKEY.countdown(summary);
        Decoder_1_3.countdown(summary);
    }

}
