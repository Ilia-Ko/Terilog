package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.two_arg.MUL;
import engine.components.memory.flat.Flat;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public class TryteMultiplier extends Component {

    private Pin[] inA, inB, out;

    // initialization
    public TryteMultiplier(ControlMain control) {
        super(control);
    }
    public TryteMultiplier(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            return FXMLLoader.load(Main.class.getResource("view/components/arithmetic/trytemultiplier.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        inA = new Pin[6];
        inB = new Pin[6];
        out = new Pin[12];
        for (int i = 0; i < 6; i++) {
            inA[i] = new Pin(this, true, 1, 0, i + 1);
            inB[i] = new Pin(this, true, 1, 0, i + 8);
            out[ i ] = new Pin(this, false, 1, 4, i + 1);
            out[i+6] = new Pin(this, false, 1, 4, i + 7);
            pins.add(inA[i]);
            pins.add(inB[i]);
            pins.add(out[ i ]);
            pins.add(out[i+6]);
        }

        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] numA = new LogicLevel[6];
        LogicLevel[] numB = new LogicLevel[6];
        for (int i = 0; i < 6; i++) {
            numA[i] = inA[i].get()[0];
            numB[i] = inB[i].get()[0];
        }

        long a = Flat.encode(numA, 6);
        long b = Flat.encode(numB, 6);
        long c = a * b;

        LogicLevel[] res = Flat.decode(c, 12);
        for (int i = 0; i < 12; i++) out[i].put(res[i]);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 30; i++) {
            if (i % 5 == 0) for (int j = 0; j < 6; j++) MUL.countdown(summary);
            if (i % 3 == 0) TryteAdder.countdown(summary);
        }
        summary.addInput(LogicLevel.NIL, 5);
    }

}
