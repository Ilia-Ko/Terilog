package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.BusComponent;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class Fork2 extends BusComponent {

    private BooleanProperty busToSingle;
    private ToggleGroup toggle;
    private Pin bus, singles[];

    public Fork2(ControlMain control) {
        super(control, false);
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
    public Fork2(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/lumped/fork2.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();
        int cap = (capacity == null) ? 1 : capacity.get();

        bus = new Pin(this, true, 2 * cap, 0, 1);
        pins.add(bus);

        singles = new Pin[2];
        for (int i = 0; i < 2; i++) {
            singles[i] = new Pin(this, false, cap, 2, i);
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
    @Override protected HashSet<Pin> getDependentPins() {
        bus.setCapacity(2 * capacity.get());
        HashSet<Pin> dep = new HashSet<>(getPins());
        dep.remove(bus);
        return dep;
    }

    @Override public void simulate() {
        int cap = capacity.get();
        if (busToSingle.get()) {
            LogicLevel[] bus = this.bus.get();
            for (int i = 0; i < 2; i++) singles[i].put(Arrays.copyOfRange(bus, i * cap, (i+1) * cap));
        } else {
            LogicLevel[] res = new LogicLevel[6 * cap];
            for (int i = 0; i < 2; i++) System.arraycopy(singles[i].get(), 0, res, i * cap, cap);
            bus.put(res);
        }
    }
    @Override protected void singleCountdown(Circuit.Summary summary) {}

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
