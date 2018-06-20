package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.beans.property.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import org.w3c.dom.Element;

public class Indicator extends Component {

    public static final String ATTR_CLASS = "indicator";

    private ObjectProperty<Color> colour;
    private StringProperty text;
    private Pin source;

    // initialization
    public Indicator(ControlMain control) {
        super(control);

        // init colouring
        Circle body = (Circle) root.lookup("#body");
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
        Label value = (Label) root.lookup("#value");
        value.textProperty().bind(text);
        DoubleProperty size = new SimpleDoubleProperty(2.0);
        value.layoutXProperty().bind(size.subtract(value.widthProperty()).divide(2.0));
        value.layoutYProperty().bind(size.subtract(value.heightProperty()).divide(2.0));
    }
    public Indicator(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        Pane pane = super.loadContent();
        source = new Pin(pane, "source", 0, 1);
        return pane;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel signal = source.query();
        colour.setValue(signal.colour());
        text.setValue(String.valueOf(signal.getDigitCharacter()));
    }

    // xml info
    @Override protected String getAttrClass() {
        return ATTR_CLASS;
    }

}
