package engine.components.logic.one_arg;

import engine.Circuit;
import engine.LogicLevel;
import gui.control.ControlMain;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class STI extends Gate1to1 {

    private BooleanProperty convertNILToZZZ;
    private ToggleGroup toggle;

    // initialization
    public STI(ControlMain control) {
        super(control);
        convertNILToZZZ = new SimpleBooleanProperty(false);

        // bind colouring
        Polygon body = (Polygon) getRoot().lookup("#body");
        Circle head = (Circle) getRoot().lookup("#head");
        convertNILToZZZ.addListener(((observable, oldValue, newValue) -> {
            Color colour = newValue ? Color.LIGHTGRAY : Color.LIGHTGREEN;
            body.setFill(colour);
            head.setFill(colour);
        }));
    }
    public STI(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected ContextMenu buildContextMenu() {
        // nil to nil
        RadioMenuItem itemNN = new RadioMenuItem("0 -> 0");
        itemNN.setOnAction(action -> convertNILToZZZ.setValue(false));

        // nil to zzz
        RadioMenuItem itemNZ = new RadioMenuItem("0 -> Z");
        itemNZ.setOnAction(action -> convertNILToZZZ.setValue(true));

        // toggling
        toggle = new ToggleGroup();
        toggle.getToggles().addAll(itemNN, itemNZ);
        toggle.selectToggle(itemNN);

        Menu menuMode = new Menu("Select mode");
        menuMode.getItems().addAll(itemNN, itemNZ);

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, menuMode);
        return menu;
    }

    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 1);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 1);
        if (convertNILToZZZ.not().get()) {
            summary.addResistor();
            summary.addInput(LogicLevel.NIL);
        }
    }

    // simulation
    @Override protected LogicLevel function(LogicLevel a) {
        int v = a.volts();
        v *= -1;
        if (v == 0 && convertNILToZZZ.get()) v = 2;
        return LogicLevel.parseValue(v);
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element s = super.writeXML(doc);
        s.setAttribute("mode", convertNILToZZZ.get() ? "0toZ" : "0to0");
        return s;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String modeAttr = comp.getAttribute("mode");
        switch (modeAttr) {
            case "0toZ":
                convertNILToZZZ.setValue(true);
                toggle.selectToggle(toggle.getToggles().get(1));
                break;
            case "0to0":
                convertNILToZZZ.setValue(false);
                toggle.selectToggle(toggle.getToggles().get(0));
                break;
            default:
                System.out.printf("WARNING: unknown STI mode '%s'. Using default 0 -> 0.\n", modeAttr);
                break;
        }
    }

}
