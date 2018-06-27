package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Indicator extends Component {

    private ObjectProperty<LogicLevel> signal;
    private Pin source;

    // initialization
    public Indicator(ControlMain control) {
        super(control);

        // init indication
        Circle body = (Circle) getRoot().lookup("#body");
        Label value = (Label) getRoot().lookup("#value");
        DoubleProperty size = new SimpleDoubleProperty(2.0);
        value.layoutXProperty().bind(size.subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(size.subtract(value.heightProperty()).divide(2.0));
        value.rotateProperty().bind(getRotation().angleProperty().negate());
        value.scaleXProperty().bind(getScale().xProperty());
        value.scaleYProperty().bind(getScale().yProperty());

        // init signal
        signal = new SimpleObjectProperty<>();
        signal.addListener((observable, oldSignal, newSignal) -> {
            RadialGradient gradient = new RadialGradient(0, 0, 1, 1, 1,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, newSignal.colour()),
                    new Stop(1.0, Color.GRAY));
            body.setFill(gradient);
            body.setStroke(newSignal.colour());
            value.setText(String.valueOf(newSignal.getDigitCharacter()));
        });
    }
    public Indicator(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected HashSet<Pin> initPins() {
        source = new Pin(this, true, false, 0, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(source);
        return pins;
    }

    // simulation
    @Override public void reset(boolean denodify) {
        super.reset(denodify);
        signal.setValue(LogicLevel.ZZZ);
    }
    @Override public HashSet<Node> simulate() {
        signal.setValue(source.querySigFromNode());
        return new HashSet<>();
    }

}
