package engine.interfaces;

import javafx.scene.canvas.Canvas;

public interface Renderable {

    void render();
    void setGridPeriod(double period);
    void setPos(int xPos, int yPos);
    void setGlobalAlpha(double alpha);

    boolean inside(int mx, int my);

    Canvas getBasis();
    int getWidth();
    int getHeight();

}
