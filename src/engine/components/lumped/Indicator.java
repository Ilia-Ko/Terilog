package engine.components.lumped;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.BusComponent;
import engine.components.Pin;
import engine.components.memory.Linear;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static engine.LogicLevel.ZZZ;

public class Indicator extends BusComponent {

    private ObjectProperty<LogicLevel[]> signal;
    private Pin source;

    // initialization
    public Indicator(ControlMain control) {
        super(control, false);

        // init indication
        Rectangle body = (Rectangle) getRoot().lookup("#body");
        Label value = (Label) getRoot().lookup("#value");
        body.widthProperty().bind(capacity.add(1));
        value.layoutXProperty().bind(body.widthProperty().subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(body.heightProperty().subtract(value.heightProperty()).divide(2.0));
        value.rotateProperty().bind(getRotation().angleProperty().negate());
        value.scaleXProperty().bind(getScale().xProperty());
        value.scaleYProperty().bind(getScale().yProperty());

        // init signal
        signal = new SimpleObjectProperty<>();
        signal.addListener((observable, oldSignal, newSignal) -> value.setText(Linear.memToString(signal.get())));
        capacity.addListener((observable, oldValue, newValue) -> {
            LogicLevel[] val = new LogicLevel[newValue.intValue()];
            Arrays.fill(val, ZZZ);
            signal.setValue(val);
        });
        capacity.setValue(1);
    }
    public Indicator(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/lumped/indicator.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        source = new Pin(this, true, 1, 0, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(source);
        return pins;
    }
    @Override protected HashSet<Pin> getDependentPins() {
        return getPins();
    }

    // simulation
    @Override public void reset(boolean denodify) {
        super.reset(denodify);
        LogicLevel[] nil = new LogicLevel[capacity.get()];
        Arrays.fill(nil, ZZZ);
        signal.setValue(nil);
    }
    @Override public void simulate() {
        signal.setValue(source.get());
    }

    @Override protected void singleCountdown(Circuit.Summary summary) {
        summary.addOutput();
    }

}
