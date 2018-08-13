package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.memory.Flat;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class MultTryte extends Component {

    private Pin inA, inB, outL, outH;

    // initialization
    public MultTryte(ControlMain control) {
        super(control);
    }
    public MultTryte(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            return FXMLLoader.load(Main.class.getResource("view/components/arithmetic/MultTryte.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        inA = new Pin(this, true, 6, 0, 2);
        inB = new Pin(this, true, 6, 0, 8);
        outL = new Pin(this, false, 6, 4, 3);
        outH = new Pin(this, false, 6, 4, 7);
        pins.add(inA);
        pins.add(inB);
        pins.add(outL);
        pins.add(outH);

        return pins;
    }

    // simulation
    @Override public void simulate() {
        long a = Flat.encode(inA.get(), 6);
        long b = Flat.encode(inB.get(), 6);
        long c = a * b;
        LogicLevel[] res = Flat.decode(c, 12);
        outL.put(Arrays.copyOfRange(res, 0, 6));
        outH.put(Arrays.copyOfRange(res, 6, 12));
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 30; i++) {
//            if (i % 5 == 0) for (int j = 0; j < 6; j++) MUL.countdown(summary);
//            if (i % 3 == 0) Adder.countdown(summary);
        }
        summary.addInput(LogicLevel.NIL, 5);
    }

}
