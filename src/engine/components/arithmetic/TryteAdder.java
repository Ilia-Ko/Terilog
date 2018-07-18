package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.one_arg.STI;
import engine.components.memory.flat.Flat;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public class TryteAdder extends Component {

    private Pin Cin, Cout;
    private Pin[] inA, inB, out;

    // initialization
    public TryteAdder(ControlMain control) {
        super(control);
    }
    public TryteAdder(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            return FXMLLoader.load(Main.class.getResource("view/components/arithmetic/adder_tryte.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        Cin = new Pin(this, true, 2, 0);
        Cout = new Pin(this, false, 2, 14);
        pins.add(Cin);
        pins.add(Cout);

        inA = new Pin[6];
        inB = new Pin[6];
        out = new Pin[6];
        for (int i = 0; i < 6; i++) {
            inA[i] = new Pin(this, true, 0, 1 + i);
            inB[i] = new Pin(this, true, 0, 8 + i);
            out[i] = new Pin(this, false, 4, 7 + i);
            pins.add(inA[i]);
            pins.add(inB[i]);
            pins.add(out[i]);
        }

        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] numA = new LogicLevel[6];
        LogicLevel[] numB = new LogicLevel[6];
        for (int i = 0; i < 6; i++) {
            numA[i] = inA[i].get();
            numB[i] = inB[i].get();
        }

        long a = Flat.encode(numA, 6);
        long b = Flat.encode(numB, 6);
        long c = a + b + Cin.get().volts();

        LogicLevel[] res = Flat.decode(c, 7);
        for (int i = 0; i < 6; i++) out[i].put(res[i]);
        Cout.put(res[6]);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 6; i++) FullAdder.countdown(summary);
        STI.countdown(summary);
        STI.countdown(summary);
    }

}
