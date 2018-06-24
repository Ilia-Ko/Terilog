package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Reconciliator extends Component {

    private ObjectProperty<Color> colour;
    private Pin source, drain;
    private LogicLevel pull;
    private ToggleGroup toggle;

    // initialization
    public Reconciliator(ControlMain control) {
        super(control);
        pull = LogicLevel.NIL;

        // init colouring
        Polygon body = (Polygon) getRoot().lookup("#body");
        colour = new SimpleObjectProperty<>(pull.colour());
        body.fillProperty().bind(colour);
    }
    public Reconciliator(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        source = new Pin(this, true, false, 0, 1);
        drain = new Pin(this, false, true, 2, 1);
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
        HashSet<Node> affected = new HashSet<>();
        boolean changed;
        LogicLevel s = source.querySigFromNode();

        // simulate
        if (s.isUnstable())
            changed = drain.update(pull);
        else
            changed = drain.update(s);

        // report about affected nodes
        if (changed) affected.add(drain.getNode());
        return affected;
    }
    private void setPull(LogicLevel sig, boolean updateToggle) {
        pull = sig;
        colour.setValue(pull.colour());
        if (updateToggle) {
            Toggle item = toggle.getToggles().get(sig.ordinal());
            toggle.selectToggle(item);
        }
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element r = super.writeXML(doc);
        r.setAttribute("pull", pull.getStandardName());
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
