package engine.components.arithmetic;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.components.logic.one_arg.PTI;
import engine.components.logic.one_arg.STI;
import engine.components.logic.two_arg.CKEY;
import engine.components.logic.two_arg.NAND;
import engine.components.lumped.Reconciliator;
import engine.components.memory.Trigger;
import engine.components.memory.flat.Flat;
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

    public static final int MIN_VALUE = -364;

    private Pin reset, clock;
    private Pin[] write, read;
    private IntegerProperty value;
    private int prevClock, prevRset, nextValue;

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
    }
    public Counter(ControlMain control, Element comp) {
        this(control);
        confirm();
        readXML(comp);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/arithmetic/counter.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        reset = new Pin(this, true, 0, 1);
        clock = new Pin(this, true, 0, 2);
        pins.add(reset);
        pins.add(clock);

        write = new Pin[6];
        read = new Pin[6];
        for (int i = 0; i < 6; i++) {
            write[i] = new Pin(this, true, 7 - i - i / 3, 0);
            read[i] = new Pin(this, false, 7 - i - i / 3, 3);
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
        LogicLevel clck = clock.get();
        LogicLevel rset = reset.get();

        if (clck == ERR || rset == ERR) {
            for (Pin pin : read) pin.put(ZZZ);
        } else if (rset == POS) {
            LogicLevel[] in = new LogicLevel[6];
            for (int i = 0; i < 6; i++) in[i] = write[i].get();
            nextValue = (int) Flat.encode(in, 6);
        } else if (rset == NIL && prevRset == 1) {
            value.setValue(nextValue);
        } else if (rset == NEG) {
            value.setValue(MIN_VALUE);
        } else if (clck == POS && prevClock != 1) {
            value.setValue(value.get() + 1);
            if (value.get() == 1 - MIN_VALUE) value.setValue(MIN_VALUE);
        }
        LogicLevel[] trits = Flat.decode(value.get(), 6);
        for (int i = 0; i < 6; i++) read[i].put(trits[i]);
        prevClock = clck.volts();
        prevRset = rset.volts();
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        for (int i = 0; i < 12; i++) {
            Trigger.countdown(summary);
            CKEY.countdown(summary);
            STI.countdown(summary);
            if (i % 2 == 0) HalfAdder.countdown(summary);
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
        copy.prevClock = prevClock;
        return copy;
    }

}
