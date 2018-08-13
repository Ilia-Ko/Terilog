package engine.components.logic.path;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public class Mux_18_6 extends Component {

    private Pin inNEG[], inNIL[], inPOS[], out[], sel;

    // initialization
    public Mux_18_6(ControlMain control) {
        super(control);
    }
    public Mux_18_6(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/path/mux_18_6.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        inNEG = new Pin[6];
        inNIL = new Pin[6];
        inPOS = new Pin[6];
        out = new Pin[6];
        for (int i = 0; i < 6; i++) {
            inNEG[i] = new Pin(this, true, 1, 0, 1+i+i/3);
            inNIL[i] = new Pin(this, true, 1, 0, 9+i+i/3);
            inPOS[i] = new Pin(this, true, 1, 0, 17+i+i/3);
            out[i] = new Pin(this, false, 1, 2, 9+i+i/3);
            pins.add(inNEG[i]);
            pins.add(inNIL[i]);
            pins.add(inPOS[i]);
            pins.add(out[i]);
        }

        sel = new Pin(this, true, 1, 1, 24);
        pins.add(sel);

        return pins;
    }

    // simulation
    @Override public void simulate() {
        Pin[] src;
        switch (sel.get()[0]) {
            case NEG:
                src = inNEG;
                break;
            case NIL:
                src = inNIL;
                break;
            case POS:
                src = inPOS;
                break;
            default:
                src = null;
        }
        if (src != null) for (int i = 0; i < 6; i++) out[i].put(STI.func(src[i].get()[0]));
        else for (int i = 0; i < 6; i++) out[i].put(LogicLevel.ZZZ);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 6; i++) Mux_3_1.countdown(summary);
        Decoder_1_3.countdown(summary);
    }

}
