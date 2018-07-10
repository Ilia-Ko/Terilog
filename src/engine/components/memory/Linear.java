package engine.components.memory;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

public abstract class Linear extends Component {

    private int digits;
    private LogicLevel[] mem;
    private Pin control, clock;
    private Pin[] write, read;

    // initialization
    Linear(ControlMain control, int digits) {
        super(control);
        this.digits = digits;
        mem = new LogicLevel[digits];
        for (int i = 0; i < digits; i++) mem[i] = LogicLevel.ZZZ;
    }
    Linear(ControlMain control, Element data, int digits) {
        this(control, digits);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/memory/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected ContextMenu buildContextMenu() {
        MenuItem itemSet = new MenuItem("Set value");
        itemSet.setOnAction(action -> {
            TextInputDialog dialog = new TextInputDialog(memToString());
            dialog.setTitle(Main.TITLE);
            dialog.setHeaderText(getClass().getSimpleName());
            dialog.setContentText(String.format("Set memory to (%d trits):", digits));
            TextField txt = dialog.getEditor();
            txt.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
                KeyCode code = key.getCode();
                String text = txt.getText();
                switch (code) {
                    case MINUS:
                        txt.setText(text + String.valueOf(LogicLevel.NEG.getDigitCharacter()));
                        break;
                    case DIGIT0:
                    case NUMPAD0:
                        txt.setText(text + "0");
                        break;
                    case DIGIT1:
                    case NUMPAD1:
                    case PLUS:
                        txt.setText(text + "1");
                        break;
                    case BACK_SPACE:
                        txt.setText(text.substring(0, text.length() - 1));
                        break;
                }
                key.consume();
            });
            Optional<String> res = dialog.showAndWait();
            res.ifPresent(this::parseString);
        });

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, itemSet);
        return menu;
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        // managing pins
        control = new Pin(this, true, false, 0, 1);
        clock = new Pin(this, true, false, 0, 3);
        pins.add(control);
        pins.add(clock);

        // data pins
        write = new Pin[digits];
        read = new Pin[digits];
        for (int i = 0; i < digits; i++) {
            write[i] = new Pin(this, true, false, 1 + i, 0);
            read[i] = new Pin(this, false, true, 1 + i, 4);
            pins.add(write[i]);
            pins.add(read[i]);
        }

        return pins;
    }

    // simulate
    @Override public HashSet<Node> simulate() {
        boolean[] changed = new boolean[digits];
        LogicLevel ctrl = control.querySigFromNode();
        LogicLevel clck = clock.querySigFromNode();

        // simulate
        if (ctrl.isUnstable() || clck.isUnstable()) {
            for (int i = 0; i < digits; i++)
                changed[i] = read[i].update(LogicLevel.ERR);
        } else {
            if (ctrl == LogicLevel.NEG) {
                for (int i = 0; i < digits; i++)
                    changed[i] = read[i].update(mem[i]);
            } else {
                if (ctrl == LogicLevel.POS && clck == LogicLevel.POS) {
                    for (int i = 0; i < digits; i++)
                        mem[i] = write[i].querySigFromNode();
                }
                for (int i = 0; i < digits; i++)
                    changed[i] = read[i].update(LogicLevel.ZZZ);
            }
        }

        // report about affected nodes
        HashSet<Node> affected = new HashSet<>();
        for (int i = 0; i < digits; i++)
            if (changed[i]) affected.add(read[i].getNode());
        return affected;
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

    // utils
    private String memToString() {
        StringBuilder builder = new StringBuilder();
        for (LogicLevel sig : mem)
            builder.append(sig.getDigitCharacter());
        return builder.toString();
    }
    private void parseString(String str) {
        for (int i = 0; i < digits; i++) {
            char d = str.charAt(i);
            LogicLevel sig = LogicLevel.parseDigit(d);
            if (sig == null)
                System.out.printf("WARNING: unknown digit '%c'. Using default Z value.\n", d);
            else
                mem[digits - 1 - i] = sig;
        }
    }

}
