package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.one_arg.PTI;
import engine.components.logic.one_arg.STI;
import engine.components.logic.path.CKEY;
import engine.components.logic.two_arg.NAND;
import engine.components.lumped.Reconciliator;
import engine.components.memory.Flat;
import engine.components.memory.Trigger;
import engine.connectivity.Selectable;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

import static engine.LogicLevel.*;

public class Counter extends Component {

    private static final int MIN_VALUE = -364;

    private Pin reset, clock;
    private Pin[] write, read;
    private IntegerProperty value;
    private int nextValue;

    public Counter(ControlMain control) {
        super(control);

        // gui
        final Label[] labels = new Label[6];
        for (int i = 0; i < 6; i++)
            labels[i] = (Label) getRoot().lookup(String.format("#t%d", i));

        value = new SimpleIntegerProperty();
        value.addListener((observable, oldValue, newValue) -> {
            LogicLevel[] trits = Flat.decode(newValue.longValue(), 6);
            for (int i = 0; i < 6; i++) {
                labels[i].setText(String.valueOf(trits[i].getDigitCharacter()));
                labels[i].setTextFill(trits[i].colour());
            }
        });
        value.setValue(MIN_VALUE);
        nextValue = MIN_VALUE;
    }
    public Counter(ControlMain control, Element comp) {
        this(control);
        confirm();
        readXML(comp);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/arithmetic/Counter.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        reset = new Pin(this, true, 1, 0, 1);
        clock = new Pin(this, true, 1, 0, 2);
        pins.add(reset);
        pins.add(clock);

        write = new Pin[6];
        read = new Pin[6];
        for (int i = 0; i < 6; i++) {
            write[i] = new Pin(this, true, 1, 7 - i - i / 3, 0);
            read[i] = new Pin(this, false, 1, 7 - i - i / 3, 3);
            pins.add(write[i]);
            pins.add(read[i]);
        }

        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        MenuItem itemReset = new MenuItem("Reset");
        itemReset.setOnAction(action -> value.setValue(MIN_VALUE));

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, itemReset);
        return menu;
    }

    @Override public void simulate() {
        LogicLevel clck = clock.get()[0];
        LogicLevel rset = reset.get()[0];
        // simulate
        if (clck == ERR || rset == ERR) {
            for (Pin pin : read) pin.put(ZZZ);
        } else if (clck == POS) {
            if (rset == NIL) {
                nextValue = value.get() + 1;
            } else if (rset == POS) {
                LogicLevel[] in = new LogicLevel[6];
                for (int i = 0; i < 6; i++) in[i] = write[i].get()[0];
                nextValue = (int) Flat.encode(in, 6);
            } else if (rset == NEG) {
                nextValue = MIN_VALUE;
            }
        } else if (clck == NIL) {
            value.setValue(nextValue);
            LogicLevel[] trits = Flat.decode(value.get(), 6);
            for (int i = 0; i < 6; i++) read[i].put(trits[i]);
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 12; i++) {
            Trigger.countdown(summary);
            CKEY.countdown(summary);
            STI.countdown(summary);
            if (i % 2 == 0) AdderTritHalf.countdown(summary);
            if (i % 4 == 0) Reconciliator.countdown(summary);
            if (i % 6 == 0) NAND.countdown(summary);
        }
        PTI.countdown(summary);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 1);
        summary.addDiode(1);
        summary.addInput(POS, 1);
        summary.addInput(NEG, 1);
    }

    @Override public Selectable copy() {
        Counter copy = (Counter) super.copy();
        final Label[] labels = new Label[6];
        for (int i = 0; i < 6; i++)
            labels[i] = (Label) copy.getRoot().lookup(String.format("#t%d", i));
        copy.value = new SimpleIntegerProperty();
        copy.value.addListener((observable, oldValue, newValue) -> {
            LogicLevel[] trits = Flat.decode(newValue.longValue(), 6);
            for (int i = 0; i < 6; i++) {
                labels[i].setText(String.valueOf(trits[i].getDigitCharacter()));
                labels[i].setTextFill(trits[i].colour());
            }
        });
        copy.value.setValue(value.get());
        return copy;
    }

}
