package engine.connectivity;

import javafx.scene.shape.Rectangle;

public interface Selectable {

    boolean checkSelection(Rectangle sel);

    void delete();
    void move();
    void stop();

}
