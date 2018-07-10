package engine.components.memory;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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
    private Label value;

    // initialization
    Linear(ControlMain control, int digits) {
        super(control);
        this.digits = digits;
        mem = new LogicLevel[digits];
        for (int i = 0; i < digits; i++) mem[i] = LogicLevel.ZZZ;

        value = (Label) getRoot().lookup("#value");
        value.setText(memToString());
        value.layoutXProperty().bind(widthProperty().subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(heightProperty().subtract(value.heightProperty()).divide(2.0));
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
            dialog.getDialogPane().setStyle(getDefaultFont());
            TextField txt = dialog.getEditor();
            txt.setOnKeyTyped(key -> {
                int pos = txt.getCaretPosition();
                StringBuilder builder = new StringBuilder();
                int c = 1;
                char lambda = LogicLevel.NEG.getDigitCharacter();
                for (char symbol : txt.getText().toCharArray()) {
                    if (symbol == '-' || symbol == lambda) builder.append(lambda);
                    else if (symbol == '+' || symbol == '=' || symbol == '1') builder.append('1');
                    else if (symbol == '0' || symbol == ' ') builder.append('0');
                    else if (symbol == 'z' || symbol == 'Z' || symbol == '?') builder.append('?');
                    else if (symbol == 'e' || symbol == 'E' || symbol == '!') builder.append('!');
                    else if (c < pos) pos--;
                    if (c % 4 == 3) {
                        builder.append('\'');
                        if (c < pos) pos++;
                    }
                    c++;
                }
                int len = builder.length();
                if (len > 0 && builder.charAt(len - 1) == '\'') builder.deleteCharAt(len - 1);
                int mustLen = digits + digits / 3 - 1;
                if (len < mustLen) {
                    for (int i = 0; i < mustLen - len; i++) builder.append('0');
                } else if (len > mustLen) {
                    builder.delete(mustLen, len);
                }
                txt.setText(builder.toString());
                txt.positionCaret(pos);
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
        clock = new Pin(this, true, false, 0, 2);
        pins.add(control);
        pins.add(clock);

        // data pins
        write = new Pin[digits];
        read = new Pin[digits];
        for (int i = 0; i < digits; i++) {
            write[i] = new Pin(this, true, false, 1 + i, 0);
            read[i] = new Pin(this, false, true, 1 + i, 3);
            pins.add(write[i]);
            pins.add(read[i]);
        }

        return pins;
    }
    protected abstract DoubleProperty widthProperty();
    private DoubleProperty heightProperty() {
        return new SimpleDoubleProperty(3.0);
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
                    value.setText(memToString());
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
        int c = 0;
        for (LogicLevel sig : mem) {
            builder.append(sig.getDigitCharacter());
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
                mem[--c] = sig;
        }
        value.setText(memToString());
    }

}
