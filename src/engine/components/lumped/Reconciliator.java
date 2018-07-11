package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.shape.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Reconciliator extends Component {

    private Pin source, drain;
    private ObjectProperty<LogicLevel> pull;
    private ToggleGroup toggle;

    // initialization
    public Reconciliator(ControlMain control) {
        super(control);

        Polygon body = (Polygon) getRoot().lookup("#body");
        pull = new SimpleObjectProperty<>();
        pull.addListener((observable, oldPull, newPull) -> body.setFill(newPull.colour()));
        pull.setValue(LogicLevel.NIL);
    }
    public Reconciliator(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        source = new Pin(this, Pin.IN, 0, 1);
        drain = new Pin(this, Pin.OUT, 2, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(source);
        pins.add(drain);
        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        LogicLevel[] levels = LogicLevel.values();
        RadioMenuItem[] items = new RadioMenuItem[levels.length];
        for (int i = 0; i < levels.length; i++) {
            LogicLevel lev = levels[i];
            items[i] = new RadioMenuItem(String.format("%c (%s)", lev.getDigitCharacter(), lev.getStandardName()));
            items[i].setOnAction(event -> setPull(lev, false));
        }

        // toggling
        toggle = new ToggleGroup();
        toggle.getToggles().addAll(items);
        toggle.selectToggle(items[1]);

        Menu menuSet = new Menu("Pull to:");
        menuSet.getItems().addAll(items);

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, menuSet);
        return menu;
    }

    // simulation
    @Override public boolean isEntryPoint() {
        return true;
    }
    @Override public HashSet<Node> simulate() {
        boolean changed;
        LogicLevel s = source.querySigFromNode();

        // simulate
        if (s.isUnstable()) changed = drain.update(pull.get());
        else changed = drain.update(s);

        // report about affected nodes
        HashSet<Node> affected = new HashSet<>();
        if (changed) affected.add(drain.getNode());
        return affected;
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addResistor();
        summary.addInput(pull.get());
    }

    private void setPull(LogicLevel signal, boolean updateToggle) {
        pull.setValue(signal);
        if (updateToggle) {
            Toggle item = toggle.getToggles().get(signal.ordinal());
            toggle.selectToggle(item);
        }
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element r = super.writeXML(doc);
        r.setAttribute("pull", pull.get().getStandardName());
        return r;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String sigAttr = comp.getAttribute("pull");
        LogicLevel sig = LogicLevel.parseName(sigAttr);
        if (sig == null)
            System.out.printf("WARNING: unknown pull signal name '%s'. Using default MID value.\n", sigAttr);
        else
            setPull(sig, true);
    }

}
