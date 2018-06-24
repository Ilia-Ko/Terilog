package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Indicator extends Component {

    private ObjectProperty<Color> colour;
    private StringProperty text;
    private Pin source;

    // initialization
    public Indicator(ControlMain control) {
        super(control);

        // init colouring
        Circle body = (Circle) getRoot().lookup("#body");
        colour = new SimpleObjectProperty<>();
        colour.addListener((observable, oldValue, newValue) -> {
            RadialGradient gradient = new RadialGradient(0, 0, 1, 1, 1,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, newValue),
                    new Stop(1.0, Color.GRAY));
            body.setFill(gradient);
        });
        colour.setValue(LogicLevel.ZZZ.colour());
        body.strokeProperty().bind(colour);

        // init indication
        text = new SimpleStringProperty(String.valueOf(LogicLevel.ZZZ.getDigitCharacter()));
        Label value = (Label) getRoot().lookup("#value");
        value.textProperty().bind(text);
        DoubleProperty size = new SimpleDoubleProperty(2.0);
        value.layoutXProperty().bind(size.subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(size.subtract(value.heightProperty()).divide(2.0));
        value.rotateProperty().bind(getRotation().angleProperty().negate());
        value.scaleXProperty().bind(getScale().xProperty());
        value.scaleYProperty().bind(getScale().yProperty());
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
    @Override public HashSet<Node> simulate() {
        LogicLevel signal = source.querySigFromNode();
        colour.setValue(signal.colour());
        text.setValue(String.valueOf(signal.getDigitCharacter()));
        return new HashSet<>();
    }

}
