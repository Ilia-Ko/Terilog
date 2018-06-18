package engine.components;

import engine.Circuit;
import gui.Main;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class Component {

    private Pane root;
    private Pane parent;

    public Component(Pane parent, IntegerProperty mouseX, IntegerProperty mouseY) { // create Component in layout mode
        this.parent = parent;
        try {
            root = loadContent();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        root.layoutXProperty().bind(mouseX);
        root.layoutYProperty().bind(mouseY);

        parent.getChildren().add(root);
    }
    private Pane loadContent() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/components/basic.fxml"));
        return loader.load();
    }

    public void confirm(Circuit circuit) {
        root.layoutXProperty().unbind();
        root.layoutYProperty().unbind();

        root.setOnMouseEntered(mouse -> root.requestFocus());

        root.setOpacity(1.0);
        circuit.add(this);
    }
    public void delete() {
        parent.getChildren().remove(root);
    }

    public void rotateCW() {
        root.setRotate(root.getRotate() + 90.0);
    }
    public void rotateCCW() {
        root.setRotate(root.getRotate() - 90.0);
    }
    public void mirrorX() {
        root.setScaleX(-root.getScaleX());
    }
    public void mirrorY() {
        root.setScaleY(-root.getScaleX());
    }

}
