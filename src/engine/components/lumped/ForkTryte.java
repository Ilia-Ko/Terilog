package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.Polyline;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

public class ForkTryte extends Component {

    private BooleanProperty busToSingle;
    private ToggleGroup toggle;
    private Pin bus, singles[];

    public ForkTryte(ControlMain control) {
        super(control);
        busToSingle = new SimpleBooleanProperty(true);
        busToSingle.addListener((observable, oldValue, newValue) -> {
            bus.setHighImpedance(newValue);
            for (Pin s : singles) s.setHighImpedance(!newValue);
        });
        Polyline div = (Polyline) getRoot().lookup("#div");
        Polyline con = (Polyline) getRoot().lookup("#con");
        div.visibleProperty().bind(busToSingle);
        con.visibleProperty().bind(busToSingle.not());
    }
    public ForkTryte(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        bus = new Pin(this, true, 6, 0, 3);
        pins.add(bus);

        singles = new Pin[6];
        for (int i = 0; i < 6; i++) {
            singles[i] = new Pin(this, false, 1, 2, i + i / 3);
            pins.add(singles[i]);
        }

        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        // nil to nil
        RadioMenuItem itemBus = new RadioMenuItem("Divergence");
        itemBus.setOnAction(action -> busToSingle.setValue(true));

        // nil to zzz
        RadioMenuItem itemSingles = new RadioMenuItem("Convergence");
        itemSingles.setOnAction(action -> busToSingle.setValue(false));

        // toggling
        toggle = new ToggleGroup();
        toggle.getToggles().addAll(itemBus, itemSingles);
        toggle.selectToggle(itemBus);

        Menu menuMode = new Menu("Select mode");
        menuMode.getItems().addAll(itemBus, itemSingles);

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, menuMode);
        return menu;
    }

    @Override public void simulate() {
        if (busToSingle.get()) {
            LogicLevel[] bus = this.bus.get();
            for (int i = 0; i < 6; i++) singles[i].put(bus[i]);
        } else {
            LogicLevel[] res = new LogicLevel[6];
            for (int i = 0; i < 6; i++) res[i] = singles[i].get()[0];
            bus.put(res);
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {}

    // xml info
    @Override public Element writeXML(Document doc) {
        Element s = super.writeXML(doc);
        s.setAttribute("mode", busToSingle.get() ? "divergence" : "convergence");
        return s;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String modeAttr = comp.getAttribute("mode");
        switch (modeAttr) {
            case "divergence":
                busToSingle.setValue(true);
                toggle.selectToggle(toggle.getToggles().get(0));
                break;
            case "convergence":
                busToSingle.setValue(false);
                toggle.selectToggle(toggle.getToggles().get(1));
                break;
            default:
                System.out.printf("WARNING: unknown fork mode '%s'. Using default: divergence.\n", modeAttr);
                break;
        }
    }

}
