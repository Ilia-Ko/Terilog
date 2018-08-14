package engine.components.logic.path;

import engine.components.BusComponent;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;

public abstract class GatePath extends BusComponent {

    // initialization
    GatePath(ControlMain control) {
        super(control, false);
    }
    GatePath(ControlMain control, Element data) {
        super(control, false);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/path/" + getClass().getSimpleName() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }

}
