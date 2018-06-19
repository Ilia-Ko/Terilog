package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import org.w3c.dom.Element;

public class Indicator extends Component {

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
                    new Stop(0.8, Color.BLACK));
            body.setFill(gradient);
        });
        colour.setValue(source.sig().colour());
        body.strokeProperty().bind(colour);

        // init indication
        text = new SimpleStringProperty(String.valueOf(source.sig().getDigitCharacter()));
        Label value = (Label) root.lookup("#value");
        value.textProperty().bind(text);
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
        LogicLevel signal = source.sig();
        colour.setValue(signal.colour());
        text.setValue(String.valueOf(signal.getDigitCharacter()));
    }

    // xml info
    @Override protected String getAttrClass() {
        return "indicator";
    }

}
