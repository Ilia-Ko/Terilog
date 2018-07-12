package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Voltage extends Component {

    private ObjectProperty<LogicLevel> signal;
    private Pin drain;
    private ToggleGroup toggle;

    // initialization
    public Voltage(ControlMain control) {
        super(control);

        // init indication
        Rectangle body = (Rectangle) getRoot().lookup("#body");
        Label value = (Label) getRoot().lookup("#value");
        DoubleProperty size = new SimpleDoubleProperty(2.0);
        value.layoutXProperty().bind(size.subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(size.subtract(value.heightProperty()).divide(2.0));
        value.rotateProperty().bind(getRotation().angleProperty().negate());
        value.scaleXProperty().bind(getScale().xProperty());
        value.scaleYProperty().bind(getScale().yProperty());

        // init signal
        signal = new SimpleObjectProperty<>();
        signal.addListener((observable, oldSignal, newSignal) -> {
            body.setFill(newSignal.colour());
            value.setText(String.valueOf(newSignal.getDigitCharacter()));
            drain.put(newSignal);
        });
        signal.setValue(LogicLevel.ZZZ);
    }
    public Voltage(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        drain = new Pin(this, false, 1, 2);
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
            items[i].setOnAction(event -> signal.setValue(lev));
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
    @Override public void simulate() {
        drain.put(signal.get());
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addInput(signal.get(), 1);
    }

    private void setSignal(LogicLevel signal) {
        this.signal.setValue(signal);
        Toggle item = toggle.getToggles().get(signal.ordinal());
        toggle.selectToggle(item);
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element v = super.writeXML(doc);
        v.setAttribute("sig", signal.get().getStandardName());
        return v;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String sigAttr = comp.getAttribute("sig");
        LogicLevel sig = LogicLevel.parseName(sigAttr);
        if (sig == null)
            System.out.printf("WARNING: unknown signal name '%s'. Using default Z value.\n", sigAttr);
        else
            setSignal(sig);
    }

}
