package engine.connectivity;

import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public interface Selectable {

    Effect HIGHLIGHT = new DropShadow(1.0, Color.BLACK);

    boolean checkSelection(Rectangle sel);
    void breakSelection();

    void delete();
    void move();
    Selectable copy();
    void stop();

}
