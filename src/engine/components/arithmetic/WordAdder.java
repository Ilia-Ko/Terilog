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
import java.util.Arrays;
import java.util.HashSet;

public class WordAdder extends Component {

    private Pin Cin, Cout;
    private Pin inA, inB, out;

    // initialization
    public WordAdder(ControlMain control) {
        super(control);
    }
    public WordAdder(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            return FXMLLoader.load(Main.class.getResource("view/components/arithmetic/wordadder.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        Cin = new Pin(this, true, 1, 2, 0);
        Cout = new Pin(this, false, 1, 2, 10);
        pins.add(Cin);
        pins.add(Cout);

        inA = new Pin(this, true, 12, 0, 2);
        inB = new Pin(this, true, 12, 0, 8);
        out = new Pin(this, false, 12, 4, 5);
        pins.add(inA);
        pins.add(inB);
        pins.add(out);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] numA = inA.get();
        LogicLevel[] numB = inB.get();

        long a = Flat.encode(numA, 12);
        long b = Flat.encode(numB, 12);
        long c = a + b + Cin.get()[0].volts();

        LogicLevel[] res = Flat.decode(c, 13);
        out.put(Arrays.copyOfRange(res, 0, 12));
        Cout.put(res[12]);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 12; i++) FullAdder.countdown(summary);
        STI.countdown(summary);
        STI.countdown(summary);
    }

}
