package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Selectable;
import gui.control.ControlMain;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Polyline;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Clock extends Component {

    public static final double DEF_FREQUENCY = 2; // Hz

    private int phase, dir;
    private Pin drain;
    private Polyline curve;

    // initialization
    public Clock(ControlMain control) {
        super(control);
        phase = 0;
        dir = 1;

        curve = (Polyline) getRoot().lookup("#curve");
        curve.setStroke(LogicLevel.POS.colour());
    }
    public Clock(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        drain = new Pin(this, false, 4, 2);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(drain);
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
    private int compNext(int current) {
        if (Math.abs(current) == 1) return 0;
        else return dir;
    }
    public void nextImpulse() {
        if (Math.abs(phase) == 1) dir *= -1;
        phase += dir;

        curve.setStroke(LogicLevel.parseValue(compNext(phase)).colour());
    }
    @Override public void simulate() {
        drain.put(LogicLevel.parseValue(phase));
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.takeClockIntoAccount();
        summary.addInput(LogicLevel.NIL, 1);
    }

    @Override public Selectable copy() {
        Clock copy = (Clock) super.copy();
        copy.phase = phase;
        copy.dir = dir;
        copy.curve = (Polyline) copy.getRoot().lookup("#curve");
        copy.curve.setStroke(LogicLevel.POS.colour());
        return copy;
    }

}
