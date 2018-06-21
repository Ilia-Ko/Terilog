package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import javafx.beans.property.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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
    }
    public Voltage(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pin[] initPins() {
        drain = new Pin(this, false, 1, 2);
        return new Pin[] {drain};
    }
    @Override protected ContextMenu buildContextMenu() {
        LogicLevel[] levels = LogicLevel.values();
        MenuItem[] items = new MenuItem[levels.length];
        for (int i = 0; i < levels.length; i++) {
            LogicLevel lev = levels[i];
            items[i] = new MenuItem(String.format("%c (%s)", lev.getDigitCharacter(), lev.getStandardName()));
            items[i].setOnAction(event -> {
                colour.setValue(lev.colour());
                text.setValue(String.valueOf(lev.getDigitCharacter()));
                signal = lev;
            });
        }

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
        if (drain.update(signal)) affected.add(drain.gather());
        return affected;
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element v = super.writeXML(doc);
        v.setAttribute("sig", signal.getStandardName());
        return v;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        signal = LogicLevel.parseName(comp.getAttribute("sig"));
    }

}
