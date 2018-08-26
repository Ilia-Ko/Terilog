package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Selectable;
import gui.control.ControlMain;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Clock extends Component {

    public static final double DEF_FREQUENCY = 0; // Hz (0 means as fast as possible)

    private int level;
    private Pin outH, outL;

    // initialization
    public Clock(ControlMain control) {
        super(control);
        level = 0;
    }
    public Clock(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        outH = new Pin(this, false, 1, 4, 1);
        outL = new Pin(this, false, 1, 4, 3);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(outH);
        pins.add(outL);
        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        MenuItem itemNext = new MenuItem("Next impulse");
        itemNext.setOnAction(action -> nextImpulse());

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, itemNext);
        return menu;
    }

    // simulation
    public void nextImpulse() {
        level = 1 - level;
    }
    @Override public void simulate() {
        outH.put(LogicLevel.parseValue(level));
        outL.put(LogicLevel.parseValue(1 - level));
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.takeClockIntoAccount();
        summary.addInput(LogicLevel.NIL, 1);
    }

    @Override public Selectable copy() {
        Clock copy = (Clock) super.copy();
        copy.level = level;
        return copy;
    }

}
