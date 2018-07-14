package engine.components.memory;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

import static engine.LogicLevel.*;

public class Trigger extends Component {

    private ObjectProperty<LogicLevel> trit; // trigger stores the single trit of memory
    private Pin read, write, control;
    private ToggleGroup toggle;

    // initialization
    public Trigger(ControlMain control) {
        super(control);

        // init indication
        Circle body = (Circle) getRoot().lookup("#body");

        // init memory
        trit = new SimpleObjectProperty<>();
        trit.addListener((observable, prevTrit, newTrit) -> body.setFill(newTrit.colour()));
        trit.setValue(LogicLevel.ZZZ);
    }
    public Trigger(ControlMain control, Element data) {
        this(control);
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
        write = new Pin(this, true, 0, 2);
        read = new Pin(this, false, 4, 2);
        control = new Pin(this, true, 2, 0);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(read);
        pins.add(write);
        pins.add(control);
        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel c = control.get();

        if (c == ERR) {
            setMemory(ERR);
            read.put(ERR);
        } else {
            read.put(ZZZ);
            if (c == NEG) read.put(trit.get());
            else if (c == POS) trit.setValue(write.get());
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 5);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 5);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 3);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 2);
        summary.addResistor(5);
        summary.addInput(LogicLevel.POS, 2);
        summary.addInput(LogicLevel.NIL, 1);
        summary.addInput(LogicLevel.NEG, 1);
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
