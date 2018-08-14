package engine.components.logic.path;

import engine.components.BusComponent;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;

public abstract class GatePath extends BusComponent {

    // initialization
    GatePath(ControlMain control) {
        super(control, false);
        // name
        Label lbl = (Label) getRoot().lookup("#name");
        lbl.setText(buildName());
        capacity.addListener((observable, oldValue, newValue) -> lbl.setText(buildName()));
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
    @Override protected ContextMenu buildContextMenu() {
        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, makeCapMenu("Set size", capacity));
        return menu;
    }

    private String buildName() {
        // get class name
        String name = getClass().getSimpleName();
        int index = name.indexOf("By");
        if (index < 0) index = name.length();
        name = name.substring(0, index).toUpperCase();

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

        // join them together
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
            res.append(cap.charAt(len + i));
            res.append("  \n");
        }

        return res.deleteCharAt(res.length() - 1).toString();
    }

}
