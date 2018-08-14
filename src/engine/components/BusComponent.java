package engine.components;

import engine.Circuit;
import engine.connectivity.Selectable;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public abstract class BusComponent extends Component {

    protected IntegerProperty capacity;
    private HashSet<Pin> dependent;

    // initialization
    protected BusComponent(ControlMain control, boolean isUnified) {
        super(control);
        dependent = getDependentPins();
        capacity.addListener((observable, oldValue, newValue) -> dependent.forEach(pin -> pin.setCapacity(newValue.intValue())));
        if (isUnified) {
            Label lbl = (Label) getRoot().lookup("#name");
            capacity.addListener((observable, oldValue, newValue) -> lbl.setText(makeName(getClass(), capacity)));
        }
    }
    protected BusComponent(ControlMain control, boolean isUnified, Element data) {
        this(control, isUnified);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            return FXMLLoader.load(Main.class.getResource("view/components/Universal.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected ContextMenu buildContextMenu() {
        capacity = new SimpleIntegerProperty(1);
        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, makeCapMenu("Set size", capacity));
        return menu;
    }
    protected abstract HashSet<Pin> getDependentPins();
    @Override public Selectable copy() {
        BusComponent copy = (BusComponent) super.copy();
        copy.capacity.setValue(capacity.get());
        return copy;
    }

    // countdown
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        for (int i = 0; i < capacity.get(); i++) singleCountdown(summary);
    }
    protected abstract void singleCountdown(Circuit.Summary summary);

    // xml info
    @Override public Element writeXML(Document doc) {
        Element g = super.writeXML(doc);
        g.setAttribute("cap", Integer.toString(capacity.get()));
        return g;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String capAttr = comp.getAttribute("cap");
        try {
            int cap = Integer.parseInt(capAttr);
            capacity.setValue(cap);
        } catch (NumberFormatException e) {
            System.out.printf("WARNING: invalid capacity '%s'. Using default 1.\n", capAttr);
        }
    }

    // menu
    public static Menu makeCapMenu(String title, IntegerProperty cap) {
        Menu menuCap = new Menu(title);
        MenuItem itemCap1 = new MenuItem("Trit");
        MenuItem itemCap3 = new MenuItem("Triplet");
        MenuItem itemCap6 = new MenuItem("Tryte");
        MenuItem itemCap12 = new MenuItem("Word");
        MenuItem itemCap24 = new MenuItem("Dword");
        itemCap1.setOnAction(action -> cap.setValue(1));
        itemCap3.setOnAction(action -> cap.setValue(3));
        itemCap6.setOnAction(action -> cap.setValue(6));
        itemCap12.setOnAction(action -> cap.setValue(12));
        itemCap24.setOnAction(action -> cap.setValue(24));
        menuCap.getItems().addAll(itemCap1, itemCap3, itemCap6, itemCap12, itemCap24);
        return menuCap;
    }
    private static String makeName(Class cls, IntegerProperty capacity) {
        // get class name
        String name = cls.getSimpleName().toUpperCase();

        // get capacity name
        String cap;
        switch (capacity.get()) {
            case 1:
                cap = "TRIT";
                break;
            case 3:
                cap = "TRIPLET";
                break;
            case 6:
                cap = "TRYTE";
                break;
            case 12:
                cap = "WORD";
                break;
            case 24:
                cap = "DWORD";
                break;
            default:
                cap = String.format("%s-TRIT", capacity.get());
        }

        // join names together
        StringBuilder res = new StringBuilder();
        int len = Math.min(cap.length(), name.length());
        for (int i = 0; i < len; i++) {
            res.append(name.charAt(i));
            res.append(' ');
            res.append(cap.charAt(i));
            res.append('\n');
        }
        int l1 = name.length() - len;
        for (int i = 0; i < l1; i++) {
            res.append(name.charAt(len + i));
            res.append("  \n");
        }
        int l2 = cap.length() - len;
        for (int i = 0; i < l2; i++) {
            res.append("  ");
            res.append(cap.charAt(len + i));
            res.append('\n');
        }

        return res.deleteCharAt(res.length() - 1).toString();
    }

}
