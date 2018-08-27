package engine.components.memory;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.path.CKEY;
import engine.connectivity.Selectable;
import gui.Main;
import gui.control.ControlMain;
import gui.control.ControlMemSet;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static engine.LogicLevel.*;

public class Linear extends Component {

    private IntegerProperty capacity;
    private LogicLevel[] memory;
    private Pin control, clock, in, out;
    private Label val;

    // initialization
    public Linear(ControlMain control) {
        super(control);
        this.capacity = new SimpleIntegerProperty();
        memory = new LogicLevel[6];
        Arrays.fill(memory, ZZZ);

        // gui
        Label lbl = (Label) getRoot().lookup("#name");
        lbl.setText("x6");
        val = (Label) getRoot().lookup("#value");
        val.setText("??????");
        val.visibleProperty().bind(capacity.isEqualTo(6));

        capacity.addListener((observable, oldValue, newValue) -> {
            int cap = newValue.intValue();
            memory = new LogicLevel[cap];
            Arrays.fill(memory, ZZZ);
            val.setText("??????");
            lbl.setText(String.format("x%d", cap));
            in.setCapacity(cap);
            out.setCapacity(cap);
        });
        capacity.setValue(6);
    }
    public Linear(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/memory/Linear.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected ContextMenu buildContextMenu() {
        // capacity
        MenuItem itemCap1 = new MenuItem("Trit");
        MenuItem itemCap3 = new MenuItem("Triplet");
        MenuItem itemCap6 = new MenuItem("Tryte");
        MenuItem itemCap12 = new MenuItem("Word");
        MenuItem itemCap24 = new MenuItem("Double");
        Menu menuCap = new Menu("Set capacity");
        menuCap.getItems().addAll(itemCap1, itemCap3, itemCap6, itemCap12, itemCap24);
        // actions
        itemCap1.setOnAction(action -> capacity.setValue(1));
        itemCap3.setOnAction(action -> capacity.setValue(3));
        itemCap6.setOnAction(action -> capacity.setValue(6));
        itemCap12.setOnAction(action -> capacity.setValue(12));
        itemCap24.setOnAction(action -> capacity.setValue(24));
        // memory
        MenuItem itemSet = new MenuItem("Set value");
        itemSet.setOnAction(action -> {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/dialogs/memset.fxml"));
                Stage dialog = getControl().initDialog(loader, false);
                ((ControlMemSet) loader.getController()).initialSetup(dialog, this);
                dialog.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to show Memory Set dialog.");
            }
        });

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, menuCap);
        menu.getItems().add(1, itemSet);
        return menu;
    }
    @Override protected HashSet<Pin> initPins() {
        int cap = (capacity == null) ? 1 : capacity.get();
        control = new Pin(this, true, 1, 0, 1);
        clock = new Pin(this, true, 1, 0, 2);
        in = new Pin(this, true, cap, 2, 0, false);
        out = new Pin(this, false, cap, 2, 3, false);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(control);
        pins.add(clock);
        pins.add(in);
        pins.add(out);
        return pins;
    }
    @Override public void confirm() {
        super.confirm();
    }

    // simulation
    @Override public void simulate() {
        LogicLevel ctrl = control.get()[0];
        LogicLevel clck = clock.get()[0];

        if (ctrl == ERR || clck == ERR) {
            Arrays.fill(memory, ERR);
        } else if (clck == POS && ctrl == POS) {
            System.arraycopy(in.get(), 0, memory, 0, capacity.get());
        }
        val.setText(memToString(memory));
        out.put(memory);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        CKEY.countdown(summary);
        for (int i = 0; i < capacity.get(); i++) Trigger.countdown(summary);
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element l = super.writeXML(doc);
        l.setAttribute("cap", Integer.toString(capacity.get()));
        l.setAttribute("mem", memToString(memory));
        return l;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String capAttr = comp.getAttribute("cap");
        try {
            int cap = Integer.parseInt(capAttr);
            capacity.setValue(cap);
        } catch (NumberFormatException e) {
            System.out.printf("WARNING: invalid linear memory capacity: '%s'. Using default 6.\n", capAttr);
        }
        parseString(comp.getAttribute("mem"));
    }

    @Override public Selectable copy() {
        Linear copy = (Linear) super.copy();
        copy.capacity.setValue(capacity.get());
        System.arraycopy(memory, 0, copy.memory, 0, capacity.get());
        return copy;
    }

    // utils
    public static String memToString(LogicLevel[] memory) {
        StringBuilder builder = new StringBuilder();
        for (LogicLevel sig : memory) builder.insert(0, sig.getDigitCharacter());
        return builder.toString();
    }
    private void parseString(String str) {
        int c = capacity.get();
        for (char d : str.toCharArray()) {
            if (d == '\'') continue;
            LogicLevel sig = LogicLevel.parseDigit(d);
            if (sig == null)
                System.out.printf("WARNING: unknown digit '%c'. Using default Z value.\n", d);
            else
                memory[--c] = sig;
        }
        val.setText(memToString(memory));
    }
    public LogicLevel[] getMemory() {
        return memory;
    }
    public void setMemory(LogicLevel[] mem) {
        assert mem.length == capacity.get();
        System.arraycopy(mem, 0, memory, 0, capacity.get());
        val.setText(memToString(memory));
    }
    public int getSize() {
        return capacity.get();
    }

}
