package engine.components.memory;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public class Trigger extends Component {

    private ObjectProperty<LogicLevel> trit; // trigger stores the single trit of memory
    private Pin read, write, control;
    private ToggleGroup toggle;

    // initialization
    public Trigger(ControlMain control) {
        super(control);

        // init indication
        Circle body = (Circle) getRoot().lookup("#body");
        Label value = (Label) getRoot().lookup("#value");
        DoubleProperty size = new SimpleDoubleProperty(2.0);
        value.layoutXProperty().bind(size.subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(size.subtract(value.heightProperty()).divide(2.0));
        value.rotateProperty().bind(getRotation().angleProperty().negate());
        value.scaleXProperty().bind(getScale().xProperty());
        value.scaleYProperty().bind(getScale().yProperty());

        // init memory
        trit = new SimpleObjectProperty<>();
        trit.addListener((observable, prevTrit, newTrit) -> body.setFill(newTrit.colour()));
        trit.setValue(LogicLevel.ZZZ);
    }
    public Trigger(ControlMain control, Element data) {
        super(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/memory/trigger.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected ContextMenu buildContextMenu() {
        LogicLevel[] levels = LogicLevel.values();
        RadioMenuItem[] items = new RadioMenuItem[levels.length];
        for (int i = 0; i < levels.length; i++) {
            LogicLevel lev = levels[i];
            items[i] = new RadioMenuItem(String.format("%c (%s)", lev.getDigitCharacter(), lev.getStandardName()));
            items[i].setOnAction(event -> trit.setValue(lev));
        }
        toggle = new ToggleGroup();
        toggle.getToggles().addAll(items);
        toggle.selectToggle(items[3]);

        Menu menuSet = new Menu("Memorize");
        menuSet.getItems().addAll(items);

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, menuSet);
        return menu;
    }
    @Override protected HashSet<Pin> initPins() {
        read = new Pin(this, false, true, 0, 2);
        write = new Pin(this, true, false, 4, 2);
        control = new Pin(this, true, false, 2, 0);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(read);
        pins.add(write);
        pins.add(control);
        return pins;
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        boolean changed;
        LogicLevel c = control.querySigFromNode();

        // simulate
        if (c.isUnstable()) {
            setMemory(LogicLevel.ERR);
            changed = read.update(LogicLevel.ERR);
        } else {
            if (c == LogicLevel.NEG) changed = read.update(trit.get());
            else {
                if (c == LogicLevel.POS) trit.setValue(write.querySigFromNode());
                changed = read.update(LogicLevel.ZZZ);
            }
        }

        // report about affected nodes
        HashSet<Node> affected = new HashSet<>();
        if (changed) affected.add(read.getNode());
        return affected;
    }
    private void setMemory(LogicLevel mem) {
        trit.setValue(mem);
        Toggle item = toggle.getToggles().get(mem.ordinal());
        toggle.selectToggle(item);
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element t = super.writeXML(doc);
        t.setAttribute("mem", trit.get().getStandardName());
        return t;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String memAttr = comp.getAttribute("mem");
        LogicLevel sig = LogicLevel.parseName(memAttr);
        if (sig == null)
            System.out.printf("WARNING: unknown signal name '%s'. Using default Z value.\n", memAttr);
        else
            setMemory(sig);
    }

}
