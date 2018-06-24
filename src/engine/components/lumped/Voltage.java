package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Voltage extends Component {

    private ObjectProperty<Color> colour;
    private StringProperty text;
    private LogicLevel signal;
    private Pin drain;
    private ToggleGroup toggle;

    // initialization
    public Voltage(ControlMain control) {
        super(control);
        signal = LogicLevel.ZZZ;

        // init colouring
        Rectangle body = (Rectangle) getRoot().lookup("#body");
        colour = new SimpleObjectProperty<>();
        body.fillProperty().bind(colour);
        colour.setValue(signal.colour());

        // init indication
        text = new SimpleStringProperty(String.valueOf(signal.getDigitCharacter()));
        Label value = (Label) getRoot().lookup("#value");
        value.textProperty().bind(text);
        DoubleProperty size = new SimpleDoubleProperty(2.0);
        value.layoutXProperty().bind(size.subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(size.subtract(value.heightProperty()).divide(2.0));
        value.rotateProperty().bind(getRotation().angleProperty().negate());
        value.scaleXProperty().bind(getScale().xProperty());
        value.scaleYProperty().bind(getScale().yProperty());
    }
    public Voltage(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        drain = new Pin(this, false, true, 1, 2);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(drain);
        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        LogicLevel[] levels = LogicLevel.values();
        RadioMenuItem[] items = new RadioMenuItem[levels.length];
        for (int i = 0; i < levels.length; i++) {
            LogicLevel lev = levels[i];
            items[i] = new RadioMenuItem(String.format("%c (%s)", lev.getDigitCharacter(), lev.getStandardName()));
            items[i].setOnAction(event -> setSignal(lev, false));
        }
        toggle = new ToggleGroup();
        toggle.getToggles().addAll(items);
        toggle.selectToggle(items[3]);

        Menu menuSet = new Menu("Set voltage");
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
        if (drain.update(signal)) affected.add(drain.getNode());
        return affected;
    }
    private void setSignal(LogicLevel sig, boolean updateToggle) {
        colour.setValue(sig.colour());
        text.setValue(String.valueOf(sig.getDigitCharacter()));
        signal = sig;
        if (updateToggle) {
            Toggle item = toggle.getToggles().get(sig.ordinal());
            toggle.selectToggle(item);
        }
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element v = super.writeXML(doc);
        v.setAttribute("sig", signal.getStandardName());
        return v;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String sigAttr = comp.getAttribute("sig");
        LogicLevel sig = LogicLevel.parseName(sigAttr);
        if (sig == null)
            System.out.printf("WARNING: unknown signal name '%s'. Using default Z value.\n", sigAttr);
        else
            setSignal(sig, true);
    }

}
