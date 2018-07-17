package engine.components.memory.linear;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.two_arg.CKEY;
import engine.components.memory.Trigger;
import engine.connectivity.Selectable;
import gui.Main;
import gui.control.ControlMain;
import gui.control.ControlMemSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public abstract class Linear extends Component {

    private int digits;
    private Pin control, fill, clock;
    private MemCell[] cells;

    // initialization
    Linear(ControlMain control, int digits) {
        super(control);
        this.digits = digits;
        getPins().addAll(makePins());

        cells = new MemCell[digits];
        for (int i = 0; i < digits; i++) cells[i] = new MemCell(this, digits - i - 1, i);
    }
    Linear(ControlMain control, Element data, int digits) {
        this(control, digits);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/memory/linear/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected ContextMenu buildContextMenu() {
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
        menu.getItems().add(0, itemSet);
        return menu;
    }
    private HashSet<Pin> makePins() {
        HashSet<Pin> pins = new HashSet<>();

        // managing pins
        control = new Pin(this, true, 0, 1);
        fill = new Pin(this, true, 0, 2);
        clock = new Pin(this, true, digits + digits / 3, 2);
        pins.add(control);
        pins.add(fill);
        pins.add(clock);

        return pins;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel ctrl = control.get();
        LogicLevel fill = this.fill.get();
        LogicLevel clck = clock.get();

        for (MemCell cell : cells) cell.simulate(ctrl, fill, clck);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        CKEY.countdown(summary);
        for (int i = 0; i < digits; i++) Trigger.countdown(summary);
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element l = super.writeXML(doc);
        l.setAttribute("mem", memToString());
        return l;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        parseString(comp.getAttribute("mem"));
    }

    @Override public Selectable copy() {
        Linear copy = (Linear) super.copy();
        copy.digits = digits;
        copy.getPins().addAll(copy.makePins());
        copy.cells = new MemCell[digits];
        for (int i = 0; i < digits; i++) {
            copy.cells[i] = new MemCell(copy, digits - i - 1, i);
            copy.cells[i].memProperty().setValue(cells[i].memProperty().getValue());
        }
        return copy;
    }

    // utils
    private String memToString() {
        StringBuilder builder = new StringBuilder();
        int c = 0;
        for (MemCell cell : cells) {
            builder.append(cell.memProperty().get().getDigitCharacter());
            if (c++ == 2) {
                builder.append('\'');
                c = 0;
            }
        }
        return builder.deleteCharAt(builder.length() - 1).toString();
    }
    private void parseString(String str) {
        int c = digits;
        for (char d : str.toCharArray()) {
            if (d == '\'') continue;
            LogicLevel sig = LogicLevel.parseDigit(d);
            if (sig == null)
                System.out.printf("WARNING: unknown digit '%c'. Using default Z value.\n", d);
            else
                cells[--c].memProperty().setValue(sig);
        }
    }
    public MemCell[] getCells() {
        return cells;
    }

}
