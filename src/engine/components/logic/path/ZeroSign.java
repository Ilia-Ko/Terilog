package engine.components.logic.path;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

import static engine.LogicLevel.*;

public class ZeroSign extends Component {

    private ObjectProperty<LogicLevel> sgn;
    private Pin in, out;

    // initialization
    public ZeroSign(ControlMain control) {
        super(control);

        sgn = new SimpleObjectProperty<>();
        Rectangle rect = (Rectangle) getRoot().lookup("#sign");
        sgn.addListener((observable, oldValue, newValue) -> rect.setFill(newValue.colour()));
        sgn.setValue(POS);
    }
    public ZeroSign(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/path/ZeroSign.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        in = new Pin(this, true, 1, 0, 0, true);
        out = new Pin(this, false, 1, 2, 0, true);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(out);
        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        MenuItem itemPos = new MenuItem("POS");
        itemPos.setOnAction(action -> sgn.setValue(POS));

        MenuItem itemNeg = new MenuItem("NEG");
        itemNeg.setOnAction(action -> sgn.setValue(NEG));

        Menu menuSet = new Menu("Set sign");
        menuSet.getItems().addAll(itemPos, itemNeg);

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, menuSet);
        return menu;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel in = this.in.get()[0];

        if (in.isUnstable()) out.put(ERR);
        else if (in == NIL) out.put(sgn.get());
        else out.put(ZZZ);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        if (sgn.get() == POS) {
            summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 1);
            summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 1);
        } else if (sgn.get() == NEG) {
            summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 1);
            summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 1);
        }
        summary.addDiode(1);
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element z = super.writeXML(doc);
        z.setAttribute("sign", sgn.get().getStandardName());
        return z;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        sgn.setValue(LogicLevel.parseName(comp.getAttribute("sign")));
    }

}
